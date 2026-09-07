---
title: Container Orchestration & Docker Stack
---

# Container Orchestration & Infrastructure Stack

Project Tarbook utilizes **Docker Compose** to orchestrate all local development and production services, ensuring complete environment parity and isolated data persistence.

---

## 📦 Container Services Overview

The local infrastructure defined in [docker-compose.yml](file:///C:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/docker-compose.yml) comprises 7 containerized services:

```mermaid
graph LR
    subgraph Storage Layer
        Postgres[storage-postgres :5432]
        Redis[storage-redis :6379]
        MinIO[storage-s3 :9000/:9001]
        PgAdmin[storage-pgadmin :5050]
    end

    subgraph Application Layer
        Backend[api-backend :8080]
        Quartz[docs-quartz :8014]
    end

    subgraph Gateway Layer
        Nginx[reverse-proxy-nginx :80/:443]
    end

    Gateway Layer --> Application Layer
    Gateway Layer --> Storage Layer
    Application Layer --> Storage Layer
```

---

## 🛠️ Service Specifications

### 1. `postgres` (`storage-postgres`)
* **Base Image**: `postgis/postgis:latest` (PostgreSQL 17.5 + PostGIS 3.5)
* **Port Mapping**: `5432:5432`
* **Data Volume**: `./storage/postgres:/var/lib/postgresql/data`
* **Healthcheck**: `pg_isready -U postgres_user -d tarbook_db` (interval 3s, retries 10)

### 2. `redis` (`storage-redis`)
* **Build Context**: `./storage/redis` (Custom Dockerfile with `redis.conf`)
* **Port Mapping**: `6379:6379`
* **Data Volume**: `./storage/redis/data:/data`
* **Healthcheck**: `redis-cli ping` with password auth

### 3. `minio` (`storage-s3`)
* **Base Image**: `minio/minio:latest`
* **Port Mapping**: `9000:9000` (S3 API), `9001:9001` (Web Console)
* **Data Volume**: `./storage/s3:/data`
* **Healthcheck**: `mc ready local`

### 4. `backend` (`api-backend`)
* **Build Context**: `./backend` (Java 21 Spring Boot)
* **Port Mapping**: `8080:8080`
* **Dependencies**: `postgres` (healthy), `redis` (healthy), `minio` (healthy)

### 5. `quartz` (`docs-quartz`)
* **Build Context**: `./quartz` (Multi-stage Node.js + Nginx)
* **Port Mapping**: `8014:80`
* **Purpose**: Serves this interactive technical documentation portal.

### 6. `nginx` (`reverse-proxy-nginx`)
* **Build Context**: `./nginx`
* **Port Mapping**: `80:80`, `443:443`
* **Dependencies**: All services started/healthy.

---

## 🔒 Environment & Configuration Contract

Environment variables are declared in [.env.example](file:///C:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.env.example) and injected via Docker Compose:

```bash
# PostgreSQL
POSTGRES_DB=tarbook_db
POSTGRES_USER=postgres_user
POSTGRES_PASSWORD=your_postgres_password

# Redis & MinIO
REDIS_PASSWORD=your_redis_password
MINIO_ROOT_USER=minio_admin
MINIO_ROOT_PASSWORD=your_minio_password

# Quartz Digital Garden
QUARTZ_PORT=8014
```

> [!IMPORTANT]
> Never commit active credentials to source control. Always copy `.env.example` to `.env` for local operations.
