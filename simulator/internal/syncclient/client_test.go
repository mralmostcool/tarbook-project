package syncclient_test

import (
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/google/uuid"
	"github.com/mralmostcool/tarbook-project/simulator/internal/outbox"
	"github.com/mralmostcool/tarbook-project/simulator/internal/syncclient"
)

func TestPushClient_DifferentialReceiptProcessing(t *testing.T) {
	ctx := context.Background()

	// 1. Setup in-memory outbox store with 3 operations
	store, err := outbox.NewSQLiteStore(":memory:")
	if err != nil {
		t.Fatalf("failed to create sqlite store: %v", err)
	}
	defer store.Close()

	op1 := outbox.OperationEnvelope{
		OperationID: uuid.New(),
		EntityType:  "TASK_ENTRY",
		Action:      "INSERT",
		Payload:     json.RawMessage(`{"task_id":"1.1"}`),
		CreatedAt:   time.Now().UTC(),
	}
	op2 := outbox.OperationEnvelope{
		OperationID: uuid.New(),
		EntityType:  "TASK_SIGNOFF",
		Action:      "INSERT",
		Payload:     json.RawMessage(`{"signing_nonce":"nonce-1"}`),
		CreatedAt:   time.Now().UTC().Add(time.Second),
	}
	op3 := outbox.OperationEnvelope{
		OperationID: uuid.New(),
		EntityType:  "TASK_ENTRY",
		Action:      "INSERT",
		Payload:     json.RawMessage(`{"task_id":"1.2"}`),
		CreatedAt:   time.Now().UTC().Add(2 * time.Second),
	}

	for _, op := range []outbox.OperationEnvelope{op1, op2, op3} {
		if err := store.Enqueue(ctx, op); err != nil {
			t.Fatalf("failed to enqueue: %v", err)
		}
	}

	seq1 := int64(101)
	seq3 := int64(102)
	errCode := "STALE_VERSION"

	// 2. Setup mock HTTP server returning differential receipt
	var receivedRequest syncclient.SyncPushRequest
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost || r.URL.Path != "/api/v1/sync/push" {
			t.Errorf("unexpected request: %s %s", r.Method, r.URL.Path)
			w.WriteHeader(http.StatusNotFound)
			return
		}

		if r.Header.Get("X-Client-Id") != "device-test-01" {
			t.Errorf("unexpected X-Client-Id: %s", r.Header.Get("X-Client-Id"))
		}

		if err := json.NewDecoder(r.Body).Decode(&receivedRequest); err != nil {
			t.Errorf("failed to decode request body: %v", err)
			w.WriteHeader(http.StatusBadRequest)
			return
		}

		receipt := syncclient.SyncReceipt{
			SyncSessionID:       receivedRequest.SyncSessionID,
			Status:              "PARTIAL_SUCCESS",
			HighestSyncSequence: 102,
			Results: []syncclient.OperationResult{
				{
					OperationID:  op1.OperationID,
					Status:       syncclient.OperationStatusApplied,
					SyncSequence: &seq1,
				},
				{
					OperationID: op2.OperationID,
					Status:      syncclient.OperationStatusConflict,
					ErrorCode:   &errCode,
				},
				{
					OperationID:  op3.OperationID,
					Status:       syncclient.OperationStatusApplied,
					SyncSequence: &seq3,
				},
			},
		}

		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		json.NewEncoder(w).Encode(receipt)
	}))
	defer server.Close()

	client := syncclient.NewPushClient("device-test-01", store, server.URL, server.Client())

	// 3. Execute Push
	report, err := client.Push(ctx, 10)
	if err != nil {
		t.Fatalf("Push failed: %v", err)
	}

	// 4. Verify request was constructed accurately
	if receivedRequest.ClientID != "device-test-01" {
		t.Errorf("client_id mismatch in request: got %s", receivedRequest.ClientID)
	}
	if len(receivedRequest.Operations) != 3 {
		t.Fatalf("expected 3 operations sent in request, got %d", len(receivedRequest.Operations))
	}

	// 5. Verify PushReport
	if report.AppliedCount != 2 {
		t.Errorf("expected 2 applied, got %d", report.AppliedCount)
	}
	if report.ConflictCount != 1 {
		t.Errorf("expected 1 conflict, got %d", report.ConflictCount)
	}
	if report.HighestSyncSequence != 102 {
		t.Errorf("expected highest_sync_sequence 102, got %d", report.HighestSyncSequence)
	}

	// 6. Verify Outbox state: differential commit occurred
	// op1 and op3 were applied, op2 must remain pending!
	pending, err := store.GetPending(ctx, 10)
	if err != nil {
		t.Fatalf("failed to query pending: %v", err)
	}
	if len(pending) != 1 {
		t.Fatalf("expected exactly 1 remaining pending op, got %d", len(pending))
	}
	if pending[0].OperationID != op2.OperationID {
		t.Fatalf("expected remaining pending op %s, got %s", op2.OperationID, pending[0].OperationID)
	}
}

