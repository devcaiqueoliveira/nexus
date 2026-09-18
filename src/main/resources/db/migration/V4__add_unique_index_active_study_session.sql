CREATE UNIQUE INDEX idx_unique_active_session_per_subject
ON study_sessions (subject_id)
WHERE status = 'IN_PROGRESS';