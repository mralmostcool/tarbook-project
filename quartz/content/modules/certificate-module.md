---
title: Seafarer Documents & Certificates Module
---

# Seafarer Documents & Certificates Module

The **Certificate Module** (`com.mralmostcool.tarbook.certificate`) manages seafarer travel documents (Passport, CDC, INDOS, SID), STCW modular safety certificates, and audit verification records.

---

## 📜 Document & Verification Model

```mermaid
graph TD
    Candidate[Candidate] -->|Holds| SeafarerDocument[SeafarerDocument - Passport/CDC]
    Candidate -->|Holds| SeafarerCertificate[SeafarerCertificate - STCW Modular]
    
    SeafarerDocument -->|Audit Verification| DocumentVerificationRecord[DocumentVerificationRecord]
    SeafarerCertificate -->|Audit Verification| DocumentVerificationRecord
    DocumentVerificationRecord -->|Digest Match| EvidenceArtifact[EvidenceArtifact]
```

---

## 🗄️ Entities & Tables

### 1. `SeafarerDocument` (`seafarer_documents`)
Travel documents (Passport, Continuous Discharge Certificate, INDOS, Seafarers Identity Document).

### 2. `SeafarerCertificate` (`seafarer_certificates`)
STCW safety certificates (PST, FPFF, EFA, PSSR, STSDSD, Medical Fitness).

### 3. `DocumentVerificationRecord` (`document_verification_records`)
Verifiable verification log recording audit decisions (`APPROVED`, `REJECTED`, `REVOKED`) with JSON Canonicalization Scheme (JCS) payloads.
