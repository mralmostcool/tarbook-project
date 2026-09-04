# 17. Offline Sync Chaos & Verification Test Harness Architecture

We adopt a programmatically-controlled network fault proxy integrated into a Spring Boot Testcontainers and JUnit 5 test harness, enforcing dynamic fault injection at exact sync boundaries, server-authority clock drift assertions, two-phase media upload recovery, and zero-tolerance invariants for Shore Sync data integrity.

## Status
accepted

## Context
[Issue #18](https://github.com/mralmostcool/tarbook-project/issues/18) and [ADR 0003](0003-offline-sync-architecture-and-server-authority.md) specify strict reliability guarantees for Shore Sync under extreme maritime telecommunications constraints (9.6–128 kbps bandwidth, 2000–3000ms satellite latency, 30% random packet drop, abrupt TCP socket drops mid-transfer, and skewed mobile device clocks). Automated chaos testing is required to verify that retries produce idempotent differential receipts in PostgreSQL without data loss or sequence corruption.

## Considered Options
1. **Manual Ad-hoc Developer Network Throttling**: Relying on manual browser/OS network throttling during local testing (unrepeatable, non-automated, and unintegrated into CI).
2. **Mock HTTP WireMock Stubs**: Simulating backend responses with mock servers (fails to test actual PostgreSQL concurrency, transaction rollbacks, or S3/MinIO upload mechanics).
3. **Programmatic Network Fault Proxy with Spring Boot Testcontainers & JUnit 5**: Containerized network fault proxy (Toxiproxy baseline) programmatically controlled via REST API during Testcontainers execution, enforcing strict idempotency, sequence monotonicity, and media recovery assertions.

## Decision Rationale

### 1. Network Fault Proxy & Dynamic Injection
- **Replaceable Fault Proxy Architecture**:
  - Uses a containerized, programmatically controllable network proxy (Shopify Toxiproxy) integrated into local Compose profiles and Spring Boot Testcontainers suites.
- **Dynamic Boundary Fault Injection**:
  - Tests programmatically inject network toxicities (latency spikes, bandwidth throttling, socket severs) at precise operation boundaries (e.g., severing TCP mid-headers or mid-body during `POST /api/v1/sync/push` or Phase 2 S3 upload).

### 2. Fast PR Checks vs Extended Nightly Chaos
- **Tiered Verification Pipeline**:
  - **Fast Deterministic PR Suite**: Executes deterministic fault-injection integration tests on every Pull Request in GitHub Actions CI using Testcontainers.
  - **Extended Nightly Satellite & Stampede Suite**: Runs prolonged satellite latency profiles and 100+ concurrent client port-docking stampede scenarios on scheduled nightly CI runs and release tags.

### 3. Idempotency & Phase 2 Media Upload Recovery
- **Mid-Response Retry Assertions**:
  - When a connection is severed after backend PostgreSQL commit but before receipt delivery, client retries. Test harness asserts backend returns `status: IDEMPOTENT_SKIPPED`, preserves original `sync_sequence`, and creates zero duplicate rows.
- **Phase 2 Upload Recovery**:
  - When Phase 2 binary transfer drops, test harness verifies fallback URL refresh (`POST /api/v1/evidence/{id}/upload-url`) acquires fresh authorization, re-uploads binary, and transitions status to `VERIFIED` or `FAILED_VERIFICATION` based on SHA-256 validation without untracked artifact states.

### 4. Clock Drift & Server Authority Verification
- **Synthetic Timestamp Skew Testing**:
  - Synthetic extreme past/future client timestamps (+/- 30 days) are injected into DTO operation envelopes.
  - Test harness asserts PostgreSQL monotonically assigns authoritative `sync_sequence` values and enforces state transitions independently of client wall-clock drift.

### 5. Concurrency Harness & Zero-Tolerance Criteria
- **Port Docking Stampede**:
  - 100+ virtual client threads fire concurrent Shore Sync requests against the proxy. Harness asserts zero database deadlocks, proper rate-limiting retry behavior, and unique monotonic sequence assignment.
- **Zero-Tolerance Quality Gates**:
  - Enforces zero-tolerance failure criteria: 0 data loss, 0 duplicate sync sequences, 0 broken hash chains, 0 untracked or contradictory artifact states.

## Consequences

### Positive
- Prevents silent data corruption and duplicate sequence assignment under real-world maritime satellite constraints.
- Automated Testcontainers integration ensures chaos verification runs continuously in CI/CD without manual setup.
- Clear separation between fast PR verification and heavy nightly concurrency stampede benchmarks.

### Negative / Trade-offs
- Nightly chaos runs require dedicated CI runner execution time for 100+ concurrent virtual seafarer simulations.
- Requires maintaining Toxiproxy API helper fixtures in test utility modules.

## References
- [GitHub Issue #18](https://github.com/mralmostcool/tarbook-project/issues/18)
- [ADR 0003: Offline Sync Architecture and Server Authority](0003-offline-sync-architecture-and-server-authority.md)
- [ADR 0007: Shore Sync API Protocol, Wire Contracts and Idempotency](0007-shore-sync-api-protocol-wire-contracts-and-idempotency.md)
- [ADR 0014: Production Cloud Infrastructure and CI/CD Pipeline](0014-production-cloud-infrastructure-and-ci-cd-pipeline.md)
