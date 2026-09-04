-- V9__seafarer_documents_and_certificates.sql
-- Project Tarbook Seafarer Travel Document & STCW Modular Certificate Lifecycle (ADR 0016)

-- 1. Seafarer Travel Documents (Polymorphic schema for Passport, CDC, SID, INDOS)
CREATE TABLE seafarer_documents (
    id UUID PRIMARY KEY,
    candidate_id UUID NOT NULL REFERENCES candidates(id) ON DELETE RESTRICT,
    document_type VARCHAR(50) NOT NULL CHECK (document_type IN ('INDOS', 'CDC', 'PASSPORT', 'SID_ILO_185')),
    document_number VARCHAR(100) NOT NULL,
    issuing_country_code VARCHAR(2) NOT NULL, -- ISO 3166-1 alpha-2
    issuing_authority VARCHAR(255),
    place_of_issue VARCHAR(255),
    issue_date DATE NOT NULL,
    expiry_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EXPIRED', 'REVOKED', 'REPLACED', 'SUSPENDED_NON_COMPLIANT')),
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_seafarer_document UNIQUE (candidate_id, document_type, document_number)
);

CREATE INDEX idx_seafarer_documents_candidate ON seafarer_documents(candidate_id);
CREATE INDEX idx_seafarer_documents_status ON seafarer_documents(status);
CREATE INDEX idx_seafarer_documents_type ON seafarer_documents(document_type);

-- 2. STCW Modular Safety Certificates (Append-Only Certificate Ledger)
CREATE TABLE seafarer_certificates (
    id UUID PRIMARY KEY,
    candidate_id UUID NOT NULL REFERENCES candidates(id) ON DELETE RESTRICT,
    certificate_type VARCHAR(50) NOT NULL CHECK (certificate_type IN ('PST', 'FPFF', 'EFA', 'PSSR', 'STSDSD', 'SAT', 'OCTCO', 'GTFC', 'MEDICAL_FITNESS', 'OTHER')),
    certificate_number VARCHAR(100) NOT NULL,
    issuing_mti_name VARCHAR(255) NOT NULL,
    issuing_mti_code VARCHAR(100),
    issue_date DATE NOT NULL,
    expiry_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'REPLACED', 'EXPIRED', 'REVOKED', 'SUSPENDED_NON_COMPLIANT')),
    replaced_by_certificate_id UUID REFERENCES seafarer_certificates(id) ON DELETE RESTRICT,
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_seafarer_certificates_candidate ON seafarer_certificates(candidate_id);
CREATE INDEX idx_seafarer_certificates_status ON seafarer_certificates(status);
CREATE INDEX idx_seafarer_certificates_type ON seafarer_certificates(certificate_type);

-- 3. Document Verification Records (Immutable verification provenance table)
CREATE TABLE document_verification_records (
    id UUID PRIMARY KEY,
    target_entity_type VARCHAR(50) NOT NULL CHECK (target_entity_type IN ('SEAFARER_DOCUMENT', 'SEAFARER_CERTIFICATE')),
    target_entity_id UUID NOT NULL,
    verifier_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    verifier_org_id UUID NOT NULL REFERENCES organizations(id) ON DELETE RESTRICT,
    decision VARCHAR(50) NOT NULL CHECK (decision IN ('APPROVED', 'REJECTED', 'REVOKED')),
    decision_reason TEXT,
    evidence_artifact_id UUID REFERENCES evidence_artifacts(id) ON DELETE RESTRICT,
    evidence_digest_sha256 VARCHAR(64) NOT NULL,
    canonical_payload_jcs TEXT NOT NULL,
    verified_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_doc_verification_target ON document_verification_records(target_entity_type, target_entity_id);
CREATE INDEX idx_doc_verification_verifier ON document_verification_records(verifier_user_id);
CREATE INDEX idx_doc_verification_org ON document_verification_records(verifier_org_id);
