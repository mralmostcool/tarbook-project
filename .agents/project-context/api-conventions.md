# API Conventions

## API Style
RESTful JSON over HTTP/2 (OpenAPI 3.1).

## URL Conventions
- Base path: `/api/v1`
- Endpoints use plural kebab-case or resource-action verbs:
  - `/api/v1/sync/push`
  - `/api/v1/sync/pull`
  - `/api/v1/evidence/{id}/upload-url`
  - `/api/v1/evidence/{id}/verify`
  - `/api/v1/officers/keys/attestation-challenge`
  - `/api/v1/officers/keys/register`
  - `/api/v1/officers/keys/{id}/approve`
  - `/api/v1/officers/keys/{id}/revoke`
  - `/api/v1/officers/keys/active`
  - `/api/v1/roster/crew-assignments/batch`

## HTTP Methods
- `POST`: Submitting sync mutation batches (`/sync/push`), requesting upload URLs, and verifying checksums.
- `GET`: Querying resources and pulling delta synchronization streams (`/sync/pull`).
- `PUT` / `DELETE`: Not used for statutory training execution records. Immutability policy mandates state transitions via sync operations.

## Request Conventions
- Headers:
  - `Content-Type: application/json`
  - `Content-Encoding: gzip` or `zstd` (client request compression)
  - `Accept-Encoding: gzip, zstd`
  - `Authorization: Bearer <jwt-access-token>`
  - `X-Client-Id: <client-device-id>`
- Outbox push request body uses heterogeneous `OperationEnvelope`:
  ```json
  {
    "sync_session_id": "018f9e61-...",
    "client_id": "mobile-android-01",
    "operations": [
      {
        "operation_id": "018f9e61-...",
        "entity_type": "TASK_ENTRY",
        "action": "INSERT",
        "payload": { ... }
      }
    ]
  }
  ```

## Response Conventions
- Sync push returns HTTP 200 OK with `SyncReceipt`:
  ```json
  {
    "sync_session_id": "018f9e61-...",
    "status": "COMPLETED",
    "highest_sync_sequence": 1420,
    "results": [
      {
        "operation_id": "018f9e61-...",
        "status": "APPLIED",
        "sync_sequence": 1419
      }
    ]
  }
  ```

## Error Handling
- Standard HTTP error response:
  ```json
  {
    "timestamp_utc": "2026-09-03T11:18:00Z",
    "status": 400,
    "error": "Bad Request",
    "code": "INVALID_SIGNING_NONCE",
    "message": "The provided signing nonce was already used",
    "path": "/api/v1/sync/push"
  }
  ```
- Differential sync errors return inside individual operation results (`status: "CONFLICT"` or `"REJECTED"`) with `error_code` and `server_state` without failing the entire batch.

## Authentication
- OAuth2/OIDC with short-lived JWT access tokens and backend-revocable refresh tokens.
- Stateless bearer token verification in Spring Security.

## Authorization
- Role-Based Access Control (RBAC): `CANDIDATE`, `OFFICER`, `MASTER`, `COMPANY_OFFICER`, `ADMIN`.

## Pagination
- Sync pull uses monotonic cursor pagination:
  `GET /api/v1/sync/pull?since={sync_sequence}&limit={limit}`
  Returns items with `has_more` and `next_sync_sequence`.

## Compression
- Dual content negotiation supporting both `gzip` and `zstd`.

## Versioning
- Path-based URI versioning: `/api/v1/...`.