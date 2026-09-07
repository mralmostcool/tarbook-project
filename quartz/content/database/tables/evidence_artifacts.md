---
title: "Table: evidence_artifacts"
---

# Table Descriptor: `evidence_artifacts`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V1__core_schema.sql`
- **Description**: Relational database table `evidence_artifacts` managed via Flyway migration `V1__core_schema.sql`.

## DDL Schema Definition

```sql
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
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/task_entries|task_entries]]

### Child Tables (Foreign Keys Outgoing)
- [[database/tables/document_verification_records|document_verification_records]]
