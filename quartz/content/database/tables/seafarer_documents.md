---
title: "Table: seafarer_documents"
---

# Table Descriptor: `seafarer_documents`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V9__seafarer_documents_and_certificates.sql`
- **Description**: Relational database table `seafarer_documents` managed via Flyway migration `V9__seafarer_documents_and_certificates.sql`.

## DDL Schema Definition

```sql
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
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/candidates|candidates]]

### Child Tables (Foreign Keys Outgoing)
- None
