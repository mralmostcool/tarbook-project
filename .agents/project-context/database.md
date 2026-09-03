# Database Conventions

## Database
PostgreSQL 16+ with PostGIS spatial extension enabled (`CREATE EXTENSION IF NOT EXISTS postgis;`).

## Schema Ownership
The Spring Boot backend owns the `public` schema. All schema changes are applied via Flyway.

## Naming
- Tables: `snake_case`, plural or cohesive domain entity (e.g., `candidates`, `tar_books`, `task_entries`, `task_signoffs`, `evidence_artifacts`).
- Columns: `snake_case` (e.g., `created_at_utc`, `task_definition_id`, `sha256_checksum`).
- Foreign keys: `fk_{referencing_table}_{referenced_table}_{column}`.
- Indexes: `idx_{table}_{column(s)}`.
- Unique constraints: `uq_{table}_{column(s)}`.
- Spatial indexes: `sidx_{table}_{geometry_column}` using GiST.

## Migrations
- Managed via Flyway in `backend/src/main/resources/db/migration`.
- Versioned files: `V{major}__{description}.sql`.
- Strictly forward-only; never modify an applied migration.

## Primary Keys
- Client-generated `UUIDv7` stored as native PostgreSQL `UUID`.
- Required for offline synchronization without ID translation or collision.

## Foreign Keys
- Explicit foreign keys with constraints.
- Execution plane records (`task_entry`, `task_signoff`, `evidence_artifact`) must use `ON DELETE RESTRICT` to prevent accidental deletion of statutory audit records.

## Constraints
- Mandatory `NOT NULL` on status, IDs, timestamps, and statutory attributes.
- `CHECK` constraints on status enums and valid numerical ranges.

## Indexing
- B-Tree indexes on foreign keys, lookups by `(candidate_id, status)`, and `(tar_book_id, status)`.
- GiST indexes on spatial columns (`location`).

## Transactions
- Unit of work per HTTP Shore Sync request or API action.
- Execution tables are append-mostly; read-committed isolation.

## Concurrency
- Optimistic concurrency using `version INTEGER DEFAULT 0` on mutable entities (`tar_book`, `task_entry`).
- Append-only entities (`task_signoff`, `evidence_artifact`) do not require optimistic locking.

## Soft Deletes
- Statutory records (`task_entry`, `task_signoff`, `evidence_artifact`, `tar_book`) MUST NOT be deleted (hard or soft).
- Status field (`DRAFT`, `SUBMITTED`, `SIGNED_OFF`, `VOIDED`) records lifecycle.

## Audit Data
- `created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`
- `updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`

## Forbidden Patterns
- Direct storage of file binary BLOBs inside PostgreSQL (use MinIO/S3 object keys).
- Floating-point `FLOAT` or `DOUBLE` for geographic locations (use PostGIS `GEOMETRY(Point, 4326)`).
- Cascading hard deletes on statutory training tables (`CASCADE DELETE`).
- Auto-incrementing sequential integers for offline syncable records.