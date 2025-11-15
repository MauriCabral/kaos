-- =============================
-- Schema and tables
-- =============================

-- Table stores
CREATE TABLE IF NOT EXISTS stores (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Table users
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    store_id INT REFERENCES stores(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Other tables...