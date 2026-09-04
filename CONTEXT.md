# Tarbook Domain Context

Electronic Training and Assessment Record Book for maritime cadets and officers, providing tamper-evident logging, verification, and statutory compliance under intermittent connectivity.

## Language

**Candidate**:
A maritime cadet or trainee undergoing approved shipboard training towards STCW certification.
_Avoid_: Trainee, student, apprentice

**Shore Sync**:
The batch synchronization process executed when a candidate's device gains network connectivity in port or coastal range.
_Avoid_: Live sync, real-time sync, cloud polling

**Cloud Backend**:
The central ashore server environment hosting primary databases, statutory audit archives, and tenant management.
_Avoid_: Vessel server, shipboard gateway, edge host

**TAR Book**:
The digital record book instance issued to a candidate for an approved training program.
_Avoid_: Logbook, training record (overloaded), portfolio

**Task Entry**:
An individual logged training task or competency activity performed by a candidate.
_Avoid_: Log entry, activity item, training item

**Task Sign-Off**:
An immutable record of assessment or verification executed by an authorized supervising officer or assessor.
_Avoid_: Approval stamp, officer endorsement, signature record

**Evidence Artifact**:
A digital file capturing verifiable supporting evidence linked to a task entry.
_Avoid_: Attachment, media file, upload

**Organization**:
A legal maritime entity participating in training governance, such as a Maritime Training Institute (MTI), shipping company, or flag state administration.
_Avoid_: Tenant, company (overloaded), institute

**Training Program**:
A structured curriculum conforming to STCW regulations governing a candidate's training syllabus.
_Avoid_: Course, syllabus, track

**Task Definition**:
The standardized specification of a required competency task within a training program.
_Avoid_: Requirement, checklist item, assignment

**Sync Session**:
A batch of state mutations submitted by an edge device during Shore Sync.
_Avoid_: Sync run, sync push, sync request

**Sync Operation**:
An independently idempotent mutation on a single entity within a sync session.
_Avoid_: Batch item, sync action, transaction item

**Sync Sequence**:
An authoritative, monotonically increasing 64-bit integer assigned by the cloud backend determining synchronization order.
_Avoid_: Sync offset, WAL position, transaction ID

**Sync Watermark**:
An authoritative checkpoint referencing the highest sync sequence acknowledged between an edge device and cloud backend.
_Avoid_: Sync cursor, offset, sync checkpoint

**Signing Key**:
A hardware-backed, non-exportable asymmetric key pair enrolled by an officer to sign statutory records.
_Avoid_: Auth key, user key, session credential

**Signing Handshake**:
The offline air-gapped protocol executed between candidate and officer devices to authorize an assessment.
_Avoid_: Bluetooth pairing, signature request, local auth

**Canonical Payload**:
The deterministic, normalized byte representation of a task assessment signed by an officer.
_Avoid_: Signable string, payload JSON, raw record

**Hash Chain**:
The sequential cryptographic digest linking consecutive immutable records to detect server-side audit alterations.
_Avoid_: Blockchain, ledger, block history

**Pure Mobile Edge**:
The operational architecture where edge computing and persistence reside exclusively on seafarer mobile devices with zero shipboard server intermediaries.
_Avoid_: Vessel edge, shipboard gateway, hybrid edge

**Sea Service Record**:
The statutory record of qualifying shipboard service aboard a designated vessel, including sea days and certified aggregate watchkeeping totals.
_Avoid_: Voyage log, sea time record, shipboard period

**Sea Service Endorsement**:
An immutable statutory certification executed by a Master or Chief Engineer attesting to a candidate's sea service, conduct, and watchkeeping.
_Avoid_: Discharge stamp, captain review, voyage sign-off

**Operation Envelope**:
The polymorphic container bundling an individual mutation, its entity type, action, and payload within a sync session.
_Avoid_: Message wrapper, command object, event payload

**Sync Receipt**:
The authoritative server response acknowledging a sync session, containing per-operation execution statuses and the current sync sequence.
_Avoid_: Sync ack, response envelope, status dump

**Key Attestation**:
The cryptographic verification proving a signing key was generated within a non-exportable hardware enclave (StrongBox/TEE/Secure Enclave).
_Avoid_: Key validation, hardware check, device cert

**Key Governance**:
The statutory lifecycle and organizational approval workflow controlling seafarer signing key authorization and revocation.
_Avoid_: Key management, credential admin, key auth

**Vessel Crew Assignment**:
The documented shipboard deployment of a seafarer to a designated vessel for a bounded temporal window.
_Avoid_: Crew allocation, ship berth, officer posting

**Roster Verification**:
The validation gate matching an officer's signing authority against active vessel crew deployments on the task execution date.
_Avoid_: Crew check, roster audit, shipboard auth

**Program Overlay**:
A modular curriculum layer defined by an organization that adds proprietary training tasks over a statutory STCW baseline without duplicating the parent program.
_Avoid_: Custom syllabus, company program, cloned course

**Competency Taxonomy**:
The 3-tier IMO/STCW hierarchical structure categorizing training into Function, Competency, and Task Definition.
_Avoid_: Task hierarchy, syllabus tree, category list

