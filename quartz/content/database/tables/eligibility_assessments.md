---
title: "Table: eligibility_assessments"
---

# Table Descriptor: `eligibility_assessments`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V7__eligibility_assessments.sql`
- **Description**: Relational database table `eligibility_assessments` managed via Flyway migration `V7__eligibility_assessments.sql`.

## DDL Schema Definition

```sql
CREATE TABLE eligibility_assessments (
id UUID PRIMARY KEY,
    tar_book_id UUID NOT NULL REFERENCES tar_books(id) ON DELETE RESTRICT,
    candidate_id UUID NOT NULL REFERENCES candidates(id) ON DELETE RESTRICT,
    certification_pathway VARCHAR(50) NOT NULL,
    rule_engine_version VARCHAR(50) NOT NULL,
    program_revision VARCHAR(50) NOT NULL,
    overall_status VARCHAR(50) NOT NULL CHECK (overall_status IN ('INELIGIBLE', 'CONDITIONALLY_ELIGIBLE', 'ELIGIBLE')),
    qualifying_sea_days NUMERIC(8,2) NOT NULL,
    qualifying_watch_hours NUMERIC(8,2) NOT NULL,
    statutory_criteria_met BOOLEAN NOT NULL,
    competency_tasks_completed INTEGER NOT NULL,
    competency_tasks_required INTEGER NOT NULL,
    pending_roster_reviews_count INTEGER NOT NULL DEFAULT 0,
    assessment_dossier JSONB NOT NULL,
    dossier_hash VARCHAR(64) NOT NULL,
    assessed_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assessed_by_user_id UUID REFERENCES app_users(id) ON DELETE RESTRICT
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/app_users|app_users]]
- [[database/tables/candidates|candidates]]
- [[database/tables/tar_books|tar_books]]

### Child Tables (Foreign Keys Outgoing)
- None
