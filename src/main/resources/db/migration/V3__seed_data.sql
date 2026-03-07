-- 1. Seed Permissions
INSERT INTO permissions (permission_key, description)
SELECT 'SYSTEM_READ', 'View system metrics and logs'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE permission_key = 'SYSTEM_READ');

INSERT INTO permissions (permission_key, description)
SELECT 'USER_WRITE', 'Create or modify users'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE permission_key = 'USER_WRITE');

INSERT INTO permissions (permission_key, description)
SELECT 'ROLE_ASSIGN', 'Assign roles to users'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE permission_key = 'ROLE_ASSIGN');

-- 2. Seed Roles
INSERT INTO roles (role_name, description)
SELECT 'ROLE_ADMIN', 'System Administrator'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'ROLE_ADMIN');

-- 3. Map Permissions to Roles
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_ADMIN'
  AND p.permission_key IN ('SYSTEM_READ', 'USER_WRITE', 'ROLE_ASSIGN')
  AND NOT EXISTS (
    SELECT 1 FROM role_permission rp
    WHERE rp.role_id = r.role_id AND rp.permission_id = p.permission_id
);

-- 4. Seed Initial User (Corrected Hash for 'noble_oath_2026')
INSERT INTO users (username, email, password_hash, is_active)
SELECT 'ranxom', 'admin@zentry.io', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgqCZ.67389.tE408Cj4xXbXG0S2', true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'ranxom');

-- 5. Map User to Role
INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u, roles r
WHERE u.username = 'ranxom' AND r.role_name = 'ROLE_ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur
    WHERE ur.user_id = u.user_id AND ur.role_id = r.role_id
);