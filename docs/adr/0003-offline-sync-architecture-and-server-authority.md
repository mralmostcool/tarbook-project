# 3. Offline Sync Architecture & Server Authority

We adopt a transactional outbox delta sync protocol using authoritative PostgreSQL-sequenced Bigints, independently idempotent batch operations, decoupled two-phase evidence transfer, and compressed JSON over HTTP/2.

## Status
accepted

## Context
Project Tarbook candidates and supervising officers operate at sea with zero or intermittent network connectivity for weeks. When reaching port or coastal reception, edge devices perform a Shore Sync. Satellite links are expensive and unstable. Training records carry statutory legal weight under STCW, demanding strict auditability without risking data loss from partial sync failures.

## Considered Options
1. **Distributed CRDTs (Automerge / Yjs)**: State-based or operation-based CRDTs replicating state across devices.
2. **Atomic Batch Sync with Last-Write-Wins (LWW)**: Full-batch rollback on any conflict, client clock timestamps determining win/loss.
3. **Independently Idempotent Delta Sync with Server Authority**: Authoritative backend sequence (`sync_sequence BIGINT`), independently idempotent batch operations with payload hashing, state-machine terminal dominance, and decoupled two-phase evidence artifact upload.

## Decision Rationale
- **Server Authority**: The client is permitted to operate offline, but is never authoritative. The cloud backend PostgreSQL database is the sole authority for state transitions, sequence ordering, and statutory validity.
- **Independently Idempotent Operations**: Sync batches are processed as independent, idempotent operations rather than an all-or-nothing atomic batch. Valid operations commit immediately; conflicting or rejected operations return granular error receipts without blocking unrelated valid entries from committing.
- **Authoritative Sync Sequence**: Delta synchronization correctness relies solely on an authoritative, monotonically increasing `sync_sequence BIGINT` assigned by PostgreSQL. Timestamps (`committed_at_utc`) are retained as operational metadata only.
- **Operational-Level Integrity**: Operational-level payload hashes ensure idempotency across connection drops and retries.
- **Decoupled Two-Phase Evidence Transfer**:
  - *Phase 1 (Metadata)*: Lightweight JSON upload commits task entries, sign-offs, and evidence metadata (`PENDING_UPLOAD` status with SHA-256 hash).
  - *Phase 2 (Binary)*: Resumable binary upload directly to S3/MinIO. Checksum verification failure logs an audit anomaly and marks `FAILED_VERIFICATION`, but never deletes metadata, never overwrites expected hashes, and does not retroactively negate the legal occurrence of an in-person statutory officer sign-off.
- **Wire Compression**: Compressed JSON over HTTP/2 with a benchmark requirement (`gzip` vs `zstd`) before standardizing.

## Consequences
- Requires a database migration to add `sync_sequence` and sync tracking tables (`sync_sessions`, `sync_operations`).
- Edge mobile clients must maintain a local transactional outbox queue and implement chunked S3 upload for media.
- Compression benchmarks must be conducted prior to production deployment.
