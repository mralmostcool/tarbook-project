---
title: "Table: training_programs"
---

# Table Descriptor: `training_programs`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V1__core_schema.sql`
- **Description**: Relational database table `training_programs` managed via Flyway migration `V1__core_schema.sql`.

## DDL Schema Definition

```sql
CREATE TABLE training_programs (
id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    training_stream VARCHAR(50) NOT NULL CHECK (training_stream IN ('DECK_CADET', 'TRAINEE_ENGINE_OFFICER', 'TRAINEE_ETO', 'GP_RATING')),
    revision VARCHAR(20) NOT NULL DEFAULT '1.0',
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- None

### Child Tables (Foreign Keys Outgoing)
- [[database/tables/tar_books|tar_books]]
- [[database/tables/task_definitions|task_definitions]]
