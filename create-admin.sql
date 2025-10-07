-- Create admin user and update stock for Coffee
USE onestopuiu;

-- Create admin user (if doesn't exist)
INSERT IGNORE INTO users (username, password, email, role) 
VALUES ('admin', 'admin123', 'admin@onestopuiu.com', 'ADMIN');

-- Update Coffee stock to 10 units
UPDATE food_items SET stock_quantity = 10 WHERE id = 3 AND name = 'Coffee';

-- Update French Fries stock to 5 units (it's also at 0)
UPDATE food_items SET stock_quantity = 5 WHERE id = 2 AND name = 'French Fries';

-- Show updated stock levels
SELECT id, name, price, stock_quantity, available FROM food_items ORDER BY id;