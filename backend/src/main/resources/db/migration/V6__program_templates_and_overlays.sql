-- V6__program_templates_and_overlays.sql
-- Project Tarbook STCW Program Templates, Syllabus Versioning & Overlay Model (ADR 0011)

-- 1. Modify training_programs for revisioning and organization overlays
ALTER TABLE training_programs
    DROP CONSTRAINT training_programs_code_key,
    ADD CONSTRAINT uq_training_programs_code_revision UNIQUE (code, revision),
    ADD COLUMN parent_program_id UUID REFERENCES training_programs(id) ON DELETE RESTRICT,
    ADD COLUMN owner_org_id UUID REFERENCES organizations(id) ON DELETE RESTRICT,
    ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_training_programs_parent ON training_programs(parent_program_id);
CREATE INDEX idx_training_programs_owner ON training_programs(owner_org_id);

-- 2. Modify task_definitions for 3-tier STCW taxonomy and multi-tenant scoping
ALTER TABLE task_definitions
    ADD COLUMN function_code VARCHAR(50),
    ADD COLUMN function_title VARCHAR(255),
    ADD COLUMN competency_code VARCHAR(50),
    ADD COLUMN competency_title VARCHAR(255),
    ADD COLUMN scope VARCHAR(50) NOT NULL DEFAULT 'STATUTORY_STCW' CHECK (scope IN ('STATUTORY_STCW', 'COMPANY_SPECIFIC')),
    ADD COLUMN owner_org_id UUID REFERENCES organizations(id) ON DELETE RESTRICT;

CREATE INDEX idx_task_definitions_competency ON task_definitions(program_id, competency_code);
CREATE INDEX idx_task_definitions_scope ON task_definitions(scope);
CREATE INDEX idx_task_definitions_owner ON task_definitions(owner_org_id);
