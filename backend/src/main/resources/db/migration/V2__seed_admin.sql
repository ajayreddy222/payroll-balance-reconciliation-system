INSERT INTO users(full_name, email, password_hash, role)
VALUES ('Administrator', 'admin@example.com', '$2a$10$3xNbRl8MeVrKmNsF0eLW8.zvLxib.CjPSRvh8tCiIxJaUQVv3YjCe', 'ROLE_ADMIN')
ON CONFLICT (email) DO NOTHING;
