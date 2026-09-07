---
title: "Table: organizations"
---

# Table Descriptor: `organizations`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V1__core_schema.sql`
- **Description**: Relational database table `organizations` managed via Flyway migration `V1__core_schema.sql`.

## DDL Schema Definition

```sql
CREATE TABLE organizations (
id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('MTI', 'SHIPPING_COMPANY', 'FLAG_ADMINISTRATION')),
    code VARCHAR(100) NOT NULL UNIQUE,
    license_number VARCHAR(100),
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- None

### Child Tables (Foreign Keys Outgoing)
- [[database/tables/candidates|candidates]]
- [[database/tables/document_verification_records|document_verification_records]]
- [[database/tables/officer_signing_keys|officer_signing_keys]]
- [[database/tables/tar_books|tar_books]]
- [[database/tables/vessel_crew_assignments|vessel_crew_assignments]]
