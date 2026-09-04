# 13. Discrepancy & Conflict Resolution Workflow Architecture

We adopt a granular machine-readable conflict taxonomy within HTTP 200 differential sync receipts, an append-only multi-attempt supervisory evaluation chain supporting `REWORK_REQUESTED`, a superseding amendment pattern for finalized statutory records preserving complete provenance, and server-authoritative Optimistic Concurrency Control (OCC) using entity version checks.

## Status
accepted

## Context
[ADR 0003](0003-offline-sync-architecture-and-server-authority.md) and [ADR 0007](0007-shore-sync-api-protocol-wire-contracts-and-idempotency.md) established the high-level offline synchronization architecture and HTTP 200 differential receipt mechanism. In maritime satellite environments, devices spend long intervals disconnected. When multiple seafarer devices reconnect during Shore Sync, operations may collide with newer server state, violate statutory invariants, or require supervisory corrections. A formal backend discrepancy and conflict resolution architecture is required to govern failure taxonomy, supervisory rework cycles, and statutory amendments without violating append-only audit guarantees.

## Considered Options
1. **Coarse HTTP 409 Batch Abort with Client-Side Last-Write-Wins (LWW)**: Failing entire sync batches on any collision and allowing mobile edge timestamps to silently overwrite server state.
2. **In-Place Mutation & Destructive Administrative Editing**: Permitting privileged backend users to modify historical sign-offs, watch hours, or task entries in place to correct errors.
3. **Differential Conflict Taxonomy, Append-Only Evaluation Chains & Superseding Amendments**: Providing granular per-operation outcome classification in differential receipts, modeling supervisory rework as an append-only multi-attempt evaluation chain, enforcing OCC with server authority, and implementing superseding amendment records for historical corrections.

## Decision Rationale

### 1. Granular Conflict Taxonomy in Differential Sync Receipts
- **Independent Operation Outcomes**:
  - The Cloud Backend processes each `sync_operation` independently within `POST /api/v1/sync/push`.
  - Operations that validate successfully receive `status = "APPLIED"` and advance `highest_sync_sequence`.
  - Failing operations receive `status = "CONFLICT"` or `status = "REJECTED"` with an explicit `error_code`, `message`, and current `server_state` snapshot.
- **Machine-Readable Conflict Taxonomy**:
  - `VERSION_CONFLICT`: Submitted `base_version` is less than current PostgreSQL `version`.
  - `TERMINAL_STATE_LOCKED`: Attempted mutation on an entity locked by final certification or discharge (`tar_book` is `CERTIFIED`/`VOIDED`, or `sea_service_record` is `DISCHARGED`).
  - `DUPLICATE_NONCE`: Cryptographic `signing_nonce` has already been consumed by an existing sign-off.
  - `EVIDENCE_HASH_MISMATCH`: Uploaded artifact SHA-256 does not match the digest recorded during Phase 1 metadata registration.
  - `CRYPTOGRAPHIC_SIGNATURE_INVALID`: Officer ECDSA signature failed verification over the RFC 8785 canonical payload.
- **Client Discrepancy Isolation**:
  - Client immediately clears `APPLIED` items from its local outbox.
  - Conflicted operations are moved into a local *Discrepancy Queue* on the mobile device, allowing the seafarer to review discrepancies without blocking the synchronization of other clean records.

### 2. Append-Only Supervisory Rework Workflow (`REWORK_REQUESTED`)
- **Multi-Attempt Evaluation Chain**:
  - When an officer evaluates a task entry and determines competency has not yet been demonstrated or evidence is insufficient, the officer issues a sign-off with verdict `NEEDS_IMPROVEMENT`.
  - The backend transitions `task_entries.status` to `REWORK_REQUESTED`.
  - **Immutability Invariant**: The officer's evaluation and supervisory comments remain permanently recorded in `task_signoffs`. It is never mutated or deleted.
- **Candidate Correction & Re-submission**:
  - The candidate's mobile edge device unlocks the task entry for modification.
  - Candidate amends `candidate_notes` or attaches supplementary `evidence_artifacts`.
  - The edit increments `task_entries.version` and sets `status = 'SUBMITTED'`.
  - The supervising officer conducts a subsequent assessment and signs off, appending a new `task_signoffs` row referencing the new version.
  - Flag State auditors can inspect the complete progression chain: initial attempt -> supervisory feedback -> candidate rework -> subsequent successful sign-off.

### 3. Statutory Correction & Superseding Amendment Pattern
- **Preserving Audit History**:
  - Direct SQL updates or deletions on finalized statutory records (`task_entries` in `COMPLETED` state, `sea_service_records` in `DISCHARGED` state) are strictly prohibited.
- **Amendment Mechanism**:
  - Corrections (e.g. correcting misspelled voyage data, rectify watch hour clerical errors) require an explicit `AMENDMENT` operation authorized by a `COMPANY_OFFICER` or `ADMIN`.
  - The original record is marked `status = 'SUPERSEDED'` with `superseded_by_id` pointing to the new record.
  - A new replacement record is inserted with the corrected values.
  - A formal `record_amendments` row is created capturing `entity_type`, `original_record_id`, `amended_record_id`, `amendment_reason`, `authorized_by_user_id`, and `created_at_utc`.
  - Emits an append-only `audit_events` record with cryptographic hash chaining. Both historical and current versions remain permanently verifiable.

### 4. Concurrency Control Authority
- **Optimistic Concurrency Control (OCC)**:
  - All mutable entities maintain an integer `version` field incremented on every applied update.
  - Client mutations must submit the `base_version` they read from their local store.
  - Server verifies `base_version == current_db_version`:
    - On match: Mutation applied, `version` incremented, `sync_sequence` stamped.
    - On mismatch: Operation rejected with `VERSION_CONFLICT` and current `server_state`.
  - Client clock timestamps are never used for write arbitration. PostgreSQL is the sole authoritative state arbiter.

## Consequences
- Requires Flyway migration `V8__discrepancy_and_amendment_model.sql` adding `REWORK_REQUESTED` and `SUPERSEDED` statuses, linkage columns, and `record_amendments` table.
- Extends Spring Boot sync controller and service to emit structured conflict taxonomy in `SyncReceipt`.
- Mobile client must implement local discrepancy queue UI for reviewing conflicted mutations.
