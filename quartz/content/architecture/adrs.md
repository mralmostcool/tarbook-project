---
title: Architectural Decision Records (ADRs)
---

# Architectural Decision Records (ADRs)

This document tracks all formal architectural decisions governing **Project Tarbook**.

---

## 📑 ADR Summary Log

| ADR # | Title | Status | Primary Decision |
| :--- | :--- | :--- | :--- |
| **ADR 0001** | Spring Modulith Architecture | Accepted | Use Spring Modulith to enforce clean bounded context package boundaries. |
| **ADR 0002** | PostgreSQL + PostGIS Persistence | Accepted | Use PostGIS spatial extension for location-stamped evidence capture. |
| **ADR 0003** | Flyway Forward Migrations Only | Accepted | Strict forward-only Flyway SQL schema migrations; never edit applied files. |
| **ADR 0004** | ECDSA P-256 Key Registry | Accepted | Hardware-backed officer signing keys with ECDSA P-256 signatures. |
| **ADR 0005** | MinIO Object Storage | Accepted | S3-compatible MinIO container for photos, documents, and evidence artifacts. |
| **ADR 0006** | GiST Sea Service Exclusion Constraint | Accepted | Enforce non-overlapping sea service voyage dates per seafarer via GiST index. |
| **ADR 0007** | Redis Multi-Tier Caching | Accepted | Cache active session data, key revocations, and program templates in Redis. |
| **ADR 0008** | Reverse Proxy via Nginx | Accepted | Nginx unified gateway on port 80/443 for API, S3, pgAdmin, and Quartz docs. |
| **ADR 0009** | Spring Boot 4.1 / Java 21 Stack | Accepted | Target Java 21 LTS with Virtual Threads for high-concurrency shipboard requests. |
| **ADR 0010** | Quartz 5 Digital Garden Documentation | Accepted | Multi-stage Dockerized Quartz 5 site container for documentation portal. |
| **ADR 0011** | STCW Curriculum & Overlay Model | Accepted | Revisioned program templates with organization-specific custom overlays. |
| **ADR 0012** | STCW Eligibility Rules Engine | Accepted | Statutory criteria evaluator for sea-time and watchkeeping hour requirements. |
| **ADR 0013** | Immutable Discrepancy & Amendment Model | Accepted | Statutory corrections preserve historical records via superseding amendments. |
| **ADR 0014** | Offline Sync Queue Protocol | Accepted | Idempotent sequence-numbered sync queues for satellite transmission. |
| **ADR 0015** | Environment & Secrets Isolation | Accepted | Strict dev/prod isolation; zero secrets in version control repository. |
| **ADR 0016** | Seafarer Document & Certificate Verification | Accepted | Append-only certificate ledger with verifiable verification provenance. |

---

## 🔍 Detailed ADR Highlights

### ADR 0004: ECDSA P-256 Officer Signing Keys
* **Context**: STCW requires non-repudiable proof of supervising officer sign-offs.
* **Decision**: Officers generate an ECDSA P-256 key pair stored in hardware (WebAuthn / Secure Enclave). The public key PEM is registered in `officer_signing_keys` and approved by the shipping company.
* **Consequence**: Signatures can be verified offline by any auditor using the public key.

### ADR 0006: GiST Non-Overlapping Sea Service Constraint
* **Context**: Cadet sea-time records must not double-count overlapping dates on separate ships.
* **Decision**: Add PostgreSQL `btree_gist` extension and an `EXCLUDE USING gist` constraint on `daterange(sign_on_date, COALESCE(sign_off_date, 'infinity'), '[]')`.
* **Consequence**: Database layer physically prevents overlapping active sea service entries.
