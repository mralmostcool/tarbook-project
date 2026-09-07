---
title: "Table: task_prerequisites"
---

# Table Descriptor: `task_prerequisites`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V12__program_and_journal_persistence_schema.sql`
- **Description**: Relational database table `task_prerequisites` managed via Flyway migration `V12__program_and_journal_persistence_schema.sql`.

## DDL Schema Definition

```sql
CREATE TABLE task_prerequisites (
id UUID PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES syllabus_tasks(id) ON DELETE CASCADE,
    prerequisite_task_id UUID NOT NULL REFERENCES syllabus_tasks(id) ON DELETE RESTRICT
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/syllabus_tasks|syllabus_tasks]]

### Child Tables (Foreign Keys Outgoing)
- None
