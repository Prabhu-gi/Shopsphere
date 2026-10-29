-- MySQL Initialization script for ShopSphere
CREATE DATABASE IF NOT EXISTS shopsphere_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS shopsphere_test_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON shopsphere_db.* TO 'shopsphere'@'%';
GRANT ALL PRIVILEGES ON shopsphere_test_db.* TO 'shopsphere'@'%';
FLUSH PRIVILEGES;
