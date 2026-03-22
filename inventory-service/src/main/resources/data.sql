INSERT INTO inventory (item_name, quantity, updated_at)
SELECT 'Laptop', 50, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM inventory WHERE item_name = 'Laptop');

INSERT INTO inventory (item_name, quantity, updated_at)
SELECT 'Phone', 200, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM inventory WHERE item_name = 'Phone');

INSERT INTO inventory (item_name, quantity, updated_at)
SELECT 'Headphones', 75, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM inventory WHERE item_name = 'Headphones');

INSERT INTO inventory (item_name, quantity, updated_at)
SELECT 'Monitor', 30, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM inventory WHERE item_name = 'Monitor');

INSERT INTO inventory (item_name, quantity, updated_at)
SELECT 'Keyboard', 120, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM inventory WHERE item_name = 'Keyboard');

INSERT INTO inventory (item_name, quantity, updated_at)
SELECT 'Mouse', 150, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM inventory WHERE item_name = 'Mouse');

INSERT INTO inventory (item_name, quantity, updated_at)
SELECT 'Webcam', 40, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM inventory WHERE item_name = 'Webcam');

INSERT INTO inventory (item_name, quantity, updated_at)
SELECT 'Desk Chair', 20, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM inventory WHERE item_name = 'Desk Chair');

INSERT INTO inventory (item_name, quantity, updated_at)
SELECT 'USB Hub', 90, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM inventory WHERE item_name = 'USB Hub');