---
title: "Table: sea_service_endorsements"
---

# Table Descriptor: `sea_service_endorsements`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V4__sea_service_records.sql`
- **Description**: Relational database table `sea_service_endorsements` managed via Flyway migration `V4__sea_service_records.sql`.

## DDL Schema Definition

```sql
CREATE TABLE sea_service_endorsements (
id UUID PRIMARY KEY,
    sea_service_id UUID NOT NULL REFERENCES sea_service_records(id) ON DELETE RESTRICT,
    endorser_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    endorser_role VARCHAR(50) NOT NULL CHECK (endorser_role IN ('MASTER', 'CHIEF_ENGINEER')),
    endorsement_type VARCHAR(50) NOT NULL CHECK (endorsement_type IN ('INTERIM_HANDOVER', 'FINAL_DISCHARGE')),
    conduct_rating VARCHAR(50) NOT NULL,
    ability_rating VARCHAR(50) NOT NULL,
    comments TEXT,
    key_id VARCHAR(100) NOT NULL REFERENCES officer_signing_keys(key_id) ON DELETE RESTRICT,
    signing_nonce UUID NOT NULL,
    signature_payload_hash VARCHAR(64) NOT NULL,
    signature_bytes BYTEA NOT NULL,
    signed_at_utc TIMESTAMPTZ NOT NULL,
    sync_sequence BIGINT DEFAULT nextval('global_sync_sequence'),
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/app_users|app_users]]
- [[database/tables/officer_signing_keys|officer_signing_keys]]
- [[database/tables/sea_service_records|sea_service_records]]

### Child Tables (Foreign Keys Outgoing)
- None
