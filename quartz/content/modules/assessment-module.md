---
title: "Assessment Bounded Context Module"
---

# Assessment Bounded Context Module

The Assessment Bounded Context handles officer sign-offs, task assessments, and competency verdicts.

## Managed Domain Entities

- [[database/tables/task_signoffs|task_signoffs]]: Cryptographically signed officer assessment records.
- [[database/tables/task_assessments|task_assessments]]: Formal task competency assessments.
- [[database/tables/assessment_signoffs|assessment_signoffs]]: Assessor and Master verdict sign-offs on task assessments.

- [[modules/security-module|Security Bounded Context]]
- [[database/tables/index|Database Table Descriptors Registry]]
