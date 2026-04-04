-- Migration V4__Create_User_Tables.sql
-- Creates user authentication and authorization tables

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    avatar VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    password_reset_token VARCHAR(500),
    password_reset_token_expiry TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_status ON users(status);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
