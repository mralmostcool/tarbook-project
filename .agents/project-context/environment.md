# Environment & Configuration Governance

This document governs the separation of application code, runtime environments, configuration, and secrets.

---

## 1. Conceptual Hierarchy

```text
Application Code
        ↓
  Configuration
        ↓
   Environment
        ↓
     Secrets
```

---

## 2. Environment Separation Contract

Development and Production environments must remain completely isolated. Local development commands MUST NEVER point to production infrastructure.

### Development Environment
* Isolated local databases (e.g. Docker Compose PostgreSQL/PostGIS).
* Isolated local object storage (e.g. MinIO).
* Isolated local supporting services (e.g. Redis).
* Safe development credentials and mocks.
* Fully reproducible local setup via version-controlled configuration.

### Production Environment
* Isolated production databases with strict access control and automated backups.
* Dedicated production object storage.
* Managed production secrets (injected via deployment platform / CI/CD secrets).
* Controlled access, monitoring, and audit logging.

---

## 3. Configuration Contract & Secrets Management

* **`.env.example`**: Authoritative contract of all required environment variables with safe defaults or empty placeholders.
* **`.env`**: Local developer overrides. Must remain git-ignored at all times.
* **Zero Secrets in Repository**: Never commit passwords, private keys, API keys, tokens, or production credentials.
* **Configuration Consistency**: Whenever adding or renaming an environment variable, simultaneously update:
  1. Application configuration (`application.yml` / `.env.example`)
  2. Docker Compose definitions (`docker-compose.yml`)
  3. Dockerfiles
  4. CI/CD workflow definitions (`.github/workflows/`)
  5. Relevant documentation