**Certification Pathway**:
The formal statutory track (e.g. STCW Reg II/1 OICNW, Reg III/1 OICEW) specifying qualifying sea-time thresholds and watchkeeping rules for a seafarer certificate.
_Avoid_: Training stream (overloaded), career path, licensing track

**Eligibility Assessment**:
The authoritative evaluation determining a candidate's qualification for Flag State examination, materializing qualifying days, watch hours, and competency task completion.
_Avoid_: Sea time calculation, readiness check, audit report

**Discrepancy Queue**:
The client-side staging area on an edge device isolating conflicted or rejected sync mutations for seafarer review without blocking clean transactions.
_Avoid_: Conflict list, error bin, retry queue

**Statutory Amendment**:
The formal superseding correction of a finalized maritime record that preserves the original entry, reasons, and authorized administrative provenance.
_Avoid_: Record update, edit overwrite, data fix

**Full Export**:
A self-contained TAR Book export bundle containing metadata, RFC 8785 JCS ECDSA signatures, trust material, and binary evidence artifacts.
_Avoid_: Full backup, complete zip, media dump

**Lightweight Export**:
A self-contained TAR Book export bundle containing metadata, RFC 8785 JCS ECDSA signatures, trust material, and SHA-256 evidence digests with binary evidence payloads omitted.
_Avoid_: Summary export, partial export, metadata-only zip

**Offline Verifier Core**:
The shared, decoupled cryptographic verification engine compiled to both zero-install single-file HTML/WASM browser distribution and native CLI binary targets.
_Avoid_: Web verifier, validation tool, cert checker

**Seafarer Document**:
A seafarer identity or travel document (such as Passport, CDC, SID ILO 185, or INDOS) issued by a Flag State administration.
_Avoid_: Travel card, ID paper, seafarer ID (overloaded)

**Modular Safety Certificate**:
An STCW safety or specialized training certification (e.g. BST PST/FPFF/EFA/PSSR, STSDSD, OCTCO) tracked in an append-only seafarer certificate ledger.
_Avoid_: Safety card, course ticket, STCW paper

**Pre-Embarkation Compliance Gate**:
The rule-driven validation evaluator determining candidate sea-time logging and TAR Book enrollment eligibility based on document validity, expiry, pathway, and vessel parameters.
_Avoid_: Enrollment check, medical gate, login blocker

**Document Verification Record**:
An immutable administrative record documenting MTI or Organization verification of a candidate's travel document or safety certificate, retaining verifier identity, timestamp, decision, and evidence digest.
_Avoid_: Document approval stamp, paper check, cert sign-off

**Maritime Satellite Chaos Proxy**:
A programmatically-controlled network proxy injecting bandwidth constraints (9.6–128 kbps), high latency (2000–3000ms), packet drops, and TCP socket severs to stress-test Shore Sync protocols.
_Avoid_: Network simulator, lag tool, drop proxy

**Port Docking Stampede**:
The high-concurrency surge scenario where dozens or hundreds of seafarer mobile edge devices initiate Shore Sync simultaneously upon vessel arrival in coastal or port cellular range.
_Avoid_: Port surge, sync stampede (overloaded), mass sync

**Pure Mobile Edge Core**:
The Kotlin Multiplatform (KMP) shared library executing offline domain logic, SQLDelight persistence, outbox state management, RFC 8785 canonical serialization, and Ktor sync transport across Android and iOS.
_Avoid_: Mobile app backend, offline client engine, shared SDK

**Platform Key Enclave**:
The platform-neutral abstraction interfacing shared KMP code with non-exportable hardware enclaves (Android Keystore/StrongBox and iOS Secure Enclave).
_Avoid_: Key wrapper, hardware enclave driver, device crypto API

**Air-Gapped QR Handshake**:
The offline seafarer-to-officer 2D QR scanning protocol (static single-frame or multi-frame animated stream) transferring canonical sign-off payloads between mobile screens.
_Avoid_: Bluetooth pairing, QR sync, screen scan

**Outbox State Machine**:
The 4-state client outbox lifecycle (`QUEUED`, `IN_FLIGHT`, `ACKNOWLEDGED`, `CONFLICT_STAGED`) governing local edge mutations during Shore Sync execution.
_Avoid_: Client queue state, sync buffer, mutation tracker

**Edge Computer Vision**:
The mobile document scanning pipeline executing quad boundary detection, perspective transformation, contrast enhancement, and adaptive compression on mobile edge devices.
_Avoid_: Mobile OCR, doc scanner (overloaded), image crop tool

**Bounded Evidence Pipeline**:
The memory-safe image processing downscaling and adaptive compression pipeline that produces final evidence bytes before SHA-256 digest computation.
_Avoid_: Media resizer, image pipeline, upload scaler

**Provenance Telemetry Envelope**:
The sidecar metadata container holding capture timestamp, GNSS coordinates, accuracy, capture mode, and device integrity signals cryptographically bound to an Evidence Artifact.
_Avoid_: EXIF wrapper, GPS tag, camera metadata





