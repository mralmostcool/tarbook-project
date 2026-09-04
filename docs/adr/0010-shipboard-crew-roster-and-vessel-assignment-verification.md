# 10. Shipboard Crew Roster and Vessel Assignment Verification Subsystem

We adopt a PostgreSQL GiST-constrained vessel crew assignment model, a soft verification gate on Shore Sync push that flags unrostered officer sign-offs as `ROSTER_UNVERIFIED` for company audit without blocking legitimate sea training, tenant-scoped idempotent batch REST ingestion for shipping company HR feeds, and Shore Sync edge roster distribution for offline advisory pre-validation.

## Status
accepted

## Context
Maritime statutory regulations (STCW) mandate that practical training tasks and assessments be signed off exclusively by qualified officers serving aboard the same vessel during the candidate's sea service period. Fraudulent or out-of-band sign-offs (e.g. shore-side backdated endorsements by officers on other vessels) must be detectable and auditable. However, shipping company HR/ERP crew roster feeds often experience operational synchronization latency. Rigidly rejecting offline assessments when crew lists are temporarily out-of-sync would disrupt seafarer training in remote waters.

## Considered Options
1. **Hard Verification Gate on Sync Push**: Immediately reject any `TASK_SIGNOFF` sync operation with `REJECTED` error `OFFICER_NOT_ASSIGNED_TO_VESSEL` if no active roster record exists for the date.
2. **Advisory Audit Logging Only**: Record sign-offs as verified without explicit status flags, emitting backend audit logs only.
3. **Soft Verification Gate with Temporal GiST Constraints, Idempotent Batch Ingestion & Edge Advisory**: Accept and persist cryptographic digital signatures, flag unconfirmed assignments as `ROSTER_UNVERIFIED` for administrative review, enforce temporal deployment non-overlap at the database layer, ingest HR feeds via idempotent batch REST endpoints, and mirror active vessel rosters to edge devices for offline advisory warnings.

## Decision Rationale

### 1. Vessel Crew Assignment Schema & Temporal Non-Overlap
- **Entity Model**:
  - Table: `vessel_crew_assignments` linking `app_user_id`, `sponsoring_org_id`, `vessel_imo`, `vessel_name`, `rank`, `sign_on_date`, `sign_off_date`, and `status` (`SCHEDULED`, `ACTIVE`, `COMPLETED`, `CANCELLED`).
- **Database GiST Exclusion Invariant**:
  - Temporal overlap across different ships for the same seafarer is physically impossible and strictly prohibited.
  - Enforced via PostgreSQL GiST exclusion constraint with `btree_gist`:
    ```sql
    CONSTRAINT uq_non_overlapping_officer_assignment EXCLUDE USING gist (
        app_user_id WITH =,
        daterange(sign_on_date, COALESCE(sign_off_date, 'infinity'), '[]') WITH &&
    ) WHERE (status IN ('ACTIVE', 'SCHEDULED', 'COMPLETED'));
    ```
  - Prevents corrupt HR data feeds or concurrent duplicate vessel assignments.

### 2. Soft Verification Gate on Shore Sync Push
- **Validation Pipeline**:
  - When candidate submits a `TASK_SIGNOFF` mutation via `POST /api/v1/sync/push`:
    1. Authenticate and verify the officer's hardware-backed ECDSA P-256 digital signature over the RFC 8785 canonical payload against their enrolled `public_key_pem`.
    2. Check `vessel_crew_assignments` for the signing `officer_user_id` on the candidate's `vessel_imo` for the task's `logged_at_utc::date`.
  - **Status Classification**:
    - **`ROSTER_VERIFIED`**: Officer had an active vessel assignment covering the task execution date.
    - **`ROSTER_UNVERIFIED`**: Valid cryptographic signature present, but no active roster record exists for that vessel and date. Persisted with `verification_status = 'ROSTER_UNVERIFIED'` and queued for company audit review.
- **Audit & Resolution Workflow**:
  - Emits an immutable `audit_events` record linking the task sign-off and flagged officer.
  - Sponsoring shipping company administrators (`COMPANY_OFFICER` / `ADMIN`) can view unverified sign-offs, reconcile late HR embarkation updates, or confirm manual exemptions in the web portal.

### 3. Shipping Company HR / ERP Crew Feed Ingestion
- **Idempotent Batch Endpoint**:
  - `POST /api/v1/roster/crew-assignments/batch`
  - Authorization: Restricted to `COMPANY_OFFICER` and `ADMIN` roles for the authenticated tenant organization (`sponsoring_org_id`).
  - Request format:
    ```json
    {
      "batch_id": "018f9e61-...",
      "assignments": [
        {
          "external_assignment_id": "HR-CREW-98124",
          "seafarer_email": "chief.mate@shipping-co.com",
          "vessel_imo": "9123456",
          "vessel_name": "MV Pacific Star",
          "rank": "CHIEF_MATE",
          "sign_on_date": "2026-08-01",
          "sign_off_date": "2026-12-15",
          "status": "ACTIVE"
        }
      ]
    }
    ```
  - Upsert semantics keyed on `(sponsoring_org_id, external_assignment_id)` ensuring retry-safety and idempotency for automated integration schedulers.

### 4. Mobile Edge Roster Synchronization & Advisory Warnings
- **Delta Stream Distribution**:
  - `GET /api/v1/sync/pull` includes active and recent vessel crew assignments (`entity_type: "VESSEL_CREW_ASSIGNMENT"`) scoped to the candidate's assigned vessel.
  - Edge database maintains a local cache of active shipboard officers and their ranks.
- **Offline Signing Handshake UX**:
  - During the air-gapped QR signing handshake:
    - Candidate app inspects local roster cache for the scanned officer's `officer_user_id`.
    - If found: Displays officer name, rank, and "Roster Confirmed" indicator.
    - If absent: Displays advisory notice: *"Warning: Officer not listed on current vessel roster. Sign-off will be submitted for company audit."*
    - The candidate and officer may proceed with signing; local edge storage records the signature without obstruction.

## Consequences
- Requires Flyway migration `V5__crew_roster_and_vessel_assignments.sql` creating `vessel_crew_assignments` with GiST exclusion constraints and updating `task_signoffs` with `verification_status`.
- Requires backend Spring Boot service `CrewRosterIngestionService` and `RosterVerificationService`.
- Requires `GET /api/v1/sync/pull` to support streaming `VESSEL_CREW_ASSIGNMENT` entity types.
- Mobile client displays roster advisory badges during offline QR assessment flow.
