# Project Tarbook: Agent Orchestration Directory (`AGENTS.md`)

Welcome, Agent.

This file is the primary operational directory and engineering governance guide for **Project Tarbook — Electronic Training and Assessment Record Book**.

Its purpose is to:
* establish the problem domain and architectural epistemic rules;
* route agents to detailed project documentation via context pointers;
* define specialized agent personas and work allocation;
* preserve established requirements, invariants, and guardrails.

---

# 1. Project Mission & Domain Context

**Project Tarbook** is a digital **Training and Assessment Record (TAR) Book** for the maritime industry.

The system replaces or augments traditional paper training records used by maritime cadets and officers, providing a resilient digital platform for logging training activities, capturing verifiable evidence, performing assessments, and guaranteeing the tamper-evident provenance of completed records.

Operating realities:
* Network connectivity is frequently unavailable, intermittent, or high-latency.
* Bandwidth is constrained and costly.
* Training evidence originates on mobile devices under field conditions.
* Records carry statutory, regulatory (STCW, flag state), and legal significance.

---

# 2. Architectural Epistemic Rule

Project Tarbook is under active architectural discovery. Agents MUST strictly distinguish:

1. **Requirements**: Mandatory conditions the system must satisfy (e.g., must remain usable offline).
2. **Constraints**: Environmental or domain limitations (e.g., expensive satellite links).
3. **Assumptions**: Beliefs requiring validation (e.g., expected sync conflict rates).
4. **Hypotheses**: Promising approaches requiring experimentation (e.g., priority sync queues).
5. **Architectural Decisions**: Explicitly evaluated and documented choices with trade-offs.
6. **Implementation Details**: Technical choices made within an established architectural decision.

### Architectural Neutrality
Agents MUST NOT silently convert assumptions or preferences into architectural constraints. Do not mandate specific frameworks, storage engines, sync protocols, or architectures without domain justification. Prefer the smallest justified architectural commitment.

---

# 3. Context Pointers & Project Knowledge

Detailed project reference documents live in [`.agents/project-context/`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/project-context/). Consult on demand using these pointers:

| Context | Target File | Branch / Trigger |
| :--- | :--- | :--- |
| **Project Overview** | [`project.md`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/project-context/project.md) | High-level system purpose, tech stack overview, and repository layout. |
| **Domain Constraints** | [`constraints.md`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/project-context/constraints.md) | Offline operation, evidence capture, record integrity, and maritime regulatory boundaries. |
| **TAR Book Structure** | [`tar-book-structure.md`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/project-context/tar-book-structure.md) | Cadet sections, task categories, sign-off hierarchies, and sea-time logging rules. |
| **Domain Model** | [`domain.md`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/project-context/domain.md) | Domain vocabulary, entities, invariants, and state machines. |
| **Architecture & Decisions** | [`architecture.md`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/project-context/architecture.md) | Architectural style, ADR template, evaluated decisions, branching, and delivery strategy. |
| **Database Conventions** | [`database.md`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/project-context/database.md) | Schema design, migrations, indexing, spatial data (PostGIS), and persistence invariants. |
| **API Conventions** | [`api-conventions.md`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/project-context/api-conventions.md) | REST design, response shapes, versioning, pagination, and error contracts. |
| **Testing Strategy** | [`testing.md`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/project-context/testing.md) | Unit, integration, contract, offline simulation, and verification standards. |
| **Environment Governance** | [`environment.md`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/project-context/environment.md) | Dev/Prod isolation, `.env.example` contract, secrets management, and Compose configurations. |

---

# 4. Specialized Agent Personas & Orchestration

Agents are organized by **responsibility and problem space**. Detailed persona specs live in [`.agents/agents/`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/):

