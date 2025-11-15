-- =============================
-- Initial Data
-- =============================

-- Insert stores (CONFLICT por nombre único)
INSERT INTO stores (name) VALUES
('Despeñaderos'),
('San Agustin')
ON CONFLICT (name) DO NOTHING;

-- Insert users
INSERT INTO users (username, password, role, store_id) VALUES
('admin', 'admin123', 'ADMIN', NULL),
('Despeñaderos', 'Despeñaderos', 'LOCAL', 1),
('SanAgustin', 'SanAgustin', 'LOCAL', 2)
ON CONFLICT (username) DO NOTHING;