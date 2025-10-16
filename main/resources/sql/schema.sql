-- filepath: main/resources/sql/schema.sql
-- Schema for Airport Flight Management System

CREATE DATABASE IF NOT EXISTS `flight_management` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `flight_management`;

-- Users table: passengers and admins
CREATE TABLE IF NOT EXISTS users (
  user_id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  password VARCHAR(500) NOT NULL,
  passport_number VARCHAR(50),
  contact_number VARCHAR(30),
  role ENUM('PASSENGER','ADMIN') NOT NULL DEFAULT 'PASSENGER'
) ENGINE=InnoDB;

-- Flights table
CREATE TABLE IF NOT EXISTS flights (
  flight_id INT PRIMARY KEY AUTO_INCREMENT,
  flight_number VARCHAR(50),
  flight_name VARCHAR(150),
  source VARCHAR(80),
  destination VARCHAR(80),
  departure_time DATETIME,
  arrival_time DATETIME,
  seat_capacity INT DEFAULT 100,
  available_seats INT DEFAULT 100,
  price DECIMAL(10,2) DEFAULT 0.00,
  status VARCHAR(20) DEFAULT 'SCHEDULED'
) ENGINE=InnoDB;

-- Bookings table
CREATE TABLE IF NOT EXISTS bookings (
  booking_id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  flight_id INT NOT NULL,
  booking_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(20) DEFAULT 'CONFIRMED',
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  FOREIGN KEY (flight_id) REFERENCES flights(flight_id) ON DELETE CASCADE
) ENGINE=InnoDB;

