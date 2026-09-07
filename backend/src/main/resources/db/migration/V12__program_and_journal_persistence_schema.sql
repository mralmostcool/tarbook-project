-- V12__program_and_journal_persistence_schema.sql
-- Missing DDL tables for Program & Journal Modulith aggregates

-- 1. STCW Programs
CREATE TABLE stcw_programs (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    stream VARCHAR(50) NOT NULL,
    total_required_sea_days INTEGER NOT NULL,
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stcw_programs_code ON stcw_programs(code);

-- 2. Syllabus Functions
CREATE TABLE syllabus_functions (
    id UUID PRIMARY KEY,
    program_id UUID NOT NULL REFERENCES stcw_programs(id) ON DELETE RESTRICT,
    function_code VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    stcw_code VARCHAR(50) NOT NULL,
    display_order INTEGER NOT NULL
);

CREATE INDEX idx_syllabus_functions_program ON syllabus_functions(program_id);

-- 3. Syllabus Tasks
CREATE TABLE syllabus_tasks (
    id UUID PRIMARY KEY,
    function_id UUID NOT NULL REFERENCES syllabus_functions(id) ON DELETE RESTRICT,
    task_code VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    required_sign_offs INTEGER NOT NULL,
    minimum_watchkeeping_hours INTEGER
);

CREATE INDEX idx_syllabus_tasks_function ON syllabus_tasks(function_id);

-- 4. Task Prerequisites
CREATE TABLE task_prerequisites (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES syllabus_tasks(id) ON DELETE CASCADE,
    prerequisite_task_id UUID NOT NULL REFERENCES syllabus_tasks(id) ON DELETE RESTRICT
);

CREATE INDEX idx_task_prerequisites_task ON task_prerequisites(task_id);

-- 5. Cadet Eligibility Rules
CREATE TABLE cadet_eligibility_rules (
    id UUID PRIMARY KEY,
    program_id UUID NOT NULL REFERENCES stcw_programs(id) ON DELETE RESTRICT,
    rule_code VARCHAR(50) NOT NULL,
    min_sea_days INTEGER NOT NULL,
    min_bridge_watchkeeping_hours INTEGER,
    min_engine_watchkeeping_hours INTEGER
);

CREATE INDEX idx_cadet_eligibility_rules_program ON cadet_eligibility_rules(program_id);

-- 6. Journal Entries
CREATE TABLE journal_entries (
    id UUID PRIMARY KEY,
    cadet_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    vessel_assignment_id UUID REFERENCES vessel_crew_assignments(id) ON DELETE RESTRICT,
    entry_date DATE NOT NULL,
    sea_days_logged NUMERIC(5,2),
    watchkeeping_hours NUMERIC(5,2),
    status VARCHAR(50) NOT NULL,
    cadet_comments TEXT,
    supervisor_comments TEXT,
    created_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_journal_entries_cadet ON journal_entries(cadet_user_id);
CREATE INDEX idx_journal_entries_date ON journal_entries(entry_date);

-- 7. Entry Attachments
CREATE TABLE entry_attachments (
    id UUID PRIMARY KEY,
    journal_entry_id UUID NOT NULL REFERENCES journal_entries(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    storage_uri VARCHAR(512) NOT NULL,
    sha256_hash VARCHAR(64) NOT NULL,
    uploaded_at_utc TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_entry_attachments_entry ON entry_attachments(journal_entry_id);
