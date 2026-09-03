package signer

import (
	"context"
	"crypto/ecdsa"
	"crypto/elliptic"
	"crypto/rand"
	"crypto/sha256"
	"fmt"
	"sync"

	"github.com/google/uuid"
)

type internalKey struct {
	meta       OfficerKey
	privateKey *ecdsa.PrivateKey
}

type memorySignerService struct {
	mu   sync.RWMutex
	keys map[string]*internalKey
}

// NewMemorySignerService initializes an in-memory simulated signer service.
// NOTE: Software protocol simulation only; does not validate hardware-enclave guarantees.
func NewMemorySignerService() Service {
	return &memorySignerService{
		keys: make(map[string]*internalKey),
	}
}

func (s *memorySignerService) GenerateKey(ctx context.Context, officerID uuid.UUID) (*OfficerKey, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	privKey, err := ecdsa.GenerateKey(elliptic.P256(), rand.Reader)
	if err != nil {
		return nil, fmt.Errorf("failed to generate ECDSA P-256 key: %w", err)
	}

	keyID := fmt.Sprintf("key_%s", uuid.New().String())

	meta := OfficerKey{
		KeyID:     keyID,
		OfficerID: officerID,
		Status:    KeyStatusActive,
		PublicKey: &privKey.PublicKey,
	}

	s.keys[keyID] = &internalKey{
		meta:       meta,
		privateKey: privKey,
	}

	// Return copy without exposing privateKey
	res := meta
	return &res, nil
}

func (s *memorySignerService) GetKey(ctx context.Context, keyID string) (*OfficerKey, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	k, exists := s.keys[keyID]
	if !exists {
		return nil, ErrKeyNotFound
	}

	res := k.meta
	return &res, nil
}

func (s *memorySignerService) SetKeyStatus(ctx context.Context, keyID string, status KeyStatus) error {
	s.mu.Lock()
	defer s.mu.Unlock()

	k, exists := s.keys[keyID]
	if !exists {
		return ErrKeyNotFound
	}

	k.meta.Status = status
	return nil
}

func (s *memorySignerService) SignCanonical(ctx context.Context, keyID string, canonicalBytes []byte) ([]byte, error) {
	s.mu.RLock()
	k, exists := s.keys[keyID]
	s.mu.RUnlock()

	if !exists {
		return nil, ErrKeyNotFound
	}

	if k.meta.Status != KeyStatusActive {
		return nil, ErrKeyRevoked
	}

	digest := sha256.Sum256(canonicalBytes)
	signature, err := ecdsa.SignASN1(rand.Reader, k.privateKey, digest[:])
	if err != nil {
		return nil, fmt.Errorf("failed to sign digest: %w", err)
	}

	return signature, nil
}

func (s *memorySignerService) VerifySignature(keyID string, canonicalBytes []byte, signature []byte) (bool, error) {
	s.mu.RLock()
	k, exists := s.keys[keyID]
	s.mu.RUnlock()

	if !exists {
		return false, ErrKeyNotFound
	}

	digest := sha256.Sum256(canonicalBytes)
	valid := ecdsa.VerifyASN1(k.meta.PublicKey, digest[:], signature)
	return valid, nil
}
