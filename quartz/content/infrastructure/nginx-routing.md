---
title: Nginx Reverse Proxy & Routing Table
---

# Nginx Reverse Proxy & Gateway Routing

The **Nginx** reverse proxy container acts as the single unified ingress gateway for all HTTP traffic in Project Tarbook.

---

## 🚦 Nginx Route Mapping Table

All incoming traffic on port 80/443 is routed according to the server configuration in [nginx/conf.d/default.conf](file:///C:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/nginx/conf.d/default.conf):

| Ingress Path | Upstream Target | Upstream Address | Purpose |
| :--- | :--- | :--- | :--- |
| `/` | Nginx Inline JSON | Local (200 OK) | Proxy Gateway Healthcheck (`{"status":"ok"}`) |
| `/api/*` | `backend_api` | `http://backend:8080/` | Spring Boot REST API |
| `/v3/*` | `backend_api` | `http://backend:8080/v3/` | OpenAPI 3.0 Specs (`/v3/api-docs`) |
| `/swagger-ui/*` | `backend_api` | `http://backend:8080/swagger-ui/` | Swagger UI Assets |
| `/swagger-ui.html` | `backend_api` | `http://backend:8080/swagger-ui.html` | Swagger UI Entrypoint |
| `/docs/*` | `quartz` | `http://quartz:80/` | Quartz 5 Digital Garden Documentation Portal |
| `/s3/*` | `minio_s3` | `http://minio:9000/` | MinIO S3 Object Storage API |
| `/minio-console/*` | `minio_console` | `http://minio:9001/` | MinIO Web Management Console |
| `/pgadmin/*` | `pgadmin` | `http://pgadmin:80/` | pgAdmin 4 Database Administration Web UI |

---

## 🛠️ Gateway Proxy Configuration Snippet

```nginx
# Upstream definitions
upstream backend_api {
    server backend:8080;
}

upstream quartz {
    server quartz:80;
}

server {
    listen 80;
    server_name localhost;

    # Global proxy headers
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_http_version 1.1;

    # Quartz Documentation Proxy
    location /docs/ {
        proxy_pass http://quartz/;
    }

    # Backend API Proxy
    location /api/ {
        proxy_pass http://backend_api/;
    }

    # OpenAPI Docs Proxy
    location /v3/ {
        proxy_pass http://backend_api/v3/;
    }
}
```

---

## 🔒 Security & Performance Features

1. **Header Normalization**: Passes `X-Forwarded-For` and `X-Forwarded-Proto` to allow backend Spring Security to correctly reconstruct original client protocol and IP address.
2. **Gzip Compression**: Compresses `text/plain`, `application/json`, `application/javascript`, and `image/svg+xml` to minimize bandwidth over shipboard satellite links.
3. **Max Body Size**: Configured to `client_max_body_size 100M` to support uploading high-resolution evidence photos and certificates.
