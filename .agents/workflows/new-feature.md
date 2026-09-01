---
description: Implement a feature from an agreed requirement while preserving the project's architectural, domain, API, database, and testing conventions.
---

# New Feature Workflow

## Objective

Implement a feature from an agreed requirement while preserving
the project's architectural, domain, API, database, and testing
conventions.

## Workflow

1. Understand the requirement.
2. Identify unresolved decisions.
3. Grill the proposed approach.
4. Produce an agreed specification.
5. Break the specification into implementation-sized tickets.
6. Implement one ticket at a time.
7. Run the relevant tests.
8. Review the resulting diff against the specification.
9. Update project context when architectural or domain decisions change.

## Skills

Use the following skills where appropriate:

- `/grill-with-docs`
- `/to-spec`
- `/to-tickets`
- `/implement`
- `/code-review`

For significant architectural uncertainty:

- `/wayfinder`
- `/research`
- `/prototype`

For domain ambiguity:

- `/domain-modeling`

## Rules

- Do not implement an underspecified feature.
- Do not silently make architectural decisions that materially affect the system.
- Do not modify unrelated code.
- Follow the project's architecture and API conventions.
- Add or update tests with implementation.
- Review the final diff against the approved specification.