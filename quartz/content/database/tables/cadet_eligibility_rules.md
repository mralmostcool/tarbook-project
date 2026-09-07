---
title: "Table: cadet_eligibility_rules"
---

# Table Descriptor: `cadet_eligibility_rules`

- **Subsystem / Bounded Context**: Database Persistence Layer
- **Flyway Migration Source**: `V12__program_and_journal_persistence_schema.sql`
- **Description**: Relational database table `cadet_eligibility_rules` managed via Flyway migration `V12__program_and_journal_persistence_schema.sql`.

## DDL Schema Definition

```sql
CREATE TABLE cadet_eligibility_rules (
id UUID PRIMARY KEY,
    program_id UUID NOT NULL REFERENCES stcw_programs(id) ON DELETE RESTRICT,
    rule_code VARCHAR(50) NOT NULL,
    min_sea_days INTEGER NOT NULL,
    min_bridge_watchkeeping_hours INTEGER,
    min_engine_watchkeeping_hours INTEGER
);
```

## Connected Tables & Foreign Keys

### Parent Tables (Foreign Keys Incoming)
- [[database/tables/stcw_programs|stcw_programs]]

### Child Tables (Foreign Keys Outgoing)
- None
