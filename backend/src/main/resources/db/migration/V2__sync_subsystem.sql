-- V2__sync_subsystem.sql
-- Project Tarbook Offline Synchronization & Idempotency Subsystem (ADR 0003)

-- 1. Authoritative Monotonic Sync Sequence
CREATE SEQUENCE global_sync_sequence AS BIGINT START WITH 1 INCREMENT BY 1;

-- 2. Add Authoritative Sync Sequence to Syncable Entities
ALTER TABLE task_entries 
    ADD COLUMN sync_sequence BIGINT DEFAULT nextval('global_sync_sequence');

ALTER TABLE task_signoffs 
    ADD COLUMN sync_sequence BIGINT DEFAULT nextval('global_sync_sequence');

ALTER TABLE evidence_artifacts 
    ADD COLUMN sync_sequence BIGINT DEFAULT nextval('global_sync_sequence');

CREATE INDEX idx_task_entries_sync_seq ON task_entries(sync_sequence);
CREATE INDEX idx_task_signoffs_sync_seq ON task_signoffs(sync_sequence);
CREATE INDEX idx_evidence_artifacts_sync_seq ON evidence_artifacts(sync_sequence);

-- 3. Sync Sessions (Authoritative batch record & client idempotency tracking)
CREATE TABLE sync_sessions (
    id UUID PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL,
    sync_session_id UUID NOT NULL UNIQUE,
    batch_payload_hash VARCHAR(64) NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'PARTIAL_SUCCESS', 'FAILED')),
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at_utc TIMESTAMPTZ
);

CREATE INDEX idx_sync_sessions_client_id ON sync_sessions(client_id);

-- 4. Sync Operations (Granular operation-level idempotency & integrity tracking)
CREATE TABLE sync_operations (
    id UUID PRIMARY KEY,
    sync_session_id UUID NOT NULL REFERENCES sync_sessions(id) ON DELETE RESTRICT,
    operation_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    operation_payload_hash VARCHAR(64) NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('APPLIED', 'REJECTED', 'CONFLICT', 'IDEMPOTENT_SKIPPED')),
    sync_sequence BIGINT,
    error_details TEXT,
    committed_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_sync_session_operation UNIQUE (sync_session_id, operation_id)
);

CREATE INDEX idx_sync_operations_session_id ON sync_operations(sync_session_id);
CREATE INDEX idx_sync_operations_entity ON sync_operations(entity_type, entity_id);
