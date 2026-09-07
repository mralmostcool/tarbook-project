---
title: "Table: task_definitions"
---

# Table Descriptor: `task_definitions`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V1__core_schema.sql`
- **Description**: Relational database table `task_definitions` managed via Flyway migration `V1__core_schema.sql`.

## DDL Schema Definition

```sql
CREATE TABLE task_definitions (
id UUID PRIMARY KEY,
    program_id UUID NOT NULL REFERENCES training_programs(id) ON DELETE RESTRICT,
    code VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    criteria TEXT NOT NULL,
    stcw_reference VARCHAR(100),
    required_evidence_count INTEGER NOT NULL DEFAULT 1 CHECK (required_evidence_count >= 0),
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_task_definition_program_code UNIQUE (program_id, code)
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/training_programs|training_programs]]

### Child Tables (Foreign Keys Outgoing)
- [[database/tables/task_assessments|task_assessments]]
- [[database/tables/task_entries|task_entries]]
