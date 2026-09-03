# 7. Shore Sync API Protocol, Wire Contracts & Idempotency

We adopt an OpenAPI 3.1 REST sync protocol using heterogeneous operation envelopes, unified HTTP 200 differential receipts, monotonic `sync_sequence` cursor pagination, decoupled presigned evidence upload with fallback refresh endpoints, and dual HTTP compression negotiation.

## Status
accepted

## Context
[ADR 0003](0003-offline-sync-architecture-and-server-authority.md) established the high-level offline sync principles (server authority in PostgreSQL, authoritative BIGINT sync sequence, independently idempotent batch operations). A concrete API specification is required to build backend Spring MVC controllers and client implementations without ambiguity.

## Considered Options
1. **Typed Entity Arrays with All-or-Nothing HTTP 409**: Rigid entity-specific JSON lists, rolling back the entire HTTP request if any record conflicts.
2. **GraphQL Subscriptions over WebSocket**: Continuous bidirectional streaming (unsuitable for high-latency, intermittent maritime satellite links).
3. **Heterogeneous Operation Envelopes with Differential REST Receipts**: Unified polymorphic operation queue, HTTP 200 differential receipt, sequence cursor pagination, and decoupled presigned S3 media transfer.

## Decision Rationale
- **Heterogeneous Operation Envelope (`POST /api/v1/sync/push`)**:
  - Pushes an array of polymorphic `operations` (`operation_id`, `entity_type`, `action`, `payload`).
  - Directly matches the `sync_operations` schema from Flyway V2.
  - Enables unified SHA-256 payload hashing per mutation, explicit execution ordering, and generic idempotency checking.
- **Unified HTTP 200 Differential Receipt**:
  - The batch endpoint returns HTTP 200 OK with a structured `SyncReceipt` containing individual operation outcomes (`APPLIED`, `CONFLICT`, `REJECTED`, `IDEMPOTENT_SKIPPED`).
  - Reports the authoritative `highest_sync_sequence` committed by PostgreSQL.
  - Allows the client's outbox to clear applied records immediately while isolating conflicting mutations for user review without blocking clean records.
- **Monotonic Sequence Cursor Pagination (`GET /api/v1/sync/pull`)**:
  - Client requests delta updates via `GET /api/v1/sync/pull?since={sync_sequence}&limit={limit}`.
  - Returns `{ "items": [...], "has_more": boolean, "next_sync_sequence": number }`.
  - Guarantees bounded packet sizes over satellite connections. Client applies incoming deltas idempotently based on primary keys and version checks.
- **Decoupled Presigned Evidence Upload**:
  - The Phase 1 push receipt *may* return opportunistic presigned PUT upload instructions (URL, expiration, required headers) for newly inserted `evidence_artifacts` to save a satellite round-trip.
  - An explicit fallback endpoint (`POST /api/v1/evidence/{id}/upload-url`) refreshes expired or failed upload URLs independently, ensuring S3 presigning availability never blocks core relational transactions.
- **Conditional Wire Compression Negotiation**:
  - API supports dual HTTP content negotiation: `Content-Encoding: zstd, gzip` for incoming requests and `Accept-Encoding: zstd, gzip` for responses.
  - Formal standardization remains conditional upon completing the required `gzip` vs `zstd` benchmark on representative Tarbook payloads.
- **Strict Server Authority**:
  - The cloud PostgreSQL database remains the sole authority for sequence generation, state transitions, and statutory validity. The client never dictates sequence numbers or statutory state.

## Consequences
- Requires Spring Boot DTOs and `@RestController` implementations for `/api/v1/sync/push`, `/api/v1/sync/pull`, and `/api/v1/evidence/**`.
- Client implementations must support reading differential operation receipts and executing chunked S3 uploads.
