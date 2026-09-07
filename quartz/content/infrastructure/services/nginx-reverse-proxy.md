---
title: "Service: Nginx Reverse Proxy"
---

# Service: Nginx Reverse Proxy

The `nginx` container (`reverse-proxy-nginx`) serves as the primary edge reverse proxy for Project Tarbook.

## Service Configuration & Port Mappings

- **Container Name**: `reverse-proxy-nginx`
- **Host Ports**: `80:80`, `443:443`
- **Configuration File**: `./nginx/nginx.conf` mounted read-only at `/etc/nginx/nginx.conf`

## Location Routing Rules

```nginx
server {
    listen 80;
    server_name localhost;

    # Spring Boot Backend API
    location /api/ {
        proxy_pass http://backend:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Quartz Static Documentation Site
    location /docs/ {
        proxy_pass http://quartz:80/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # MinIO S3 Object Storage API
    location /s3/ {
        proxy_pass http://minio:9000/;
        proxy_set_header Host $host;
    }
}
```

- [[infrastructure/docker-compose-stack|Docker Compose Stack]]
- [[architecture/adrs/adr-0008-nginx-proxy|ADR 0008: Nginx Reverse Proxy]]
