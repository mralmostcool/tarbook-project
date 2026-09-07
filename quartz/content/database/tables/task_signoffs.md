---
title: "Table: task_signoffs"
---

# Table Descriptor: `task_signoffs`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V1__core_schema.sql`
- **Description**: Relational database table `task_signoffs` managed via Flyway migration `V1__core_schema.sql`.

## DDL Schema Definition

```sql
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
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/app_users|app_users]]
- [[database/tables/task_entries|task_entries]]

### Child Tables (Foreign Keys Outgoing)
- None
