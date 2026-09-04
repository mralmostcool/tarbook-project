# 15. Auditor and Flag State Verification System Architecture

We adopt a decoupled, self-contained offline verification architecture for Port State Control (PSC) inspectors, Flag State surveyors, and maritime company auditors. The system provides self-contained exports, a shared verification engine compiled to HTML/WASM and native CLI, full genesis-to-present hash-chain recalculation, dual export packaging modes, and statutory sea service endorsement scope-locking.

## Status
accepted

## Context
[Issue #16](https://github.com/mralmostcool/tarbook-project/issues/16) specifies statutory compliance requirements for external auditors, PSC inspectors (e.g. Paris MoU, Tokyo MoU), and Flag State administrations (MCA, USCG, DG Shipping). Inspecting maritime seafarer records frequently occurs on ships or in port facilities without internet access. Verification must prove that candidate `Task Entry`, `Task Sign-Off`, `Sea Service Record`, and `Sea Service Endorsement` records are authentic, untampered, and correctly signed by authorized officers using hardware-backed `Signing Key` pairs.

## Considered Options
1. **Online Verification Portal Only**: Centralized web portal requiring live cloud connectivity to verify seafarer credentials (fails during offline shipboard PSC inspections).
2. **Monolithic Single PDF Exporter with Monolithic Script**: Simple PDF export verified via custom server-side scripts (lacks offline WASM browser support and formal digest validation for evidence payloads).
3. **Decoupled Shared Verification Core with Self-Contained Offline Exports**: Decoupled verification engine targeting zero-install HTML/WASM browser distribution (runnable directly from USB) and native CLI, supporting Full and Lightweight export bundles, full genesis hash-chain recalculation, and statutory scope-locking.

## Decision Rationale

### 1. Export Packaging & Dual Export Modes
- **Self-Contained Offline Package**:
  - Export bundles contain all candidate TAR Book records, officer RFC 8785 JCS ECDSA signatures, hardware key attestation certificates, signed revocation lists, and statutory metadata required for independent verification.
- **Dual Export Modes**:
  - **Full Export**: Includes all metadata, JCS ECDSA signatures, trust materials, and complete binary evidence artifacts (photos/scans).
  - **Lightweight Export**: Includes all metadata, signatures, trust materials, and SHA-256 evidence digests, while omitting binary evidence payloads for low-bandwidth transfer. The verifier flags missing binary payloads while maintaining cryptographic verification of record signatures.

### 2. Shared Verifier Core & Execution Targets
- **Single Shared Verification Engine**:
  - Core cryptographic verification logic is implemented once and compiled to multiple execution targets to prevent verification behavior divergence between web and CLI tools.
- **WASM / HTML Distribution**:
  - Packaged as a single zero-install HTML/WASM web application runnable directly from a USB flash drive or local file system in any modern browser without internet access or administrative privileges.
- **Native CLI Distribution**:
  - Compiled as a multi-platform native CLI binary for automated auditing and batch processing in company server environments.

### 3. Cryptographic Verification & Hash-Chain Auditing
- **Canonical Signatures**:
  - RFC 8785 JSON Canonicalization Scheme (JCS) paired with ECDSA P-256 signatures serves as the statutory source of truth for all seafarer records and officer endorsements.
- **Full Genesis Hash-Chain Recalculation**:
  - The verifier recomputes the append-only `Hash Chain` sequentially from origin (genesis digest) through all `Task Sign-Off` and `Sea Service Endorsement` entries.
  - This independently detects intermediate record modification, entry deletion, or chain breaks without relying on cloud backend checkpoint trust.

### 4. Statutory Testimonials & Endorsement Scope Locking
- **Master Sea Service Endorsement Immutability**:
  - Executing a `Sea Service Endorsement` hard-locks all `Sea Service Record` entries within its temporal scope against ordinary mutation or deletion.
  - Any post-endorsement correction requires the append-only `Statutory Amendment` workflow.
- **Jurisdiction-Driven Layouts**:
  - Testimonial generation dynamically formats official Master Sea Service Testimonials (USCG CG-719S, MCA, DG Shipping) based on candidate `Certification Pathway` and target jurisdiction parameters.

## Consequences

### Positive
- Full statutory compliance and offline auditability during Port State Control inspections without internet connectivity.
- Dual distribution (WASM + CLI) from a single verification core guarantees consistent verification logic.
- Full hash-chain re-evaluation guarantees detection of record deletion or server-side audit tampering.

### Negative / Trade-offs
- Exporter trust bundles must bundle signed key attestation and revocation snapshots, slightly increasing export package metadata size.
- Exact container archive format (single PDF/A-3 vs `.tarbook` ZIP) and specific PDF rendering engine are deferred to prototyping.

## References
- [GitHub Issue #16](https://github.com/mralmostcool/tarbook-project/issues/16)
- [ADR 0004: Authentication, Cryptographic Provenance, and Audit Integrity](0004-authentication-cryptographic-provenance-and-audit-integrity.md)
- [ADR 0006: Sea Service Records and Statutory Endorsement Architecture](0006-sea-service-records-and-statutory-endorsement-architecture.md)
- [ADR 0009: Officer Key Governance, Device Attestation, and Canonical Signing](0009-officer-key-governance-device-attestation-and-canonical-signing.md)
- [ADR 0013: Discrepancy and Conflict Resolution Workflow](0013-discrepancy-and-conflict-resolution-workflow.md)
