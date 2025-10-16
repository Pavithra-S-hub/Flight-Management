-- seat_assignment.sql
-- Adds seat assignment support to the flight management DB.
-- Run this in MySQL Workbench or mysql CLI after applying schema.sql.

USE flight_management;

-- 1) Add seat_number column to bookings (if not present)
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS seat_number VARCHAR(10) DEFAULT NULL;

-- 2) Create flight_seats table to track each seat on every flight
CREATE TABLE IF NOT EXISTS flight_seats (
  seat_id INT PRIMARY KEY AUTO_INCREMENT,
  flight_id INT NOT NULL,
  seat_number VARCHAR(10) NOT NULL,
  seat_type ENUM('WINDOW','AISLE','MIDDLE') NOT NULL,
  is_available TINYINT(1) DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (flight_id) REFERENCES flights(flight_id) ON DELETE CASCADE,
  UNIQUE KEY ux_flight_seat (flight_id, seat_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) Stored procedure to generate seats for a given flight
-- Usage: CALL create_seats_for_flight(FLIGHT_ID, SEAT_CAPACITY);
DELIMITER $$
DROP PROCEDURE IF EXISTS create_seats_for_flight$$
CREATE PROCEDURE create_seats_for_flight(IN fid INT, IN capacity INT)
BEGIN
  DECLARE row INT DEFAULT 1;
  DECLARE col INT DEFAULT 0;
  DECLARE inserted INT DEFAULT 0;
  DECLARE seatLabel CHAR(3);
  DECLARE letters CHAR(6) DEFAULT 'ABCDEF';

  IF capacity <= 0 THEN
    LEAVE create_seats_for_flight;
  END IF;

  SET inserted = 0;
  SET row = 1;
  WHILE inserted < capacity DO
    SET col = 1;
    WHILE col <= 6 AND inserted < capacity DO
      SET seatLabel = CONCAT(row, SUBSTRING(letters, col, 1));
      -- Determine seat type: A/F WINDOW, C/D AISLE, B/E MIDDLE
      CASE SUBSTRING(letters, col, 1)
        WHEN 'A' THEN INSERT IGNORE INTO flight_seats(flight_id, seat_number, seat_type) VALUES (fid, seatLabel, 'WINDOW');
        WHEN 'F' THEN INSERT IGNORE INTO flight_seats(flight_id, seat_number, seat_type) VALUES (fid, seatLabel, 'WINDOW');
        WHEN 'C' THEN INSERT IGNORE INTO flight_seats(flight_id, seat_number, seat_type) VALUES (fid, seatLabel, 'AISLE');
        WHEN 'D' THEN INSERT IGNORE INTO flight_seats(flight_id, seat_number, seat_type) VALUES (fid, seatLabel, 'AISLE');
        WHEN 'B' THEN INSERT IGNORE INTO flight_seats(flight_id, seat_number, seat_type) VALUES (fid, seatLabel, 'MIDDLE');
        WHEN 'E' THEN INSERT IGNORE INTO flight_seats(flight_id, seat_number, seat_type) VALUES (fid, seatLabel, 'MIDDLE');
        ELSE INSERT IGNORE INTO flight_seats(flight_id, seat_number, seat_type) VALUES (fid, seatLabel, 'MIDDLE');
      END CASE;
      SET inserted = inserted + 1;
      SET col = col + 1;
    END WHILE;
    SET row = row + 1;
  END WHILE;
END$$
DELIMITER ;

-- 4) Example: generate seats for all existing flights using their seat_capacity
-- This will call the procedure for each flight. Uncomment to run.
-- Note: for large datasets you might want to run per-flight manually.

-- SELECT CONCAT('CALL create_seats_for_flight(', flight_id, ',', COALESCE(seat_capacity, 120), ');') FROM flights;

-- To generate seats for a specific flight, run:
-- CALL create_seats_for_flight(1, 120);

-- 5) Quick checks
SELECT COUNT(*) AS total_flight_seats FROM flight_seats;
SELECT COUNT(*) AS bookings_with_seatnum FROM bookings WHERE seat_number IS NOT NULL;

-- End of file

