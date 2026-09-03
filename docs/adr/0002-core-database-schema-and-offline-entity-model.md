# 2. Core Database Schema & Offline Entity Model

We adopt a two-plane relational schema on PostgreSQL with PostGIS, using client-generated UUIDv7 primary keys, append-only cryptographic sign-offs, and separate MinIO object storage for evidence artifacts.

## Status
accepted

## Context
Project Tarbook operates under intermittent maritime connectivity where candidates and supervising officers log and assess STCW training tasks on mobile devices disconnected from the internet. Training records have statutory legal weight under STCW and flag state regulations, requiring tamper-evident non-repudiation and fraud-resistant spatial verification.

## Considered Options
1. **Centralized Sequential IDs with Inline Mutable Sign-Offs**: Server-assigned `BIGSERIAL` PKs, single `training_record` table with mutable `signed_by_officer` columns, binary BLOBs in PostgreSQL.
2. **Two-Plane UUIDv7 Schema with Append-Only Sign-Offs & S3 Evidence**: Normalized template plane ashore (`organization`, `training_program`, `task_definition`) vs execution plane at sea (`tar_book`, `task_entry`, `task_signoff`, `evidence_artifact`) using UUIDv7, PostGIS geometry, offline device cryptographic signatures, and external MinIO object storage.

## Decision Rationale
- **Two-Plane Separation**: Decouples statutory STCW syllabus definitions and organizational tenancy (managed centrally ashore) from offline execution books and task entries (minted at sea).
- **UUIDv7 Primary Keys**: Enables mobile edge generation without server communication, avoids key collision on Shore Sync, and preserves chronological B-Tree index clustering in PostgreSQL.
- **Append-Only Cryptographic Sign-Offs**: Retains immutable audit history in `task_signoff` with device public key fingerprints and canonical payload hashes, satisfying STCW non-repudiation offline.
- **Evidence Storage Separation**: Relational tables track metadata, checksums (SHA-256), and lifecycle states (`PENDING_UPLOAD`, `UPLOADED`, `VERIFIED`, `FAILED_VERIFICATION`), while binary artifacts reside in MinIO/S3.
- **Spatial Verification**: PostGIS `GEOMETRY(Point, 4326)` and vessel IMO tracking ground task execution in verifiable physical coordinates without relying solely on client device clock claims.
- **Immutability Policy**: Hard and soft deletes are rejected on statutory execution tables (`task_entry`, `task_signoff`, `evidence_artifact`). Adjustments require explicit status transitions or correction records.

## Consequences
- Requires PostgreSQL PostGIS extension enabled in Flyway migrations.
- Mobile clients must implement UUIDv7 generation and asymmetric key signing for officer credentials.
- Application backend must handle asynchronous evidence upload verification against recorded SHA-256 checksums.
