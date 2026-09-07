---
title: System Architecture Overview
---

# System Architecture Overview

Project Tarbook is engineered as a **Spring Modulith** backend application deployed within a Docker-containerized runtime.

## C4 System Context

```mermaid
graph TD
    Cadet[Cadet / Officer Mobile App] -->|HTTPS / REST| Nginx[Nginx Reverse Proxy :80]
    Nginx -->|/api/*| Backend[Spring Boot 4.1.1 Backend :8080]
    Nginx -->|/docs/*| Quartz[Quartz 5 Docs :8014]
    
    Backend --> Postgres[(PostgreSQL 17 + PostGIS :5432)]
    Backend --> Redis[(Redis 7 Cache :6379)]
    Backend --> MinIO[(MinIO S3 Storage :9000)]
```

See also: [[architecture/bounded-contexts|Bounded Context Map]], [[architecture/domain-invariants|Domain Invariants]].
