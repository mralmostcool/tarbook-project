---
title: "Table: tar_books"
---

# Table Descriptor: `tar_books`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V1__core_schema.sql`
- **Description**: Relational database table `tar_books` managed via Flyway migration `V1__core_schema.sql`.

## DDL Schema Definition

```sql
CREATE TABLE tar_books (
id UUID PRIMARY KEY,
    candidate_id UUID NOT NULL REFERENCES candidates(id) ON DELETE RESTRICT,
    program_id UUID NOT NULL REFERENCES training_programs(id) ON DELETE RESTRICT,
    issuing_org_id UUID NOT NULL REFERENCES organizations(id) ON DELETE RESTRICT,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUBMITTED', 'UNDER_REVIEW', 'CERTIFIED', 'VOIDED')),
    issued_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at_utc TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0,
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/candidates|candidates]]
- [[database/tables/organizations|organizations]]
- [[database/tables/training_programs|training_programs]]

### Child Tables (Foreign Keys Outgoing)
- [[database/tables/eligibility_assessments|eligibility_assessments]]
- [[database/tables/sea_service_records|sea_service_records]]
- [[database/tables/task_assessments|task_assessments]]
- [[database/tables/task_entries|task_entries]]
