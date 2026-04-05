CREATE TABLE async_jobs (
    id BIGSERIAL PRIMARY KEY,
    job_id VARCHAR(36) NOT NULL UNIQUE,
    job_type VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    progress INTEGER DEFAULT 0,
    result_data TEXT,
    error_message TEXT,
    estimated_seconds_remaining INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_job_user_id ON async_jobs(user_id);
CREATE INDEX idx_job_status ON async_jobs(status);
CREATE INDEX idx_job_type ON async_jobs(job_type);
CREATE INDEX idx_job_created ON async_jobs(created_at DESC);

-- Add comment explaining table purpose
COMMENT ON TABLE async_jobs IS 'Tracks asynchronous background job execution status and results';
COMMENT ON COLUMN async_jobs.job_id IS 'External unique job identifier for client polling';
COMMENT ON COLUMN async_jobs.status IS 'Job status: PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED';
COMMENT ON COLUMN async_jobs.progress IS 'Progress percentage 0-100';
COMMENT ON COLUMN async_jobs.result_data IS 'JSON with result details (e.g., file path)';
