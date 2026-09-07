---
title: Domain Model & Invariants
---

# Domain Model & STCW Invariants

The domain model of Project Tarbook encapsulates the operational realities of maritime cadet training under international statutory frameworks.

---

## ⚓ Key Domain Entities & Aggregates

```mermaid
erDiagram
    AppUser ||--o| Candidate : "is candidate"
    Organization ||--o{ Candidate : "sponsors"
    AppUser ||--o{ OfficerSigningKey : "owns"
    Candidate ||--o{ VesselCrewAssignment : "assigned to"
    StcwProgram ||--o{ SyllabusFunction : "contains"
    SyllabusFunction ||--o{ SyllabusTask : "defines"
    SyllabusTask ||--o{ TaskPrerequisite : "requires"
    AppUser ||--o{ JournalEntry : "logs"
    JournalEntry ||--o{ EntryAttachment : "attaches"
    JournalEntry ||--o{ EvidenceArtifact : "provides evidence"
    SyllabusTask ||--o{ TaskAssessment : "assessed in"
    TaskAssessment ||--o{ AssessmentSignOff : "signed by officer"
    Candidate ||--o{ SeaServiceRecord : "records sea time"
    SeaServiceRecord ||--o{ SeaServiceEndorsement : "endorsed by master"
    Candidate ||--o{ SeafarerDocument : "holds travel doc"
    Candidate ||--o{ SeafarerCertificate : "holds STCW cert"
```

---

## 📜 STCW Compliance & Domain Invariants

### 1. Sea Service Non-Overlapping Invariant
A seafarer cannot physically serve on two vessels simultaneously. Sea service records enforce non-overlapping date ranges per candidate using PostgreSQL GiST exclusion constraints (`uq_non_overlapping_sea_service`).

### 2. Statutory Officer Sign-Off & Hardware Provenance
All task sign-offs and master endorsements must be signed using ECDSA P-256 keys registered in `OfficerSigningKey`. Keys must be approved by the sponsoring organization before sign-offs are valid.

### 3. Hash Chaining for Audit Logs
Security and audit logs enforce append-only immutability through SHA-256 hash chaining (`prev_hash` -> `entry_hash`), preventing silent deletion or retrospective modification of logs.

### 4. Prerequisite Task Verification
A candidate cannot obtain final sign-off on advanced tasks until all prerequisite tasks (defined in `TaskPrerequisite`) are verified as completed.

---

## 🔄 Lifecycle State Machines

### Journal Entry State Machine
```mermaid
stateDiagram-v2
    [*] --> DRAFT : Candidate creates entry
    DRAFT --> SUBMITTED : Submitted for review
    SUBMITTED --> IN_REVIEW : Supervising officer reviews
    IN_REVIEW --> COMPLETED : Officer approves
    IN_REVIEW --> REWORK_REQUESTED : Officer requests changes
    REWORK_REQUESTED --> DRAFT : Candidate edits entry
    COMPLETED --> SUPERSEDED : Statutory amendment issued
```

### Assessment Status State Machine
```mermaid
stateDiagram-v2
    [*] --> PENDING : Task assigned
    PENDING --> IN_PROGRESS : Evidence attached
    IN_PROGRESS --> APPROVED : Officer signs off
    IN_PROGRESS --> REJECTED : Grade unsatisfactory
    IN_PROGRESS --> REWORK_REQUESTED : Re-submission needed
    REWORK_REQUESTED --> IN_PROGRESS : Candidate re-submits
```
