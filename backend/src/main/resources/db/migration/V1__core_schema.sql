-- V1__core_schema.sql
-- Project Tarbook Core Database Schema (ADR 0002)

-- 1. Extensions
CREATE EXTENSION IF NOT EXISTS postgis;

-- 2. Organizations (MTI, Shipping Company, Flag Administration)
CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('MTI', 'SHIPPING_COMPANY', 'FLAG_ADMINISTRATION')),
    code VARCHAR(100) NOT NULL UNIQUE,
    license_number VARCHAR(100),
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Base App Users
CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    system_role VARCHAR(50) NOT NULL CHECK (system_role IN ('CANDIDATE', 'OFFICER', 'MASTER', 'COMPANY_OFFICER', 'ADMIN')),
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Candidates (Maritime seafarer training profile)
CREATE TABLE candidates (
    id UUID PRIMARY KEY REFERENCES app_users(id) ON DELETE RESTRICT,
    sponsoring_org_id UUID REFERENCES organizations(id) ON DELETE RESTRICT,
    indos_number VARCHAR(20) NOT NULL UNIQUE,
    cdc_number VARCHAR(50) NOT NULL UNIQUE,
    training_stream VARCHAR(50) NOT NULL CHECK (training_stream IN ('DECK_CADET', 'TRAINEE_ENGINE_OFFICER', 'TRAINEE_ETO', 'GP_RATING')),
    date_of_birth DATE NOT NULL,
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. Training Programs (STCW curriculum / syllabus)
CREATE TABLE training_programs (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    training_stream VARCHAR(50) NOT NULL CHECK (training_stream IN ('DECK_CADET', 'TRAINEE_ENGINE_OFFICER', 'TRAINEE_ETO', 'GP_RATING')),
    revision VARCHAR(20) NOT NULL DEFAULT '1.0',
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 6. Task Definitions (Standardized STCW competency requirements)
CREATE TABLE task_definitions (
    id UUID PRIMARY KEY,
    program_id UUID NOT NULL REFERENCES training_programs(id) ON DELETE RESTRICT,
    code VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    criteria TEXT NOT NULL,
    stcw_reference VARCHAR(100),
    required_evidence_count INTEGER NOT NULL DEFAULT 1 CHECK (required_evidence_count >= 0),
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_task_definition_program_code UNIQUE (program_id, code)
);

-- 7. TAR Books (Issued book instance assigned to a candidate)
CREATE TABLE tar_books (
    id UUID PRIMARY KEY,
    candidate_id UUID NOT NULL REFERENCES candidates(id) ON DELETE RESTRICT,
    program_id UUID NOT NULL REFERENCES training_programs(id) ON DELETE RESTRICT,
    issuing_org_id UUID NOT NULL REFERENCES organizations(id) ON DELETE RESTRICT,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUBMITTED', 'UNDER_REVIEW', 'CERTIFIED', 'VOIDED')),
    issued_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at_utc TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0,
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 8. Task Entries (Logged competency activity executed at sea)
CREATE TABLE task_entries (
    id UUID PRIMARY KEY,
    tar_book_id UUID NOT NULL REFERENCES tar_books(id) ON DELETE RESTRICT,
    task_definition_id UUID NOT NULL REFERENCES task_definitions(id) ON DELETE RESTRICT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'SUBMITTED', 'SIGNED_OFF', 'REWORK_REQUESTED', 'VOIDED')),
    candidate_notes TEXT,
    location GEOMETRY(Point, 4326),
    gnss_accuracy_meters NUMERIC(6,2),
    vessel_imo VARCHAR(10),
    logged_at_utc TIMESTAMPTZ NOT NULL,
    synced_at_utc TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0,
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 9. Task Sign-Offs (Immutable append-only officer assessment records)
CREATE TABLE task_signoffs (
    id UUID PRIMARY KEY,
    task_entry_id UUID NOT NULL REFERENCES task_entries(id) ON DELETE RESTRICT,
    officer_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    signer_role VARCHAR(50) NOT NULL CHECK (signer_role IN ('SUPERVISING_OFFICER', 'CHIEF_ENGINEER', 'MASTER', 'COMPANY_TRAINING_OFFICER')),
    verdict VARCHAR(50) NOT NULL CHECK (verdict IN ('COMPETENT', 'NOT_YET_COMPETENT', 'REWORK_REQUESTED')),
    comments TEXT,
    signature_payload_hash VARCHAR(64) NOT NULL,
    signature_bytes BYTEA NOT NULL,
    public_key_fingerprint VARCHAR(128) NOT NULL,
    signed_at_utc TIMESTAMPTZ NOT NULL,
    synced_at_utc TIMESTAMPTZ,
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 10. Evidence Artifacts (Metadata, checksum, and lifecycle linking to S3/MinIO)
CREATE TABLE evidence_artifacts (
    id UUID PRIMARY KEY,
    task_entry_id UUID NOT NULL REFERENCES task_entries(id) ON DELETE RESTRICT,
    s3_key VARCHAR(512) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL CHECK (file_size_bytes >= 0),
    sha256_checksum VARCHAR(64) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_UPLOAD' CHECK (status IN ('PENDING_UPLOAD', 'UPLOADED', 'VERIFIED', 'FAILED_VERIFICATION')),
    location GEOMETRY(Point, 4326),
    captured_at_utc TIMESTAMPTZ NOT NULL,
    synced_at_utc TIMESTAMPTZ,
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_candidates_sponsoring_org_id ON candidates(sponsoring_org_id);
CREATE INDEX idx_task_definitions_program_id ON task_definitions(program_id);
CREATE INDEX idx_tar_books_candidate_id ON tar_books(candidate_id);
CREATE INDEX idx_tar_books_program_id ON tar_books(program_id);
CREATE INDEX idx_tar_books_status ON tar_books(status);
CREATE INDEX idx_task_entries_tar_book_id ON task_entries(tar_book_id);
CREATE INDEX idx_task_entries_task_def_id ON task_entries(task_definition_id);
CREATE INDEX idx_task_entries_status ON task_entries(status);
CREATE INDEX idx_task_signoffs_task_entry_id ON task_signoffs(task_entry_id);
CREATE INDEX idx_task_signoffs_officer_id ON task_signoffs(officer_user_id);
CREATE INDEX idx_evidence_artifacts_task_entry_id ON evidence_artifacts(task_entry_id);
CREATE INDEX idx_evidence_artifacts_sha256 ON evidence_artifacts(sha256_checksum);

-- Spatial Indexes (GiST)
CREATE INDEX sidx_task_entries_location ON task_entries USING GIST (location);
CREATE INDEX sidx_evidence_artifacts_location ON evidence_artifacts USING GIST (location);
