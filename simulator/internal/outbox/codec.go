package outbox

import (
	"encoding/json"
	"errors"
	"fmt"

	"github.com/google/uuid"
	"github.com/gowebpki/jcs"
)

var (
	ErrInvalidEnvelope = errors.New("invalid operation envelope")
)

// wireEnvelope defines the exact REST wire format for operations inside POST /api/v1/sync/push.
type wireEnvelope struct {
	OperationID uuid.UUID       `json:"operation_id"`
	EntityType  string          `json:"entity_type"`
	Action      string          `json:"action"`
	Payload     json.RawMessage `json:"payload"`
}

// MarshalWire serializes an OperationEnvelope into the agreed REST sync wire format.
func MarshalWire(op OperationEnvelope) ([]byte, error) {
	if op.OperationID == uuid.Nil || op.EntityType == "" || op.Action == "" {
		return nil, ErrInvalidEnvelope
	}

	wire := wireEnvelope{
		OperationID: op.OperationID,
		EntityType:  op.EntityType,
		Action:      op.Action,
		Payload:     op.Payload,
	}

	return json.Marshal(wire)
}

// UnmarshalWire parses raw wire JSON into an OperationEnvelope.
func UnmarshalWire(data []byte) (OperationEnvelope, error) {
	var wire wireEnvelope
	if err := json.Unmarshal(data, &wire); err != nil {
		return OperationEnvelope{}, fmt.Errorf("failed to unmarshal wire envelope: %w", err)
	}

	if wire.OperationID == uuid.Nil || wire.EntityType == "" || wire.Action == "" {
		return OperationEnvelope{}, ErrInvalidEnvelope
	}

	return OperationEnvelope{
		OperationID: wire.OperationID,
		EntityType:  wire.EntityType,
		Action:      wire.Action,
		Payload:     wire.Payload,
	}, nil
}

// CanonicalBytes generates a deterministic, normalized byte representation of the operation
// strictly conforming to RFC 8785 (JSON Canonicalization Scheme - JCS).
// It recursively normalizes whitespace, number formatting, string encodings, and lexicographical key ordering.
func CanonicalBytes(op OperationEnvelope) ([]byte, error) {
	if op.OperationID == uuid.Nil || op.EntityType == "" || op.Action == "" {
		return nil, ErrInvalidEnvelope
	}

	// Validate payload is well-formed JSON before wrapping
	if !json.Valid(op.Payload) {
		return nil, fmt.Errorf("payload is not valid JSON: %w", ErrInvalidEnvelope)
	}

	envelope := struct {
		Action      string          `json:"action"`
		EntityType  string          `json:"entity_type"`
		OperationID string          `json:"operation_id"`
		Payload     json.RawMessage `json:"payload"`
	}{
		Action:      op.Action,
		EntityType:  op.EntityType,
		OperationID: op.OperationID.String(),
		Payload:     op.Payload,
	}

	raw, err := json.Marshal(envelope)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal envelope for canonicalization: %w", err)
	}

	canonical, err := jcs.Transform(raw)
	if err != nil {
		return nil, fmt.Errorf("RFC 8785 JCS canonicalization failed: %w", err)
	}

	return canonical, nil
}
