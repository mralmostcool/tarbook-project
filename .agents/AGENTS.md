# Project Tarbook: Agent Orchestration Directory (`AGENTS.md`)

Welcome, Agent.

This file is the primary operational directory and engineering guide for **Project Tarbook — Electronic Training and Assessment Record Book**.

Its purpose is to help agents:

* understand the problem domain;
* locate project documentation and source code;
* determine which area of responsibility applies to a task;
* preserve established requirements and invariants;
* avoid introducing unjustified architectural assumptions;
* and coordinate changes across the project safely.

---

# 1. Project Mission & Domain Context

**Project Tarbook** is a digital **Training and Assessment Record (TAR) Book** for the maritime industry.

The system is intended to replace or augment traditional paper training records used by maritime cadets and officers by providing a resilient digital system for recording training activity, documenting evidence, performing assessments, and preserving the integrity and provenance of completed records.

The project is expected to operate in environments where:

* network connectivity may be unavailable or intermittent;
* communication may be expensive and bandwidth-constrained;
* users may work remotely from shore-based infrastructure;
* training evidence may originate from mobile devices;
* records may have regulatory, organizational, or legal significance;
* and the integrity and traceability of records are important.

The project is being developed through **domain discovery, regulatory research, stakeholder analysis, requirements analysis, architectural investigation, implementation, and verification**.

The architecture is therefore expected to evolve as understanding improves.

---

# 2. Architectural Epistemic Rule

Project Tarbook is under active architectural discovery.

Agents MUST distinguish between the following categories:

### 2.1 Requirements

Things the system must satisfy.

Example:

> The system must remain usable when network connectivity is unavailable.

### 2.2 Constraints

Limitations imposed by the domain, operating environment, regulations, users, infrastructure, or other external factors.

Example:

> Maritime users may operate with expensive and highly intermittent connectivity.

### 2.3 Assumptions

Things currently believed to be true but which have not yet been adequately validated.

Example:

> A particular synchronization strategy will be sufficient for the expected conflict patterns.

### 2.4 Hypotheses

Potential approaches that appear promising but require investigation, experimentation, or validation.

Example:

> A priority-based synchronization queue may be an effective way of handling constrained connectivity.

### 2.5 Architectural Decisions

Approaches that have been explicitly evaluated and accepted by the project.

An architectural decision should have a documented rationale and, where appropriate, alternatives and consequences.

### 2.6 Implementation Details

Specific technical choices made within an established architectural direction.

---

## Architectural Neutrality

Agents MUST NOT silently convert an assumption, hypothesis, or implementation preference into an architectural constraint.

In particular, agents MUST NOT assume that the project must use a particular:

* bounded-context structure;
* aggregate structure;
* module/package layout;
* application architecture;
* synchronization algorithm;
* queueing mechanism;
* consistency model;
* event-sourcing approach;
* cryptographic algorithm;
* API style;
* programming model;
* database access strategy;
* storage format;
* deployment platform;
* CI/CD strategy;
* branch strategy;
* or infrastructure topology.

Where multiple technically viable approaches exist, agents should identify the relevant trade-offs before selecting one.

When an unresolved architectural question is discovered, the preferred action is to:

1. identify the question;
2. state the relevant constraints;
3. identify reasonable alternatives;
4. evaluate the alternatives;
5. document the resulting decision when one is made.

The goal of the repository is not merely to produce working software.

The goal is to progressively discover an architecture that is justified by the domain, operating environment, regulatory context, security requirements, and actual engineering evidence.

---

# 3. Core Constraints & Operating Environment

These constraints describe the **problem the system must solve**.

They are not, by themselves, implementation prescriptions.

## 3.1 Offline and Intermittent Connectivity

The system must remain usable when network connectivity is:

* unavailable;
* unreliable;
* highly latent;
* expensive;
* or otherwise unsuitable for continuous communication.

Core user workflows must not depend on continuous network access.

The design must account for eventual synchronization between information recorded locally and information available to remote systems.

The exact synchronization architecture, consistency model, conflict-resolution strategy, persistence mechanism, and transport protocol are architectural decisions and MUST be established through design work rather than assumed here.

---

## 3.2 Integrity of Training Records

Training records are high-integrity records.

The system must preserve the provenance and history of important actions, including where applicable:

* record creation;
* training activity;
* assessment;
* approval or sign-off;
* correction;
* reversal;
* synchronization;
* and other material state changes.

The system should make unauthorized or undetectable modification of important completed records difficult or detectable.

The exact mechanisms used to provide:

* integrity;
* authenticity;
* non-repudiation;
* auditability;
* tamper evidence;
* and accountability

are implementation and architectural decisions that must be derived from the actual requirements and threat model.

