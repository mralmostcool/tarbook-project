---
title: "Table: record_amendments"
---

# Table Descriptor: `record_amendments`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V8__discrepancy_and_amendment_model.sql`
- **Description**: Relational database table `record_amendments` managed via Flyway migration `V8__discrepancy_and_amendment_model.sql`.

## DDL Schema Definition

```sql
CREATE TABLE record_amendments (
id UUID PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL CHECK (entity_type IN ('TASK_ENTRY', 'SEA_SERVICE_RECORD', 'TAR_BOOK')),
    original_record_id UUID NOT NULL,
    amended_record_id UUID NOT NULL,
    amendment_reason TEXT NOT NULL,
    authorized_by_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    sync_sequence BIGINT DEFAULT nextval('global_sync_sequence'),
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/app_users|app_users]]

### Child Tables (Foreign Keys Outgoing)
- None
