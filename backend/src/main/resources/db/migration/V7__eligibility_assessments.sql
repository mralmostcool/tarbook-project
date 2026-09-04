-- V7__eligibility_assessments.sql
-- Project Tarbook STCW Sea-Time & Eligibility Rule Engine (ADR 0012)

CREATE TABLE eligibility_assessments (
    id UUID PRIMARY KEY,
    tar_book_id UUID NOT NULL REFERENCES tar_books(id) ON DELETE RESTRICT,
    candidate_id UUID NOT NULL REFERENCES candidates(id) ON DELETE RESTRICT,
    certification_pathway VARCHAR(50) NOT NULL,
    rule_engine_version VARCHAR(50) NOT NULL,
    program_revision VARCHAR(50) NOT NULL,
    overall_status VARCHAR(50) NOT NULL CHECK (overall_status IN ('INELIGIBLE', 'CONDITIONALLY_ELIGIBLE', 'ELIGIBLE')),
    qualifying_sea_days NUMERIC(8,2) NOT NULL,
    qualifying_watch_hours NUMERIC(8,2) NOT NULL,
    statutory_criteria_met BOOLEAN NOT NULL,
    competency_tasks_completed INTEGER NOT NULL,
    competency_tasks_required INTEGER NOT NULL,
    pending_roster_reviews_count INTEGER NOT NULL DEFAULT 0,
    assessment_dossier JSONB NOT NULL,
    dossier_hash VARCHAR(64) NOT NULL,
    assessed_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assessed_by_user_id UUID REFERENCES app_users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_eligibility_assessments_tar_book ON eligibility_assessments(tar_book_id);
CREATE INDEX idx_eligibility_assessments_candidate ON eligibility_assessments(candidate_id);
CREATE INDEX idx_eligibility_assessments_status ON eligibility_assessments(overall_status);
CREATE INDEX idx_eligibility_assessments_pathway ON eligibility_assessments(certification_pathway);
