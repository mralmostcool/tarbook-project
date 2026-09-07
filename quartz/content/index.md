---
title: "Project Tarbook Documentation Garden"
---

# Project Tarbook Documentation Garden

Welcome to the official technical documentation for **Project Tarbook — Electronic Training and Assessment Record Book**.

Project Tarbook is an enterprise digital Training and Assessment Record (TAR) Book designed for the maritime industry, replacing traditional paper training records used by maritime cadets and officers with a resilient, offline-first, cryptographically verifiable digital platform.

## Documentation Knowledge Map

### 🏗️ Architecture & Governance
- [[architecture/overview|Architecture Overview]]: Spring Modulith, bounded contexts, and system design.
- [[architecture/bounded-contexts|Bounded Contexts]]: Module boundaries and domain event communication.
- [[architecture/domain-model|Domain Model]]: Core aggregates, entities, and state machines.
- [[architecture/domain-invariants|Domain Invariants]]: Statutory STCW and security rules.
- [[architecture/adrs/index|Architectural Decision Records (ADRs)]]: Evaluated architectural decisions.

### 💾 Relational Persistence & PostGIS Data
- [[database/overview|Database Overview]]: PostgreSQL 17 + PostGIS 3.5 architecture.
- [[database/tables/index|Table Descriptors Registry (31 Tables)]]: Complete DDL, schemas, foreign keys, and connections for all 31 database tables.
- [[database/indexing-and-performance|Indexing & Performance]]: Spatial GiST indexes, btree indexing, and performance tuning.
- [[database/postgis-spatial|PostGIS Spatial Extensions]]: WGS84 GNSS coordinates and temporal exclusion constraints.
- [[database/migrations/index|Flyway Migrations Registry]]: Complete SQL migration history (`V1` to `V13`).

### 🐳 Infrastructure, Containers & Reverse Proxy
- [[infrastructure/docker-compose-stack|Docker Compose Orchestration]]: Container definitions and ports.
- [[infrastructure/services/nginx-reverse-proxy|Nginx Reverse Proxy]]: Edge routing table, SSL termination, and location proxy configuration.
- [[infrastructure/services/postgres-postgis|PostgreSQL + PostGIS Service]]: Persistence configuration and volume mounts.
- [[infrastructure/services/redis-cache|Redis Service]]: Session caching and rate limiting.
- [[infrastructure/services/minio-s3|MinIO Object Storage]]: Evidence artifact storage and S3 checksum verification.
- [[infrastructure/environment-variables|Environment Variables Contract]]: `.env.example` specifications.

### 🔄 Offline Synchronization & Provenance
- [[sync/offline-sync-protocol|Offline Synchronization Protocol]]: High-latency batch synchronization engine.
- [[sync/conflict-resolution|Conflict Resolution]]: Optimistic locking and superseding record amendments.
- [[sync/idempotency-and-deduplication|Idempotency Engine]]: Operation payload hash verification.

### 🔌 REST APIs & Contracts
- [[api/rest-contracts|REST API Contracts]]: Endpoint conventions, URI shapes, and HTTP verbs.
- [[api/openapi-specification|OpenAPI 3.0 Specs]]: Swagger UI and schema contracts.
- [[api/problem-details-errors|RFC 7807 Error Contracts]]: Standardized problem detail response format.
- [[api/rate-limiting-and-headers|Rate Limiting & Security Headers]]: Response headers and rate controls.

### 🧩 Spring Modulith Backend Modules
- [[modules/assessment-module|Assessment Module]]: Task assessments and officer sign-offs.
- [[modules/certificate-module|Certificate Module]]: STCW modular safety certificates and document verification.
- [[modules/core-module|Core Module]]: User profiles, candidates, organizations, and crew rosters.
- [[modules/journal-module|Journal Module]]: Task entries, daily sea logs, and evidence metadata.
- [[modules/program-module|Program Module]]: Training program syllabus and eligibility engine.
- [[modules/seaservice-module|SeaService Module]]: Sea service records, watchkeeping hours, and Master discharge endorsements.
- [[modules/security-module|Security Module]]: ECDSA P-256 cryptographic signatures, hardware keys, and hash-chained audit events.
