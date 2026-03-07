-- Seed Permissions
INSERT INTO permissions (permission_key, description)
SELECT 'SYSTEM_READ', 'View system metrics and logs'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE permission_key = 'SYSTEM_READ');

-- Repeat for USER_WRITE and ROLE_ASSIGN...

-- Seed Roles
INSERT INTO roles (role_name, description)
SELECT 'ROLE_ADMIN', 'System Administrator'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'ROLE_ADMIN');

-- Mapping (This is the important part)
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_ADMIN'
  AND p.permission_key IN ('SYSTEM_READ', 'USER_WRITE', 'ROLE_ASSIGN')
  AND NOT EXISTS (
    SELECT 1 FROM role_permission rp
    WHERE rp.role_id = r.role_id AND rp.permission_id = p.permission_id
);