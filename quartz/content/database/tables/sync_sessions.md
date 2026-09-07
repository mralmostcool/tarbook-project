---
title: "Table: sync_sessions"
---

# Table Descriptor: `sync_sessions`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V2__sync_subsystem.sql`
- **Description**: Relational database table `sync_sessions` managed via Flyway migration `V2__sync_subsystem.sql`.

## DDL Schema Definition

```sql
CREATE TABLE sync_sessions (
id UUID PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL,
    sync_session_id UUID NOT NULL UNIQUE,
    batch_payload_hash VARCHAR(64) NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'PARTIAL_SUCCESS', 'FAILED')),
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at_utc TIMESTAMPTZ
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- None

### Child Tables (Foreign Keys Outgoing)
- [[database/tables/sync_operations|sync_operations]]
