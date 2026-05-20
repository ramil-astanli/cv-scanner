ALTER TABLE candidates
    ADD COLUMN created_by VARCHAR(20),
    ADD COLUMN updated_at TIMESTAMP,
    ADD COLUMN updated_by VARCHAR(20);