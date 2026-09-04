# 12. STCW Sea-Time and Eligibility Rule Engine Architecture

We adopt an explicit pathway-based STCW rule engine modeling statutory criteria per maritime certification standard (STCW Reg II/1, III/1, III/6), configurable watchkeeping aggregate validators without arbitrary uncertified thresholds, a stateless on-demand domain evaluator (`StcwEligibilityEvaluator`), immutable materialized `eligibility_assessments` snapshots for formal audit submissions, and a three-state (`INELIGIBLE`, `CONDITIONALLY_ELIGIBLE`, `ELIGIBLE`) evaluation classification.

## Status
accepted

## Context
[ADR 0006](0006-sea-service-records-and-statutory-endorsement-architecture.md) and [Flyway V4](../../backend/src/main/resources/db/migration/V4__sea_service_records.sql) established the data model for `sea_service_records` and Master `sea_service_endorsements`. Candidates and administrative auditors require automated verification of statutory seagoing service and competency completion to determine eligibility for Flag State Certificate of Competency (CoC) examinations under STCW. Calculating eligibility involves disparate rules depending on the candidate's certification stream, vessel technical parameters (gross tonnage, propulsion power), watchkeeping duties, and sign-off verification states.

## Considered Options
1. **Simple Cumulative Day Counters**: Summing total sea calendar days across all records without filtering by vessel qualifications or certification stream.
2. **Hard-Coded Rule Heuristics**: Embedding rigid numeric thresholds in SQL queries with arbitrary rules (such as unratified steering hour constants).
3. **Explicit Certification Pathway Rule Engine with Materialized Audit Dossiers**: Modeling certification pathways as first-class domain specifications, dynamically validating sea service and watchkeeping aggregates against governing STCW/Flag rules, providing on-demand evaluation, and materializing tamper-evident audit snapshots.

## Decision Rationale

### 1. Explicit Certification Pathways & Statutory Criteria
- **First-Class Domain Pathways**:
  - The rule engine evaluates candidate sea service against explicit certification pathways rather than generalized day sums:
    1. **STCW Regulation II/1 (Deck - Officer in Charge of Navigational Watch / OICNW)**:
       - Requires approved seagoing service of not less than 12 months as part of an approved training programme documented in an approved TAR Book (or 36 months without TAR Book).
       - Filters qualifying service to seagoing vessels of **500 Gross Tonnage (GT) or more**.
       - Requires not less than 6 months of bridge watchkeeping duties under the supervision of the Master or a certified officer.
    2. **STCW Regulation III/1 (Engine - Officer in Charge of Engineering Watch / OICEW)**:
       - Requires approved seagoing service of not less than 6 months in the engine department as part of an approved training programme documented in an approved TAR Book.
       - Filters qualifying service to seagoing vessels powered by main propulsion machinery of **750 kW propulsion power or more**.
       - Requires documented engine-room watchkeeping under the supervision of the Chief Engineer Officer or qualified engineer officer.
    3. **STCW Regulation III/6 (Electro-Technical Officer / ETO)**:
       - Requires not less than 6 months seagoing service on vessels of **750 kW or more** documented in an approved TAR Book.
- **Service Status & Endorsement Prerequisite**:
  - Only sea service records in `DISCHARGED` status bearing a valid Master/Chief Engineer `FINAL_DISCHARGE` digital endorsement are recognized as certified statutory qualifying service. In-progress voyages (`status = 'IN_PROGRESS'`) or unendorsed records are reported as preliminary/accruing only.

### 2. Watchkeeping Aggregate Validation
- **Configurable Rule Parameters**:
  - The evaluator asserts watchkeeping duration and practical aggregates where explicitly stipulated by STCW Code Tables A-II/1, A-III/1, or specific Flag Administration requirements.
  - Arbitrary constants (such as specific fixed steering hour quotas) are not hard-coded into the engine core; they are configured as pathway parameter constraints, ensuring system portability across diverse maritime administrations.
- **Watchkeeping Verification**:
  - Evaluates Day vs. Night bridge/engine watch hours and verifies supervisory endorsements.

### 3. Stateless Domain Evaluator & Materialized Audit Snapshots
- **Stateless Runtime Evaluation (`StcwEligibilityEvaluator`)**:
  - Pure Spring Boot domain service calculating candidate eligibility on demand.
  - Endpoint: `GET /api/v1/tar-books/{id}/eligibility` returns current calculated readiness from live database state without writing to storage.
- **Immutable Materialized Snapshots (`eligibility_assessments`)**:
  - When a candidate's TAR Book is submitted for MTI review, shipping company sign-off, or Flag State oral examination audit, the system materializes an immutable snapshot in `eligibility_assessments`.
  - The record captures:
    - `tar_book_id`, `candidate_id`, `certification_pathway`.
    - Exact `rule_engine_version` and `program_revision` applied during calculation.
    - Quantitative totals (qualifying sea days, watch hours, competency task counts).
    - `overall_status`: `INELIGIBLE`, `CONDITIONALLY_ELIGIBLE`, or `ELIGIBLE`.
    - Comprehensive `assessment_dossier` JSONB detailing vessel-by-vessel breakdown, endorsement validity, and task completion matrix.
    - `dossier_hash`: SHA-256 cryptographic digest ensuring tamper evidence across statutory audit archives.

### 4. Three-State Eligibility Classification
- **Classification Taxonomy**:
  - **`INELIGIBLE`**: Candidate exhibits quantitative shortfalls (insufficient qualifying sea days, missing required watchkeeping months, or unfulfilled mandatory STCW competency tasks).
  - **`CONDITIONALLY_ELIGIBLE`**: Candidate meets all quantitative sea-time, watchkeeping, and task completion thresholds, but contains flagged records (e.g. sign-offs marked `ROSTER_UNVERIFIED` pending administrative audit, or voyages awaiting formal final discharge endorsement).
  - **`ELIGIBLE`**: All statutory sea-time days, certified watchkeeping aggregates, and mandatory STCW competency sign-offs are 100% satisfied, endorsed, and roster-verified.
- **Dossier Export**:
  - Emits machine-readable JSON and standardized PDF readiness report for Flag State examiners.

## Consequences
- Requires Flyway migration `V7__eligibility_assessments.sql` creating `eligibility_assessments` table.
- Requires Spring Boot domain service `StcwEligibilityEvaluator` and controller endpoints for on-demand assessment and formal snapshot materialization.
- Enables automated pre-audit validation for maritime training institutes and shipping companies prior to candidate discharge.
