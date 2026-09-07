---
title: Project Tarbook Documentation Hub
---

# Project Tarbook — Comprehensive Technical Knowledge Base

Welcome to the official, fully interconnected technical documentation garden for **Project Tarbook (Electronic Training and Assessment Record Book)**.

This knowledge base documents every layer of the system: architecture, domain entities, database migrations, security primitives, Docker infrastructure, Spring Modulith services, REST APIs, and offline synchronization protocols.

---

## 🗺️ Master Documentation Directory

### 🏗️ 1. Architecture & Design
* [[architecture/overview|System Architecture Overview]]: C4 topology, Modulith bounded contexts, and system layers.
* [[architecture/domain-invariants|Domain Invariants & Rules]]: Statutory STCW constraints, non-overlapping sea service, hash-chaining.
* [[architecture/bounded-contexts|Bounded Context Map]]: The 7 Spring Modulith modules and inter-module communications.
* [[architecture/adrs/index|Architectural Decision Records (ADRs)]]: Complete index of 16 formal ADRs.

### 🗄️ 2. Database & Spatial Persistence
* [[database/overview|Database Subsystem Overview]]: PostgreSQL 17 + PostGIS 3.5 architecture.
* [[database/postgis-spatial|PostGIS Spatial Model]]: WGS 84 GPS point geometries for field evidence capture.
* [[database/indexing-and-performance|Indexing & Performance]]: B-tree, GiST, unique indexes, and partition considerations.
* [[database/migrations/index|Flyway Migrations Registry]]: Detailed reference for Flyway migrations V1 through V13.

### 🐳 3. Infrastructure & Docker Runtime
* [[infrastructure/overview|Infrastructure Overview]]: Multi-container architecture.
* [[infrastructure/docker-compose-stack|Docker Compose Services]]: Service configurations, ports, and volumes.
* [[infrastructure/environment-variables|Environment Contract]]: Baseline environment variables contract and secret governance.
* [[infrastructure/services/index|Services Reference]]: Postgres, Redis, MinIO, Nginx, Backend, Quartz, pgAdmin.

### 🧩 4. Spring Modulith Modules
* [[modules/core/index|Core Module]]: Users, Organizations, Candidates, Vessel Assignments.
* [[modules/security/index|Security Module]]: ECDSA P-256 Keys, Nonces, Attestation, Hardware Key Registry.
* [[modules/program/index|Program Module]]: Syllabi, Functions, Tasks, Prerequisites, Eligibility Rules.
* [[modules/journal/index|Journal Module]]: Journal Entries, Attachments, Evidence Artifacts, Audit Logs.
* [[modules/assessment/index|Assessment Module]]: Competency Evaluations, Multi-Tier Officer Sign-offs.
* [[modules/seaservice/index|SeaService Module]]: Voyages, GiST Exclusion Constraints, Discharge Endorsements.
* [[modules/certificate/index|Certificate Module]]: Travel Docs, STCW Modular Certificates, Verification Records.

### 🔌 5. API & Synchronization
* [[api/openapi-specification|REST API & OpenAPI Specs]]: Endpoints, schemas, and interactive Swagger UI.
* [[api/problem-details-errors|RFC 7807 Error Contracts]]: Standardized API error payloads.
* [[sync/satellite-sync-protocol|Offline & Satellite Sync Protocol]]: Low-bandwidth sync engine, queues, and LWW rules.
