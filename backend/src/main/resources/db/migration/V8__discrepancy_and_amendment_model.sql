-- V8__discrepancy_and_amendment_model.sql
-- Project Tarbook Discrepancy & Conflict Resolution Workflow (ADR 0013)

-- 1. Extend task_entries with rework and superseding amendment support
ALTER TABLE task_entries
    DROP CONSTRAINT task_entries_status_check,
    ADD CONSTRAINT task_entries_status_check CHECK (status IN ('DRAFT', 'SUBMITTED', 'IN_REVIEW', 'COMPLETED', 'REJECTED', 'REWORK_REQUESTED', 'SUPERSEDED')),
    ADD COLUMN superseded_by_id UUID REFERENCES task_entries(id) ON DELETE RESTRICT,
    ADD COLUMN amendment_reason VARCHAR(255);

CREATE INDEX idx_task_entries_superseded ON task_entries(superseded_by_id);

-- 2. Extend sea_service_records with superseding amendment support
ALTER TABLE sea_service_records
    DROP CONSTRAINT sea_service_records_status_check,
    ADD CONSTRAINT sea_service_records_status_check CHECK (status IN ('IN_PROGRESS', 'SUBMITTED', 'DISCHARGED', 'VOIDED', 'SUPERSEDED')),
    ADD COLUMN superseded_by_id UUID REFERENCES sea_service_records(id) ON DELETE RESTRICT,
    ADD COLUMN amendment_reason VARCHAR(255);

CREATE INDEX idx_sea_service_superseded ON sea_service_records(superseded_by_id);

-- 3. Record Amendments (Immutable statutory correction provenance table)
CREATE TABLE record_amendments (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL CHECK (entity_type IN ('TASK_ENTRY', 'SEA_SERVICE_RECORD', 'TAR_BOOK')),
    original_record_id UUID NOT NULL,
    amended_record_id UUID NOT NULL,
    amendment_reason TEXT NOT NULL,
    authorized_by_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    sync_sequence BIGINT DEFAULT nextval('global_sync_sequence'),
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_record_amendments_entity ON record_amendments(entity_type, original_record_id);
CREATE INDEX idx_record_amendments_authorized ON record_amendments(authorized_by_user_id);
CREATE INDEX idx_record_amendments_sync_seq ON record_amendments(sync_sequence);
