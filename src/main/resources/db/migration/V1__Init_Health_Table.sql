-- Migration V1__Init_Health_Table.sql
-- Initial database schema baseline (Phase 1)

CREATE TABLE IF NOT EXISTS health_check (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(10) NOT NULL DEFAULT 'UP',
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    uptime_ms BIGINT,
    version VARCHAR(20)
);

CREATE INDEX IF NOT EXISTS idx_health_check_timestamp ON health_check(timestamp);