---

## 3.3 Verification of Training Activity

Where required by the domain, the system must be capable of recording contextual information associated with training activity.

Potential contextual information includes:

* identity;
* time;
* location;
* device context;
* assessment context;
* supporting evidence;
* and synchronization context.

Location information may be used as an input into verification or fraud-detection mechanisms.

Agents MUST NOT assume that GPS or any other single signal independently proves that a training activity occurred.

Verification rules must be grounded in established domain, operational, security, or regulatory requirements.

---

## 3.4 Evidence and Attachments

The system must support evidence associated with training and assessment activities.

Examples may include:

* photographs;
* hand-drawn schematics;
* engineering drawings;
* calculations;
* diagrams;
* scanned documents;
* and other digital artifacts.

Evidence may be created on mobile devices under constraints involving:

* storage;
* CPU;
* battery;
* bandwidth;
* network reliability;
* and processing capability.

The system therefore requires an explicit strategy for:

* capturing evidence;
* processing evidence;
* storing evidence;
* associating evidence with records;
* transmitting evidence;
* verifying evidence integrity;
* and managing evidence lifecycle.

Specific formats, compression algorithms, file-size limits, and transfer policies are architectural or implementation decisions unless explicitly elevated into requirements.

---

## 3.5 Regulatory and Legal Context

Project Tarbook operates in the context of maritime training, assessment, certification, and record keeping.

Applicable regulatory requirements, standards, organizational procedures, and legal obligations take precedence over implementation preferences.

Agents MUST distinguish between:

* confirmed regulatory requirements;
* stakeholder requirements;
* product decisions;
* technical assumptions;
* security requirements;
* hypotheses;
* and implementation choices.

Agents MUST NOT invent regulatory requirements.

When a requirement is uncertain, the uncertainty should be surfaced and documented rather than silently turned into an invariant.

---

## 3.6 Data Integrity and Traceability

The system should preserve enough information to establish, where required:

* who performed an action;
* what occurred;
* when it occurred;
* what evidence supported it;
* what record state resulted;
* and under what synchronization or verification conditions the action was recorded.

Whether this requires:

* audit tables;
* append-only records;
* event-based designs;
* cryptographic signatures;
* hashes;
* versioning;
* or another mechanism

is an architectural decision.

---

## 3.7 Environment Separation

Development and production are distinct operating environments.

Development data MUST NOT unintentionally share a production database, production object store, production credentials, or other production resources.

Agents should preserve a clear distinction between:

```text
Development Environment
    ↓
Development configuration
Development credentials
Development database
Development object storage
Development supporting services
```

and:

```text
Production Environment
    ↓
Production configuration
Production secrets
Production database
Production object storage
Production supporting services
```

The exact mechanism used to provide that separation is an infrastructure and deployment decision.

---

# 4. Repository & Context Map

Use the repository and project documentation to orient yourself before modifying code.

The current project is expected to contain, where applicable:

```text
.
├── backend/
│   ├── src/
│   ├── pom.xml / build configuration
│   └── ...
│
├── .agents/project-context/
│   ├── project.md
│   ├── domain.md
│   ├── architecture.md
│   ├── database.md
│   ├── api-conventions.md
│   ├── testing.md
│   └── tar-book-structure.md
│
├── .agents/
│   └── project-context/
│       └── ...
│
├── nginx/
│   └── ...
│
├── storage/
│   └── ...
│
├── docker-compose.yml
└── ...
```

This map is an orientation aid, not a statement that every listed directory or architecture is permanently required.

Before using a documented path or command, verify that it still exists and corresponds to the current repository state.

---

# 5. Specialized Agent Personas & Work Allocation

Agents are organized by **responsibility and problem space**, not by predetermined technical solutions.

A persona does not automatically authorize an agent to introduce a particular framework, pattern, algorithm, or technology.

Agents should investigate requirements before selecting implementation mechanisms.

---

Detailed persona definitions and system instructions live in [`.agents/agents/`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/):

* **[Lead Orchestrator: Harambe (`/harambe`)](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/harambe.md)**: Chief orchestrator and single point of contact. Decomposes developer intent and delegates across the 10 specialist personas.

