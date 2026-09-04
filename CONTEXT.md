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










