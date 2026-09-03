package outbox

import (
	"context"
	"encoding/json"
	"time"

	"github.com/google/uuid"
)

// OperationEnvelope represents an enqueued client mutation per ADR 0007 and ADR 0008.
type OperationEnvelope struct {
	OperationID uuid.UUID       `json:"operation_id"`
	EntityType  string          `json:"entity_type"`
	Action      string          `json:"action"`
	Payload     json.RawMessage `json:"payload"`
	CreatedAt   time.Time       `json:"created_at"`
}

// AppliedOperation pairs an applied operation ID with its authoritative server sync sequence.
type AppliedOperation struct {
	OperationID  uuid.UUID
	SyncSequence int64
}

// Store defines the public seam for the local edge transactional outbox.
type Store interface {
	Enqueue(ctx context.Context, op OperationEnvelope) error
	GetPending(ctx context.Context, limit int) ([]OperationEnvelope, error)
	MarkApplied(ctx context.Context, applied []AppliedOperation) error
	Close() error
}
