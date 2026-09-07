-- V10__assessment_and_signoff_schema.sql
-- Project Tarbook STCW Assessment, Sign-off Workflow & Progress Tracking Engine (Phase 5)

CREATE TABLE task_assessments (
    id UUID PRIMARY KEY,
    tar_book_id UUID NOT NULL REFERENCES tar_books(id) ON DELETE RESTRICT,
    task_definition_id UUID NOT NULL REFERENCES task_definitions(id) ON DELETE RESTRICT,
    candidate_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    grade VARCHAR(50) NOT NULL CHECK (grade IN ('EXCELLENT', 'SATISFACTORY', 'NEEDS_IMPROVEMENT', 'NOT_YET_COMPETENT')),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'IN_PROGRESS', 'APPROVED', 'REJECTED', 'REWORK_REQUESTED')),
    comments TEXT,
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE assessment_signoffs (
    id UUID PRIMARY KEY,
    task_assessment_id UUID NOT NULL REFERENCES task_assessments(id) ON DELETE RESTRICT,
    signer_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    signer_role VARCHAR(50) NOT NULL CHECK (signer_role IN ('ASSESSOR', 'CHIEF_OFFICER', 'CHIEF_ENGINEER', 'MASTER', 'COMPANY_TRAINING_OFFICER')),
    verdict VARCHAR(50) NOT NULL CHECK (verdict IN ('APPROVED', 'REJECTED', 'REWORK_REQUESTED')),
    comments TEXT,
    signed_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_task_assessments_tar_book ON task_assessments(tar_book_id);
CREATE INDEX idx_task_assessments_candidate ON task_assessments(candidate_user_id);
CREATE INDEX idx_task_assessments_task_def ON task_assessments(task_definition_id);
CREATE INDEX idx_task_assessments_status ON task_assessments(status);
CREATE INDEX idx_assessment_signoffs_assessment ON assessment_signoffs(task_assessment_id);
CREATE INDEX idx_assessment_signoffs_signer ON assessment_signoffs(signer_user_id);
