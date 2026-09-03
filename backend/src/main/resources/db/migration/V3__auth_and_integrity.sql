-- V3__auth_and_integrity.sql
-- Project Tarbook Authentication, Cryptographic Provenance & Audit Integrity (ADR 0004)

-- 1. Officer Signing Keys (Hardware-backed non-exportable key registry)
CREATE TABLE officer_signing_keys (
    id UUID PRIMARY KEY,
    officer_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    key_id VARCHAR(100) NOT NULL UNIQUE,
    public_key_pem TEXT NOT NULL,
    algorithm VARCHAR(50) NOT NULL DEFAULT 'ECDSA_P256',
    hardware_backed BOOLEAN NOT NULL DEFAULT TRUE,
    attestation_statement TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_APPROVAL' CHECK (status IN ('PENDING_APPROVAL', 'ACTIVE', 'REVOKED', 'EXPIRED')),
    approved_by_org_id UUID REFERENCES organizations(id) ON DELETE RESTRICT,
    activated_at_utc TIMESTAMPTZ,
    revoked_at_utc TIMESTAMPTZ,
    revocation_reason VARCHAR(255),
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_officer_signing_keys_officer_id ON officer_signing_keys(officer_user_id);
CREATE INDEX idx_officer_signing_keys_status ON officer_signing_keys(status);

-- 2. Enhance Task Sign-Offs with Key Linkage, Nonces, and Hash Chaining
ALTER TABLE task_signoffs
    ADD COLUMN key_id VARCHAR(100) REFERENCES officer_signing_keys(key_id) ON DELETE RESTRICT,
    ADD COLUMN signing_nonce UUID,
    ADD COLUMN prev_record_hash VARCHAR(64);

CREATE INDEX idx_task_signoffs_key_id ON task_signoffs(key_id);
CREATE INDEX idx_task_signoffs_nonce ON task_signoffs(signing_nonce);

-- 3. Immutable Security Audit Events with Cryptographic Hash Chaining
CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor_user_id UUID REFERENCES app_users(id) ON DELETE RESTRICT,
    prev_event_hash VARCHAR(64),
    event_hash VARCHAR(64) NOT NULL,
    metadata JSONB,
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_events_entity ON audit_events(entity_type, entity_id);
CREATE INDEX idx_audit_events_actor ON audit_events(actor_user_id);
