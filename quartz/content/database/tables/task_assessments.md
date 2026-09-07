---
title: "Table: task_assessments"
---

# Table Descriptor: `task_assessments`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V10__assessment_and_signoff_schema.sql`
- **Description**: Relational database table `task_assessments` managed via Flyway migration `V10__assessment_and_signoff_schema.sql`.

## DDL Schema Definition

```sql
CREATE TABLE task_assessments (
id UUID PRIMARY KEY,
    tar_book_id UUID NOT NULL REFERENCES tar_books(id) ON DELETE RESTRICT,
    task_definition_id UUID NOT NULL REFERENCES task_definitions(id) ON DELETE RESTRICT,
    candidate_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    grade VARCHAR(50) NOT NULL CHECK (grade IN ('EXCELLENT', 'SATISFACTORY', 'NEEDS_IMPROVEMENT', 'NOT_YET_COMPETENT')),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'IN_PROGRESS', 'APPROVED', 'REJECTED', 'REWORK_REQUESTED')),
    comments TEXT,
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/app_users|app_users]]
- [[database/tables/tar_books|tar_books]]
- [[database/tables/task_definitions|task_definitions]]

### Child Tables (Foreign Keys Outgoing)
- [[database/tables/assessment_signoffs|assessment_signoffs]]
