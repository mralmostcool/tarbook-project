# 6. Sea Service Records & Statutory Endorsement Architecture

We adopt a relational model for sea service records permanently bound to TAR Books via composite foreign keys, certified aggregate watchkeeping totals as the statutory source of truth, PostgreSQL GiST exclusion constraints preventing overlapping service, and append-only Master endorsements supporting interim and final discharge certifications.

## Status
accepted

## Context
Under STCW Regulation II/1, Section A-II/1 (Deck) and Section A-III/1 (Engine), candidates must complete approved seagoing service and bridge/engine room watchkeeping documented in an approved Training Record Book and verified via Sea Service Testimonials signed by the Master. Maritime operations involve changing Masters mid-voyage and require fraud prevention against overlapping vessel service claims.

## Considered Options
1. **Candidate-Only Sea Time with Granular Daily Watch Logs**: Decoupled sea service from TAR Books, tracking daily 4-hour watch shifts with individual officer sign-offs.
2. **Transferable Sea Records with Application-Layer Overlap Checks**: Mutable single-table design with software-only validation for date collisions.
3. **TAR Book Bound Aggregates with Storage-Level GiST Exclusion Constraints**: Dual linkage via composite FK `(tar_book_id, candidate_id)`, Master-certified aggregate watchkeeping totals on the record, native PostgreSQL GiST exclusion constraint on active date ranges, and append-only endorsement lifecycle (`INTERIM_HANDOVER` vs single `FINAL_DISCHARGE`).

## Decision Rationale
- **Dual Linkage & Permanent TAR Book Binding**:
  - `sea_service_record` belongs permanently to exactly one `tar_book` and one `candidate`.
  - Enforced via composite foreign key `FOREIGN KEY (tar_book_id, candidate_id) REFERENCES tar_books(id, candidate_id)` to eliminate orphan or mismatched records at the storage layer.
- **Storage-Layer Non-Overlap Guarantee**:
  - PostgreSQL GiST exclusion constraint (`btree_gist`) enforces:
    `EXCLUDE USING gist (candidate_id WITH =, daterange(sign_on_date, COALESCE(sign_off_date, 'infinity'), '[]') WITH &&) WHERE (status != 'VOIDED')`
  - Explicit boundary semantics: `[]` inclusive range prevents concurrent sign-ons across different vessels, even during concurrent Shore Syncs.
- **Statutory Aggregate Source of Truth (No Shift Logs)**:
  - Flag State examiners inspect the Master's certified aggregate totals (Sea Days, Port Days, Bridge Watch Hours Day/Night, Engine Watch Hours Day/Night, Steering Hours).
  - Storing structured certified totals directly on `sea_service_records` aligns with statutory reality and eliminates massive offline synchronization overhead and duplicate sources of truth. Educational tasks continue to be logged as `task_entry`.
- **Append-Only Endorsements & Lifecycle Control**:
  - `sea_service_endorsements` child table records endorser identity (`endorser_user_id`, `endorser_role`), `key_id`, `signing_nonce`, and ECDSA P-256 signature.
  - Supports multiple `INTERIM_HANDOVER` events (e.g. Master change of command), but enforces exactly one `FINAL_DISCHARGE` per record via partial unique index:
    `CREATE UNIQUE INDEX uq_sea_service_final_discharge ON sea_service_endorsements (sea_service_id) WHERE (endorsement_type = 'FINAL_DISCHARGE');`

## Consequences
- Requires PostgreSQL `btree_gist` extension enabled in Flyway migrations.
- UI and client models must handle open voyages (`sign_off_date IS NULL`) and transition to `DISCHARGED` upon receiving the final endorsement.
