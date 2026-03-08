-- Sample data for testing JWT authentication and user-specific sales
-- This file can be placed in src/main/resources/ as data.sql or imported manually

-- Create sample store stands (if not exists)
INSERT INTO store_stands (id, city, mall_name, storage_capacity)
VALUES
  (1, 'BUCURESTI', 'Plaza Mall Downtown', 5000),
  (2, 'BUCURESTI', 'AFI Cotroceni', 4500),
  (3, 'CONSTANTA', 'Constanta City Center', 3000)
ON CONFLICT (id) DO NOTHING;

-- Create sample users with BCrypt hashed passwords
-- Password for all users: password123
-- BCrypt hash: $2a$10$slYQmyNdGzin7olVyeIS/OPST9/PgBkqquzi.Ss72gAZS98/LzUm2
INSERT INTO app_users (id, username, email, password, roles, enabled, store_stand_id)
VALUES
  (1, 'employee_plaza', 'employee.plaza@example.com', '$2a$10$slYQmyNdGzin7olVyeIS/OPST9/PgBkqquzi.Ss72gAZS98/LzUm2', 'ROLE_EMPLOYEE', true, 1),
  (2, 'employee_afi', 'employee.afi@example.com', '$2a$10$slYQmyNdGzin7olVyeIS/OPST9/PgBkqquzi.Ss72gAZS98/LzUm2', 'ROLE_EMPLOYEE', true, 2),
  (3, 'employee_constanta', 'employee.constanta@example.com', '$2a$10$slYQmyNdGzin7olVyeIS/OPST9/PgBkqquzi.Ss72gAZS98/LzUm2', 'ROLE_EMPLOYEE', true, 3),
  (4, 'admin_user', 'admin@example.com', '$2a$10$slYQmyNdGzin7olVyeIS/OPST9/PgBkqquzi.Ss72gAZS98/LzUm2', 'ROLE_ADMIN', true, 1)
ON CONFLICT (id) DO NOTHING;

-- Sample products
INSERT INTO products (id, sku, category, name, brand, model, color, price)
VALUES
  (1, 'CASE-APPLE-IPHONE15-RED', 'PHONE_CASE', 'iPhone 15 Red Case', 'APPLE', 'IPHONE15', 'RED', 29.99),
  (2, 'CASE-APPLE-IPHONE15-BLACK', 'PHONE_CASE', 'iPhone 15 Black Case', 'APPLE', 'IPHONE15', 'BLACK', 29.99),
  (3, 'SCREEN-APPLE-IPHONE15-CLEAR', 'SCREEN_PROTECTOR', 'iPhone 15 Clear Screen Protector', 'APPLE', 'IPHONE15', 'CLEAR', 9.99),
  (4, 'CHARGER-APPLE-IPHONE-WHITE', 'CHARGER', 'Apple USB-C Charger', 'APPLE', 'GENERIC', 'WHITE', 19.99),
  (5, 'CABLE-APPLE-IPHONE-WHITE', 'CABLE', 'Apple USB-C Cable', 'APPLE', 'GENERIC', 'WHITE', 14.99)
ON CONFLICT (id) DO NOTHING;

-- Sample inventory for Plaza Mall store stand
INSERT INTO inventory (id, product_id, stand_id, quantity, arrival_date)
VALUES
  (1, 1, 1, 100, '2026-02-01'),
  (2, 2, 1, 85, '2026-02-05'),
  (3, 3, 1, 150, '2026-02-10'),
  (4, 4, 1, 60, '2026-02-15'),
  (5, 5, 1, 75, '2026-02-20')
ON CONFLICT (id) DO NOTHING;

-- Sample inventory for AFI Cotroceni store stand
INSERT INTO inventory (id, product_id, stand_id, quantity, arrival_date)
VALUES
  (6, 1, 2, 120, '2026-02-01'),
  (7, 2, 2, 95, '2026-02-05'),
  (8, 3, 2, 160, '2026-02-10'),
  (9, 4, 2, 70, '2026-02-15'),
  (10, 5, 2, 85, '2026-02-20')
ON CONFLICT (id) DO NOTHING;

-- Sample inventory for Constanta store stand
INSERT INTO inventory (id, product_id, stand_id, quantity, arrival_date)
VALUES
  (11, 1, 3, 80, '2026-02-01'),
  (12, 2, 3, 65, '2026-02-05'),
  (13, 3, 3, 110, '2026-02-10'),
  (14, 4, 3, 45, '2026-02-15'),
  (15, 5, 3, 55, '2026-02-20')
ON CONFLICT (id) DO NOTHING;

-- Sample sales data
INSERT INTO sales (id, product_id, stand_id, user_id, quantity_sold, sale_date)
VALUES
  (1, 1, 1, 1, 5, '2026-03-01'),
  (2, 2, 1, 1, 3, '2026-03-02'),
  (3, 4, 1, 1, 2, '2026-03-03'),
  (4, 1, 2, 2, 4, '2026-03-01'),
  (5, 3, 2, 2, 10, '2026-03-04'),
  (6, 5, 3, 3, 6, '2026-03-02')
ON CONFLICT (id) DO NOTHING;
