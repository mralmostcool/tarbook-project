---
title: "Table: vessel_crew_assignments"
---

# Table Descriptor: `vessel_crew_assignments`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V5__crew_roster_and_vessel_assignments.sql`
- **Description**: Relational database table `vessel_crew_assignments` managed via Flyway migration `V5__crew_roster_and_vessel_assignments.sql`.

## DDL Schema Definition

```sql
CREATE TABLE vessel_crew_assignments (
id UUID PRIMARY KEY,
    sponsoring_org_id UUID NOT NULL REFERENCES organizations(id) ON DELETE RESTRICT,
    officer_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    external_assignment_id VARCHAR(100),
    vessel_name VARCHAR(255) NOT NULL,
    vessel_imo VARCHAR(10) NOT NULL,
    rank VARCHAR(50) NOT NULL,
    sign_on_date DATE NOT NULL,
    sign_off_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('SCHEDULED', 'ACTIVE', 'COMPLETED', 'CANCELLED')),
    sync_sequence BIGINT DEFAULT nextval('global_sync_sequence'),
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_crew_assignment_dates CHECK (sign_off_date IS NULL OR sign_off_date >= sign_on_date),
    CONSTRAINT uq_non_overlapping_officer_assignment EXCLUDE USING gist (
        officer_user_id WITH =,
        daterange(sign_on_date, COALESCE(sign_off_date, 'infinity'), '[]') WITH &&
    ) WHERE (status IN ('ACTIVE', 'SCHEDULED', 'COMPLETED'))
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/app_users|app_users]]
- [[database/tables/organizations|organizations]]

### Child Tables (Foreign Keys Outgoing)
- [[database/tables/journal_entries|journal_entries]]
