package outbox

import (
	"context"
	"database/sql"
	"fmt"
	"time"

	"github.com/google/uuid"
	_ "modernc.org/sqlite"
)

type sqliteStore struct {
	db *sql.DB
}

// NewSQLiteStore initializes a pure-Go SQLite outbox store.
func NewSQLiteStore(dsn string) (Store, error) {
	db, err := sql.Open("sqlite", dsn)
	if err != nil {
		return nil, fmt.Errorf("failed to open sqlite database: %w", err)
	}

	schema := `
	CREATE TABLE IF NOT EXISTS outbox_operations (
		id INTEGER PRIMARY KEY AUTOINCREMENT,
		operation_id TEXT NOT NULL UNIQUE,
		entity_type TEXT NOT NULL,
		action TEXT NOT NULL,
		payload TEXT NOT NULL,
		status TEXT NOT NULL DEFAULT 'PENDING',
		sync_sequence INTEGER,
		created_at TEXT NOT NULL
	);
	CREATE INDEX IF NOT EXISTS idx_outbox_status_id ON outbox_operations (status, id ASC);
	`
	if _, err := db.Exec(schema); err != nil {
		db.Close()
		return nil, fmt.Errorf("failed to initialize outbox schema: %w", err)
	}

	return &sqliteStore{db: db}, nil
}

func (s *sqliteStore) Enqueue(ctx context.Context, op OperationEnvelope) error {
	query := `
	INSERT INTO outbox_operations (operation_id, entity_type, action, payload, status, created_at)
	VALUES (?, ?, ?, ?, 'PENDING', ?)
	`
	_, err := s.db.ExecContext(ctx, query,
		op.OperationID.String(),
		op.EntityType,
		op.Action,
		string(op.Payload),
		op.CreatedAt.Format(time.RFC3339Nano),
	)
	if err != nil {
		return fmt.Errorf("failed to enqueue outbox operation: %w", err)
	}
	return nil
}

func (s *sqliteStore) GetPending(ctx context.Context, limit int) ([]OperationEnvelope, error) {
	query := `
	SELECT operation_id, entity_type, action, payload, created_at
	FROM outbox_operations
	WHERE status = 'PENDING'
	ORDER BY id ASC
	LIMIT ?
	`
	rows, err := s.db.QueryContext(ctx, query, limit)
	if err != nil {
		return nil, fmt.Errorf("failed to query pending operations: %w", err)
	}
	defer rows.Close()

	var result []OperationEnvelope
	for rows.Next() {
		var opIDStr, entityType, action, payloadStr, createdAtStr string
		if err := rows.Scan(&opIDStr, &entityType, &action, &payloadStr, &createdAtStr); err != nil {
			return nil, fmt.Errorf("failed to scan outbox row: %w", err)
		}

		opID, err := uuid.Parse(opIDStr)
		if err != nil {
			return nil, fmt.Errorf("corrupt operation_id in sqlite outbox: %w", err)
		}

		createdAt, err := time.Parse(time.RFC3339Nano, createdAtStr)
		if err != nil {
			return nil, fmt.Errorf("corrupt created_at timestamp in sqlite outbox: %w", err)
		}

		result = append(result, OperationEnvelope{
			OperationID: opID,
			EntityType:  entityType,
			Action:      action,
			Payload:     []byte(payloadStr),
			CreatedAt:   createdAt,
		})
	}

	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("error during outbox rows iteration: %w", err)
	}

	return result, nil
}

func (s *sqliteStore) MarkApplied(ctx context.Context, applied []AppliedOperation) error {
	if len(applied) == 0 {
		return nil
	}

	tx, err := s.db.BeginTx(ctx, nil)
	if err != nil {
		return fmt.Errorf("failed to begin transaction: %w", err)
	}
	defer tx.Rollback()

	stmt, err := tx.PrepareContext(ctx, `
		UPDATE outbox_operations
		SET status = 'APPLIED', sync_sequence = ?
		WHERE operation_id = ? AND status = 'PENDING'
	`)
	if err != nil {
		return fmt.Errorf("failed to prepare update statement: %w", err)
	}
	defer stmt.Close()

	for _, item := range applied {
		if _, err := stmt.ExecContext(ctx, item.SyncSequence, item.OperationID.String()); err != nil {
			return fmt.Errorf("failed to mark operation %s applied: %w", item.OperationID, err)
		}
	}

	if err := tx.Commit(); err != nil {
		return fmt.Errorf("failed to commit mark applied transaction: %w", err)
	}

	return nil
}

func (s *sqliteStore) Close() error {
	return s.db.Close()
}
