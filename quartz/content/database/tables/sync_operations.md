---
title: "Table: sync_operations"
---

# Table Descriptor: `sync_operations`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V2__sync_subsystem.sql`
- **Description**: Relational database table `sync_operations` managed via Flyway migration `V2__sync_subsystem.sql`.

## DDL Schema Definition

```sql
CREATE TABLE sync_operations (
id UUID PRIMARY KEY,
    sync_session_id UUID NOT NULL REFERENCES sync_sessions(id) ON DELETE RESTRICT,
    operation_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    operation_payload_hash VARCHAR(64) NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('APPLIED', 'REJECTED', 'CONFLICT', 'IDEMPOTENT_SKIPPED')),
    sync_sequence BIGINT,
    error_details TEXT,
    committed_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_sync_session_operation UNIQUE (sync_session_id, operation_id)
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/sync_sessions|sync_sessions]]

### Child Tables (Foreign Keys Outgoing)
- None
