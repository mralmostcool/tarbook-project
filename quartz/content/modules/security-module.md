---
title: "Security Bounded Context Module"
---

# Security Bounded Context Module

The Security Bounded Context enforces cryptographic provenance, ECDSA P-256 hardware key registration, and SHA-256 hash-chained audit logging.

## Managed Domain Entities

- [[database/tables/officer_signing_keys|officer_signing_keys]]: Hardware-backed ECDSA P-256 public key registry.
- [[database/tables/audit_events|audit_events]]: Cryptographically hash-chained security audit log.

- [[architecture/domain-invariants|Domain Invariants & Cryptographic Verification]]
- [[database/tables/index|Database Table Descriptors Registry]]
