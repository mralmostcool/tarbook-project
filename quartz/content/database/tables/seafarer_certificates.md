---
title: "Table: seafarer_certificates"
---

# Table Descriptor: `seafarer_certificates`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V9__seafarer_documents_and_certificates.sql`
- **Description**: Relational database table `seafarer_certificates` managed via Flyway migration `V9__seafarer_documents_and_certificates.sql`.

## DDL Schema Definition

```sql
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
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/candidates|candidates]]

### Child Tables (Foreign Keys Outgoing)
- None
