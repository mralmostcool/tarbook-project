# Project Context

## Project
**Project Tarbook** — Electronic Training and Assessment Record (TAR) Book for the maritime industry.

## Purpose
Replaces or augments traditional paper maritime training books used by deck and engine cadets. Provides a tamper-evident, offline-first digital platform for logging training tasks, capturing field evidence, executing officer assessments, and preserving verifiable statutory records conforming to STCW and flag state mandates.

## Current Status
Foundational architecture and discovery phase. Core backend skeleton initialized with reactive persistence and local multi-service container environment.

## Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Language & Runtime** | Java 21 |
| **Backend Framework** | Spring Boot 3.x, Spring WebFlux (Reactive), Spring Modulith |
| **Security** | Spring Security |
| **Relational & Spatial DB** | PostgreSQL with PostGIS extension (accessed via Spring Data R2DBC) |
| **Cache & Queues** | Redis (Reactive) |
| **Object Storage** | MinIO (S3-compatible storage for photos, schematics, and evidence artifacts) |
| **Database Migrations** | Flyway (`flyway-database-postgresql` via Spring JDBC) |
| **API Documentation** | SpringDoc OpenAPI WebFlux UI |
| **Infrastructure** | Docker, Docker Compose, Nginx reverse proxy |

## Repository Structure

```text
.
├── backend/                  # Java 21 Spring Boot WebFlux application
│   ├── src/                  # Application source code and tests
│   ├── pom.xml               # Maven dependencies and build configuration
│   └── Dockerfile            # Container build definition
├── nginx/                    # Reverse proxy configuration
├── storage/                  # Local container mount points & configurations
│   ├── postgres/             # Database volume
│   ├── redis/                # Redis config and persistent data
│   ├── s3/                   # MinIO object storage volume
│   └── pgadmin/              # pgAdmin server definitions and preferences
├── .agents/                  # Agent governance, skills, and project memory
│   ├── AGENTS.md             # Operational hub & engineering guardrails
│   ├── agents/               # 10 specialized persona specs + Harambe orchestrator
│   ├── project-context/      # Deep domain, architectural, and database context
│   ├── skills/               # Reusable agent workflows and slash commands
│   └── workflows/            # Structured execution guides
├── docker-compose.yml        # Local multi-container infrastructure
└── .env.example              # Baseline environment configuration contract
```

## Core Invariants & Operating Realities
* **Offline-First**: Core recording and assessment workflows must function with zero network connectivity.
* **Tamper Evidence**: Completed records, approvals, and reversals carry statutory weight and must be verifiable.
* **Resource Sensitivity**: Evidence capture must accommodate constrained shipboard bandwidth, mobile battery, and storage limits.
* **Environment Parity**: Development and Production environments remain strictly isolated; no production credentials in repository.

## Context Pointers
* Domain Constraints: [`constraints.md`](constraints.md)
* TAR Book Structure: [`tar-book-structure.md`](tar-book-structure.md)
* Domain Vocabulary: [`domain.md`](domain.md)
* Architecture & Delivery: [`architecture.md`](architecture.md)
* Environment Governance: [`environment.md`](environment.md)
* Database Conventions: [`database.md`](database.md)
* Operational Rules: [`.agents/AGENTS.md`](../AGENTS.md)