---
title: "Table: journal_entries"
---

# Table Descriptor: `journal_entries`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V12__program_and_journal_persistence_schema.sql`
- **Description**: Relational database table `journal_entries` managed via Flyway migration `V12__program_and_journal_persistence_schema.sql`.

## DDL Schema Definition

```sql
CREATE TABLE journal_entries (
id UUID PRIMARY KEY,
    cadet_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    vessel_assignment_id UUID REFERENCES vessel_crew_assignments(id) ON DELETE RESTRICT,
    entry_date DATE NOT NULL,
    sea_days_logged NUMERIC(5,2),
    watchkeeping_hours NUMERIC(5,2),
    status VARCHAR(50) NOT NULL,
    cadet_comments TEXT,
    supervisor_comments TEXT,
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/app_users|app_users]]
- [[database/tables/vessel_crew_assignments|vessel_crew_assignments]]

### Child Tables (Foreign Keys Outgoing)
- [[database/tables/entry_attachments|entry_attachments]]