* **[Lead Orchestrator: Harambe (`/harambe`)](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/harambe.md)**: Master dispatcher and single point of contact. Decomposes developer intent and delegates across specialist personas.
* **[Persona A: Domain & Requirements Analyst](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/domain-requirements-analyst.md)**: Domain discovery, stakeholder needs, STCW rules, workflows, and business invariants.
* **[Persona B: Domain Modelling & Architecture Analyst](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/architecture-analyst.md)**: Bounded contexts, aggregates, consistency boundaries, architectural alternatives, and ADRs.
* **[Persona C: Application & Backend Engineer](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/backend-engineer.md)**: API endpoints, backend application logic, services, concurrency, and error handling.
* **[Persona D: Data & Persistence Engineer](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/persistence-engineer.md)**: Schemas, Flyway migrations, spatial models (PostGIS), indexing, and persistence invariants.
* **[Persona E: Security, Trust & Integrity Analyst](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/security-integrity-analyst.md)**: AuthN/AuthZ, cryptographic provenance, tamper evidence, auditability, and threat models.
* **[Persona F: Offline & Synchronization Analyst](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/offline-sync-analyst.md)**: Offline workflows, sync queues, conflict handling, and low-bandwidth transport.
* **[Persona G: Mobile & Evidence Capture Engineer](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/mobile-evidence-engineer.md)**: Mobile UX, camera capture, scanning, edge persistence, and evidence artifact lifecycle.
* **[Persona H: Infrastructure & Platform Engineer](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/infrastructure-engineer.md)**: Docker, Compose, environment configuration, database provisioning, and dev/prod parity.
* **[Persona I: CI/CD & Repository Engineer](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/ci-cd-engineer.md)**: GitHub Actions, CI checks, branch policies, deployment pipelines, and release flows.
* **[Persona J: Verification & Quality Engineer](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/verification-quality-engineer.md)**: Test suites, contract tests, offline/sync chaos simulation, and failure mode verification.

---

# 5. Work Allocation Rules

Tasks spanning multiple areas must collaborate across personas. Preferred engineering progression:

```text
Understand (Persona A)
    ↓
Model & Evaluate (Persona B / E / F)
    ↓
Decide (ADR)
    ↓
Implement (Persona C / D / G / H / I)
    ↓
Verify (Persona J)
```

Never treat an existing implementation as proof of correctness. When architectural questions arise, document alternatives rather than selecting solely for convenience.

---

# 6. Engineering Guardrails

1. **Do Not Invent Requirements**: Never fabricate regulatory, legal, or domain constraints without evidence.
2. **Do Not Hide Decisions**: Record architectural choices explicitly in ADRs, not inside code comments.
3. **Preserve Important History**: Never silently destroy audit trails or assessment records; maintain append-only semantics where required.
4. **Migration Integrity**: Never edit already-applied migrations. Add forward migrations.
5. **Zero Secrets in Repository**: Never commit credentials, tokens, or private keys. Maintain `.env.example`.
6. **Strict Environment Separation**: Development and production environments, databases, and credentials must remain strictly isolated.
7. **Configuration Consistency**: Updating an environment variable requires updating Compose, `.env.example`, Dockerfiles, and CI/CD simultaneously.
8. **Data Integrity Over Convenience**: Never weaken data integrity guarantees to make implementation easier.
9. **Reasoned Security**: Cryptographic choices and auth models must be grounded in explicit threat models.
10. **Reproducible Infrastructure**: Local dev environments must run reliably from version-controlled configuration.
11. **CI Must Verify**: Automated pipelines must execute automated tests before allowing merges or deployments.
12. **Never Use GitHub CLI (`gh`)**: NEVER invoke the `gh` tool or CLI under any circumstances. Use `rtk git` or standard `git` exclusively.

---

# 7. Change Discipline & Final Principle

Before changing architectural or infrastructure code, verify:
* What confirmed requirement does this satisfy?
* Is there an accepted ADR?
* Does it impact dev/production isolation or secrets?
* Does it preserve record integrity and offline availability?
* How is it verified in automated tests?

> **Requirements constrain the solution.  
> Architecture explains the solution.  
> Implementation realizes the architecture.  
> Tests verify the result.**
