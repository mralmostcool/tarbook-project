---
title: "System Architecture Overview"
---

# System Architecture Overview

Project Tarbook is engineered using a **Spring Modulith** architecture running on Java 21 and Spring Boot 3. It provides modular monolith capabilities with strict package boundaries, compile-time module isolation, and domain event messaging.

## Key Architectural Principles

1. **Modular Monolith**: Eliminates network latency between domains while strictly enforcing bounded context boundaries.
2. **Offline-First Resilience**: Mobile clients log tasks, capture evidence, and execute sign-offs offline, synchronizing asynchronously when connected.
3. **Cryptographic Non-Repudiation**: Officer sign-offs and statutory discharge endorsements are signed using ECDSA P-256 hardware-backed keys.
4. **Spatial Provenance**: Task executions and evidence captures include PostGIS spatial coordinates (`GEOMETRY(Point, 4326)`).

## Connected Architecture Links

- [[architecture/bounded-contexts|Bounded Contexts Matrix]]
- [[database/tables/index|Database Table Descriptors Registry]]
- [[infrastructure/docker-compose-stack|Docker Compose Infrastructure Stack]]
- [[sync/offline-sync-protocol|Offline Synchronization Protocol]]
