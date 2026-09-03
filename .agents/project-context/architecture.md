# Architecture & Delivery Decisions

This document records the architectural principles, system structures, and accepted delivery decisions for Project Tarbook.

---

## 1. Architectural Style & Principles
* **Evidence-Driven**: Architecture evolves from confirmed domain requirements and engineering evidence rather than fashion or premature frameworks.
* **Smallest Justified Commitment**: Adopt the simplest structural model that satisfies known constraints.
* **Separation of Concerns**: Isolate core maritime domain logic from delivery protocols (REST, CLI), edge storage, and infrastructure.

---

## 2. Accepted Delivery Decisions

### Branching Strategy
* **Primary Integration Branch**: `main`.
* **Discipline**: Intentional simplicity during early development. Agents MUST NOT introduce complex branching models (e.g. GitFlow) without operational necessity.
* **Evolution**: When maturity requires, transition to trunk-based development or lightweight feature branches with pull requests.

### Environment Promotion
* CI/CD must preserve the strict distinction between **Development** and **Production** environments even when deploying from the same `main` branch.
* A single `main` branch does NOT imply a shared database or runtime environment.

---

## 3. Architectural Decisions (ADR Format)

Each architectural decision must be explicitly evaluated and recorded using this standard structure:

```text
Decision: <decision title>

Context:
<why the decision was necessary, including domain constraints>

Alternatives considered:
<option A>
<option B>
<option C>

Rationale:
<why the chosen approach was selected over alternatives>

Consequences:
<benefits, trade-offs, operational costs, risks>

Status:
<proposed | accepted | superseded | deprecated>
```

---

## 4. Current Architectural Decisions
* [ADR 0001: Backend Stack & Deployment Model](../../docs/adr/0001-backend-stack-and-deployment-model.md) — Cloud-only Java 21, Spring Boot 3 MVC + Virtual Threads, JPA/Hibernate Spatial on PostgreSQL/PostGIS.
* [ADR 0002: Core Database Schema & Offline Entity Model](../../docs/adr/0002-core-database-schema-and-offline-entity-model.md) — Two-plane relational schema, UUIDv7 client primary keys, append-only cryptographic sign-offs, PostGIS spatial verification, MinIO evidence separation.
* [ADR 0003: Offline Sync Architecture & Server Authority](../../docs/adr/0003-offline-sync-architecture-and-server-authority.md) — Authoritative BIGINT sync sequences, independently idempotent batch operations, decoupled two-phase evidence transfer.
* [ADR 0004: Authentication, Cryptographic Provenance & Audit Integrity](../../docs/adr/0004-authentication-cryptographic-provenance-and-audit-integrity.md) — OAuth2/OIDC, hardware-backed ECDSA P-256 officer signing keys, air-gapped QR handshake with nonces, and database hash chaining.
* [ADR 0005: Production Service Deployment & Edge Topology](../../docs/adr/0005-production-service-deployment-and-edge-topology.md) — Provider-agnostic managed containers, dedicated ingress/TLS termination, pure mobile edge.
* [ADR 0006: Sea Service Records & Statutory Endorsement Architecture](../../docs/adr/0006-sea-service-records-and-statutory-endorsement-architecture.md) — Composite FK TAR book binding, Master-certified aggregate watchkeeping totals, GiST date range exclusion, append-only endorsement lifecycle.
* [ADR 0007: Shore Sync API Protocol, Wire Contracts & Idempotency](../../docs/adr/0007-shore-sync-api-protocol-wire-contracts-and-idempotency.md) — Heterogeneous operation envelopes, HTTP 200 differential receipts, monotonic sequence cursor pagination, presigned evidence upload.
* [ADR 0008: Maritime Edge Device Simulator Architecture & Client Authority Boundary](../../docs/adr/0008-maritime-edge-device-simulator-architecture-and-client-authority-boundary.md) — Monorepo Go Bubbletea CLI, pure-Go SQLite, dual interactive/headless CI runner, and explicit non-authoritative boundary.







---

## 5. Forbidden Patterns
* Introducing frameworks, persistence technologies, or synchronization protocols without an evaluated ADR.
* Hiding architectural choices inside implementation details.
* Converting hypotheses or assumptions into permanent architectural constraints.