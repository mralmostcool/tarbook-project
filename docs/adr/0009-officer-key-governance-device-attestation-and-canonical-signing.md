# 9. Officer Key Governance, Device Attestation & Canonical Signing Specification

We adopt standard pre-embarkation hardware enclave attestation (Android Key Attestation and Apple App Attest), explicit organizational approval workflows for seafarer signing keys, sync-driven edge key distribution and revocation lists, RFC 8785 JSON Canonicalization Scheme (JCS) normalization, and ECDSA P-256 digital signatures with ASN.1 DER encoding.

## Status
accepted

## Context
[ADR 0004](0004-authentication-cryptographic-provenance-and-audit-integrity.md) established the high-level security requirement for hardware-backed digital signatures and zero Trust-On-First-Use (TOFU). [Flyway V3](../../backend/src/main/resources/db/migration/V3__auth_and_integrity.sql) provisioned `officer_signing_keys` and audit tables. A comprehensive technical specification is required to implement backend attestation verifiers, organization key governance workflows, edge distribution protocols, and byte-exact canonical signing payloads across mobile and cloud components.

## Considered Options
1. **Self-Signed Certificates / Ad-Hoc Public Key Registration**: Device generates asymmetric key pair without hardware attestation proof; backend blindly registers public key.
2. **WebAuthn / FIDO2 Passkeys for Statutory Signatures**: Using standard WebAuthn APIs for assertion signatures during offline sign-off.
3. **Hardware-Enclave Attested ECDSA P-256 with Org Approval and RFC 8785 JCS**: Pre-embarkation hardware enclave key generation (Android Keystore StrongBox/TEE, iOS Secure Enclave via App Attest), backend X.509 certificate chain validation, multi-tenant organizational activation, delta sync key propagation, and RFC 8785 deterministic JSON canonical signing.

## Decision Rationale

### 1. Pre-Embarkation Hardware Key Attestation
- **Zero TOFU Principle**: Software-generated keys or unverified hardware keys are rejected for statutory assessments. Key enrollment occurs exclusively ashore while connected to cloud services prior to vessel embarkation.
- **Android Key Attestation**:
  - Android client generates an asymmetric key pair in hardware (`KeyProperties.PURPOSE_SIGN`, `KeyProperties.DIGEST_SHA256`, algorithm `EC`, curve `secp256r1`) inside Android StrongBox Keymaster or TEE.
  - Client requests attestation certificate chain using a server-issued one-time cryptographic challenge (`POST /api/v1/officers/keys/attestation-challenge`).
  - Spring Boot backend parses the ASN.1 X.509 certificate chain:
    1. Validates chain of trust against Google Hardware Attestation Root CA (`https://android.googleapis.com/attestation/status`).
    2. Extracts the `KeyDescription` extension (OID `1.3.6.1.4.1.11129.2.1.17`).
    3. Asserts `attestationSecurityLevel` and `keymasterSecurityLevel` equal `TrustedEnvironment` or `StrongBox`.
    4. Asserts `attestationChallenge` matches the expected server-issued challenge nonce (expires in 10 minutes, single use).
    5. Asserts `keymasterSoftwareEnforced` and `keymasterTeeEnforced` specify `origin = GENERATED` (key was generated on-device, not imported).
    6. Extracts the certified public key in PEM format (`public_key_pem`) and sets `hardware_backed = TRUE`.
- **Apple DeviceCheck / App Attest**:
  - iOS client initializes `DCAppAttestService.shared.generateKey()` and `generateAssertion()`.
  - Client submits the CBOR-encoded attestation statement containing the `x5c` certificate chain and the server challenge.
  - Backend verifies:
    1. Validates Apple App Attest Root CA signature on the leaf certificate.
    2. Verifies `authData` matches the application bundle ID and team ID.
    3. Computes SHA-256 of challenge nonce + clientData and verifies against authenticator data.
    4. Extracts the P-256 public key from `credCert`.
- **Attestation Fallback Matrix**: Emulators and uncertified devices without hardware security modules fail key attestation and are barred from officer signing roles.

### 2. Key Governance & Organizational Approval Workflow
- **Multi-Tenant & Multi-Device Ownership**:
  - Every seafarer signing key belongs to a specific `officer_user_id` and is bound to a physical device (`device_id`, `device_name`).
  - Production model supports multiple active, device-specific hardware keys per seafarer (e.g. primary smartphone and backup tablet), each with its own independent `key_id` and lifecycle state.
  - Signing keys are submitted to the officer's sponsoring `approved_by_org_id` (Shipping Company, Manning Agency, or MTI).
