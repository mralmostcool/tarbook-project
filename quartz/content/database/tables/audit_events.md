---
title: "Table: audit_events"
---

# Table Descriptor: `audit_events`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V3__auth_and_integrity.sql`
- **Description**: Relational database table `audit_events` managed via Flyway migration `V3__auth_and_integrity.sql`.

## DDL Schema Definition

```sql
CREATE TABLE audit_events (
id UUID PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor_user_id UUID REFERENCES app_users(id) ON DELETE RESTRICT,
    prev_event_hash VARCHAR(64),
    event_hash VARCHAR(64) NOT NULL,
    metadata JSONB,
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/app_users|app_users]]

### Child Tables (Foreign Keys Outgoing)
- None
