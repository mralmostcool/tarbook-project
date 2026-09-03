package syncclient

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"net/url"

	"github.com/google/uuid"
	"github.com/mralmostcool/tarbook-project/simulator/internal/outbox"
)

type OperationStatus string

const (
	OperationStatusApplied  OperationStatus = "APPLIED"
	OperationStatusConflict OperationStatus = "CONFLICT"
	OperationStatusRejected OperationStatus = "REJECTED"
)

var (
	ErrUntrustworthyReceipt = errors.New("untrustworthy or invalid sync receipt")
)

type OperationResult struct {
	OperationID  uuid.UUID       `json:"operation_id"`
	Status       OperationStatus `json:"status"`
	SyncSequence *int64          `json:"sync_sequence,omitempty"`
	ErrorCode    *string         `json:"error_code,omitempty"`
	ServerState  json.RawMessage `json:"server_state,omitempty"`
}

type SyncReceipt struct {
	SyncSessionID       uuid.UUID         `json:"sync_session_id"`
	Status              string            `json:"status"`
	HighestSyncSequence int64             `json:"highest_sync_sequence"`
	Results             []OperationResult `json:"results"`
}

type SyncPushRequest struct {
	SyncSessionID uuid.UUID                  `json:"sync_session_id"`
	ClientID      string                     `json:"client_id"`
	Operations    []outbox.OperationEnvelope `json:"operations"`
}

type PushReport struct {
	SyncSessionID       uuid.UUID
	HighestSyncSequence int64
	AppliedCount        int
	ConflictCount       int
	RejectedCount       int
	Receipt             SyncReceipt
}

// PushClient defines the public seam for executing push synchronizations.
type PushClient interface {
	Push(ctx context.Context, batchSize int) (*PushReport, error)
}

type httpPushClient struct {
	clientID   string
	store      outbox.Store
	baseURL    string
	httpClient *http.Client
}

// NewPushClient creates an HTTP push client adapter.
func NewPushClient(clientID string, store outbox.Store, baseURL string, httpClient *http.Client) PushClient {
	if httpClient == nil {
		httpClient = http.DefaultClient
	}
	return &httpPushClient{
		clientID:   clientID,
		store:      store,
		baseURL:    baseURL,
		httpClient: httpClient,
	}
}

func (c *httpPushClient) Push(ctx context.Context, batchSize int) (*PushReport, error) {
	// 1. Read pending operations from outbox
	pendingOps, err := c.store.GetPending(ctx, batchSize)
	if err != nil {
		return nil, fmt.Errorf("failed to read pending operations from outbox: %w", err)
	}

	if len(pendingOps) == 0 {
		return &PushReport{}, nil
	}

	// 2. Construct heterogeneous sync push envelope
	sessionID := uuid.New()
	reqBody := SyncPushRequest{
		SyncSessionID: sessionID,
		ClientID:      c.clientID,
		Operations:    pendingOps,
	}

	reqBytes, err := json.Marshal(reqBody)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal sync push request: %w", err)
	}

	endpoint, err := url.JoinPath(c.baseURL, "/api/v1/sync/push")
	if err != nil {
		return nil, fmt.Errorf("invalid endpoint URL: %w", err)
	}

	httpReq, err := http.NewRequestWithContext(ctx, http.MethodPost, endpoint, bytes.NewReader(reqBytes))
	if err != nil {
		return nil, fmt.Errorf("failed to create HTTP request: %w", err)
	}

	httpReq.Header.Set("Content-Type", "application/json")
	httpReq.Header.Set("Accept", "application/json")
	httpReq.Header.Set("X-Client-Id", c.clientID)

	// 3. Send request to backend
	resp, err := c.httpClient.Do(httpReq)
	if err != nil {
		// Case 2: Network failure -> zero local applied changes
		return nil, fmt.Errorf("sync push HTTP transport error: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		// Case 2: Untrustworthy response -> zero local applied changes
		return nil, fmt.Errorf("%w: HTTP %d", ErrUntrustworthyReceipt, resp.StatusCode)
	}

	// 4. Parse structured differential receipt
	var receipt SyncReceipt
	if err := json.NewDecoder(resp.Body).Decode(&receipt); err != nil {
		// Case 2: Malformed receipt -> zero local applied changes
		return nil, fmt.Errorf("%w: failed to decode receipt: %v", ErrUntrustworthyReceipt, err)
	}

	// 5. Differential processing: Case 1 (Trustworthy HTTP 200 receipt)
	var appliedOps []outbox.AppliedOperation
	report := &PushReport{
		SyncSessionID:       receipt.SyncSessionID,
		HighestSyncSequence: receipt.HighestSyncSequence,
		Receipt:             receipt,
	}

	for _, res := range receipt.Results {
		switch res.Status {
		case OperationStatusApplied:
			if res.SyncSequence != nil {
				appliedOps = append(appliedOps, outbox.AppliedOperation{
					OperationID:  res.OperationID,
					SyncSequence: *res.SyncSequence,
				})
				report.AppliedCount++
			}
		case OperationStatusConflict:
			report.ConflictCount++
			// Invariant: Keep CONFLICT operations pending in outbox.
		case OperationStatusRejected:
			report.RejectedCount++
			// Invariant: Keep REJECTED operations pending in outbox.
		}
	}

	// Apply individual sequence numbers for APPLIED operations only
	if len(appliedOps) > 0 {
		if err := c.store.MarkApplied(ctx, appliedOps); err != nil {
			return nil, fmt.Errorf("failed to commit applied operations to outbox: %w", err)
		}
	}

	return report, nil
}
