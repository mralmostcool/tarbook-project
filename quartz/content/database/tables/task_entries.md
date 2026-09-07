---
title: "Table: task_entries"
---

# Table Descriptor: `task_entries`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V1__core_schema.sql`
- **Description**: Relational database table `task_entries` managed via Flyway migration `V1__core_schema.sql`.

## DDL Schema Definition

```sql
CREATE TABLE task_entries (
id UUID PRIMARY KEY,
    tar_book_id UUID NOT NULL REFERENCES tar_books(id) ON DELETE RESTRICT,
    task_definition_id UUID NOT NULL REFERENCES task_definitions(id) ON DELETE RESTRICT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'SUBMITTED', 'SIGNED_OFF', 'REWORK_REQUESTED', 'VOIDED')),
    candidate_notes TEXT,
    location GEOMETRY(Point, 4326),
    gnss_accuracy_meters NUMERIC(6,2),
    vessel_imo VARCHAR(10),
    logged_at_utc TIMESTAMPTZ NOT NULL,
    synced_at_utc TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0,
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/tar_books|tar_books]]
- [[database/tables/task_definitions|task_definitions]]

### Child Tables (Foreign Keys Outgoing)
- [[database/tables/evidence_artifacts|evidence_artifacts]]
- [[database/tables/task_signoffs|task_signoffs]]
