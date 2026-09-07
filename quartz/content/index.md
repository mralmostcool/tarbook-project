---
title: Project Tarbook Documentation Hub
---

# Project Tarbook — Technical Documentation Hub

Welcome to the official technical documentation portal for **Project Tarbook (Electronic Training and Assessment Record Book)**.

Project Tarbook is an offline-first, tamper-evident digital platform designed for the maritime industry to manage cadet training, capture verifiable evidence, execute officer assessments, and ensure statutory compliance under the International Convention on Standards of Training, Certification and Watchkeeping for Seafarers (**STCW**).

---

## 🗺️ Master Documentation Directory

### 🏗️ 1. Architecture & Domain
* [[architecture/system-overview|System Topology & C4 Architecture]]: High-level architecture, Spring Modulith bounded contexts, and system boundaries.
* [[architecture/domain-model|Domain Model & Invariants]]: Domain vocabulary, entity relationships, state machines, and STCW regulatory constraints.
* [[architecture/adrs|Architectural Decision Records (ADRs)]]: Record of all architectural decisions (ADR 0001 through ADR 0016).

### 🐳 2. Infrastructure & Runtime Environment
* [[infrastructure/container-orchestration|Container Orchestration]]: Docker Compose multi-container runtime configuration, environment isolation, and service contracts.
* [[infrastructure/nginx-routing|Nginx Reverse Proxy & Routing]]: Reverse proxy routing table, upstreams, SSL termination, and path mapping.
* [[infrastructure/database-and-migrations|Relational & Spatial Database]]: PostgreSQL 17 + PostGIS 3.5 schema, Flyway migrations (V1–V13), indexing, and spatial data models.
* [[infrastructure/caching-and-object-storage|Caching & Object Storage]]: Redis caching, session state management, and MinIO S3 object storage bucket topology.

### 🧩 3. Spring Boot Backend & Modulith Architecture
* [[modules/core-module|Core Module]]: Users, Organizations, Candidates, and Vessel Crew Assignments.
* [[modules/security-module|Security & Trust Module]]: Authentication, ECDSA P-256 signing keys, hardware attestation, and cryptographic provenance.
* [[modules/program-module|Program & Syllabus Module]]: STCW Training Programs, Syllabus Functions, Tasks, Prerequisites, and Eligibility Rules.
* [[modules/journal-module|Sea Journal & Evidence Module]]: Sea Journal Entries, Attachments, Evidence Artifacts, and Hash-Chained Audit Logs.
* [[modules/assessment-module|Assessment & Sign-off Module]]: Task Assessments, Multi-Tier Officer Sign-Offs, and Competency Verification.
* [[modules/seaservice-module|Sea Service & Endorsement Module]]: Voyage Logging, Non-Overlapping GiST Constraints, and Master/Chief Engineer Discharge Endorsements.
* [[modules/certificate-module|Seafarer Documents & Certificates Module]]: Travel Documents, STCW Modular Certificates, and Verifiable Verification Records.

### 🔌 4. API Contracts & Synchronization
* [[api/rest-contracts|REST API Contracts & OpenAPI]]: SpringDoc OpenAPI 3.0 specs, API endpoints, response shapes, and error contracts.
* [[sync/offline-and-sync-protocol|Offline-First & Satellite Sync Engine]]: Low-bandwidth sync protocol, priority sync queues, and conflict resolution rules.

---

## ⚡ Quick References & Live Interfaces

* **Nginx Gateway**: `http://localhost/`
* **Swagger UI (Interactive API Docs)**: `http://localhost/swagger-ui.html` or `http://localhost/v3/api-docs`
* **Digital Garden Docs (Quartz)**: `http://localhost:8014` or `http://localhost/docs/`
* **pgAdmin Database Console**: `http://localhost:5050`
* **MinIO Object Storage Console**: `http://localhost:9001`
