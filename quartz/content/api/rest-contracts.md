---
title: REST API Contracts & OpenAPI Specifications
---

# REST API Contracts & OpenAPI Specifications

Project Tarbook exposes RESTful Web APIs documented via **SpringDoc OpenAPI 3.0**.

---

## 📡 OpenAPI & Swagger Endpoint URLs

* **Interactive Swagger UI**: `http://localhost/swagger-ui.html`
* **Raw OpenAPI 3.0 JSON Spec**: `http://localhost/v3/api-docs`
* **Raw OpenAPI 3.0 YAML Spec**: `http://localhost/v3/api-docs.yaml`

---

## 🗺️ Controller Routes Directory

| Module | Controller Class | Base Path | Key Endpoints |
| :--- | :--- | :--- | :--- |
| **Core** | `CoreController` | `/api/v1/core` | `/organizations`, `/users`, `/candidates`, `/vessels/assign` |
| **Security** | `SecurityController` | `/api/v1/security` | `/keys/enroll`, `/keys/revocations`, `/attestation/challenge` |
| **Program** | `ProgramController` | `/api/v1/program` | `/programs`, `/functions`, `/tasks`, `/eligibility/check` |
| **Journal** | `JournalController` | `/api/v1/journal` | `/entries`, `/attachments`, `/artifacts`, `/audit-logs` |
| **Assessment** | `AssessmentController` | `/api/v1/assessment` | `/tasks/assess`, `/signoffs` |
| **SeaService** | `SeaServiceController` | `/api/v1/seaservice` | `/records`, `/endorsements` |
| **Certificate** | `CertificateController` | `/api/v1/certificate` | `/documents`, `/certificates`, `/verify` |

---

## ⚠️ Standardized Error Response Contract

All API error responses follow RFC 7807 Problem Details format:

```json
{
  "timestamp": "2026-09-07T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Overlapping sea service date range detected for candidate.",
  "path": "/api/v1/seaservice/records"
}
```