* **[Persona A: Domain & Requirements Analyst](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/domain-requirements-analyst.md)**: Domain discovery, stakeholder needs, regulatory requirements, terminology, workflows, and business rules.
* **[Persona B: Domain Modelling & Architecture Analyst](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/architecture-analyst.md)**: Conceptual models, candidate bounded contexts, consistency boundaries, architectural trade-offs, and ADRs.
* **[Persona C: Application & Backend Engineer](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/backend-engineer.md)**: Application behavior, APIs, business logic, persistence integration, concurrency, and error handling.
* **[Persona D: Data & Persistence Engineer](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/persistence-engineer.md)**: Schemas, migrations, relational/spatial models, indexes, query performance, and persistence invariants.
* **[Persona E: Security, Trust & Integrity Analyst](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/security-integrity-analyst.md)**: Authentication, authorization, identity, evidence provenance, cryptography, auditability, and threat modeling.
* **[Persona F: Offline & Synchronization Analyst](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/offline-sync-analyst.md)**: Offline workflows, sync queues, consistency models, conflict handling, and low-bandwidth resilience.
* **[Persona G: Mobile & Evidence Capture Engineer](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/mobile-evidence-engineer.md)**: Mobile UX, camera capture, scanning, edge persistence, artifact lifecycle, and resource constraints.
* **[Persona H: Infrastructure & Platform Engineer](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/infrastructure-engineer.md)**: Docker, Compose, environment configuration, database/service provisioning, and development-production parity.
* **[Persona I: CI/CD & Repository Engineer](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/ci-cd-engineer.md)**: GitHub Actions, build verification, deployment pipelines, branch protection, and release flow.
* **[Persona J: Verification & Quality Engineer](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/verification-quality-engineer.md)**: Test-driven verification, integration/API tests, offline/sync simulation, failure modes, and invariant assertions.

---

# 6. Work Allocation Rules

When a task spans multiple areas, agents should collaborate rather than force the task into one persona.

The preferred engineering progression is:

```text
Understand
    ↓
Model
    ↓
Evaluate alternatives
    ↓
Decide
    ↓
Implement
    ↓
Verify
```

Agents MUST NOT treat an existing implementation as proof that the implementation was the correct architectural decision.

When a task exposes an unresolved architectural question, the agent should surface the question and document the competing options rather than silently selecting an approach solely for convenience.

Agents should prefer the smallest justified architectural commitment.

---

# 7. Engineering Guardrails

These rules protect project integrity without unnecessarily constraining architecture.

## 7.1 Do Not Invent Requirements

Agents MUST NOT create domain, regulatory, legal, security, or operational requirements without evidence.

When uncertain, mark the assumption explicitly.

---

## 7.2 Do Not Hide Architectural Decisions

If a change introduces a meaningful architectural decision, do not bury it inside implementation code.

Document the decision and its rationale.

---

## 7.3 Preserve Important History

Do not silently destroy historical information associated with completed training, assessment, approval, or audit activity.

Where correction is required, follow the established domain and data model for representing that correction.

Whether that is implemented through append-only events, audit records, versioning, or another mechanism is an architectural decision.

---

## 7.4 Migration Integrity

Never edit an already-applied database migration.

If schema changes are required, create a new migration according to the project's chosen migration system.

Before changing the migration mechanism itself, verify the current repository configuration rather than relying on outdated documentation.

---

## 7.5 Secrets and Credentials

Never commit:

* passwords;
* API keys;
* private keys;
* signing keys;
* production credentials;
* cloud credentials;
* tokens;
* or other secret material

to the repository.

Use the project's approved environment/configuration mechanism.

`.env.example` SHOULD contain variable names and safe example values or placeholders, but MUST NOT contain real secrets.

---

## 7.6 Environment Separation

Development and production credentials, databases, object stores, and other stateful resources MUST remain separated.

A local development command MUST NOT accidentally point at production infrastructure.

---

## 7.7 Configuration Consistency

Whenever adding or renaming an environment variable, update all relevant configuration documentation and example files.

At minimum, consider:

```text
application configuration
Docker Compose
Dockerfiles
.env.example
CI/CD configuration
GitHub environment variables
GitHub secrets
deployment configuration
documentation
```

Do not assume that changing one location is sufficient.

---

## 7.8 Data Integrity

Never weaken integrity guarantees merely to make a feature easier to implement.

When a requirement conflicts with implementation convenience, surface the conflict.

---

## 7.9 Security Changes Require Reasoning

Security-sensitive changes should identify:

* the protected asset;
* the threat;
* the trust boundary;
* the expected guarantee;
* and the consequences of the proposed mechanism.

Do not select cryptographic mechanisms merely because they are familiar.

---

## 7.10 Infrastructure Must Be Reproducible

Infrastructure required for local development should be reproducible from version-controlled configuration wherever practical.

Do not rely on undocumented manual setup as the only path to a functioning development environment.

---

## 7.11 CI Must Verify, Not Merely Deploy

Deployment automation should depend on appropriate verification.

Do not create a pipeline that deploys known-unverified code merely because the deployment mechanism works.

---

# 8. Current Architectural Decisions

