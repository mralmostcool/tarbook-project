---
title: "Table: assessment_signoffs"
---

# Table Descriptor: `assessment_signoffs`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V10__assessment_and_signoff_schema.sql`
- **Description**: Relational database table `assessment_signoffs` managed via Flyway migration `V10__assessment_and_signoff_schema.sql`.

## DDL Schema Definition

```sql
CREATE TABLE assessment_signoffs (
id UUID PRIMARY KEY,
    task_assessment_id UUID NOT NULL REFERENCES task_assessments(id) ON DELETE RESTRICT,
    signer_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    signer_role VARCHAR(50) NOT NULL CHECK (signer_role IN ('ASSESSOR', 'CHIEF_OFFICER', 'CHIEF_ENGINEER', 'MASTER', 'COMPANY_TRAINING_OFFICER')),
    verdict VARCHAR(50) NOT NULL CHECK (verdict IN ('APPROVED', 'REJECTED', 'REWORK_REQUESTED')),
    comments TEXT,
    signed_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/app_users|app_users]]
- [[database/tables/task_assessments|task_assessments]]

### Child Tables (Foreign Keys Outgoing)
- None
