# 18. Mobile Edge and Evidence Capture Architecture

We adopt Kotlin Multiplatform (KMP) for a shared offline mobile core, SQLDelight with native SQLite drivers for targeted edge persistence and a 4-state outbox queue, platform-neutral hardware key enclave adapters, an air-gapped 2D QR handshake protocol, and pre-hashing evidence media processing.

## Status
accepted

## Context
[Issue #19](https://github.com/mralmostcool/tarbook-project/issues/19) and [ADR 0005](0005-production-service-deployment-and-edge-topology.md) mandate implementing the Pure Mobile Edge execution plane for seafarers and supervising officers operating on personal Android and iOS mobile devices. Seafarers must log task entries, perform air-gapped officer sign-offs via screen scanning, capture photo/video evidence, and execute Shore Sync directly with the cloud backend without shipboard server intermediaries.

## Considered Options
1. **Progressive Web Application (PWA) / WebView Hybrid**: Deploying a web-based mobile app (fails offline hardware key enclave attestation, raw camera control, and high-density QR scanning).
2. **Flutter / React Native UI Frameworks**: Using Javascript/Dart cross-platform frameworks (introduces non-native bridge overhead and awkward C/C++ bindings for RFC 8785 JCS canonicalization and hardware enclave attestation).
3. **Kotlin Multiplatform (KMP) Shared Core with Native Adapters**: Shared Kotlin Multiplatform core handling domain logic, SQLDelight SQLite storage, outbox state machine, RFC 8785 canonicalization, and Ktor HTTP sync transport, paired with native UI (Jetpack Compose / SwiftUI) and platform security adapters.

## Decision Rationale

### 1. Kotlin Multiplatform (KMP) Shared Architecture
- **Shared Domain & Sync Core**:
  - 100% of domain rules, SQLDelight SQLite storage, RFC 8785 JCS canonicalization, outbox state management, and Ktor HTTP sync transport are compiled into a shared KMP library across Android and iOS.
- **Native Platform Adapters**:
  - UI (Jetpack Compose on Android, SwiftUI on iOS), hardware enclave bindings (`PlatformKeyEnclave`), platform-secure storage, and camera capture stay behind native platform adapters.

### 2. Targeted Offline Persistence & Outbox State Machine
- **SQLDelight Targeted Schema**:
  - Mobile edge database uses SQLDelight with native SQLite drivers. Mobile schema contains only the targeted subset of entities required for offline operations rather than a literal full server database mirror.
- **4-State Outbox Lifecycle**:
  - Client outbox (`sync_outbox`) governs mutations using 4 explicit states: `QUEUED`, `IN_FLIGHT`, `ACKNOWLEDGED`, and `CONFLICT_STAGED`.
  - Operations transition from `IN_FLIGHT` to `ACKNOWLEDGED` exclusively upon receiving a trustworthy differential `SyncReceipt`. Network drops or ambiguity during push never cause committed mutations to be lost or duplicated. Conflicting operations move to `CONFLICT_STAGED` in the Discrepancy Queue for seafarer review.

### 3. Air-Gapped 2D QR Handshake Engine
- **Dual-Mode Sign-Off Protocol**:
  - Air-gapped officer task sign-offs use static 2D QR codes for small payloads and reliable multi-frame animated 2D QR streams for larger payloads exceeding single-frame QR limits.
  - Transport parameters (chunk sizes, frame rate, encoding) are validated through implementation testing.

### 4. Pre-Hashing Evidence Pipeline & Secure Storage
- **Camera Evidence Pipeline**:
  - Captured photo/video evidence is processed into its final stored bytes, stripping EXIF privacy metadata as required.
  - The client calculates the SHA-256 checksum over those exact final bytes *before* creating the `PENDING_UPLOAD` metadata outbox operation.
- **Hardware Enclave & Secure Credentials**:
  - Private seafarer signing keys remain non-exportable inside Android StrongBox Keystore / TEE or iOS Secure Enclave.
  - Auth tokens and key references reside in platform-secure storage (`EncryptedSharedPreferences` / iOS `Keychain`).
- **Configurable Evidence Eviction**:
  - Uploaded evidence binaries are managed via configurable local retention/eviction policies after confirmed server verification (`VERIFIED`), preserving metadata and SHA-256 digests.

## Consequences

### Positive
- Single Kotlin codebase for domain, crypto, canonicalization, and sync outbox eliminates cross-platform implementation drift between Android and iOS.
- Native UI and native hardware enclave integration preserve platform-native UX and hardware security guarantees.
- Pre-hashing evidence media pipeline guarantees byte-exact SHA-256 verification in cloud S3/MinIO.

### Negative / Trade-offs
- Requires maintaining native `PlatformKeyEnclave` adapter wrappers for Android StrongBox and iOS Secure Enclave APIs.
- Animated multi-frame QR streams require camera scanner tuning for low-light shipboard conditions.

## References
- [GitHub Issue #19](https://github.com/mralmostcool/tarbook-project/issues/19)
- [ADR 0004: Authentication, Cryptographic Provenance, and Audit Integrity](0004-authentication-cryptographic-provenance-and-audit-integrity.md)
- [ADR 0005: Production Service Deployment and Edge Topology](0005-production-service-deployment-and-edge-topology.md)
- [ADR 0009: Officer Key Governance, Device Attestation and Canonical Signing](0009-officer-key-governance-device-attestation-and-canonical-signing.md)
