package scenario

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"time"

	"github.com/google/uuid"
	"github.com/mralmostcool/tarbook-project/simulator/internal/mirror"
	"github.com/mralmostcool/tarbook-project/simulator/internal/outbox"
	"github.com/mralmostcool/tarbook-project/simulator/internal/signer"
	"github.com/mralmostcool/tarbook-project/simulator/internal/syncclient"
)

// Config holds the parameters for a headless simulation run.
type Config struct {
	Scenario   string       `json:"scenario"` // "candidate", "officer", "dual", "smoke"
	Role       string       `json:"role"`
	BackendURL string       `json:"backend_url"`
	DBPath     string       `json:"db_path"`
	ClientID   string       `json:"client_id"`
	HTTPClient *http.Client `json:"-"`
}

// Report encapsulates the execution metrics of a scenario run.
type Report struct {
	Scenario         string `json:"scenario"`
	ClientID         string `json:"client_id"`
	EnqueuedOps      int    `json:"enqueued_ops"`
	SignedOps        int    `json:"signed_ops"`
	PushedOps        int    `json:"pushed_ops"`
	PulledDeltas     int    `json:"pulled_deltas"`
	EvidenceUploaded int    `json:"evidence_uploaded"`
	Success          bool   `json:"success"`
	Error            string `json:"error,omitempty"`
}

// Run executes a specified simulation scenario.
func Run(ctx context.Context, cfg Config) (*Report, error) {
	if cfg.ClientID == "" {
		cfg.ClientID = "vessel-sim-node-01"
	}
	if cfg.HTTPClient == nil {
		cfg.HTTPClient = http.DefaultClient
	}

	outboxStore, err := outbox.NewSQLiteStore(cfg.DBPath)
	if err != nil {
		return nil, fmt.Errorf("failed to open outbox store: %w", err)
	}
	defer outboxStore.Close()

	mirrorStore, err := mirror.NewSQLiteMirrorStore(cfg.DBPath)
	if err != nil {
		return nil, fmt.Errorf("failed to open mirror store: %w", err)
	}
	defer mirrorStore.Close()

	report := &Report{
		Scenario: cfg.Scenario,
		ClientID: cfg.ClientID,
		Success:  true,
	}

	taskEntryID := uuid.New()
	taskDefID := uuid.New()
	tarBookID := uuid.New()

	// 1. Candidate Step: Log Task Entry
	if cfg.Scenario == "candidate" || cfg.Scenario == "dual" {
		taskPayload, _ := json.Marshal(map[string]interface{}{
			"id":                 taskEntryID.String(),
			"tar_book_id":        tarBookID.String(),
			"task_definition_id": taskDefID.String(),
			"status":             "SUBMITTED",
			"candidate_notes":    "Performed navigational watch on bridge under pilotage through Malacca Strait.",
			"vessel_imo":         "9876543",
			"logged_at_utc":      time.Now().UTC().Format(time.RFC3339Nano),
		})

		taskOp := outbox.OperationEnvelope{
			OperationID: uuid.New(),
			EntityType:  "task_entries",
			Action:      "INSERT",
			Payload:     taskPayload,
			CreatedAt:   time.Now().UTC(),
		}

		if err := outboxStore.Enqueue(ctx, taskOp); err != nil {
			report.Success = false
			report.Error = fmt.Sprintf("failed to enqueue candidate task: %v", err)
			return report, err
		}
		report.EnqueuedOps++
	}

	// 2. Officer Step: Cryptographic Sign-Off
	if cfg.Scenario == "officer" || cfg.Scenario == "dual" {
		signerSvc := signer.NewMemorySignerService()
		officerUser := uuid.New()

		key, err := signerSvc.GenerateKey(ctx, officerUser)
		if err != nil {
			report.Success = false
			report.Error = fmt.Sprintf("failed to generate officer key: %v", err)
			return report, err
		}

		signoffPayloadMap := map[string]interface{}{
			"id":            uuid.New().String(),
			"task_entry_id": taskEntryID.String(),
			"signer_role":   "SUPERVISING_OFFICER",
			"verdict":       "COMPETENT",
			"comments":      "Cadet demonstrated thorough understanding of collision regulations and radar plotting.",
			"signed_at_utc": time.Now().UTC().Format(time.RFC3339Nano),
		}
		rawSignoffPayload, _ := json.Marshal(signoffPayloadMap)

		sigBytes, err := signerSvc.SignCanonical(ctx, key.KeyID, rawSignoffPayload)
		if err != nil {
			report.Success = false
			report.Error = fmt.Sprintf("failed to sign signoff payload: %v", err)
			return report, err
		}

		signoffEnvelopePayload, _ := json.Marshal(map[string]interface{}{
			"signoff":         signoffPayloadMap,
			"key_id":          key.KeyID,
			"signature_asn1":  sigBytes,
			"hardware_backed": false,
		})

		signoffOp := outbox.OperationEnvelope{
			OperationID: uuid.New(),
			EntityType:  "task_signoffs",
			Action:      "INSERT",
			Payload:     signoffEnvelopePayload,
			CreatedAt:   time.Now().UTC(),
		}

		if err := outboxStore.Enqueue(ctx, signoffOp); err != nil {
			report.Success = false
			report.Error = fmt.Sprintf("failed to enqueue signed signoff: %v", err)
			return report, err
		}
		report.EnqueuedOps++
		report.SignedOps++
	}

	// 3. Shore Sync Push & Pull (if BackendURL configured)
	if cfg.BackendURL != "" {
		pushClient := syncclient.NewPushClient(cfg.ClientID, outboxStore, cfg.BackendURL, cfg.HTTPClient)
		pushRep, err := pushClient.Push(ctx, 50)
		if err != nil {
			report.Success = false
			report.Error = fmt.Sprintf("shore sync push failed: %v", err)
			return report, err
		}
		report.PushedOps = pushRep.AppliedCount

		pullClient := syncclient.NewPullClient(cfg.ClientID, mirrorStore, cfg.BackendURL, cfg.HTTPClient)
		pullRep, err := pullClient.Pull(ctx, 50)
		if err != nil {
			report.Success = false
			report.Error = fmt.Sprintf("shore sync pull failed: %v", err)
			return report, err
		}
		report.PulledDeltas = pullRep.ItemsApplied
	}

	return report, nil
}
