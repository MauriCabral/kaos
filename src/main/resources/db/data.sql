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
('admin', 'admin123456', 'ADMIN', 1),
('Despeñaderos', 'despe123', 'LOCAL', 1),
('SanAgustin', 'sanagustin123', 'LOCAL', 2)
ON CONFLICT (username) DO NOTHING;