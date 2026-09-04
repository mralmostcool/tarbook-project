# 14. Production Cloud Infrastructure & CI/CD Pipeline Architecture

We adopt OpenTofu / Terraform modular Infrastructure-as-Code (IaC) targeting a managed container platform with externalized managed data stores, managed cloud load balancing with automated TLS termination and DDoS mitigation, a 4-stage automated GitHub Actions CI/CD pipeline enforcing Testcontainers integration tests and Flyway migration dry-runs before container publishing, and runtime environment secret injection ensuring zero repository secrets.

## Status
accepted

## Context
[ADR 0005](0005-production-service-deployment-and-edge-topology.md) established the high-level principle of a managed container platform, stateless backend instances, and pure mobile edge direct-to-cloud synchronization. A concrete production infrastructure specification, CI/CD pipeline, and secrets management policy are required to realize reproducible cloud provisioning, automated verification gates, and dev/production parity per [.agents/project-context/environment.md](../../.agents/project-context/environment.md).

## Considered Options
1. **Unmanaged Virtual Machines with Manual Shell Scripts**: Manual provisioning of VPS instances running custom Docker setups.
2. **Kubernetes (EKS/GKE) with Helm**: Complex distributed orchestration (unnecessary operational overhead for a stateless Spring Boot service with externalized data stores).
3. **Modular OpenTofu / Terraform IaC with Managed Containers & Strict GitHub Actions CI**: Declarative IaC provisioning managed container services (AWS ECS Fargate / GCP Cloud Run), managed PostgreSQL with PostGIS, S3-compatible object storage, managed Redis, cloud load balancer with automated TLS/WAF, and a multi-stage automated GitHub Actions CI pipeline.

## Decision Rationale

### 1. Cloud Architecture & Modular Infrastructure as Code
- **Managed Container Runtime**:
  - Stateless Spring Boot backend deployed as an autoscaling service on a managed container runtime (e.g. AWS ECS Fargate).
  - Multi-cloud parity is treated as a modular architectural blueprint rather than requiring simultaneous active multi-cloud deployments.
- **Managed External Data Tier**:
  - Managed PostgreSQL with PostGIS extension (e.g. AWS Aurora PostgreSQL / RDS) with automated daily snapshots and point-in-time recovery.
  - Managed Redis (e.g. AWS ElastiCache / Redis Cloud) for session caching, sync lockouts, and rate limiting counters.
  - S3-compatible object storage bucket (e.g. AWS S3) with lifecycle policies for statutory `evidence_artifacts`.
- **Infrastructure as Code**:
  - Provisioned via modular OpenTofu / Terraform scripts located in `infra/terraform/` ensuring reproducible environments.

### 2. Ingress, TLS Termination & Rate Limiting
- **Managed Cloud Load Balancer (Outer Tier)**:
  - Managed Cloud Application Load Balancer terminates public TLS using automated ACM/Let's Encrypt certificates.
  - HTTP/2 multiplexing, HTTPS redirection, and Web Application Firewall (WAF) / DDoS protection shield the application layer.
- **Ingress & Rate Limiting Responsibilities**:
  - In local development, Docker Compose deploys a containerized Nginx reverse proxy (`nginx`) handling routing, TLS, and compression.
  - In production, rate-limiting and connection management protect against "port docking stampedes" (hundreds of offline mobile edge devices attempting simultaneous Shore Sync pushes when a vessel arrives within coastal cellular range).
  - Rate limiting is enforced via Redis token-bucket counters in Spring Boot / Load Balancer.

### 3. Automated CI/CD Verification Pipeline (GitHub Actions)
- **4-Stage Verification Pipeline (`.github/workflows/ci.yml`)**:
  1. **Stage 1: Lint, Compile & Security Scan**:
     - JDK 21 compilation with Maven dependency caching.
     - Static code analysis and dependency vulnerability audit.
  2. **Stage 2: Unit & Domain Tests**:
     - Fast, isolated unit tests validating rule engines, canonical serializers, and domain models.
  3. **Stage 3: Testcontainers Integration Verification**:
     - Full integration test execution against live containerized dependencies (PostgreSQL with PostGIS, Redis, MinIO) via Spring Boot Testcontainers.
  4. **Stage 4: Flyway Migration Validation**:
     - Forward migration dry-run validating all Flyway scripts (`V1` through `V8`) execute cleanly against an empty PostgreSQL database without index collisions or syntax errors.
- **Container Publishing**:
  - Merges to `main` trigger automated Docker image build and publication to GitHub Container Registry (`ghcr.io/mralmostcool/tarbook-backend`) tagged with commit SHA and `latest`.

### 4. Secrets Management & Key Isolation Invariants
- **Runtime Environment Injection**:
  - Zero secrets in version control or baked container images.
  - Production secrets (database credentials, Redis auth, S3 access keys, OIDC client secrets) are stored in Cloud Secrets Manager and injected as environment variables at task runtime.
  - Development environments utilize `.env` overrides conforming to `.env.example`.
- **Cryptographic Key Separation**:
  - Statutory officer private signing keys reside exclusively in physical device hardware enclaves (Android StrongBox/TEE, iOS Secure Enclave).
  - Cloud Backend never stores, generates, or possesses seafarer signing private keys. Backend infrastructure stores only public keys (`public_key_pem`), attestation certificates, and validation roots.

## Consequences
- Creates `.github/workflows/ci.yml` defining the multi-stage automated build and verification pipeline.
- Requires `infra/` directory structure for modular OpenTofu / Terraform blueprints.
- Local Docker Compose configuration continues to mirror production topology with high fidelity.
