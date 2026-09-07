---
title: Security & Cryptographic Provenance Module
---

# Security & Cryptographic Provenance Module

The **Security Module** (`com.mralmostcool.tarbook.security`) provides cryptographic non-repudiation, hardware key registry, and statutory sign-off verification under [[architecture/adrs#adr-0004-ecdsa-p-256-officer-signing-keys|ADR 0004]].

---

## 🔒 Cryptographic Signing Architecture

```mermaid
sequenceDiagram
    autonumber
    actor Officer as Supervising Officer
    participant App as Mobile/Web App
    participant SecurityMod as Security Module
    participant Redis as Redis Revocation List
    participant DB as PostgreSQL DB

    Officer->>App: Approve Task / Endorsement
    App->>App: Generate ECDSA P-256 Signature over Canonical Payload
    App->>SecurityMod: Submit Payload + Key ID + Nonce + Signature
    SecurityMod->>Redis: Check Key Revocation Status (officer_key_revocations)
    Redis-->>SecurityMod: Key Active
    SecurityMod->>DB: Fetch Public Key PEM (officer_signing_keys)
    SecurityMod->>SecurityMod: Verify ECDSA Signature (SHA256withECDSA)
    SecurityMod-->>App: Signature Validated (200 OK)
```

---

## 🗝️ Key Entities & Structures

### `OfficerSigningKey`
Stores public keys for supervising officers, Chief Engineers, and Masters.
* **Table**: `officer_signing_keys`
* **Algorithm**: `ECDSA_P256`
* **Key Statuses**: `PENDING_APPROVAL`, `ACTIVE`, `REVOKED`, `EXPIRED`.
* **Hardware Backed**: Boolean flag indicating hardware enclave / WebAuthn origin.

---

## 📜 Key Security Invariants

1. **Non-Exportable Hardware Keys**: Private keys never leave the officer's mobile secure enclave / YubiKey. Only public key PEMs are transmitted.
2. **Replay Attack Prevention**: Every digital signature incorporates a unique, cryptographically random `signing_nonce` (UUID) and timestamp.
3. **Instant Revocation**: When an officer reports a lost device, `SecurityService.revokeKey(keyId, reason)` immediately writes to Redis (`officer_key_revocations`) to reject future sign-offs instantly.
