# 16. Seafarer Document and STCW Modular Certificate Lifecycle Architecture

We adopt a generalized, polymorphic seafarer travel document schema, an append-only STCW modular certificate ledger, a rule-driven pre-embarkation compliance gate engine, and immutable verification records with bounded RPSL/DPA tenant access.

## Status
accepted

## Context
[Issue #17](https://github.com/mralmostcool/tarbook-project/issues/17) and [`tar-book-structure.md`](../../.agents/project-context/tar-book-structure.md) (Sections 2, 4, 6) mandate managing seafarer travel documents (INDOS, CDC, Passport, SID ILO 185), STCW modular safety certificates (BST: PST, FPFF, EFA, PSSR; STSDSD; Tanker Familiarization), and pre-sea medical fitness. Pre-embarkation compliance gates must validate candidate credentials before TAR Book enrollment and sea-time logging while respecting offline mid-voyage seafarer operational realities.

## Considered Options
1. **Flat Overwritable Schema with Hard-Coded Expiry Checks**: Overwriting old certificates upon renewal and using static boolean flags for pre-sea compliance (destroys historical audit provenance).
2. **Country-Specific Isolated Document Tables**: Creating separate dedicated database tables per flag state document type (creates schema rigidity across international seafarer rosters).
3. **Generalized Polymorphic Document Schema with Append-Only Ledger & Rule-Driven Compliance Matrix**: Polymorphic seafarer document table, append-only certificate ledger, rule-driven compliance gate engine, and immutable verification records.

## Decision Rationale

### 1. Generalized Seafarer Travel Document Schema
- **Polymorphic Document Entity**:
  - `Seafarer Document` table utilizes strongly typed document discriminators (`INDOS`, `CDC`, `PASSPORT`, `SID_ILO_185`) and issuing country ISO 3166-1 alpha-2 codes.
  - Accommodates multi-flag seafarer rosters without requiring database schema migrations for new flag state document variants.

### 2. Append-Only Certificate Ledger & Lifecycle States
- **Historical Audit Provenance**:
  - STCW modular safety certificates are stored in an append-only ledger (`Seafarer Certificate` entity). Certificate renewals create new records rather than overwriting historical certificates.
  - Supported lifecycle states: `ACTIVE`, `REPLACED`, `EXPIRED`, `REVOKED`, `SUSPENDED_NON_COMPLIANT`.

### 3. Rule-Driven Pre-Embarkation Compliance Gates
- **Evaluator & State Engine**:
  - Compliance engine evaluates credential type, expiry date, seafarer `Certification Pathway`, and target vessel parameters to determine compliance status (`ACTIVE`, `CONDITIONAL_EXPIRING`, `HARD_BLOCK`, `SUSPENDED_NON_COMPLIANT`).
- **Mid-Voyage Offline Policy (`CONDITIONAL_EXPIRING`)**:
  - When a certificate or medical fitness expires during an active voyage with low connectivity, the edge device enters `CONDITIONAL_EXPIRING` state. Candidate offline training logs and task entries are preserved without hard-blocking, flagging entries for MTI/DPA review upon sync.

### 4. Document Verification & Revocation Governance
- **Verification Records**:
  - Pre-sea documents require evidence artifact upload (scan/photo) and explicit MTI / Organization verifier approval before satisfying pre-embarkation gates.
  - Verifications create an immutable `Document Verification Record` storing verifier identity, timestamp, decision, canonical document payload, and evidence SHA-256 digest.
- **Revocation Integrity**:
  - Document/certificate revocation immediately restricts future seafarer signing and compliance eligibility (`SUSPENDED_NON_COMPLIANT`).
  - Historical `Task Sign-Off` records signed prior to the revocation timestamp remain valid cryptographic evidence.

### 5. Tenant Access Bounded by Sponsorship & Vessel Assignment
- **RPSL Agency / DPA Access**:
  - Read-only candidate compliance status is exposed to authorized RPSL agency and Designated Person Ashore (DPA) tenant users strictly limited to candidates connected through an active `Vessel Crew Assignment` or formal sponsorship enrollment.

## Consequences

### Positive
- Flexible international travel document schema supporting seafarers with multi-flag state identity papers.
- Full historical compliance auditability via append-only certificate ledger.
- Prevents offline seafarer data loss at sea through `CONDITIONAL_EXPIRING` soft-flags during active voyages.

### Negative / Trade-offs
- Compliance evaluator must evaluate rules dynamically against credential type, certification pathway, and vessel parameters.
- Storage requirement increases slightly due to append-only certificate history preservation.

## References
- [GitHub Issue #17](https://github.com/mralmostcool/tarbook-project/issues/17)
- [TAR Book Structure Docs](../../.agents/project-context/tar-book-structure.md)
- [ADR 0002: Core Database Schema and Offline Entity Model](0002-core-database-schema-and-offline-entity-model.md)
- [ADR 0010: Shipboard Crew Roster and Vessel Assignment Verification](0010-shipboard-crew-roster-and-vessel-assignment-verification.md)
- [ADR 0012: STCW Sea-Time and Eligibility Rule Engine](0012-stcw-sea-time-and-eligibility-rule-engine.md)
