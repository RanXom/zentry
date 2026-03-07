-- 1. Permissions Table
CREATE TABLE permissions (
                             permission_id BIGSERIAL PRIMARY KEY,
                             permission_key VARCHAR(100) UNIQUE NOT NULL,
                             description TEXT
);

-- 2. Roles Table
CREATE TABLE roles (
                       role_id BIGSERIAL PRIMARY KEY,
                       role_name VARCHAR(100) UNIQUE NOT NULL,
                       description TEXT
);

-- 3. Role-Permission Mapping (Many-to-Many)
CREATE TABLE role_permission (
                                 role_id BIGINT REFERENCES roles(role_id) ON DELETE CASCADE,
                                 permission_id BIGINT REFERENCES permissions(permission_id) ON DELETE CASCADE,
                                 PRIMARY KEY (role_id, permission_id)
);

-- 4. Users Table
CREATE TABLE users (
                       user_id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(100) UNIQUE NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password_hash TEXT NOT NULL,
                       is_active BOOLEAN DEFAULT TRUE,
                       account_locked BOOLEAN DEFAULT FALSE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       last_login TIMESTAMP
);

-- 5. User-Role Mapping (Many-to-Many)
CREATE TABLE user_roles (
                            user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
                            role_id BIGINT REFERENCES roles(role_id) ON DELETE CASCADE,
                            PRIMARY KEY (user_id, role_id)
);

-- 6. Audit Logs Table (Using JSONB for PostgreSQL)
CREATE TABLE audit_logs (
                            log_id BIGSERIAL PRIMARY KEY,
                            actor_id BIGINT REFERENCES users(user_id),
                            action_type VARCHAR(100) NOT NULL,
                            target_entity_id BIGINT,
                            details JSONB,
                            ip_address VARCHAR(45),
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);