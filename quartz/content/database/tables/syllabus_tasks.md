---
title: "Table: syllabus_tasks"
---

# Table Descriptor: `syllabus_tasks`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V12__program_and_journal_persistence_schema.sql`
- **Description**: Relational database table `syllabus_tasks` managed via Flyway migration `V12__program_and_journal_persistence_schema.sql`.

## DDL Schema Definition

```sql
CREATE TABLE syllabus_tasks (
id UUID PRIMARY KEY,
    function_id UUID NOT NULL REFERENCES syllabus_functions(id) ON DELETE RESTRICT,
    task_code VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    required_sign_offs INTEGER NOT NULL,
    minimum_watchkeeping_hours INTEGER
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/syllabus_functions|syllabus_functions]]

### Child Tables (Foreign Keys Outgoing)
- [[database/tables/task_prerequisites|task_prerequisites]]
