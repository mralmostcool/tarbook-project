package mirror

import (
	"context"
	"encoding/json"
	"time"

	"github.com/google/uuid"
	"github.com/mralmostcool/tarbook-project/simulator/internal/syncclient"
)

// EntityRecord represents a locally stored snapshot of an authoritative shore entity.
type EntityRecord struct {
	EntityType   string          `json:"entity_type"`
	EntityID     uuid.UUID       `json:"entity_id"`
	Payload      json.RawMessage `json:"payload"`
	SyncSequence int64           `json:"sync_sequence"`
	UpdatedAt    time.Time       `json:"updated_at"`
}

// Store extends the syncclient.MirrorStore seam with edge read methods.
type Store interface {
	syncclient.MirrorStore
	GetEntity(ctx context.Context, entityType string, entityID uuid.UUID) (*EntityRecord, error)
	Close() error
}
