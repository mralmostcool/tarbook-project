# 4. Authentication, Cryptographic Provenance & Audit Integrity

We adopt OAuth2/OIDC for online API authentication, pre-embarkation hardware-enclave ECDSA P-256 officer signing keys with organizational approval, air-gapped QR signing handshakes with informed consent display and nonces, and append-only cryptographic hash chaining combined with digital signatures.

## Status
accepted

## Context
Statutory maritime training records under STCW require verifiable proof of authenticity and non-repudiation under long disconnected periods at sea. Edge devices cannot contact an online Certificate Authority or IdP while offshore. Supervising officers and candidates operate separate physical devices in steel vessel compartments with poor wireless propagation.

## Considered Options
1. **Unified API Credentials for Signing**: Using session tokens, Basic Auth, or shared PINs to stamp officer sign-offs.
2. **Trust On First Use (TOFU) with Software Keys**: Device generates software keys at sea; server trusts first seen key.
3. **Dual-Layer Security Model**: Strict separation between ephemeral online API transport authentication (OAuth2/JWT) and statutory non-repudiation signing (hardware-enclave ECDSA P-256 keys, pre-embarkation registration, air-gapped QR handshake, and database hash chaining).

## Decision Rationale
- **Separation of API Auth & Record Signing**:
  - *Online API Transport*: OAuth2/OIDC with short-lived JWT access tokens and backend-revocable refresh tokens for Shore Sync.
  - *Statutory Record Signing*: Completely isolated from API auth. Signing keys possess their own distinct lifecycle (`PENDING_APPROVAL`, `ACTIVE`, `REVOKED`, `EXPIRED`) and are bound to individual officer identities.
- **Pre-Embarkation Officer Key Enrollment (No TOFU)**:
  - Officers generate hardware-backed, non-exportable keys (Android Keystore / iOS Secure Enclave) while ashore.
  - Hardware attestation is validated against a supported device matrix.
  - Key activation requires explicit approval from sponsoring shipping company or MTI before vessel departure. An explicit `key_id` identifies the active key.
- **Deterministic Canonical Payload & Algorithm**:
  - Officers sign ECDSA P-256 (`secp256r1`) signatures over a deterministic, normalized canonical byte payload (`task_entry_id`, `candidate_id`, `task_definition_id`, `evidence_hashes`, `verdict`, `signing_nonce`, `utc_timestamp`).
- **Air-Gapped QR Handshake with Informed Consent & Anti-Replay**:
  - Offline transfer between candidate and officer phones uses two-way 2D QR codes.
  - Officer's device reconstructs and presents human-readable task details and evidence summaries for visual confirmation prior to biometric/PIN-authorized enclave signing.
  - Candidate generates a unique `signing_nonce` per request to prevent replay attacks.
  - Officer's response returns `signature_bytes` + `key_id`.
- **Tamper Detection & Non-Repudiation**:
  - `task_signoffs` incorporate an append-only cryptographic hash chain (`prev_record_hash = sha256(...)`) for tamper detection across the audit log.
  - Acknowledged trade-off: While hash chaining detects unauthorized server-side modifications, it cannot prevent full database rewrites by a rogue privileged DBA; therefore, true non-repudiation relies on verifying officer hardware-backed digital signatures against pre-registered public keys.

## Consequences
- Need Flyway migration `V3__auth_and_integrity.sql` to introduce `officer_signing_keys` and link `key_id`, `signing_nonce`, and `prev_record_hash` in `task_signoffs`.
- Mobile edge applications must implement hardware enclave key generation, attestation verification, and QR code payload serialization.
