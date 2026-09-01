---
description: Improve internal structure without changing externally observable behavior.
---

# Refactoring Workflow

## Objective

Improve internal structure without changing externally observable behavior.

## Workflow

1. Identify the architectural problem.
2. Establish the desired structure.
3. Verify existing behavior through tests.
4. Define the refactoring boundary.
5. Implement incrementally.
6. Run tests after each meaningful change.
7. Review the resulting architecture and diff.

## Skills

- `/improve-codebase-architecture`
- `/wayfinder`
- `/prototype`
- `/implement`
- `/code-review`

## Rules

- Do not change behavior unless explicitly required.
- Do not mix unrelated features into a refactor.
- Preserve existing tests unless they encode obsolete behavior.
- Prefer small, reversible changes.