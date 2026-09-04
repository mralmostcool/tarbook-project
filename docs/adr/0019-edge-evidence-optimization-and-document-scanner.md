# 19. Edge Evidence Optimization and Document Scanner Architecture

We adopt native platform document-vision adapters behind a platform-neutral KMP interface, adaptive multi-mode compression, multi-page PDF artifact packaging, cryptographically-bound provenance telemetry envelopes, non-blocking image quality gates, and a bounded-memory processing pipeline.

## Status
accepted

## Context
[Issue #20](https://github.com/mralmostcool/tarbook-project/issues/20) specifies optimizing evidence artifacts at the mobile edge prior to Shore Sync transmission. Commercial vessel satellite telecommunications (9.6–128 kbps) make uploading raw 5–15MB camera photos cost-prohibitive. Edge Computer Vision (Edge CV) must perform document boundary detection, perspective correction, adaptive compression, and provenance telemetry collection directly on seafarer mobile devices before calculating final SHA-256 digests.

## Considered Options
1. **Uncompressed Camera Capture Upload**: Uploading full-resolution raw camera photos (wastes satellite bandwidth and incurs excessive satellite data costs).
2. **Server-Side Cloud Crop & Compression**: Pushing raw unoptimized images to the cloud for processing (fails offline satellite bandwidth constraints).
3. **Edge CV Pipeline with Native Platform Vision Adapters & Adaptive Compression**: Client-side document detection and adaptive compression producing sub-100KB document artifacts prior to outbox staging and SHA-256 hashing.

## Decision Rationale

### 1. Native Platform Document-Vision Adapters
- **KMP Vision Abstraction**:
  - Native Android (Google ML Kit / Android Vision APIs) and iOS (Apple Vision `VNDetectRectangleRequest`) document-vision adapters are exposed behind a platform-neutral KMP interface.
  - OpenCV / C++ fallback engines are deferred until supported-device matrix testing demonstrates a specific hardware requirement.

### 2. Adaptive Evidence Compression & Multi-Page PDF Packaging
- **Adaptive Multi-Mode Compression**:
  - Image pipeline dynamically selects monochrome (B&W thresholding), grayscale, or color representation and quality settings based on evidence artifact type (WebP candidate subject to file-size, readability, and decoder compatibility validation).
  - Targets sub-100KB per page for text documents/schematics and sub-200KB for machinery photos.
- **Multi-Page PDF Packaging**:
  - Multi-page document scans (e.g. 5-page paper certificates or schematics) package into a single logical compressed PDF Evidence Artifact.
  - A single SHA-256 digest is computed over the final complete PDF bytes.

### 3. Provenance Telemetry Envelope & Policy-Driven Governance
- **Sidecar Metadata Envelope**:
  - Provenance metadata (capture timestamp, GNSS lat/long, accuracy radius, capture mode `LIVE_CAMERA` vs `GALLERY_IMPORT`, and device integrity signals) is stored in a sidecar metadata envelope separate from the binary file byte hash.
  - The sidecar envelope is cryptographically bound to the Evidence Artifact and `Task Sign-Off` record to prevent silent telemetry tampering.
- **Policy-Driven Anti-Spoofing**:
  - Live-camera capture requirements and anti-spoofing enforcement are task/compliance-policy dependent rather than rigid global blockers.

### 4. Scanner UX, Quality Gates & OOM Memory Safety
- **Live Viewfinder UX**:
  - Live camera viewfinder displays real-time quad document boundary detection with interactive 4-corner adjustment handles before perspective transformation.
  - Supports manual capture trigger when automatic stability detection is unsuitable.
- **Non-Blocking Image Quality Gate**:
  - Analyzes sharpness (Laplacian variance) and illumination; displays non-blocking quality warnings if blurry or dark while allowing seafarer override.
- **Bounded-Memory Downscaling Pipeline**:
  - Executes a bounded-memory image downscaling pipeline prior to heavy CV processing, avoiding RAM Out-Of-Memory (OOM) crashes on low-end seafarer mobile hardware.

## Consequences

### Positive
- Substantially reduces satellite bandwidth consumption and transmission latency during Shore Sync.
- Native Android/iOS vision adapters maximize camera performance and hardware acceleration.
- Cryptographically bound sidecar telemetry preserves provenance without modifying evidence binary digests.

### Negative / Trade-offs
- Multi-mode compression pipeline requires tuning quality thresholds across diverse seafarer phone cameras.
- Multi-page PDF compilation on mobile requires PDF generator library integration in KMP.

## References
- [GitHub Issue #20](https://github.com/mralmostcool/tarbook-project/issues/20)
- [ADR 0003: Offline Sync Architecture and Server Authority](0003-offline-sync-architecture-and-server-authority.md)
- [ADR 0007: Shore Sync API Protocol, Wire Contracts and Idempotency](0007-shore-sync-api-protocol-wire-contracts-and-idempotency.md)
- [ADR 0018: Mobile Edge and Evidence Capture Architecture](0018-mobile-edge-and-evidence-capture-architecture.md)
