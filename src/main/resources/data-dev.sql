-- ============================================
-- Sample data for H2 dev profile
-- Uses MERGE INTO instead of ON CONFLICT
-- ============================================

-- Store stands
MERGE INTO store_stands (id, city, mall_name, storage_capacity) KEY(id) VALUES
  (1, 'BUCURESTI', 'Plaza Mall Downtown', 5000);
MERGE INTO store_stands (id, city, mall_name, storage_capacity) KEY(id) VALUES
  (2, 'BUCURESTI', 'AFI Cotroceni', 4500);
MERGE INTO store_stands (id, city, mall_name, storage_capacity) KEY(id) VALUES
  (3, 'CONSTANTA', 'Constanta City Center', 3000);

-- Users (password: password123)
MERGE INTO app_users (id, username, email, password, roles, enabled, store_stand_id) KEY(id) VALUES
  (1, 'employee_plaza', 'employee.plaza@example.com', '$2a$10$slYQmyNdGzin7olVyeIS/OPST9/PgBkqquzi.Ss72gAZS98/LzUm2', 'ROLE_EMPLOYEE', true, 1);
MERGE INTO app_users (id, username, email, password, roles, enabled, store_stand_id) KEY(id) VALUES
  (2, 'employee_afi', 'employee.afi@example.com', '$2a$10$slYQmyNdGzin7olVyeIS/OPST9/PgBkqquzi.Ss72gAZS98/LzUm2', 'ROLE_EMPLOYEE', true, 2);
MERGE INTO app_users (id, username, email, password, roles, enabled, store_stand_id) KEY(id) VALUES
  (3, 'employee_constanta', 'employee.constanta@example.com', '$2a$10$slYQmyNdGzin7olVyeIS/OPST9/PgBkqquzi.Ss72gAZS98/LzUm2', 'ROLE_EMPLOYEE', true, 3);
MERGE INTO app_users (id, username, email, password, roles, enabled, store_stand_id) KEY(id) VALUES
  (4, 'admin_user', 'admin@example.com', '$2a$10$slYQmyNdGzin7olVyeIS/OPST9/PgBkqquzi.Ss72gAZS98/LzUm2', 'ROLE_ADMIN', true, NULL);

-- Products
MERGE INTO products (id, sku, category, name, brand, model, color, price) KEY(id) VALUES
  (1, 'CASE-APPLE-IPHONE15-RED', 'CASE', 'iPhone 15 Red Case', 'APPLE', 'IPHONE15', 'RED', 29.99);
MERGE INTO products (id, sku, category, name, brand, model, color, price) KEY(id) VALUES
  (2, 'CASE-APPLE-IPHONE15-BLACK', 'CASE', 'iPhone 15 Black Case', 'APPLE', 'IPHONE15', 'BLACK', 29.99);
MERGE INTO products (id, sku, category, name, brand, model, color, price) KEY(id) VALUES
  (3, 'SCREEN_PROTECTOR-APPLE-IPHONE15-CLEAR', 'SCREEN_PROTECTOR', 'iPhone 15 Clear Screen Protector', 'APPLE', 'IPHONE15', 'CLEAR', 9.99);
MERGE INTO products (id, sku, category, name, brand, model, color, price) KEY(id) VALUES
  (4, 'CHARGER-APPLE-IPHONE-WHITE', 'CHARGER', 'Apple USB-C Charger', 'APPLE', 'GENERIC', 'WHITE', 19.99);
MERGE INTO products (id, sku, category, name, brand, model, color, price) KEY(id) VALUES
  (5, 'CABLE-APPLE-IPHONE-WHITE', 'CABLE', 'Apple USB-C Cable', 'APPLE', 'GENERIC', 'WHITE', 14.99);

-- Inventory for Plaza Mall
MERGE INTO inventory (id, product_id, stand_id, quantity, arrival_date) KEY(id) VALUES (1, 1, 1, 100, '2026-02-01');
MERGE INTO inventory (id, product_id, stand_id, quantity, arrival_date) KEY(id) VALUES (2, 2, 1, 85, '2026-02-05');
MERGE INTO inventory (id, product_id, stand_id, quantity, arrival_date) KEY(id) VALUES (3, 3, 1, 150, '2026-02-10');
MERGE INTO inventory (id, product_id, stand_id, quantity, arrival_date) KEY(id) VALUES (4, 4, 1, 60, '2026-02-15');
MERGE INTO inventory (id, product_id, stand_id, quantity, arrival_date) KEY(id) VALUES (5, 5, 1, 75, '2026-02-20');

-- Inventory for AFI Cotroceni
MERGE INTO inventory (id, product_id, stand_id, quantity, arrival_date) KEY(id) VALUES (6, 1, 2, 120, '2026-02-01');
MERGE INTO inventory (id, product_id, stand_id, quantity, arrival_date) KEY(id) VALUES (7, 2, 2, 95, '2026-02-05');
MERGE INTO inventory (id, product_id, stand_id, quantity, arrival_date) KEY(id) VALUES (8, 3, 2, 160, '2026-02-10');
MERGE INTO inventory (id, product_id, stand_id, quantity, arrival_date) KEY(id) VALUES (9, 4, 2, 70, '2026-02-15');
MERGE INTO inventory (id, product_id, stand_id, quantity, arrival_date) KEY(id) VALUES (10, 5, 2, 85, '2026-02-20');

-- Inventory for Constanta
MERGE INTO inventory (id, product_id, stand_id, quantity, arrival_date) KEY(id) VALUES (11, 1, 3, 80, '2026-02-01');
MERGE INTO inventory (id, product_id, stand_id, quantity, arrival_date) KEY(id) VALUES (12, 2, 3, 65, '2026-02-05');
MERGE INTO inventory (id, product_id, stand_id, quantity, arrival_date) KEY(id) VALUES (13, 3, 3, 110, '2026-02-10');
MERGE INTO inventory (id, product_id, stand_id, quantity, arrival_date) KEY(id) VALUES (14, 4, 3, 45, '2026-02-15');
MERGE INTO inventory (id, product_id, stand_id, quantity, arrival_date) KEY(id) VALUES (15, 5, 3, 55, '2026-02-20');

-- Sales data
MERGE INTO sales (id, product_id, stand_id, user_id, quantity_sold, sale_date) KEY(id) VALUES (1, 1, 1, 1, 5, '2026-03-01');
MERGE INTO sales (id, product_id, stand_id, user_id, quantity_sold, sale_date) KEY(id) VALUES (2, 2, 1, 1, 3, '2026-03-02');
MERGE INTO sales (id, product_id, stand_id, user_id, quantity_sold, sale_date) KEY(id) VALUES (3, 4, 1, 1, 2, '2026-03-03');
MERGE INTO sales (id, product_id, stand_id, user_id, quantity_sold, sale_date) KEY(id) VALUES (4, 1, 2, 2, 4, '2026-03-01');
MERGE INTO sales (id, product_id, stand_id, user_id, quantity_sold, sale_date) KEY(id) VALUES (5, 3, 2, 2, 10, '2026-03-04');
MERGE INTO sales (id, product_id, stand_id, user_id, quantity_sold, sale_date) KEY(id) VALUES (6, 5, 3, 3, 6, '2026-03-02');
