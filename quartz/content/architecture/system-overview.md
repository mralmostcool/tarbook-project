---
title: System Topology & C4 Architecture
---

# System Topology & Modular Architecture

Project Tarbook is implemented as a **Spring Modulith** backend application with explicit bounded contexts, served through a containerized infrastructure stack.

---

## 🏛️ System Architecture Diagram

```mermaid
graph TD
    Client[Mobile App / Web Client] -->|HTTP / REST| Nginx[Nginx Reverse Proxy :80 / :443]
    
    subgraph Reverse Proxy & Gateway Layer
        Nginx -->|/api/*| SpringBoot[Spring Boot 4.1.1 Modulith :8080]
        Nginx -->|/docs/*| Quartz[Quartz 5 Digital Garden :8014]
        Nginx -->|/s3/*| MinIO[MinIO Object Storage :9000]
        Nginx -->|/minio-console/*| MinIOCConsole[MinIO Web Console :9001]
        Nginx -->|/pgadmin/*| PgAdmin[pgAdmin 4 Console :5050]
    end

    subgraph Spring Modulith Bounded Contexts
        SpringBoot --> Core[Core Module]
        SpringBoot --> Security[Security Module]
        SpringBoot --> Program[Program Module]
        SpringBoot --> Journal[Journal Module]
        SpringBoot --> Assessment[Assessment Module]
        SpringBoot --> SeaService[Sea Service Module]
        SpringBoot --> Certificate[Certificate Module]
    end

    subgraph Persistence & Infrastructure Layer
        SpringBoot -->|Spring Data JPA / PostGIS| Postgres[(PostgreSQL 17 + PostGIS :5432)]
        SpringBoot -->|Spring Data Redis| Redis[(Redis 7 Cache & Session :6379)]
        SpringBoot -->|AWS S3 SDK| MinIO
    end

    linkStyle default stroke-width:2px;
```

---

## 🧩 Spring Modulith Bounded Contexts

The backend is structured into 7 distinct Java packages under `com.mralmostcool.tarbook`, enforcing module boundary encapsulation:

| Bounded Context | Package | Primary Responsibility | Related Documentation |
| :--- | :--- | :--- | :--- |
| **Core** | `com.mralmostcool.tarbook.core` | Users, Organizations, Candidates, Vessel Crew Assignments | [[modules/core-module\|Core Module]] |
| **Security** | `com.mralmostcool.tarbook.security` | Officer ECDSA Keys, Hardware Attestation, Provenance | [[modules/security-module\|Security Module]] |
| **Program** | `com.mralmostcool.tarbook.program` | STCW Syllabi, Tasks, Prerequisites, Eligibility Rules | [[modules/program-module\|Program Module]] |
| **Journal** | `com.mralmostcool.tarbook.journal` | Sea Journal Entries, Attachments, Evidence, Audit Logs | [[modules/journal-module\|Journal Module]] |
| **Assessment** | `com.mralmostcool.tarbook.assessment` | Task Assessments, Multi-Tier Officer Sign-offs | [[modules/assessment-module\|Assessment Module]] |
| **SeaService** | `com.mralmostcool.tarbook.seaservice` | Voyage Logbooks, Master Endorsements, GiST Constraints | [[modules/seaservice-module\|SeaService Module]] |
| **Certificate** | `com.mralmostcool.tarbook.certificate` | Seafarer Travel Docs, STCW Modular Certificates | [[modules/certificate-module\|Certificate Module]] |

---

## 🧱 Key Architectural Principles

1. **Modulith Encapsulation**: Modules communicate via public service interfaces (e.g., `JournalService`, `SecurityService`) while internal domain entities and repositories remain package-private in `.internal.*`.
2. **Offline-First Resilience**: All operations support local queuing and idempotent sync protocols for satellite-connected ship environments.
3. **Statutory Non-Repudiation**: Officer sign-offs and master endorsements utilize ECDSA P-256 digital signatures with cryptographic hash chaining.
4. **Environment Isolation**: Strict separation between development (`dev`) and production (`prod`) configurations.
