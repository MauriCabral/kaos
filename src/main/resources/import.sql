-- =============================
-- Initial Data
-- =============================

-- Insert stores (CONFLICT por nombre único)
INSERT INTO stores (name) VALUES
('Despeñaderos'),
('San Agustin')
ON CONFLICT (name) DO NOTHING;

-- Insert variant types
INSERT INTO variant_types (name) VALUES
('SIMPLE'),
('DOBLE'),
('TRIPLE')
ON CONFLICT (name) DO NOTHING;

-- Insert users
INSERT INTO users (username, password, role, store_id) VALUES
('admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPjYfYf5Hf5K6', 'ADMIN', NULL),
('Despeñaderos', '$2a$12$abcdefghijklmnopqrstuvwxYz1234567890', 'LOCAL', 1),
('SanAgustin', '$2a$12$zyxwvutsrqponmlkjihgfedcba0987654321', 'LOCAL', 2)
ON CONFLICT (username) DO NOTHING;