# 8. Maritime Edge Device Simulator Architecture & Client Authority Boundary

We adopt a standalone Go CLI simulator in `simulator/` using Charm Bubbletea, embedded pure-Go SQLite, dual interactive/headless execution modes, and an explicit ownership boundary enforcing that the simulator is non-authoritative.

## Status
accepted

## Context
Developing and validating the Spring Boot Shore Sync backend requires a realistic offline client. Relying solely on Postman cannot simulate accumulated offline outboxes, UUIDv7 generation, or air-gapped signing handshakes. Running Android Studio / Xcode emulators introduces heavy dev friction and is incompatible with headless CI environments.

## Considered Options
1. **Postman / Newman Test Collection**: Static HTTP request scripts (lacks local edge state, offline outbox queuing, and cryptographic handshake emulation).
2. **Native Mobile App in Emulator**: High fidelity, but slow, resource-heavy, and unsuited for rapid backend iteration or headless CI pipelines.
3. **Dedicated Go / Bubbletea CLI Simulator**: Fast, cross-platform standalone binary implementing the offline edge lifecycle with both interactive TUI and automated scriptable modes.

## Decision Rationale
- **Repository Location**: Monorepo `simulator/` directory with an independent `go.mod` file. Versioned alongside the backend and directly invocable in local dev and CI.
- **Durable Local Edge State (Pure Go SQLite)**:
  - Uses `modernc.org/sqlite` (pure Go, zero CGO).
  - Eliminates C compiler requirements on Windows/Linux/macOS while matching the relational schema of native mobile SQLite/Room.
  - Supports durable transactional outbox queuing across repeated CLI invocations.
- **Candidate & Officer Role Switching**:
  - Supports `--role=candidate` (logs tasks, stages outbox, triggers Shore Sync) and `--role=officer` (stores simulated ECDSA P-256 private key, reviews human-readable task details, issues cryptographic signatures).
  - Autonomous mode (`--scenario=dual`) simulates the complete offline 2-party air-gapped handshake without human intervention.
  - Treats simulator keys as protocol- and security-semantic simulation rather than physical hardware-enclave validation.
- **Dual gzip/zstd Engine & Built-in Benchmark**:
  - Implements both `klauspost/compress/gzip` and `klauspost/compress/zstd`.
  - Includes a built-in benchmark command (`tarbook-sim benchmark`) to execute the empirical comparison mandated by [ADR 0003](0003-offline-sync-architecture-and-server-authority.md) and [ADR 0007](0007-shore-sync-api-protocol-wire-contracts-and-idempotency.md) before standardizing the wire compression.
- **Dual Execution Modes**:
  - *Interactive TUI Mode*: Visual Charm Bubbletea dashboard for developer exploration and manual flow inspection.
  - *Headless Scriptable Mode*: Command-line execution (`tarbook-sim run --scenario=<name> --backend=<url> --json`) returning exit code 0 or 1 for automated CI integration tests.

## Explicit Ownership & Authority Boundary
To prevent testing the wrong system, the boundary between edge simulator and backend is strictly defined:

| Simulator Owns (Client Edge Scope) | Backend Owns (Authoritative Scope) |
| :--- | :--- |
| Local SQLite schema & persistence | Authoritative `sync_sequence BIGINT` |
| Local client outbox queue | Accepted canonical server state |
| Client UUIDv7 generation | Conflict resolution & terminal state dominance |
| Local task drafts & candidate notes | Statutory lifecycle state transitions |
| Signing request & QR payload formatting | Officer signature verification against registered keys |
| Signing key simulation (ECDSA P-256) | Evidence artifact SHA-256 validation |
| Canonical payload byte construction | Server-side immutable audit records & hash chains |
| Local retry state & backoff | Tenant scoping and authorization enforcement |

**Core Invariant**: The simulator is allowed to operate offline, but is NEVER authoritative. PostgreSQL remains the sole authority for sequence ordering, state transitions, and statutory validity.

## Consequences
- Requires scaffolding the `simulator/` Go module with Bubbletea, Lip Gloss, and `modernc.org/sqlite`.
- Backend CI workflow can execute end-to-end integration tests using the compiled simulator binary without booting an Android/iOS emulator.