This section contains **decisions that have been explicitly evaluated and accepted**.

It is intentionally separate from the general project constraints.

Each meaningful architectural decision should eventually record:

* **Decision**
* **Context**
* **Alternatives considered**
* **Rationale**
* **Consequences**
* **Status**

Example:

```text
Decision: <decision>

Context:
<why the decision was necessary>

Alternatives:
<option A>
<option B>
<option C>

Rationale:
<why the chosen approach was selected>

Consequences:
<benefits, costs, trade-offs, risks>

Status:
<proposed | accepted | superseded | deprecated>
```

Do not add a technology or architecture here merely because it currently exists in the repository.

---

# 9. Repository & Delivery Decisions

The repository currently uses:

```text
main
```

as the primary integration branch.

The project is intentionally keeping branch management simple during early development.

Development and production remain separate environments even when code is integrated through the same branch.

The project should not adopt a more complex branch model until there is a demonstrated need.

Changes to:

* branch protection;
* release strategy;
* deployment promotion;
* environment promotion;
* repository permissions;
* CI/CD architecture;
* or branching strategy

should be documented as repository/delivery decisions.

---

# 10. Environment & Configuration Governance

The project should maintain a clear conceptual separation between:

```text
Application Code
        ↓
Configuration
        ↓
Environment
        ↓
Secrets
```

A practical implementation may use different mechanisms for local development and CI/CD.

The exact mechanism is an infrastructure decision.

The important properties are:

### Development

Development should provide:

* isolated development databases;
* isolated development object storage;
* isolated development supporting services;
* development credentials;
* development configuration;
* reproducible local setup.

### Production

Production should provide:

* isolated production databases;
* isolated production object storage;
* production credentials;
* production configuration;
* controlled deployment and access.

### Example Configuration Contract

`.env.example` documents the configuration contract.

A developer's `.env` contains local values.

CI/CD supplies environment-specific values through the approved CI/CD secret/configuration system.

Production secrets MUST NOT be copied into development configuration.

---

# 11. Documentation Context

Before performing work in a particular area, agents should consult the relevant project documentation where available.

### Stakeholders, users, and workflows

Consult the project and stakeholder documentation.

Focus on:

* actors;
* workflows;
* operational environments;
* responsibilities;
* and actual user needs.

### Domain modelling

Consult the domain documentation.

Focus on:

* domain terminology;
* business rules;
* state transitions;
* invariants;
* and lifecycle.

### Architecture

Consult architecture documentation.

Focus on:

* accepted architectural decisions;
* explicit alternatives;
* dependencies;
* failure modes;
* and known trade-offs.

### Database

Consult database documentation.

Focus on:

* established persistence requirements;
* schema;
* migrations;
* indexes;
* spatial requirements;
* and data integrity.

### APIs

Consult API documentation.

Focus on:

* established contracts;
* authentication;
* synchronization behaviour;
* and compatibility.

### Testing

Consult testing documentation.

Focus on:

* existing test strategy;
* infrastructure requirements;
* resilience scenarios;
* and established verification practices.

If documentation conflicts with the current repository, do not silently choose one.

Identify the discrepancy and determine which source represents the current accepted decision.

---

# 12. Developer Command Directory

Commands in this section are examples of the types of commands that may exist.

Agents MUST verify commands against the actual repository before executing or documenting them.

Do not assume that a command remains valid simply because it appears here.

Typical categories include:

### Backend

```text
Build
Test
Run locally
Clean
Static analysis
Database migration
```

### Infrastructure

```text
docker compose up
docker compose down
docker compose logs
docker compose exec
```

### Frontend / Mobile

```text
Install dependencies
Run development server
Build application
Run device/emulator
```

When the actual tooling changes, update this section rather than preserving obsolete commands.

---

# 13. Change Discipline

Before changing an architectural or infrastructure-sensitive area, agents should determine:

1. What requirement is this change satisfying?
2. Is there already an accepted architectural decision?
3. Is the proposed approach merely a hypothesis?
4. Does the change introduce a new architectural decision?
5. Does the change affect development/production separation?
6. Does the change affect secrets or configuration?
7. Does the change affect data integrity?
8. How will the change be verified?

Prefer small, reversible changes while architectural understanding is still evolving.

---

# 14. Final Principle

Project Tarbook should evolve from **requirements and evidence**, not from assumptions embedded in tooling.

The project may eventually adopt strong architectural patterns and specific technologies.

Those choices should become explicit because the project determined that they were appropriate — not because an agent encountered them in an old `AGENTS.md`.

**Requirements constrain the solution.
Architecture explains the solution.
Implementation realizes the architecture.
Tests verify the result.**

Agents should help preserve that distinction.
