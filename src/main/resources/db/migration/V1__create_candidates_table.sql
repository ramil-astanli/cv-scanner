-- Candidates əsas cədvəli
CREATE TABLE candidates (
                            id                  BIGSERIAL       PRIMARY KEY,
                            cv_file_name        VARCHAR(255),
                            cv_file_path        VARCHAR(500),
                            full_name           VARCHAR(255),
                            email               VARCHAR(255),
                            phone               VARCHAR(50),
                            years_of_experience INTEGER,
                            preferred_location  VARCHAR(255),
                            job_type            VARCHAR(50),
                            processing_status   VARCHAR(20)     NOT NULL DEFAULT 'SUCCESS',
                            error_message       TEXT,
                            created_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE candidate_skills (
                                  candidate_id BIGINT      NOT NULL,
                                  skill        VARCHAR(100) NOT NULL,
                                  CONSTRAINT fk_candidate
                                      FOREIGN KEY (candidate_id)
                                          REFERENCES candidates (id)
                                          ON DELETE CASCADE
);

CREATE INDEX idx_candidates_status
    ON candidates (processing_status);

CREATE INDEX idx_candidates_job_type
    ON candidates (job_type);

CREATE INDEX idx_candidates_experience
    ON candidates (years_of_experience);

CREATE INDEX idx_candidates_location
    ON candidates (preferred_location);

CREATE INDEX idx_candidate_skills_candidate_id
    ON candidate_skills (candidate_id);

CREATE INDEX idx_candidate_skills_skill
    ON candidate_skills (skill);