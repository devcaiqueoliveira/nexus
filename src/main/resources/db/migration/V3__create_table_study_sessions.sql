CREATE TABLE study_sessions (
    id UUID PRIMARY KEY,
    subject_id UUID NOT NULL,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_study_sessions_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);