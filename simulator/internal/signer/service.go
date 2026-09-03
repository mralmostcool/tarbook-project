package signer

import (
	"context"
	"crypto/ecdsa"
	"errors"

	"github.com/google/uuid"
)

type KeyStatus string

const (
	KeyStatusActive  KeyStatus = "ACTIVE"
	KeyStatusRevoked KeyStatus = "REVOKED"
)

var (
	ErrKeyRevoked       = errors.New("signing key is revoked")
	ErrInvalidSignature = errors.New("signature verification failed")
	ErrKeyNotFound      = errors.New("signing key not found")
	ErrNotImplemented   = errors.New("not implemented")
)

// OfficerKey represents a simulated officer's key registration metadata and public key.
// Invariant: The private key is never exposed through this structure.
type OfficerKey struct {
	KeyID     string           `json:"key_id"`
	OfficerID uuid.UUID        `json:"officer_id"`
	Status    KeyStatus        `json:"status"`
	PublicKey *ecdsa.PublicKey `json:"-"`
}

// Service defines the public seam for simulated protocol signing and verification.
// NOTE: Pure software simulation for protocol validation; does NOT simulate hardware-enclave guarantees.
type Service interface {
	GenerateKey(ctx context.Context, officerID uuid.UUID) (*OfficerKey, error)
	GetKey(ctx context.Context, keyID string) (*OfficerKey, error)
	SetKeyStatus(ctx context.Context, keyID string, status KeyStatus) error
	SignCanonical(ctx context.Context, keyID string, canonicalBytes []byte) ([]byte, error)
	VerifySignature(keyID string, canonicalBytes []byte, signature []byte) (bool, error)
}
