---
title: "Table: app_users"
---

# Table Descriptor: `app_users`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V1__core_schema.sql`
- **Description**: Relational database table `app_users` managed via Flyway migration `V1__core_schema.sql`.

## DDL Schema Definition

```sql
CREATE TABLE app_users (
id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    system_role VARCHAR(50) NOT NULL CHECK (system_role IN ('CANDIDATE', 'OFFICER', 'MASTER', 'COMPANY_OFFICER', 'ADMIN')),
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- None

### Child Tables (Foreign Keys Outgoing)
- [[database/tables/assessment_signoffs|assessment_signoffs]]
- [[database/tables/audit_events|audit_events]]
- [[database/tables/audit_logs|audit_logs]]
- [[database/tables/candidates|candidates]]
- [[database/tables/document_verification_records|document_verification_records]]
- [[database/tables/eligibility_assessments|eligibility_assessments]]
- [[database/tables/journal_entries|journal_entries]]
- [[database/tables/officer_signing_keys|officer_signing_keys]]
- [[database/tables/record_amendments|record_amendments]]
- [[database/tables/sea_service_endorsements|sea_service_endorsements]]
- [[database/tables/task_assessments|task_assessments]]
- [[database/tables/task_signoffs|task_signoffs]]
- [[database/tables/vessel_crew_assignments|vessel_crew_assignments]]
