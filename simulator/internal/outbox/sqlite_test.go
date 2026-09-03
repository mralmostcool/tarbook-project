package outbox_test

import (
	"context"
	"encoding/json"
	"testing"
	"time"

	"github.com/google/uuid"
	"github.com/mralmostcool/tarbook-project/simulator/internal/outbox"
)

func TestSQLiteOutboxStore_ObservableBehavior(t *testing.T) {
	ctx := context.Background()

	// 1. Initialize in-memory pure-Go SQLite store
	store, err := outbox.NewSQLiteStore(":memory:")
	if err != nil {
		t.Fatalf("failed to initialize store: %v", err)
	}
	defer store.Close()

	op1 := outbox.OperationEnvelope{
		OperationID: uuid.New(),
		EntityType:  "TASK_ENTRY",
		Action:      "INSERT",
		Payload:     json.RawMessage(`{"candidate_notes":"Mooring operation completed"}`),
		CreatedAt:   time.Now().UTC().Truncate(time.Millisecond),
	}

	op2 := outbox.OperationEnvelope{
		OperationID: uuid.New(),
		EntityType:  "TASK_SIGNOFF",
		Action:      "INSERT",
		Payload:     json.RawMessage(`{"signing_nonce":"` + uuid.New().String() + `"}`),
		CreatedAt:   time.Now().UTC().Add(time.Second).Truncate(time.Millisecond),
	}

	// 2. Enqueue operations
	if err := store.Enqueue(ctx, op1); err != nil {
		t.Fatalf("enqueue op1 failed: %v", err)
	}
	if err := store.Enqueue(ctx, op2); err != nil {
		t.Fatalf("enqueue op2 failed: %v", err)
	}

	// 3. Verify FIFO ordering and limit
	pendingLimit1, err := store.GetPending(ctx, 1)
	if err != nil {
		t.Fatalf("get pending with limit 1 failed: %v", err)
	}
	if len(pendingLimit1) != 1 {
		t.Fatalf("expected 1 pending op, got %d", len(pendingLimit1))
	}
	if pendingLimit1[0].OperationID != op1.OperationID {
		t.Fatalf("expected first op %s, got %s", op1.OperationID, pendingLimit1[0].OperationID)
	}

	pendingAll, err := store.GetPending(ctx, 10)
	if err != nil {
		t.Fatalf("get pending all failed: %v", err)
	}
	if len(pendingAll) != 2 {
		t.Fatalf("expected 2 pending ops, got %d", len(pendingAll))
	}

	// 4. Mark op1 applied with authoritative server sync_sequence
	applied := []outbox.AppliedOperation{
		{
			OperationID:  op1.OperationID,
			SyncSequence: 1042,
		},
	}
	if err := store.MarkApplied(ctx, applied); err != nil {
		t.Fatalf("mark applied failed: %v", err)
	}

	// 5. Verify op1 is no longer returned as pending
	remaining, err := store.GetPending(ctx, 10)
	if err != nil {
		t.Fatalf("get pending after mark applied failed: %v", err)
	}
	if len(remaining) != 1 {
		t.Fatalf("expected 1 pending op after mark applied, got %d", len(remaining))
	}
	if remaining[0].OperationID != op2.OperationID {
		t.Fatalf("expected remaining op %s, got %s", op2.OperationID, remaining[0].OperationID)
	}
}
