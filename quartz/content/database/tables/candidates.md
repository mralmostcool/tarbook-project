---
title: "Table: candidates"
---

# Table Descriptor: `candidates`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V1__core_schema.sql`
- **Description**: Relational database table `candidates` managed via Flyway migration `V1__core_schema.sql`.

## DDL Schema Definition

```sql
CREATE TABLE candidates (
id UUID PRIMARY KEY REFERENCES app_users(id) ON DELETE RESTRICT,
    sponsoring_org_id UUID REFERENCES organizations(id) ON DELETE RESTRICT,
    indos_number VARCHAR(20) NOT NULL UNIQUE,
    cdc_number VARCHAR(50) NOT NULL UNIQUE,
    training_stream VARCHAR(50) NOT NULL CHECK (training_stream IN ('DECK_CADET', 'TRAINEE_ENGINE_OFFICER', 'TRAINEE_ETO', 'GP_RATING')),
    date_of_birth DATE NOT NULL,
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/app_users|app_users]]
- [[database/tables/organizations|organizations]]

### Child Tables (Foreign Keys Outgoing)
- [[database/tables/eligibility_assessments|eligibility_assessments]]
- [[database/tables/seafarer_certificates|seafarer_certificates]]
- [[database/tables/seafarer_documents|seafarer_documents]]
- [[database/tables/tar_books|tar_books]]