func TestPushClient_UntrustworthyReceipt_PreservesAllPending(t *testing.T) {
	ctx := context.Background()

	store, err := outbox.NewSQLiteStore(":memory:")
	if err != nil {
		t.Fatalf("failed to create sqlite store: %v", err)
	}
	defer store.Close()

	op1 := outbox.OperationEnvelope{
		OperationID: uuid.New(),
		EntityType:  "TASK_ENTRY",
		Action:      "INSERT",
		Payload:     json.RawMessage(`{"task_id":"1.1"}`),
		CreatedAt:   time.Now().UTC(),
	}
	op2 := outbox.OperationEnvelope{
		OperationID: uuid.New(),
		EntityType:  "TASK_SIGNOFF",
		Action:      "INSERT",
		Payload:     json.RawMessage(`{"signing_nonce":"nonce-1"}`),
		CreatedAt:   time.Now().UTC(),
	}

	if err := store.Enqueue(ctx, op1); err != nil {
		t.Fatalf("failed to enqueue op1: %v", err)
	}
	if err := store.Enqueue(ctx, op2); err != nil {
		t.Fatalf("failed to enqueue op2: %v", err)
	}

	// Mock server returns 500 Internal Server Error
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(`{"error":"database unavailable"}`))
	}))
	defer server.Close()

	client := syncclient.NewPushClient("device-test-01", store, server.URL, server.Client())

	// Push should fail
	_, err = client.Push(ctx, 10)
	if err == nil {
		t.Fatal("expected error on HTTP 500 response, got nil")
	}

	// Invariant: zero local applied-state changes. Both operations remain pending for retry.
	pending, err := store.GetPending(ctx, 10)
	if err != nil {
		t.Fatalf("failed to query pending: %v", err)
	}
	if len(pending) != 2 {
		t.Fatalf("expected both operations to remain pending, got %d", len(pending))
	}
}

func TestPushClient_EmptyOutbox(t *testing.T) {
	ctx := context.Background()

	store, err := outbox.NewSQLiteStore(":memory:")
	if err != nil {
		t.Fatalf("failed to create sqlite store: %v", err)
	}
	defer store.Close()

	called := false
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		called = true
		w.WriteHeader(http.StatusOK)
	}))
	defer server.Close()

	client := syncclient.NewPushClient("device-test-01", store, server.URL, server.Client())

	report, err := client.Push(ctx, 10)
	if err != nil {
		t.Fatalf("Push failed on empty outbox: %v", err)
	}

	if called {
		t.Error("expected server NOT to be called when outbox is empty")
	}
	if report.AppliedCount != 0 || report.ConflictCount != 0 {
		t.Errorf("expected 0 counts, got applied=%d, conflict=%d", report.AppliedCount, report.ConflictCount)
	}
}
