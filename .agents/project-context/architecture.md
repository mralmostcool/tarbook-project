# Architecture & Delivery Decisions

This document records the architectural principles, system structures, and accepted delivery decisions for Project Tarbook.

---

## 1. Architectural Style & Principles
* **Evidence-Driven**: Architecture evolves from confirmed domain requirements and engineering evidence rather than fashion or premature frameworks.
* **Smallest Justified Commitment**: Adopt the simplest structural model that satisfies known constraints.
* **Separation of Concerns**: Isolate core maritime domain logic from delivery protocols (REST, CLI), edge storage, and infrastructure.

---

## 2. Accepted Delivery Decisions

### Branching Strategy
* **Primary Integration Branch**: `main`.
* **Discipline**: Intentional simplicity during early development. Agents MUST NOT introduce complex branching models (e.g. GitFlow) without operational necessity.
* **Evolution**: When maturity requires, transition to trunk-based development or lightweight feature branches with pull requests.

### Environment Promotion
* CI/CD must preserve the strict distinction between **Development** and **Production** environments even when deploying from the same `main` branch.
* A single `main` branch does NOT imply a shared database or runtime environment.

---

## 3. Architectural Decisions (ADR Format)

Each architectural decision must be explicitly evaluated and recorded using this standard structure:

```text
Decision: <decision title>

Context:
<why the decision was necessary, including domain constraints>

Alternatives considered:
<option A>
<option B>
<option C>

Rationale:
<why the chosen approach was selected over alternatives>

Consequences:
<benefits, trade-offs, operational costs, risks>

Status:
<proposed | accepted | superseded | deprecated>
```

---

## 4. Current Architectural Decisions
* *(No decisions accepted yet — under active architectural discovery).*

---

## 5. Forbidden Patterns
* Introducing frameworks, persistence technologies, or synchronization protocols without an evaluated ADR.
* Hiding architectural choices inside implementation details.
* Converting hypotheses or assumptions into permanent architectural constraints.