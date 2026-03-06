-- Seed Permissions
INSERT INTO permissions (permission_key, description) VALUES
                                                          ('SYSTEM_READ', 'View system metrics and logs'),
                                                          ('USER_WRITE', 'Create or modify users'),
                                                          ('ROLE_ASSIGN', 'Assign roles to users');

-- Seed Roles
INSERT INTO roles (role_name, description) VALUES
                                               ('SUPER_ADMIN', 'Full system access'),
                                               ('SECURITY_AUDITOR', 'Read-only access to audit logs');

-- Map Permissions to Roles
INSERT INTO role_permission (role_id, permission_id) VALUES
                                                         (1, 1), (1, 2), (1, 3), -- SUPER_ADMIN gets everything
                                                         (2, 1);                 -- AUDITOR only reads

-- Seed Initial Users (Pass: 'noble_oath_2026')
INSERT INTO users (username, email, password_hash, is_active) VALUES
                                                                  ('ranxom', 'admin@zentry.io', '$2a$12$R9h/lZKSV6Wyv.p3rA1S5eXv.8Wb8p.O7Yv.7Yv.7Yv.7Yv.7Yv', true),
                                                                  ('ghost_dev', 'ghost@zentry.io', '$2a$12$R9h/lZKSV6Wyv.p3rA1S5eXv.8Wb8p.O7Yv.7Yv.7Yv.7Yv.7Yv', false);

-- Map Users to Roles
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);