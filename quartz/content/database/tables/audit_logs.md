---
title: "Table: audit_logs"
---

# Table Descriptor: `audit_logs`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V11__audit_logs_schema.sql`
- **Description**: Relational database table `audit_logs` managed via Flyway migration `V11__audit_logs_schema.sql`.

## DDL Schema Definition

```sql
CREATE TABLE audit_logs (
id UUID PRIMARY KEY,
    actor_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    action VARCHAR(100) NOT NULL,
    entity_name VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    details_json TEXT,
    prev_hash VARCHAR(64),
    entry_hash VARCHAR(64) NOT NULL,
    logged_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/app_users|app_users]]

### Child Tables (Foreign Keys Outgoing)
- None
