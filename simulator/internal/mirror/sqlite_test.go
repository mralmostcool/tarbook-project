package mirror

import (
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"path/filepath"
	"testing"
	"time"

	"github.com/google/uuid"
	"github.com/mralmostcool/tarbook-project/simulator/internal/syncclient"
)

func TestSQLiteMirrorStore_ObservableBehavior(t *testing.T) {
	ctx := context.Background()
	tempDir := t.TempDir()
	dbPath := filepath.Join(tempDir, "mirror_test.db")

	store, err := NewSQLiteMirrorStore(dbPath)
	if err != nil {
		t.Fatalf("failed to create sqlite mirror store: %v", err)
	}
	defer store.Close()

	// Initial sequence must be 0
	seq, err := store.GetLastSyncSequence(ctx)
	if err != nil {
		t.Fatalf("failed to get initial sequence: %v", err)
	}
	if seq != 0 {
		t.Fatalf("expected initial sequence 0, got %d", seq)
	}

	entityID := uuid.New()
	item1 := syncclient.DeltaItem{
		SyncSequence: 1,
		OperationID:  uuid.New(),
		EntityType:   "task_entries",
		EntityID:     entityID,
		Action:       "UPSERT",
		Payload:      json.RawMessage(`{"title":"Mooring Stations"}`),
		CommittedAt:  time.Now().UTC(),
	}

	// Apply delta
	if err := store.ApplyDeltas(ctx, []syncclient.DeltaItem{item1}, 1); err != nil {
		t.Fatalf("failed to apply deltas: %v", err)
	}

	// Verify sequence updated
	seq, err = store.GetLastSyncSequence(ctx)
	if err != nil {
		t.Fatalf("failed to get updated sequence: %v", err)
	}
	if seq != 1 {
		t.Fatalf("expected sequence 1, got %d", seq)
	}

	// Verify entity exists
	rec, err := store.GetEntity(ctx, "task_entries", entityID)
	if err != nil {
		t.Fatalf("failed to get entity: %v", err)
	}
	if rec == nil {
		t.Fatalf("expected entity record, got nil")
	}
	if string(rec.Payload) != `{"title":"Mooring Stations"}` {
		t.Fatalf("unexpected entity payload: %s", string(rec.Payload))
	}

	// Verify DELETE delta removes entity
	item2 := syncclient.DeltaItem{
		SyncSequence: 2,
		OperationID:  uuid.New(),
		EntityType:   "task_entries",
		EntityID:     entityID,
		Action:       "DELETE",
		Payload:      json.RawMessage(`{}`),
		CommittedAt:  time.Now().UTC(),
	}
	if err := store.ApplyDeltas(ctx, []syncclient.DeltaItem{item2}, 2); err != nil {
		t.Fatalf("failed to apply delete delta: %v", err)
	}

	rec, err = store.GetEntity(ctx, "task_entries", entityID)
	if err != nil {
		t.Fatalf("failed to query deleted entity: %v", err)
	}
	if rec != nil {
		t.Fatalf("expected nil for deleted entity, got %+v", rec)
	}
}

func TestSQLiteMirrorStore_IntegratedWithPullClient(t *testing.T) {
	ctx := context.Background()
	tempDir := t.TempDir()
	dbPath := filepath.Join(tempDir, "mirror_pull_integration.db")

	store, err := NewSQLiteMirrorStore(dbPath)
	if err != nil {
		t.Fatalf("failed to create sqlite mirror store: %v", err)
	}
	defer store.Close()

	taskID := uuid.New()
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		resp := syncclient.SyncPullResponse{
			Items: []syncclient.DeltaItem{
				{
					SyncSequence: 101,
					OperationID:  uuid.New(),
					EntityType:   "task_entries",
					EntityID:     taskID,
					Action:       "UPSERT",
					Payload:      json.RawMessage(`{"title":"Fire Drill Assessment"}`),
					CommittedAt:  time.Now().UTC(),
				},
			},
			HasMore:          false,
			NextSyncSequence: 101,
		}
		w.Header().Set("Content-Type", "application/json")
		_ = json.NewEncoder(w).Encode(resp)
	}))
	defer server.Close()

	client := syncclient.NewPullClient("client-edge-1", store, server.URL, server.Client())
	report, err := client.Pull(ctx, 10)
	if err != nil {
		t.Fatalf("pull failed: %v", err)
	}

	if report.ItemsApplied != 1 || report.NextSyncSequence != 101 {
		t.Fatalf("unexpected pull report: %+v", report)
	}

	seq, err := store.GetLastSyncSequence(ctx)
	if err != nil || seq != 101 {
		t.Fatalf("expected sequence 101, got %d (err: %v)", seq, err)
	}

	entity, err := store.GetEntity(ctx, "task_entries", taskID)
	if err != nil || entity == nil {
		t.Fatalf("expected entity in SQLite mirror, got %+v (err: %v)", entity, err)
	}
	if string(entity.Payload) != `{"title":"Fire Drill Assessment"}` {
		t.Fatalf("unexpected payload in SQLite mirror: %s", string(entity.Payload))
	}
}
