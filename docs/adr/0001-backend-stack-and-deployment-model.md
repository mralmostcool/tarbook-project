# 1. Backend Stack & Deployment Model

We adopt a cloud-only deployment topology using Java 21, Spring Boot 3 MVC with virtual threads (Project Loom), and Spring Data JPA / Hibernate Spatial on PostgreSQL/PostGIS.

## Status
accepted

## Context
Project Tarbook requires a resilient backend to receive periodic batch synchronizations from candidates, verify digital training logs and cryptographic provenance, process supporting evidence attachments, and support spatial operations (PostGIS) for location verification under maritime regulations. 

Initial repository scaffolding included Spring WebFlux with Spring Data R2DBC. We evaluated whether to maintain reactive non-blocking execution or align with imperative virtual threads, and whether to run backend edge instances onboard vessels.

## Considered Options
1. **Cloud-Only Spring Boot 3 MVC + Virtual Threads (Java 21) + Spring Data JPA / Hibernate Spatial**
2. **Cloud-Only Spring Boot 3 WebFlux + R2DBC**
3. **Dual Topology (Vessel Edge Server + Cloud) using Go or Rust**

## Decision Rationale
- **Deployment Topology**: A cloud-only backend avoids distributed multi-master synchronization across shipboard edge servers. Candidates store training activities locally on mobile devices and execute shore syncs whenever cellular or port internet connectivity is reached.
- **Runtime**: Java 21 and Spring Boot 3 leverage existing developer familiarity, a battle-tested enterprise ecosystem, and full Flyway migration compatibility.
- **Execution Model**: Spring MVC with Java 21 Virtual Threads (`spring.threads.virtual.enabled=true`) handles concurrent I/O-bound sync requests and object storage uploads without reactive programming overhead (`Mono`/`Flux`), debugging difficulty, or subtle transaction context loss.
- **Persistence & Spatial**: Hibernate Spatial and JDBC provide native, mature PostGIS integration for training location verification, whereas R2DBC lacks first-class spatial type support and complicates Flyway migrations.

## Consequences
- Need to update `backend/pom.xml` dependencies: swap `spring-boot-starter-webflux` for `spring-boot-starter-web`, swap `spring-boot-starter-data-r2dbc` for `spring-boot-starter-data-jpa` and `hibernate-spatial`, use PostgreSQL JDBC driver.
- Enable `spring.threads.virtual.enabled=true` in `application.yaml`.
- P2P and vessel-edge server deployments are deferred as future considerations without impacting Day 1 mobile-to-cloud sync architecture.
