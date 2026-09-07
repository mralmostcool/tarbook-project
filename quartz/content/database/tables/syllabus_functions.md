---
title: "Table: syllabus_functions"
---

# Table Descriptor: `syllabus_functions`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V12__program_and_journal_persistence_schema.sql`
- **Description**: Relational database table `syllabus_functions` managed via Flyway migration `V12__program_and_journal_persistence_schema.sql`.

## DDL Schema Definition

```sql
CREATE TABLE syllabus_functions (
id UUID PRIMARY KEY,
    program_id UUID NOT NULL REFERENCES stcw_programs(id) ON DELETE RESTRICT,
    function_code VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    stcw_code VARCHAR(50) NOT NULL,
    display_order INTEGER NOT NULL
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/stcw_programs|stcw_programs]]

### Child Tables (Foreign Keys Outgoing)
- [[database/tables/syllabus_tasks|syllabus_tasks]]
