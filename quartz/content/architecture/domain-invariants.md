---
title: Domain Invariants & Rules
---

# Domain Invariants & Rules

1. **GiST Non-Overlapping Voyages**: Seafarers cannot serve on multiple vessels concurrently. Enforced by PostgreSQL GiST constraint.
2. **ECDSA Signature Non-Repudiation**: Statutory sign-offs require valid ECDSA P-256 signatures from approved officer keys.
3. **Audit Log Hash-Chaining**: Audit entries are linked via SHA-256 hash chains (`prev_hash` -> `entry_hash`).
