-- V4__sea_service_records.sql
-- Project Tarbook Sea Service & Master Statutory Endorsement Subsystem (ADR 0006)

-- 1. Enable btree_gist for composite GiST exclusion constraints
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- 2. Composite unique constraint on parent tar_books to guarantee permanent dual-linkage
ALTER TABLE tar_books 
    ADD CONSTRAINT uq_tar_books_id_candidate UNIQUE (id, candidate_id);

-- 3. Sea Service Records (Shipboard voyages & certified aggregate watchkeeping totals)
CREATE TABLE sea_service_records (
    id UUID PRIMARY KEY,
    tar_book_id UUID NOT NULL,
    candidate_id UUID NOT NULL,
    vessel_name VARCHAR(255) NOT NULL,
    vessel_imo VARCHAR(10) NOT NULL,
    flag_state VARCHAR(100) NOT NULL,
    vessel_type VARCHAR(100) NOT NULL,
    gross_tonnage NUMERIC(10,2) NOT NULL CHECK (gross_tonnage > 0),
    engine_power_kw NUMERIC(10,2) CHECK (engine_power_kw >= 0),
    sign_on_date DATE NOT NULL,
    sign_off_date DATE,
    days_at_sea INTEGER NOT NULL DEFAULT 0 CHECK (days_at_sea >= 0),
    days_in_port INTEGER NOT NULL DEFAULT 0 CHECK (days_in_port >= 0),
    bridge_watch_hours_day NUMERIC(8,2) NOT NULL DEFAULT 0 CHECK (bridge_watch_hours_day >= 0),
    bridge_watch_hours_night NUMERIC(8,2) NOT NULL DEFAULT 0 CHECK (bridge_watch_hours_night >= 0),
    engine_watch_hours_day NUMERIC(8,2) NOT NULL DEFAULT 0 CHECK (engine_watch_hours_day >= 0),
    engine_watch_hours_night NUMERIC(8,2) NOT NULL DEFAULT 0 CHECK (engine_watch_hours_night >= 0),
    steering_hours NUMERIC(8,2) NOT NULL DEFAULT 0 CHECK (steering_hours >= 0),
    rank_served VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS', 'SUBMITTED', 'DISCHARGED', 'VOIDED')),
    sync_sequence BIGINT DEFAULT nextval('global_sync_sequence'),
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sea_service_tar_book_candidate FOREIGN KEY (tar_book_id, candidate_id) REFERENCES tar_books(id, candidate_id) ON DELETE RESTRICT,
    CONSTRAINT chk_sea_service_dates CHECK (sign_off_date IS NULL OR sign_off_date >= sign_on_date),
    CONSTRAINT uq_non_overlapping_sea_service EXCLUDE USING gist (
        candidate_id WITH =,
        daterange(sign_on_date, COALESCE(sign_off_date, 'infinity'), '[]') WITH &&
    ) WHERE (status != 'VOIDED')
);

CREATE INDEX idx_sea_service_candidate_id ON sea_service_records(candidate_id);
CREATE INDEX idx_sea_service_tar_book_id ON sea_service_records(tar_book_id);
CREATE INDEX idx_sea_service_status ON sea_service_records(status);
CREATE INDEX idx_sea_service_sync_seq ON sea_service_records(sync_sequence);

-- 4. Sea Service Endorsements (Append-only Master/Chief Engineer statutory certifications)
CREATE TABLE sea_service_endorsements (
    id UUID PRIMARY KEY,
    sea_service_id UUID NOT NULL REFERENCES sea_service_records(id) ON DELETE RESTRICT,
    endorser_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    endorser_role VARCHAR(50) NOT NULL CHECK (endorser_role IN ('MASTER', 'CHIEF_ENGINEER')),
    endorsement_type VARCHAR(50) NOT NULL CHECK (endorsement_type IN ('INTERIM_HANDOVER', 'FINAL_DISCHARGE')),
    conduct_rating VARCHAR(50) NOT NULL,
    ability_rating VARCHAR(50) NOT NULL,
    comments TEXT,
    key_id VARCHAR(100) NOT NULL REFERENCES officer_signing_keys(key_id) ON DELETE RESTRICT,
    signing_nonce UUID NOT NULL,
    signature_payload_hash VARCHAR(64) NOT NULL,
    signature_bytes BYTEA NOT NULL,
    signed_at_utc TIMESTAMPTZ NOT NULL,
    sync_sequence BIGINT DEFAULT nextval('global_sync_sequence'),
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Exactly one FINAL_DISCHARGE endorsement permitted per sea-service period
CREATE UNIQUE INDEX uq_sea_service_final_discharge ON sea_service_endorsements (sea_service_id) WHERE (endorsement_type = 'FINAL_DISCHARGE');

CREATE INDEX idx_sea_service_endorsements_service_id ON sea_service_endorsements(sea_service_id);
CREATE INDEX idx_sea_service_endorsements_endorser ON sea_service_endorsements(endorser_user_id);
CREATE INDEX idx_sea_service_endorsements_key_id ON sea_service_endorsements(key_id);
