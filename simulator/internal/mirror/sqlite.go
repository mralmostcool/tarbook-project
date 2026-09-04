package mirror

import (
	"context"
	"database/sql"
	"fmt"
	"strconv"
	"time"

	"github.com/google/uuid"
	"github.com/mralmostcool/tarbook-project/simulator/internal/syncclient"
	_ "modernc.org/sqlite"
)

type sqliteMirrorStore struct {
	db *sql.DB
}

// NewSQLiteMirrorStore initializes a pure-Go SQLite local replica mirror.
func NewSQLiteMirrorStore(dsn string) (Store, error) {
	db, err := sql.Open("sqlite", dsn)
	if err != nil {
		return nil, fmt.Errorf("failed to open sqlite mirror: %w", err)
	}

	schema := `
	CREATE TABLE IF NOT EXISTS mirror_state (
		key TEXT PRIMARY KEY,
		value TEXT NOT NULL
	);
	CREATE TABLE IF NOT EXISTS mirror_entities (
		entity_type TEXT NOT NULL,
		entity_id TEXT NOT NULL,
		payload TEXT NOT NULL,
		sync_sequence INTEGER NOT NULL,
		updated_at TEXT NOT NULL,
		PRIMARY KEY (entity_type, entity_id)
	);
	CREATE INDEX IF NOT EXISTS idx_mirror_entities_seq ON mirror_entities (sync_sequence);
	`
	if _, err := db.Exec(schema); err != nil {
		db.Close()
		return nil, fmt.Errorf("failed to initialize mirror schema: %w", err)
	}

	return &sqliteMirrorStore{db: db}, nil
}

func (s *sqliteMirrorStore) GetLastSyncSequence(ctx context.Context) (int64, error) {
	query := `SELECT value FROM mirror_state WHERE key = 'last_sync_sequence'`
	var valStr string
	err := s.db.QueryRowContext(ctx, query).Scan(&valStr)
	if err == sql.ErrNoRows {
		return 0, nil
	}
	if err != nil {
		return 0, fmt.Errorf("failed to read last_sync_sequence: %w", err)
	}

	seq, err := strconv.ParseInt(valStr, 10, 64)
	if err != nil {
		return 0, fmt.Errorf("corrupt last_sync_sequence value in mirror_state: %w", err)
	}
	return seq, nil
}

func (s *sqliteMirrorStore) ApplyDeltas(ctx context.Context, items []syncclient.DeltaItem, nextSyncSequence int64) error {
	tx, err := s.db.BeginTx(ctx, nil)
	if err != nil {
		return fmt.Errorf("failed to begin mirror transaction: %w", err)
	}
	defer tx.Rollback()

	upsertStmt, err := tx.PrepareContext(ctx, `
		INSERT INTO mirror_entities (entity_type, entity_id, payload, sync_sequence, updated_at)
		VALUES (?, ?, ?, ?, ?)
		ON CONFLICT(entity_type, entity_id) DO UPDATE SET
			payload = excluded.payload,
			sync_sequence = excluded.sync_sequence,
			updated_at = excluded.updated_at
		WHERE excluded.sync_sequence >= mirror_entities.sync_sequence
	`)
	if err != nil {
		return fmt.Errorf("failed to prepare upsert statement: %w", err)
	}
	defer upsertStmt.Close()

	deleteStmt, err := tx.PrepareContext(ctx, `
		DELETE FROM mirror_entities
		WHERE entity_type = ? AND entity_id = ?
	`)
	if err != nil {
		return fmt.Errorf("failed to prepare delete statement: %w", err)
	}
	defer deleteStmt.Close()

	for _, item := range items {
		switch item.Action {
		case "DELETE":
			if _, err := deleteStmt.ExecContext(ctx, item.EntityType, item.EntityID.String()); err != nil {
				return fmt.Errorf("failed to execute delta delete on %s/%s: %w", item.EntityType, item.EntityID, err)
			}
		default: // "UPSERT" or any mutation
			if _, err := upsertStmt.ExecContext(ctx,
				item.EntityType,
				item.EntityID.String(),
				string(item.Payload),
				item.SyncSequence,
				item.CommittedAt.Format(time.RFC3339Nano),
			); err != nil {
				return fmt.Errorf("failed to execute delta upsert on %s/%s: %w", item.EntityType, item.EntityID, err)
			}
		}
	}

	// Update sync cursor
	cursorQuery := `
		INSERT INTO mirror_state (key, value)
		VALUES ('last_sync_sequence', ?)
		ON CONFLICT(key) DO UPDATE SET value = excluded.value
	`
	if _, err := tx.ExecContext(ctx, cursorQuery, strconv.FormatInt(nextSyncSequence, 10)); err != nil {
		return fmt.Errorf("failed to update last_sync_sequence cursor: %w", err)
	}

	if err := tx.Commit(); err != nil {
		return fmt.Errorf("failed to commit mirror delta transaction: %w", err)
	}

	return nil
}

func (s *sqliteMirrorStore) GetEntity(ctx context.Context, entityType string, entityID uuid.UUID) (*EntityRecord, error) {
	query := `
		SELECT payload, sync_sequence, updated_at
		FROM mirror_entities
		WHERE entity_type = ? AND entity_id = ?
	`
	var payloadStr, updatedAtStr string
	var seq int64
	err := s.db.QueryRowContext(ctx, query, entityType, entityID.String()).Scan(&payloadStr, &seq, &updatedAtStr)
	if err == sql.ErrNoRows {
		return nil, nil
	}
	if err != nil {
		return nil, fmt.Errorf("failed to query entity %s/%s: %w", entityType, entityID, err)
	}

	updatedAt, err := time.Parse(time.RFC3339Nano, updatedAtStr)
	if err != nil {
		return nil, fmt.Errorf("corrupt updated_at timestamp in mirror_entities: %w", err)
	}

	return &EntityRecord{
		EntityType:   entityType,
		EntityID:     entityID,
		Payload:      []byte(payloadStr),
		SyncSequence: seq,
		UpdatedAt:    updatedAt,
	}, nil
}

func (s *sqliteMirrorStore) Close() error {
	return s.db.Close()
}