- **Lifecycle State Machine**:
  ```text
  [Enrolled via Device App]
              ↓
      (PENDING_APPROVAL)
              ↓ (Admin Approves ashore)
           (ACTIVE)
          ↙        ↘
      (REVOKED)   (EXPIRED)
  ```
  - `PENDING_APPROVAL`: Initial state post-attestation. Cannot produce valid statutory sign-offs.
  - `ACTIVE`: Activated by an authorized organizational administrator (`COMPANY_OFFICER` or `ADMIN`). Multiple distinct devices may hold active keys concurrently; enrolling a replacement device allows selective revocation of older device keys without forcing a full identity lockout.
  - `REVOKED`: Key explicitly invalidated due to device loss, compromised credential, or crew debarkation/off-signing. Revocation reason recorded in `revocation_reason` with timestamp `revoked_at_utc`. Irreversible state.
  - `EXPIRED`: Key exceeds statutory validity lifespan (default: 24 months from activation).
- **Administrative Endpoints**:
  - `POST /api/v1/officers/keys/attestation-challenge` -> Obtain server nonce.
  - `POST /api/v1/officers/keys/register` -> Submit public key + attestation object (`status = PENDING_APPROVAL`).
  - `POST /api/v1/officers/keys/{id}/approve` -> Authorize and activate key (`status = ACTIVE`).
  - `POST /api/v1/officers/keys/{id}/revoke` -> Invalidate key (`status = REVOKED`, with mandatory reason).
  - `GET /api/v1/officers/keys/active` -> Fetch current seafarer active keys list.

### 3. Key Revocation & Edge Distribution
- **Edge Public Key Mirroring Architecture**:
  - Sequence: `Cloud Authoritative Key State` -> `Shore Sync Delta` -> `Mobile Local Key-State Store` -> `Offline QR Handshake Checks Local State`.
  - Key lifecycle transitions (`ACTIVE`, `REVOKED`, `EXPIRED`) produce standard `sync_operations` with `entity_type: "OFFICER_SIGNING_KEY"` and monotonic `sync_sequence` numbers.
  - Edge devices pull key updates through the standard `GET /api/v1/sync/pull?since={sync_sequence}` stream during every Shore Sync.
  - Edge database maintains a local read-only table of approved and revoked officer keys (`key_id`, `officer_user_id`, `public_key_pem`, `status`, `revoked_at_utc`).
- **Offline Signature Validation Rule & Eventual Revocation Boundary**:
  - During an offline air-gapped QR handshake, candidate edge device accepts a signature if:
    1. The `key_id` exists in local mirror as `ACTIVE` and belongs to the designated assessor officer.
    2. Assessment `timestamp_utc` is within `activated_at_utc` and prior to any known `revoked_at_utc`.
    3. ECDSA signature over RFC 8785 canonical payload verifies against `public_key_pem`.
  - **Explicit Invariant on Offline Revocation**: Offline revocation is inherently eventual; a disconnected edge device cannot enforce a revocation that has not yet been delivered over Shore Sync. When the candidate executes Shore Sync push, the cloud backend applies authoritative validation against server state at `timestamp_utc`.

### 4. Deterministic Canonical Payload Specification
- **Standard Format**: RFC 8785 JSON Canonicalization Scheme (JCS).
- **Task Sign-Off Canonical Payload Schema**:
  The canonical JSON object represents the precise assessment parameters:
  ```json
  {
    "candidate_id": "018f9e61-7e32-7000-8000-000000000001",
    "evidence_hashes": [
      "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    ],
    "signing_nonce": "018f9e61-7e32-7000-8000-000000000002",
    "task_definition_id": "018f9e61-7e32-7000-8000-000000000003",
    "task_entry_id": "018f9e61-7e32-7000-8000-000000000004",
    "timestamp_utc": "2026-09-04T12:00:00Z",
    "verdict": "COMPLETED"
  }
  ```
- **Canonical Encoding Rules**:
  - RFC 8785 lexicographical property key sorting (UTF-16 code units).
  - No whitespace between keys, colons, or values.
  - Standard IEEE 754 number serialization (no trailing zeros, exponential notation according to ECMAScript standard).
  - Strict UTF-8 character encoding without BOM.
  - `evidence_hashes` array is sorted lexicographically in ascending order prior to canonicalization.
  - `timestamp_utc` formatted strictly in ISO 8601 UTC representation (`YYYY-MM-DDTHH:mm:ssZ`).
- **Signature Cryptographic Algorithm**:
  - Algorithm: `SHA256withECDSA` over NIST P-256 (`secp256r1`).
  - Wire & storage signature encoding: Standard ASN.1 DER sequence `SEQUENCE { r INTEGER, s INTEGER }`, base64-encoded on REST interfaces and hex-encoded or byte array in cryptographic storage.

## Consequences
- Requires Spring Boot service `OfficerKeyAttestationService` integrating Android Attestation and Apple App Attest certificate parsers.
- Requires Spring Boot service `OfficerKeyGovernanceService` implementing the approval lifecycle and sync delta event publishing.
- Requires backend canonical payload builder and signature verifier conforming to RFC 8785.
- Requires mobile edge applications to integrate hardware enclave key generators and RFC 8785 JCS serialization prior to invoking cryptographic signing APIs.
