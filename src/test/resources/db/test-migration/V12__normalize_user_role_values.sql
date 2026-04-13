UPDATE users
SET role = 'USER'
WHERE UPPER(TRIM(role)) = 'ROLE_USER';

UPDATE users
SET role = 'ADMIN'
WHERE UPPER(TRIM(role)) = 'ROLE_ADMIN';

UPDATE users
SET role = 'USER'
WHERE role IS NULL OR TRIM(role) = '';
