---
title: "Journal Bounded Context Module"
---

# Journal Bounded Context Module

The Journal Bounded Context handles task entries, daily sea logs, evidence artifact metadata, and immutable amendments.

## Managed Domain Entities

- [[database/tables/task_entries|task_entries]]: Shipboard competency logs executed by candidates.
- [[database/tables/evidence_artifacts|evidence_artifacts]]: Evidence metadata linked to MinIO S3.
- [[database/tables/journal_entries|journal_entries]]: Daily sea service journal logs created by cadets.
- [[database/tables/entry_attachments|entry_attachments]]: File attachments for daily journal entries.
- [[database/tables/record_amendments|record_amendments]]: Immutable audit trail of statutory record amendments.

- [[sync/offline-sync-protocol|Offline Synchronization Protocol]]
- [[database/tables/index|Database Table Descriptors Registry]]
