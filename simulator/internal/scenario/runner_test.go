package scenario

import (
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"path/filepath"
	"testing"
	"github.com/mralmostcool/tarbook-project/simulator/internal/syncclient"
)

func TestScenarioRunner_DualRoleHandshake(t *testing.T) {
	ctx := context.Background()
	tempDir := t.TempDir()
	dbPath := filepath.Join(tempDir, "sim_test.db")

	var receivedPushOps int
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch r.URL.Path {
		case "/api/v1/sync/push":
			var req syncclient.SyncPushRequest
			if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
				http.Error(w, err.Error(), http.StatusBadRequest)
				return
			}
			receivedPushOps = len(req.Operations)
			var results []syncclient.OperationResult
			for i, op := range req.Operations {
				seq := int64(100 + i)
				results = append(results, syncclient.OperationResult{
					OperationID:  op.OperationID,
					Status:       syncclient.OperationStatusApplied,
					SyncSequence: &seq,
				})
			}
			receipt := syncclient.SyncReceipt{
				SyncSessionID:       req.SyncSessionID,
				Status:              "COMPLETED",
				HighestSyncSequence: int64(100 + len(req.Operations) - 1),
				Results:             results,
			}
			w.Header().Set("Content-Type", "application/json")
			_ = json.NewEncoder(w).Encode(receipt)

		case "/api/v1/sync/pull":
			resp := syncclient.SyncPullResponse{
				Items:            []syncclient.DeltaItem{},
				HasMore:          false,
				NextSyncSequence: 105,
			}
			w.Header().Set("Content-Type", "application/json")
			_ = json.NewEncoder(w).Encode(resp)

		default:
			http.NotFound(w, r)
		}
	}))
	defer server.Close()

	cfg := Config{
		Scenario:   "dual",
		BackendURL: server.URL,
		DBPath:     dbPath,
		ClientID:   "test-ship-node",
		HTTPClient: server.Client(),
	}

	report, err := Run(ctx, cfg)
	if err != nil {
		t.Fatalf("scenario execution failed: %v", err)
	}

	if !report.Success {
		t.Fatalf("expected report.Success to be true, got error: %s", report.Error)
	}
	if report.EnqueuedOps != 2 { // 1 task entry + 1 task signoff
		t.Fatalf("expected 2 enqueued operations, got %d", report.EnqueuedOps)
	}
	if report.SignedOps != 1 {
		t.Fatalf("expected 1 signed signoff, got %d", report.SignedOps)
	}
	if report.PushedOps != 2 {
		t.Fatalf("expected 2 pushed operations, got %d", report.PushedOps)
	}
	if receivedPushOps != 2 {
		t.Fatalf("expected server to receive 2 ops, got %d", receivedPushOps)
	}
}

func TestScenarioRunner_CandidateOnly(t *testing.T) {
	ctx := context.Background()
	tempDir := t.TempDir()
	dbPath := filepath.Join(tempDir, "candidate_test.db")

	cfg := Config{
		Scenario: "candidate",
		DBPath:   dbPath,
		ClientID: "test-candidate",
	}

	report, err := Run(ctx, cfg)
	if err != nil {
		t.Fatalf("scenario failed: %v", err)
	}

	if !report.Success {
		t.Fatalf("expected success")
	}
	if report.EnqueuedOps != 1 {
		t.Fatalf("expected 1 enqueued task entry, got %d", report.EnqueuedOps)
	}
	if report.SignedOps != 0 {
		t.Fatalf("candidate must not sign operations")
	}
}

func TestScenarioRunner_OfficerOnly(t *testing.T) {
	ctx := context.Background()
	tempDir := t.TempDir()
	dbPath := filepath.Join(tempDir, "officer_test.db")

	cfg := Config{
		Scenario: "officer",
		DBPath:   dbPath,
		ClientID: "test-officer",
	}

	report, err := Run(ctx, cfg)
	if err != nil {
		t.Fatalf("scenario failed: %v", err)
	}

	if !report.Success {
		t.Fatalf("expected success")
	}
	if report.SignedOps != 1 {
		t.Fatalf("expected 1 signed signoff, got %d", report.SignedOps)
	}
}
