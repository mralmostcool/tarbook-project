package syncclient

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"net/http/httptest"
	"sync"
	"testing"
	"time"

	"github.com/google/uuid"
)

type memoryMirrorStore struct {
	mu               sync.Mutex
	lastSyncSequence int64
	appliedDeltas    []DeltaItem
	failApply        bool
}

func (m *memoryMirrorStore) GetLastSyncSequence(ctx context.Context) (int64, error) {
	m.mu.Lock()
	defer m.mu.Unlock()
	return m.lastSyncSequence, nil
}

func (m *memoryMirrorStore) ApplyDeltas(ctx context.Context, items []DeltaItem, nextSyncSequence int64) error {
	m.mu.Lock()
	defer m.mu.Unlock()
	if m.failApply {
		return fmt.Errorf("simulated mirror write failure")
	}
	m.appliedDeltas = append(m.appliedDeltas, items...)
	m.lastSyncSequence = nextSyncSequence
	return nil
}

func TestPullClient_Success_AppliesDeltasAndUpdatesCursor(t *testing.T) {
	ctx := context.Background()
	clientID := "vessel-sim-42"

	item1ID := uuid.New()
	item2ID := uuid.New()
	entity1ID := uuid.New()
	entity2ID := uuid.New()

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet {
			t.Fatalf("expected GET, got %s", r.Method)
		}
		if r.URL.Path != "/api/v1/sync/pull" {
			t.Fatalf("unexpected path: %s", r.URL.Path)
		}
		if r.URL.Query().Get("since") != "10" {
			t.Fatalf("expected since=10, got %s", r.URL.Query().Get("since"))
		}
		if r.URL.Query().Get("limit") != "50" {
			t.Fatalf("expected limit=50, got %s", r.URL.Query().Get("limit"))
		}
		if r.Header.Get("X-Client-Id") != clientID {
			t.Fatalf("missing or invalid X-Client-Id header")
		}

		resp := SyncPullResponse{
			Items: []DeltaItem{
				{
					SyncSequence: 11,
					OperationID:  item1ID,
					EntityType:   "task_entries",
					EntityID:     entity1ID,
					Action:       "UPSERT",
					Payload:      json.RawMessage(`{"title":"Bridge Navigation Watch"}`),
					CommittedAt:  time.Now().UTC(),
				},
				{
					SyncSequence: 12,
					OperationID:  item2ID,
					EntityType:   "task_signoffs",
					EntityID:     entity2ID,
					Action:       "UPSERT",
					Payload:      json.RawMessage(`{"status":"OFFICER_APPROVED"}`),
					CommittedAt:  time.Now().UTC(),
				},
			},
			HasMore:          true,
			NextSyncSequence: 12,
		}

		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_ = json.NewEncoder(w).Encode(resp)
	}))
	defer server.Close()

	mirror := &memoryMirrorStore{lastSyncSequence: 10}
	client := NewPullClient(clientID, mirror, server.URL, server.Client())

	report, err := client.Pull(ctx, 50)
	if err != nil {
		t.Fatalf("unexpected pull error: %v", err)
	}

	if report.ItemsApplied != 2 {
		t.Fatalf("expected 2 applied items, got %d", report.ItemsApplied)
	}
	if !report.HasMore {
		t.Fatalf("expected has_more to be true")
	}
	if report.NextSyncSequence != 12 {
		t.Fatalf("expected next sync sequence 12, got %d", report.NextSyncSequence)
	}

	if len(mirror.appliedDeltas) != 2 {
		t.Fatalf("expected mirror to store 2 deltas, got %d", len(mirror.appliedDeltas))
	}
	if mirror.lastSyncSequence != 12 {
		t.Fatalf("expected mirror sequence updated to 12, got %d", mirror.lastSyncSequence)
	}
}

func TestPullClient_NonMonotonicSequence_RejectsUntrustworthyPayload(t *testing.T) {
	ctx := context.Background()
	clientID := "vessel-sim-42"

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		// Server sends next_sync_sequence lower than 'since' cursor
		resp := SyncPullResponse{
			Items:            []DeltaItem{},
			HasMore:          false,
			NextSyncSequence: 5, // Regression: since was 10!
		}
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_ = json.NewEncoder(w).Encode(resp)
	}))
	defer server.Close()

	mirror := &memoryMirrorStore{lastSyncSequence: 10}
	client := NewPullClient(clientID, mirror, server.URL, server.Client())

	_, err := client.Pull(ctx, 50)
	if err == nil {
		t.Fatalf("expected error on non-monotonic sequence regression, got nil")
	}
	if mirror.lastSyncSequence != 10 {
		t.Fatalf("mirror sequence must not mutate on invalid response")
	}
}

func TestPullClient_TransportError_NoLocalMutation(t *testing.T) {
	ctx := context.Background()
	clientID := "vessel-sim-42"

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		http.Error(w, "internal server error", http.StatusInternalServerError)
	}))
	defer server.Close()

	mirror := &memoryMirrorStore{lastSyncSequence: 10}
	client := NewPullClient(clientID, mirror, server.URL, server.Client())

	_, err := client.Pull(ctx, 50)
	if err == nil {
		t.Fatalf("expected error on HTTP 500, got nil")
	}
	if mirror.lastSyncSequence != 10 {
		t.Fatalf("mirror sequence must remain unchanged on transport error")
	}
	if len(mirror.appliedDeltas) != 0 {
		t.Fatalf("no deltas should be applied on error")
	}
}

func TestPullClient_ApplicationFailure_LeavesCursorUnchanged(t *testing.T) {
	ctx := context.Background()
	clientID := "vessel-sim-42"

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		resp := SyncPullResponse{
			Items: []DeltaItem{
				{
					SyncSequence: 11,
					OperationID:  uuid.New(),
					EntityType:   "task_entries",
					EntityID:     uuid.New(),
					Action:       "UPSERT",
					Payload:      json.RawMessage(`{}`),
					CommittedAt:  time.Now().UTC(),
				},
			},
			HasMore:          false,
			NextSyncSequence: 11,
		}
		w.Header().Set("Content-Type", "application/json")
		_ = json.NewEncoder(w).Encode(resp)
	}))
	defer server.Close()

	mirror := &memoryMirrorStore{lastSyncSequence: 10, failApply: true}
	client := NewPullClient(clientID, mirror, server.URL, server.Client())

	_, err := client.Pull(ctx, 50)
	if err == nil {
		t.Fatalf("expected error on mirror write failure, got nil")
	}
	if mirror.lastSyncSequence != 10 {
		t.Fatalf("cursor must remain unchanged at 10 when mirror apply fails, got %d", mirror.lastSyncSequence)
	}
}

