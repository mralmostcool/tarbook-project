package outbox_test

import (
	"bytes"
	"context"
	"crypto/sha256"
	"encoding/json"
	"testing"
	"time"

	"github.com/google/uuid"
	"github.com/mralmostcool/tarbook-project/simulator/internal/outbox"
)

func TestOperationEnvelope_WireSerializationRoundTrip(t *testing.T) {
	ctx := context.Background()

	// 1. Durably store in SQLite
	store, err := outbox.NewSQLiteStore(":memory:")
	if err != nil {
		t.Fatalf("failed to create store: %v", err)
	}
	defer store.Close()

	original := outbox.OperationEnvelope{
		OperationID: uuid.MustParse("018f9e61-89ab-7def-8123-456789abcdef"),
		EntityType:  "TASK_ENTRY",
		Action:      "INSERT",
		Payload:     json.RawMessage(`{"candidate_notes":"Engine overhaul","task_id":"1.1"}`),
		CreatedAt:   time.Now().UTC().Truncate(time.Millisecond),
	}

	if err := store.Enqueue(ctx, original); err != nil {
		t.Fatalf("failed to enqueue: %v", err)
	}

	pending, err := store.GetPending(ctx, 1)
	if err != nil || len(pending) != 1 {
		t.Fatalf("failed to read from store: %v", err)
	}
	fromStore := pending[0]

	// 2. Serialize to wire format
	wireBytes, err := outbox.MarshalWire(fromStore)
	if err != nil {
		t.Fatalf("MarshalWire failed: %v", err)
	}

	// 3. Deserialize from wire format
	deserialized, err := outbox.UnmarshalWire(wireBytes)
	if err != nil {
		t.Fatalf("UnmarshalWire failed: %v", err)
	}

	// 4. Assert semantic equality without data loss
	if deserialized.OperationID != original.OperationID {
		t.Errorf("OperationID mismatch: got %s, want %s", deserialized.OperationID, original.OperationID)
	}
	if deserialized.EntityType != original.EntityType {
		t.Errorf("EntityType mismatch: got %s, want %s", deserialized.EntityType, original.EntityType)
	}
	if deserialized.Action != original.Action {
		t.Errorf("Action mismatch: got %s, want %s", deserialized.Action, original.Action)
	}

	var originalMap, deserializedMap map[string]any
	if err := json.Unmarshal(original.Payload, &originalMap); err != nil {
		t.Fatalf("failed to unmarshal original payload: %v", err)
	}
	if err := json.Unmarshal(deserialized.Payload, &deserializedMap); err != nil {
		t.Fatalf("failed to unmarshal deserialized payload: %v", err)
	}
	if originalMap["candidate_notes"] != deserializedMap["candidate_notes"] ||
		originalMap["task_id"] != deserializedMap["task_id"] {
		t.Errorf("Payload content mismatch: got %v, want %v", deserializedMap, originalMap)
	}
}

func TestOperationEnvelope_CanonicalSerializationDeterministic(t *testing.T) {
	opID := uuid.MustParse("018f9e61-89ab-7def-8123-456789abcdef")

	// Same semantic payload with inverted keys, different spacing and line breaks
	payload1 := json.RawMessage(`{"hours": 4, "task_id": "STCW-A-II-1", "verified": true}`)
	payload2 := json.RawMessage("{\n  \"verified\": true,\n  \"hours\": 4,\n  \"task_id\": \"STCW-A-II-1\"\n}")

	op1 := outbox.OperationEnvelope{
		OperationID: opID,
		EntityType:  "TASK_ENTRY",
		Action:      "INSERT",
		Payload:     payload1,
	}

	op2 := outbox.OperationEnvelope{
		OperationID: opID,
		EntityType:  "TASK_ENTRY",
		Action:      "INSERT",
		Payload:     payload2,
	}

	canonical1, err := outbox.CanonicalBytes(op1)
	if err != nil {
		t.Fatalf("CanonicalBytes op1 failed: %v", err)
	}

	canonical2, err := outbox.CanonicalBytes(op2)
	if err != nil {
		t.Fatalf("CanonicalBytes op2 failed: %v", err)
	}

	if !bytes.Equal(canonical1, canonical2) {
		t.Fatalf("Canonical serialization non-deterministic:\ncanonical1: %s\ncanonical2: %s", string(canonical1), string(canonical2))
	}

	// Verify SHA-256 hashes match exactly
	hash1 := sha256.Sum256(canonical1)
	hash2 := sha256.Sum256(canonical2)
	if hash1 != hash2 {
		t.Fatalf("SHA-256 hash mismatch on canonical bytes: %x vs %x", hash1, hash2)
	}
}

func TestOperationEnvelope_RFC8785JCSConformance(t *testing.T) {
	opID := uuid.MustParse("018f9e61-89ab-7def-8123-456789abcdef")

	// Payload with deep nesting, float representations, non-canonical whitespace
	chaoticPayload := json.RawMessage(`{
		"z_nested": {
			"b": 100.0,
			"a": "test\u0020value"
		},
		"a_list": [3, 2, 1],
		"m_number": 1e2
	}`)

	op := outbox.OperationEnvelope{
		OperationID: opID,
		EntityType:  "TASK_ENTRY",
		Action:      "INSERT",
		Payload:     chaoticPayload,
	}

	canonical, err := outbox.CanonicalBytes(op)
	if err != nil {
		t.Fatalf("CanonicalBytes failed: %v", err)
	}

	// Expected RFC 8785 canonical JSON:
	// 1. Top-level keys sorted: "action", "entity_type", "operation_id", "payload"
	// 2. Nested keys sorted: "a_list", "m_number", "z_nested" -> "a", "b"
	// 3. Numbers normalized: 1e2 -> 100, 100.0 -> 100
	// 4. Unicode escape \u0020 normalized to literal space
	// 5. Zero insignificant whitespace
	expected := `{"action":"INSERT","entity_type":"TASK_ENTRY","operation_id":"018f9e61-89ab-7def-8123-456789abcdef","payload":{"a_list":[3,2,1],"m_number":100,"z_nested":{"a":"test value","b":100}}}`

	if string(canonical) != expected {
		t.Fatalf("RFC 8785 JCS conformance failure:\ngot:  %s\nwant: %s", string(canonical), expected)
	}

	// Verify non-JSON payload returns an error
	invalidOp := outbox.OperationEnvelope{
		OperationID: opID,
		EntityType:  "TASK_ENTRY",
		Action:      "INSERT",
		Payload:     json.RawMessage(`{invalid_json}`),
	}
	if _, err := outbox.CanonicalBytes(invalidOp); err == nil {
		t.Fatal("expected error on invalid JSON payload, got nil")
	}
}

