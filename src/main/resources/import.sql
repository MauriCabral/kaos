-- =============================
-- Initial Data
-- =============================

-- Insert stores (CONFLICT por nombre único)
INSERT INTO stores (name, delivery_price) VALUES
('Despeñaderos', 0.00),
('San Agustin', 0.00)
ON CONFLICT (name) DO NOTHING;

-- Update existing stores to set delivery price if not already set
UPDATE stores SET delivery_price = 0.00 WHERE delivery_price IS NULL;

-- Insert variant types
INSERT INTO variant_types (name) VALUES
('SIMPLE'),
('DOBLE'),
('TRIPLE')
ON CONFLICT (name) DO NOTHING;

-- Insert users
INSERT INTO users (username, password, role, store_id) VALUES
('admin', 'admin123456', 'ADMIN', NULL),
('Despeñaderos', 'despe123', 'LOCAL', 1),
('SanAgustin', 'sanagustin123', 'LOCAL', 2)
ON CONFLICT (username) DO NOTHING;