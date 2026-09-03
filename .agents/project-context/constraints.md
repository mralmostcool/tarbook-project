# Core Constraints & Operating Environment

These constraints define the maritime domain requirements and operational realities the system must satisfy.

---

## 1. Offline and Intermittent Connectivity
The system must remain usable when network connectivity is:
* unavailable;
* unreliable;
* highly latent;
* expensive;
* or otherwise unsuitable for continuous communication.

Core user workflows must not depend on continuous network access.
The design must account for eventual synchronization between local records and remote systems.
The exact synchronization architecture, consistency model, conflict-resolution strategy, persistence mechanism, and transport protocol are architectural decisions.

---

## 2. Integrity of Training Records
Training records are high-integrity, legally significant records.
The system must preserve the provenance and history of critical actions:
* record creation;
* training activity logging;
* assessment;
* approval or sign-off;
* correction;
* reversal;
* synchronization;
* and other material state changes.

The system must make unauthorized or undetectable modification of completed records difficult and detectable. Authenticity, non-repudiation, and auditability mechanisms derive from the threat model.

---

## 3. Verification of Training Activity
Where required, the system must record contextual information associated with training activity:
* identity;
* time;
* location;
* device context;
* assessment context;
* supporting evidence;
* and synchronization context.

Location information may serve as an input into fraud-detection mechanisms. GPS or any single signal does NOT independently prove training activity occurred. Verification rules must be grounded in established domain and regulatory requirements.

---

## 4. Evidence and Attachments
The system must support digital evidence associated with training and assessments:
* photographs;
* hand-drawn schematics;
* engineering drawings;
* calculations;
* diagrams;
* scanned documents;
* and other digital artifacts.

Evidence originates on mobile devices under constraints of storage, CPU, battery, bandwidth, and processing capability.
The system requires an explicit strategy for:
* capturing evidence;
* processing and compressing evidence;
* storing evidence securely;
* associating evidence with records;
* transmitting evidence across constrained links;
* verifying evidence integrity (hashing/provenance);
* and managing evidence lifecycle.

---

## 5. Regulatory and Legal Context
Project Tarbook operates under maritime training, assessment, and certification regulations (e.g., STCW, flag state administrations, company policies).
Applicable regulations take precedence over implementation preferences.
Agents MUST distinguish between confirmed regulatory requirements, stakeholder needs, technical assumptions, and implementation choices. Never invent regulatory mandates.

---

## 6. Data Integrity and Traceability
The system must preserve enough information to establish:
* who performed an action;
* what occurred;
* when it occurred;
* what evidence supported it;
* what record state resulted;
* and under what synchronization or verification conditions the action was recorded.

---

## 7. Environment Separation
Development and production are distinct operating environments.
Development data MUST NOT unintentionally share a production database, production object store, production credentials, or other production resources.
See [`environment.md`](environment.md) for configuration and credential governance.
