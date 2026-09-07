---
title: "Table: sea_service_records"
---

# Table Descriptor: `sea_service_records`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V4__sea_service_records.sql`
- **Description**: Relational database table `sea_service_records` managed via Flyway migration `V4__sea_service_records.sql`.

## DDL Schema Definition

```sql
CREATE TABLE sea_service_records (
id UUID PRIMARY KEY,
    tar_book_id UUID NOT NULL,
    candidate_id UUID NOT NULL,
    vessel_name VARCHAR(255) NOT NULL,
    vessel_imo VARCHAR(10) NOT NULL,
    flag_state VARCHAR(100) NOT NULL,
    vessel_type VARCHAR(100) NOT NULL,
    gross_tonnage NUMERIC(10,2) NOT NULL CHECK (gross_tonnage > 0),
    engine_power_kw NUMERIC(10,2) CHECK (engine_power_kw >= 0),
    sign_on_date DATE NOT NULL,
    sign_off_date DATE,
    days_at_sea INTEGER NOT NULL DEFAULT 0 CHECK (days_at_sea >= 0),
    days_in_port INTEGER NOT NULL DEFAULT 0 CHECK (days_in_port >= 0),
    bridge_watch_hours_day NUMERIC(8,2) NOT NULL DEFAULT 0 CHECK (bridge_watch_hours_day >= 0),
    bridge_watch_hours_night NUMERIC(8,2) NOT NULL DEFAULT 0 CHECK (bridge_watch_hours_night >= 0),
    engine_watch_hours_day NUMERIC(8,2) NOT NULL DEFAULT 0 CHECK (engine_watch_hours_day >= 0),
    engine_watch_hours_night NUMERIC(8,2) NOT NULL DEFAULT 0 CHECK (engine_watch_hours_night >= 0),
    steering_hours NUMERIC(8,2) NOT NULL DEFAULT 0 CHECK (steering_hours >= 0),
    rank_served VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS', 'SUBMITTED', 'DISCHARGED', 'VOIDED')),
    sync_sequence BIGINT DEFAULT nextval('global_sync_sequence'),
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sea_service_tar_book_candidate FOREIGN KEY (tar_book_id, candidate_id) REFERENCES tar_books(id, candidate_id) ON DELETE RESTRICT,
    CONSTRAINT chk_sea_service_dates CHECK (sign_off_date IS NULL OR sign_off_date >= sign_on_date),
    CONSTRAINT uq_non_overlapping_sea_service EXCLUDE USING gist (
        candidate_id WITH =,
        daterange(sign_on_date, COALESCE(sign_off_date, 'infinity'), '[]') WITH &&
    ) WHERE (status != 'VOIDED')
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/tar_books|tar_books]]

### Child Tables (Foreign Keys Outgoing)
- [[database/tables/sea_service_endorsements|sea_service_endorsements]]
