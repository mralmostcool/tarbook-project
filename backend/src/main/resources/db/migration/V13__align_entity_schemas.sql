-- V13__align_entity_schemas.sql
-- Schema alignment for AppUser, Organization, VesselCrewAssignment, and EvidenceArtifact JPA Entities

-- 1. AppUser entity schema alignment
ALTER TABLE app_users
    ADD COLUMN IF NOT EXISTS full_name VARCHAR(255) NOT NULL DEFAULT 'System User',
    ADD COLUMN IF NOT EXISTS system_role VARCHAR(50) NOT NULL DEFAULT 'CADET';

-- 2. Organization entity schema alignment
ALTER TABLE organizations
    ADD COLUMN IF NOT EXISTS type VARCHAR(50) NOT NULL DEFAULT 'SHIPPING_COMPANY',
    ADD COLUMN IF NOT EXISTS license_number VARCHAR(100);

-- 3. VesselCrewAssignment entity schema alignment
ALTER TABLE vessel_crew_assignments
    ADD COLUMN IF NOT EXISTS officer_user_id UUID REFERENCES app_users(id) ON DELETE RESTRICT,
    ADD COLUMN IF NOT EXISTS external_assignment_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS rank VARCHAR(50) NOT NULL DEFAULT 'CADET',
    ADD COLUMN IF NOT EXISTS status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN IF NOT EXISTS sync_sequence BIGINT;

-- 4. EvidenceArtifact entity schema alignment
ALTER TABLE evidence_artifacts
    ALTER COLUMN task_entry_id DROP NOT NULL,
    ALTER COLUMN s3_key DROP NOT NULL,
    ALTER COLUMN file_name DROP NOT NULL,
    ALTER COLUMN mime_type DROP NOT NULL,
    ALTER COLUMN file_size_bytes DROP NOT NULL,
    ALTER COLUMN sha256_checksum DROP NOT NULL,
    ALTER COLUMN captured_at_utc DROP NOT NULL,
    ADD COLUMN IF NOT EXISTS journal_entry_id UUID REFERENCES journal_entries(id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS officer_signing_key_id UUID REFERENCES officer_signing_keys(id) ON DELETE RESTRICT,
    ADD COLUMN IF NOT EXISTS artifact_type VARCHAR(50) NOT NULL DEFAULT 'PHOTO',
    ADD COLUMN IF NOT EXISTS content_hash VARCHAR(64) NOT NULL DEFAULT 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855',
    ADD COLUMN IF NOT EXISTS officer_signature TEXT,
    ADD COLUMN IF NOT EXISTS storage_key VARCHAR(512) NOT NULL DEFAULT '';
