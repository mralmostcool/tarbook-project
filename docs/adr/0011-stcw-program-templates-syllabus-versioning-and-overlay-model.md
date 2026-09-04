# 11. STCW Program Templates, Syllabus Versioning & Overlay Model

We adopt immutable snapshot pinning for candidate TAR Books, an organization-level program overlay model for proprietary curriculum customization, a 3-tier STCW competency hierarchy (`Function` -> `Competency` -> `Task Definition`), and declarative YAML/JSON template definitions under version control with an idempotent REST ingestion pipeline.

## Status
accepted

## Context
[ADR 0002](0002-core-database-schema-and-offline-entity-model.md) and [Flyway V1](../../backend/src/main/resources/db/migration/V1__core_schema.sql) established baseline relational models for `training_programs`, `task_definitions`, and `tar_books`. Statutory training under STCW (Model Courses 7.03 for Navigation, 7.04 for Engineering, and 7.08 for ETO) requires structured competency frameworks. Sponsoring shipping companies and Maritime Training Institutes (MTIs) mandate additional proprietary training requirements (e.g., Tanker Management Self-Assessment / TMSA matrices, company safety management system procedures) on top of the statutory baseline without compromising regulatory compliance.

## Considered Options
1. **Dynamic In-Place Mutation**: Training programs and task definitions are mutated directly in place; completed and in-progress TAR Books map dynamically to changed syllabus items.
2. **Full Cloned Forking**: Sponsoring companies fork and duplicate the entire STCW syllabus into a standalone program instance for every variation.
3. **Immutable Snapshot Pinning with Program Overlays and 3-Tier STCW Taxonomy**: Issued TAR Books pin an immutable program revision snapshot. Company extensions layer cleanly via non-destructive overlays (`parent_program_id`), structured in a 3-tier STCW hierarchy and loaded declaratively from version-controlled YAML/JSON definitions.

## Decision Rationale

### 1. Immutable Snapshot Pinning & Grandfathering
- **Statutory Non-Repudiation**:
  - Once a `tar_books` record is issued to a candidate, it is permanently pinned to a specific `program_id` (and therefore a specific `code` and `revision`).
  - Revisions to statutory syllabi (e.g. IMO STCW revision cycles) create new rows in `training_programs` (`revision = '2026.1'`).
  - Active candidate TAR Books remain grandfathered under their original syllabus. In-flight sign-offs and requirements remain 100% stable during months-long sea voyages.
- **Administrative Syllabus Migration Workflow**:
  - Upgrading an in-progress candidate to a newer syllabus revision cannot happen silently. It requires an explicit ashore administrative workflow (`POST /api/v1/admin/tar-books/{id}/migrate-program`).
  - The migration engine maps completed tasks by identical STCW competency codes and generates bridging tasks for newly introduced statutory requirements.

### 2. Multi-Tenant Curriculum Customization (Program Overlay Model)
- **Overlay Hierarchy**:
  - Sponsoring organizations (shipping companies, MTIs) do not clone statutory syllabi.
  - Sponsoring organizations create an overlay program record in `training_programs` referencing `parent_program_id` and specifying `owner_org_id`.
  - The overlay inherits all statutory task definitions from the parent program and appends company-specific tasks with `scope = 'COMPANY_SPECIFIC'` and `owner_org_id`.
- **Query Resolution**:
  - When candidate TAR Book is instantiated under an overlay program, the effective syllabus is the union of parent statutory tasks and active company overlay tasks.
  - Statutory reporting separates pure STCW compliance from company-specific training metrics for Flag State auditing.

### 3. 3-Tier STCW Standard Taxonomy
- **Hierarchy Structure**:
  - **Tier 1: Function**: Broad maritime operational domain matching STCW Code tables (e.g., `Function 1: Navigation at the Operational Level`, `Function 2: Cargo Handling and Stowage`).
  - **Tier 2: Competency**: Specific capability requirement (e.g., `Competency 1.1: Plan and conduct a passage and determine position`).
  - **Tier 3: Task Definition**: Granular operational demonstration or practical training activity evaluated by an officer (e.g., `Task 1.1.1: Terrestrial navigation fixes using landmarks and celestial observations`).
- **Metadata Fields**:
  - Each `task_definitions` record includes `function_code`, `function_title`, `competency_code`, `competency_title`, `stcw_reference` (e.g., `STCW Table A-II/1`), `criteria`, and `required_evidence_count`.

### 4. Declarative YAML/JSON Curriculum Ingestion
- **Version-Controlled Templates**:
  - Statutory curricula (7.03, 7.04, 7.08) are authored as declarative YAML/JSON files stored under `templates/curriculum/` in the repository.
- **Ingestion & Loader Pipeline**:
  - Backend provides an idempotent administrative endpoint `POST /api/v1/admin/programs/import`.
  - Ingestion parses the template, validates the 3-tier hierarchy and uniqueness constraints, and creates or activates `training_programs` and associated `task_definitions` within a single relational transaction.
  - Safe replay semantics: Re-importing identical program revisions updates descriptive text without altering immutable primary keys or existing task associations.

## Consequences
- Requires Flyway migration `V6__program_templates_and_overlays.sql` adding `parent_program_id`, `owner_org_id`, `(code, revision)` uniqueness to `training_programs`, and 3-tier STCW hierarchy columns to `task_definitions`.
- Requires Spring Boot service `CurriculumTemplateService` implementing YAML/JSON parsing and hierarchical validation.
- Adds declarative template schema specification and endpoint `POST /api/v1/admin/programs/import`.
- Ensures offline mobile devices receive resolved statutory and overlay tasks during Shore Sync pull.
