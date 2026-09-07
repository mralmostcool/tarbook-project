---
title: "Database Table Descriptors Registry"
---

# Database Table Descriptors Registry

Project Tarbook utilizes **PostgreSQL 17** with **PostGIS 3.5** spatial extensions. The relational persistence layer comprises **31 authoritative database tables** managed via Flyway schema migrations (`V1__core_schema.sql` through `V13__align_entity_schemas.sql`).

## Complete Database Tables Matrix (31 Tables)

- [[database/tables/app_users|app_users]]: Defined in `V1__core_schema.sql`
- [[database/tables/assessment_signoffs|assessment_signoffs]]: Defined in `V10__assessment_and_signoff_schema.sql`
- [[database/tables/audit_events|audit_events]]: Defined in `V3__auth_and_integrity.sql`
- [[database/tables/audit_logs|audit_logs]]: Defined in `V11__audit_logs_schema.sql`
- [[database/tables/cadet_eligibility_rules|cadet_eligibility_rules]]: Defined in `V12__program_and_journal_persistence_schema.sql`
- [[database/tables/candidates|candidates]]: Defined in `V1__core_schema.sql`
- [[database/tables/document_verification_records|document_verification_records]]: Defined in `V9__seafarer_documents_and_certificates.sql`
- [[database/tables/eligibility_assessments|eligibility_assessments]]: Defined in `V7__eligibility_assessments.sql`
- [[database/tables/entry_attachments|entry_attachments]]: Defined in `V12__program_and_journal_persistence_schema.sql`
- [[database/tables/evidence_artifacts|evidence_artifacts]]: Defined in `V1__core_schema.sql`
- [[database/tables/journal_entries|journal_entries]]: Defined in `V12__program_and_journal_persistence_schema.sql`
- [[database/tables/officer_signing_keys|officer_signing_keys]]: Defined in `V3__auth_and_integrity.sql`
- [[database/tables/organizations|organizations]]: Defined in `V1__core_schema.sql`
- [[database/tables/record_amendments|record_amendments]]: Defined in `V8__discrepancy_and_amendment_model.sql`
- [[database/tables/sea_service_endorsements|sea_service_endorsements]]: Defined in `V4__sea_service_records.sql`
- [[database/tables/sea_service_records|sea_service_records]]: Defined in `V4__sea_service_records.sql`
- [[database/tables/seafarer_certificates|seafarer_certificates]]: Defined in `V9__seafarer_documents_and_certificates.sql`
- [[database/tables/seafarer_documents|seafarer_documents]]: Defined in `V9__seafarer_documents_and_certificates.sql`
- [[database/tables/stcw_programs|stcw_programs]]: Defined in `V12__program_and_journal_persistence_schema.sql`
- [[database/tables/syllabus_functions|syllabus_functions]]: Defined in `V12__program_and_journal_persistence_schema.sql`
- [[database/tables/syllabus_tasks|syllabus_tasks]]: Defined in `V12__program_and_journal_persistence_schema.sql`
- [[database/tables/sync_operations|sync_operations]]: Defined in `V2__sync_subsystem.sql`
- [[database/tables/sync_sessions|sync_sessions]]: Defined in `V2__sync_subsystem.sql`
- [[database/tables/tar_books|tar_books]]: Defined in `V1__core_schema.sql`
- [[database/tables/task_assessments|task_assessments]]: Defined in `V10__assessment_and_signoff_schema.sql`
- [[database/tables/task_definitions|task_definitions]]: Defined in `V1__core_schema.sql`
- [[database/tables/task_entries|task_entries]]: Defined in `V1__core_schema.sql`
- [[database/tables/task_prerequisites|task_prerequisites]]: Defined in `V12__program_and_journal_persistence_schema.sql`
- [[database/tables/task_signoffs|task_signoffs]]: Defined in `V1__core_schema.sql`
- [[database/tables/training_programs|training_programs]]: Defined in `V1__core_schema.sql`
- [[database/tables/vessel_crew_assignments|vessel_crew_assignments]]: Defined in `V5__crew_roster_and_vessel_assignments.sql`
