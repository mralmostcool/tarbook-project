-- V11__audit_logs_schema.sql
-- Audit Log Persistence Schema for Journal Subsystem

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    action VARCHAR(100) NOT NULL,
    entity_name VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    details_json TEXT,
    prev_hash VARCHAR(64),
    entry_hash VARCHAR(64) NOT NULL,
    logged_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_name, entity_id);
CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_user_id);
CREATE INDEX idx_audit_logs_logged_at ON audit_logs(logged_at_utc DESC);
