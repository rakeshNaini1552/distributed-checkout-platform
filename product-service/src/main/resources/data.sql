INSERT INTO products (name, description, price, category, image_url)
SELECT 'Laptop', 'High performance laptop for professionals', 1299.99, 'Electronics', 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Laptop');

INSERT INTO products (name, description, price, category, image_url)
SELECT 'Phone', 'Latest smartphone with advanced camera', 799.99, 'Electronics', 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Phone');

INSERT INTO products (name, description, price, category, image_url)
SELECT 'Headphones', 'Noise cancelling wireless headphones', 299.99, 'Electronics', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Headphones');

INSERT INTO products (name, description, price, category, image_url)
SELECT 'Monitor', '4K Ultra HD 27 inch display', 499.99, 'Electronics', 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=400'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Monitor');

INSERT INTO products (name, description, price, category, image_url)
SELECT 'Keyboard', 'Mechanical keyboard with RGB lighting', 129.99, 'Accessories', 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=400'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Keyboard');

INSERT INTO products (name, description, price, category, image_url)
SELECT 'Mouse', 'Ergonomic wireless mouse', 59.99, 'Accessories', 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=400'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Mouse');

INSERT INTO products (name, description, price, category, image_url)
SELECT 'Webcam', 'Full HD 1080p webcam with built-in mic', 89.99, 'Accessories', 'https://images.unsplash.com/photo-1587825140708-dfaf72ae4b04?w=400'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Webcam');

INSERT INTO products (name, description, price, category, image_url)
SELECT 'Desk Chair', 'Ergonomic office chair with lumbar support', 399.99, 'Furniture', 'https://images.unsplash.com/photo-1589384267710-7a170981ca78?w=400'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Desk Chair');

INSERT INTO products (name, description, price, category, image_url)
SELECT 'USB Hub', '7-port USB 3.0 hub with power adapter', 39.99, 'Accessories', 'https://images.unsplash.com/photo-1625723044792-44de16ccb4e9?w=400'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'USB Hub');

INSERT INTO products (name, description, price, category, image_url)
SELECT 'SSD Drive', '1TB NVMe SSD for fast storage', 109.99, 'Storage', 'https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?w=400'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'SSD Drive');

INSERT INTO products (name, description, price, category, image_url)
SELECT 'Graphics Card', 'High performance GPU for gaming and ML', 899.99, 'Electronics', 'https://images.unsplash.com/photo-1591488320449-011701bb6704?w=400'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Graphics Card');