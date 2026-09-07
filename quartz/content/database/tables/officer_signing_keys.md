---
title: "Table: officer_signing_keys"
---

# Table Descriptor: `officer_signing_keys`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V3__auth_and_integrity.sql`
- **Description**: Relational database table `officer_signing_keys` managed via Flyway migration `V3__auth_and_integrity.sql`.

## DDL Schema Definition

```sql
CREATE TABLE officer_signing_keys (
id UUID PRIMARY KEY,
    officer_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    key_id VARCHAR(100) NOT NULL UNIQUE,
    public_key_pem TEXT NOT NULL,
    algorithm VARCHAR(50) NOT NULL DEFAULT 'ECDSA_P256',
    hardware_backed BOOLEAN NOT NULL DEFAULT TRUE,
    attestation_statement TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_APPROVAL' CHECK (status IN ('PENDING_APPROVAL', 'ACTIVE', 'REVOKED', 'EXPIRED')),
    approved_by_org_id UUID REFERENCES organizations(id) ON DELETE RESTRICT,
    activated_at_utc TIMESTAMPTZ,
    revoked_at_utc TIMESTAMPTZ,
    revocation_reason VARCHAR(255),
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/app_users|app_users]]
- [[database/tables/organizations|organizations]]

### Child Tables (Foreign Keys Outgoing)
- [[database/tables/sea_service_endorsements|sea_service_endorsements]]
