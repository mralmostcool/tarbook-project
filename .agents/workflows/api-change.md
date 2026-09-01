---
description: Change an API while preserving compatibility unless a breaking change has been explicitly approved.
---

# API Change Workflow

## Objective

Change an API while preserving compatibility unless a breaking change
has been explicitly approved.

## Workflow

1. Identify the existing API contract.
2. Identify consumers.
3. Determine whether the change is breaking.
4. Research external/API standards where necessary.
5. Define the new contract.
6. Update the specification.
7. Implement backend changes.
8. Update tests.
9. Update frontend consumers.
10. Review compatibility.

## Skills

- `/grill-with-docs`
- `/research`
- `/to-spec`
- `/to-tickets`
- `/implement`
- `/code-review`

## Rules

- Do not silently introduce breaking changes.
- Update API documentation when the contract changes.
- Maintain backward compatibility where required.
- Test both success and failure behavior.