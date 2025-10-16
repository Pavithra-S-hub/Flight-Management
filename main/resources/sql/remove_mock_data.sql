-- remove_mock_data.sql
-- Removes mock/sample data inserted for quick testing.
-- Safe to run multiple times (idempotent).

USE flight_management;

-- 1) Remove bookings linked to sample passengers or sample flights
DELETE b
FROM bookings b
LEFT JOIN users u ON b.user_id = u.user_id
LEFT JOIN flights f ON b.flight_id = f.flight_id
WHERE u.email IN ('alice@example.com')
   OR f.flight_number IN ('FM100', 'FM200');

-- 2) Remove sample flights
DELETE FROM flights WHERE flight_number IN ('FM100', 'FM200');

-- 3) Remove sample users (passenger and convenience admin)
DELETE FROM users WHERE email IN ('alice@example.com', 'admin@flightmanagement.com');

-- 4) Optionally reset AUTO_INCREMENT values to keep IDs compact (uncomment if desired)
-- ALTER TABLE bookings AUTO_INCREMENT = 1;
-- ALTER TABLE flights AUTO_INCREMENT = 1;
-- ALTER TABLE users AUTO_INCREMENT = 1;

-- 5) Show remaining counts for quick verification
SELECT 'users' AS table_name, COUNT(*) AS rows FROM users;
SELECT 'flights' AS table_name, COUNT(*) AS rows FROM flights;
SELECT 'bookings' AS table_name, COUNT(*) AS rows FROM bookings;

