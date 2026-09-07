---
title: "Certificate Bounded Context Module"
---

# Certificate Bounded Context Module

The Certificate Bounded Context manages seafarer identity documents, STCW modular safety certificates, and document verification audits.

## Managed Domain Entities

- [[database/tables/seafarer_documents|seafarer_documents]]: Travel and identity documents (Passport, CDC, SID, INDOS).
- [[database/tables/seafarer_certificates|seafarer_certificates]]: STCW modular safety certificates (PST, FPFF, EFA, PSSR, STSDSD, etc.).
- [[database/tables/document_verification_records|document_verification_records]]: Verification audit logs with JSON Canonicalization Scheme payload digests.

- [[database/tables/index|Database Table Descriptors Registry]]
