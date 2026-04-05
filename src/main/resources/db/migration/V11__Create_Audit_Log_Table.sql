-- V10__Create_Audit_Log_Table.sql
-- Create Audit_Log table for tracking user actions and system events per PDF spec

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    user_id BIGINT,
    admin_id BIGINT,
    old_value TEXT,
    new_value TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details TEXT,
    status VARCHAR(50) DEFAULT 'SUCCESS',
    
    -- Constraints
    CONSTRAINT fk_audit_log_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_log_admin 
        FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes for efficient queries
CREATE INDEX IF NOT EXISTS idx_audit_log_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_admin_id ON audit_logs(admin_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_log_timestamp ON audit_logs(timestamp DESC);

-- Add comments
COMMENT ON TABLE audit_logs IS 'Comprehensive audit trail for tracking user actions, admin operations, and system events';
COMMENT ON COLUMN audit_logs.entity_type IS 'Type of entity being audited (e.g., Product, Order, User)';
COMMENT ON COLUMN audit_logs.entity_id IS 'ID of the entity being audited';
COMMENT ON COLUMN audit_logs.action IS 'Action performed (CREATE, UPDATE, DELETE, APPROVE, REJECT, etc.)';
COMMENT ON COLUMN audit_logs.user_id IS 'Regular user who triggered the action';
COMMENT ON COLUMN audit_logs.admin_id IS 'Admin user who triggered the action (for admin-only operations)';
COMMENT ON COLUMN audit_logs.old_value IS 'Previous value before change (for UPDATE actions)';
COMMENT ON COLUMN audit_logs.new_value IS 'New value after change (for UPDATE actions)';
COMMENT ON COLUMN audit_logs.status IS 'Status of the audited action (SUCCESS, FAILURE, PENDING)';
COMMENT ON COLUMN audit_logs.details IS 'Additional details or context about the action';
