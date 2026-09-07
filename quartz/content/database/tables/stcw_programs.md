---
title: "Table: stcw_programs"
---

# Table Descriptor: `stcw_programs`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V12__program_and_journal_persistence_schema.sql`
- **Description**: Relational database table `stcw_programs` managed via Flyway migration `V12__program_and_journal_persistence_schema.sql`.

## DDL Schema Definition

```sql
CREATE TABLE stcw_programs (
id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    stream VARCHAR(50) NOT NULL,
    total_required_sea_days INTEGER NOT NULL,
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- None

### Child Tables (Foreign Keys Outgoing)
- [[database/tables/cadet_eligibility_rules|cadet_eligibility_rules]]
- [[database/tables/syllabus_functions|syllabus_functions]]
