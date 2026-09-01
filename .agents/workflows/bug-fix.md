---
description: Diagnose and fix bugs using a reproducible failure rather than guessing at the cause.
---

# Bug Fix Workflow

## Objective

Diagnose and fix bugs using a reproducible failure rather than
guessing at the cause.

## Workflow

1. Reproduce the bug.
2. Create a minimal failing test or reproduction where practical.
3. Diagnose the root cause.
4. Establish the expected behavior.
5. Implement the smallest appropriate fix.
6. Verify the regression test.
7. Run relevant tests.
8. Review the diff.

## Skills

- `/diagnosing-bugs`
- `/implement`
- `/code-review`

## Rules

- Do not make speculative fixes.
- Preserve the reproduction until the bug is resolved.
- Prefer a regression test for fixed behavior.
- Do not refactor unrelated code during the bug fix.