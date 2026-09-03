# 5. Production Service Deployment & Edge Topology

We adopt a provider-agnostic managed container platform for production, stateless Spring Boot instances behind an ingress reverse proxy/load balancer, and a pure mobile edge topology with direct-to-cloud synchronization.

## Status
accepted

## Context
Project Tarbook requires a reliable production deployment specification for cloud infrastructure and edge synchronization. Operating across commercial vessels worldwide introduces challenges with physical shipboard hardware maintenance, satellite communication costs, and remote system administration.

## Considered Options
1. **Hybrid Shipboard Gateway with Multi-Master Replication**: Physical mini-servers or edge appliances installed aboard each vessel running a local database replica.
2. **Provider-Locked Container Orchestration (e.g. strict EKS/ECS)**: Prematurely coupling deployment scripts to a single cloud provider's proprietary APIs.
3. **Provider-Agnostic Managed Container Platform with Pure Mobile Edge**: Cloud-only backend deployed on a managed container service behind a dedicated ingress layer, paired with direct mobile-to-cloud synchronization.

## Decision Rationale
- **Provider-Agnostic Managed Containers**:
  - Production workloads run as stateless Spring Boot OCI containers on a managed container platform (e.g. AWS ECS/Fargate, GCP Cloud Run, or Kubernetes).
  - Keeps deployment specifications provider-agnostic at this stage rather than prematurely coupling to cloud-specific tooling.
  - State is strictly externalized to managed services: PostgreSQL with PostGIS, Redis, and S3-compatible object storage.
- **Dedicated Ingress & TLS Termination**:
  - A cloud load balancer / reverse proxy layer (e.g. Nginx / Cloud Load Balancer) sits in front of the application tier.
  - Handles external TLS termination, HTTP/2 connection pooling, rate limiting, routing, and gzip/zstd compression negotiation.
  - Spring Boot embedded runtime remains shielded from direct public ingress.
- **Pure Mobile Edge Model (Zero Shipboard Servers)**:
  - Mobile devices (Android / iOS) are the sole offline edge execution nodes.
  - Seafarers log activities and sign off tasks locally on personal devices.
  - Sync occurs directly between mobile edge and cloud backend during Shore Sync.
  - Avoids distributed multi-master database replication, satellite bandwidth drain from server-to-server heartbeats, and shipboard hardware maintenance.

## Consequences
- Local development environment maintains high fidelity with production using Docker Compose (`postgres`, `minio`, `redis`, `backend`, `nginx`).
- Application backend must remain strictly stateless to support horizontal autoscaling behind the load balancer.
- Mobile client application carries full local persistence responsibility (SQLite).
