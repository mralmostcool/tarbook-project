---
title: "Table: document_verification_records"
---

# Table Descriptor: `document_verification_records`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V9__seafarer_documents_and_certificates.sql`
- **Description**: Relational database table `document_verification_records` managed via Flyway migration `V9__seafarer_documents_and_certificates.sql`.

## DDL Schema Definition

```sql
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
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/app_users|app_users]]
- [[database/tables/evidence_artifacts|evidence_artifacts]]
- [[database/tables/organizations|organizations]]

### Child Tables (Foreign Keys Outgoing)
- None
