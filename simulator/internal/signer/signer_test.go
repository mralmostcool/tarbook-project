package signer_test

import (
	"context"
	"encoding/json"
	"testing"

	"github.com/google/uuid"
	"github.com/mralmostcool/tarbook-project/simulator/internal/outbox"
	"github.com/mralmostcool/tarbook-project/simulator/internal/signer"
)

func TestSignerService_ObservableBehavior(t *testing.T) {
	ctx := context.Background()
	svc := signer.NewMemorySignerService()

	officerID := uuid.New()

	// 1. Generate P-256 key pair with unique key_id associated with officer_id
	key, err := svc.GenerateKey(ctx, officerID)
	if err != nil {
		t.Fatalf("GenerateKey failed: %v", err)
	}

	if key.KeyID == "" {
		t.Fatal("expected non-empty key_id")
	}
	if key.OfficerID != officerID {
		t.Fatalf("officer_id mismatch: got %s, want %s", key.OfficerID, officerID)
	}
	if key.Status != signer.KeyStatusActive {
		t.Fatalf("expected status ACTIVE, got %s", key.Status)
	}
	if key.PublicKey == nil {
		t.Fatal("expected non-nil public key")
	}

	payload := []byte(`{"action":"INSERT","entity_type":"TASK_ENTRY","operation_id":"test-op-1"}`)

	// 2. Sign canonical bytes successfully while ACTIVE
	sig, err := svc.SignCanonical(ctx, key.KeyID, payload)
	if err != nil {
		t.Fatalf("SignCanonical failed: %v", err)
	}
	if len(sig) == 0 {
		t.Fatal("expected non-empty signature bytes")
	}

	// 3. Verify valid signature successfully
	valid, err := svc.VerifySignature(key.KeyID, payload, sig)
	if err != nil {
		t.Fatalf("VerifySignature error: %v", err)
	}
	if !valid {
		t.Fatal("expected signature to be valid")
	}

	// 4. Reject verification when canonical payload is modified
	tamperedPayload := []byte(`{"action":"INSERT","entity_type":"TASK_ENTRY","operation_id":"test-op-2"}`)
	validTampered, err := svc.VerifySignature(key.KeyID, tamperedPayload, sig)
	if err != nil {
		t.Fatalf("unexpected error on tampered payload: %v", err)
	}
	if validTampered {
		t.Fatal("expected verification to fail for tampered payload")
	}

	// 5. Reject verification when signature is modified
	corruptSig := make([]byte, len(sig))
	copy(corruptSig, sig)
	corruptSig[len(corruptSig)-1] ^= 0xFF // flip last byte
	validCorrupt, err := svc.VerifySignature(key.KeyID, payload, corruptSig)
	if err != nil {
		t.Fatalf("unexpected error on corrupt signature: %v", err)
	}
	if validCorrupt {
		t.Fatal("expected verification to fail for corrupt signature")
	}

	// 6. Refuse signing when key is REVOKED
	if err := svc.SetKeyStatus(ctx, key.KeyID, signer.KeyStatusRevoked); err != nil {
		t.Fatalf("SetKeyStatus failed: %v", err)
	}

	_, err = svc.SignCanonical(ctx, key.KeyID, payload)
	if err != signer.ErrKeyRevoked {
		t.Fatalf("expected ErrKeyRevoked, got: %v", err)
	}

	// 7. Verification of previously signed bytes remains cryptographically valid even after revocation
	validPostRevoke, err := svc.VerifySignature(key.KeyID, payload, sig)
	if err != nil {
		t.Fatalf("VerifySignature post-revocation failed: %v", err)
	}
	if !validPostRevoke {
		t.Fatal("expected historical signature to remain cryptographically valid")
	}
}

func TestSigner_CrossSeamCanonicalCompatibility(t *testing.T) {
	ctx := context.Background()
	svc := signer.NewMemorySignerService()
	officerID := uuid.New()

	key, err := svc.GenerateKey(ctx, officerID)
	if err != nil {
		t.Fatalf("GenerateKey failed: %v", err)
	}

	opID := uuid.New()
	// Two semantically identical operations with inverted JSON keys and disparate whitespace
	opA := outbox.OperationEnvelope{
		OperationID: opID,
		EntityType:  "TASK_ENTRY",
		Action:      "INSERT",
		Payload:     json.RawMessage(`{"hours": 6, "task_id": "STCW-A-II-1"}`),
	}
	opB := outbox.OperationEnvelope{
		OperationID: opID,
		EntityType:  "TASK_ENTRY",
		Action:      "INSERT",
		Payload:     json.RawMessage("{\n  \"task_id\": \"STCW-A-II-1\",\n  \"hours\": 6\n}"),
	}

	canonicalA, err := outbox.CanonicalBytes(opA)
	if err != nil {
		t.Fatalf("CanonicalBytes opA failed: %v", err)
	}

	canonicalB, err := outbox.CanonicalBytes(opB)
	if err != nil {
		t.Fatalf("CanonicalBytes opB failed: %v", err)
	}

	// Sign canonical bytes of opA
	sigA, err := svc.SignCanonical(ctx, key.KeyID, canonicalA)
	if err != nil {
		t.Fatalf("SignCanonical failed: %v", err)
	}

	// Verify signature created from opA against canonical bytes of opB
	valid, err := svc.VerifySignature(key.KeyID, canonicalB, sigA)
	if err != nil {
		t.Fatalf("VerifySignature failed: %v", err)
	}
	if !valid {
		t.Fatal("expected signature generated from opA to verify against canonical bytes of opB")
	}
}
