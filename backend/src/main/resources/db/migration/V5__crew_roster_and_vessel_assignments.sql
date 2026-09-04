-- V5__crew_roster_and_vessel_assignments.sql
-- Project Tarbook Shipboard Crew Roster and Vessel Assignment Verification Subsystem (ADR 0010)

-- 1. Vessel Crew Assignments (Officer shipboard deployments)
CREATE TABLE vessel_crew_assignments (
    id UUID PRIMARY KEY,
    sponsoring_org_id UUID NOT NULL REFERENCES organizations(id) ON DELETE RESTRICT,
    officer_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    external_assignment_id VARCHAR(100),
    vessel_name VARCHAR(255) NOT NULL,
    vessel_imo VARCHAR(10) NOT NULL,
    rank VARCHAR(50) NOT NULL,
    sign_on_date DATE NOT NULL,
    sign_off_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('SCHEDULED', 'ACTIVE', 'COMPLETED', 'CANCELLED')),
    sync_sequence BIGINT DEFAULT nextval('global_sync_sequence'),
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_crew_assignment_dates CHECK (sign_off_date IS NULL OR sign_off_date >= sign_on_date),
    CONSTRAINT uq_non_overlapping_officer_assignment EXCLUDE USING gist (
        officer_user_id WITH =,
        daterange(sign_on_date, COALESCE(sign_off_date, 'infinity'), '[]') WITH &&
    ) WHERE (status IN ('ACTIVE', 'SCHEDULED', 'COMPLETED'))
);

CREATE INDEX idx_crew_assignments_officer ON vessel_crew_assignments(officer_user_id);
CREATE INDEX idx_crew_assignments_vessel_imo ON vessel_crew_assignments(vessel_imo);
CREATE INDEX idx_crew_assignments_org ON vessel_crew_assignments(sponsoring_org_id);
CREATE INDEX idx_crew_assignments_sync_seq ON vessel_crew_assignments(sync_sequence);
CREATE UNIQUE INDEX uq_crew_assignments_external ON vessel_crew_assignments (sponsoring_org_id, external_assignment_id) WHERE (external_assignment_id IS NOT NULL);

-- 2. Add verification_status to task_signoffs for roster verification
ALTER TABLE task_signoffs
    ADD COLUMN verification_status VARCHAR(50) NOT NULL DEFAULT 'ROSTER_VERIFIED' CHECK (verification_status IN ('ROSTER_VERIFIED', 'ROSTER_UNVERIFIED', 'MANUALLY_VERIFIED', 'REJECTED'));

CREATE INDEX idx_task_signoffs_verification ON task_signoffs(verification_status);
