-- Flight Management System Initial Data
-- This file populates the database with sample data

USE flight_management;

-- Insert default admin user
INSERT INTO admin (username, password, email) VALUES 
('admin', 'admin123', 'admin@flightmanagement.com'),
('manager', 'manager123', 'manager@flightmanagement.com');

-- Insert sample flights
INSERT INTO flights (flight_name, source, destination, departure_time, arrival_time, price) VALUES 
('AI-101', 'Mumbai', 'Delhi', '06:00', '08:30', 8500.00),
('AI-102', 'Delhi', 'Mumbai', '09:00', '11:30', 8500.00),
('SG-201', 'Bangalore', 'Chennai', '07:30', '09:00', 4200.00),
('SG-202', 'Chennai', 'Bangalore', '10:00', '11:30', 4200.00),
('6E-301', 'Kolkata', 'Pune', '08:00', '10:45', 6800.00),
('6E-302', 'Pune', 'Kolkata', '11:30', '14:15', 6800.00),
('G8-401', 'Hyderabad', 'Goa', '14:00', '15:30', 7200.00),
('G8-402', 'Goa', 'Hyderabad', '16:00', '17:30', 7200.00),
('AI-501', 'Mumbai', 'Bangalore', '12:00', '13:45', 5600.00),
('AI-502', 'Bangalore', 'Mumbai', '15:00', '16:45', 5600.00);

-- Insert sample passengers
INSERT INTO passengers (name, email, phone) VALUES 
('Rajesh Kumar', 'rajesh.kumar@email.com', '9876543210'),
('Priya Sharma', 'priya.sharma@email.com', '9876543211'),
('Amit Patel', 'amit.patel@email.com', '9876543212'),
('Sunita Singh', 'sunita.singh@email.com', '9876543213'),
('Vikram Reddy', 'vikram.reddy@email.com', '9876543214');

-- Insert sample bookings
INSERT INTO bookings (passenger_id, flight_id, booking_date, status) VALUES 
(1, 1, '2024-01-15', 'confirmed'),
(2, 3, '2024-01-16', 'confirmed'),
(3, 5, '2024-01-17', 'confirmed'),
(4, 2, '2024-01-18', 'confirmed'),
(5, 7, '2024-01-19', 'confirmed'),
(1, 9, '2024-01-20', 'confirmed'),
(2, 4, '2024-01-21', 'confirmed');

-- Display inserted data for verification
SELECT 'Admin Users:' as Table_Name;
SELECT username, email FROM admin;

SELECT 'Sample Flights:' as Table_Name;
SELECT flight_id, flight_name, source, destination, departure_time, arrival_time, price FROM flights;

SELECT 'Sample Passengers:' as Table_Name;
SELECT passenger_id, name, email, phone FROM passengers;

SELECT 'Sample Bookings:' as Table_Name;
SELECT b.booking_id, p.name as passenger_name, f.flight_name, b.booking_date, b.status
FROM bookings b
JOIN passengers p ON b.passenger_id = p.passenger_id
JOIN flights f ON b.flight_id = f.flight_id;
