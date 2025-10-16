# Airport Flight Management System (Terminal)

A terminal-based Airport Flight Ticket Management System implemented in Java with MySQL persistence (JDBC).

This repository contains:
- Java sources: `src/flightmanagement` (Passenger, Admin, Flight, Booking, BookingService, DatabaseConnection, PasswordUtil, Main, etc.)
- SQL schema: `main/resources/sql/schema.sql`
- MySQL JDBC driver: `lib/mysql-connector-j-9.4.0.jar`

This README covers quick setup, build, run and troubleshooting on Windows (cmd). For a user-focused guide see `USER_MANUAL.md`.

---

## Prerequisites
- Java JDK 11+ installed and `javac`/`java` on PATH
- MySQL server (local or remote)
- `lib/mysql-connector-j-9.4.0.jar` is provided in the `lib/` folder

## Environment variables (optional)
The app reads DB connection overrides from environment variables:
- `FLIGHT_DB_URL` (default: `jdbc:mysql://localhost:3306/flight_management?serverTimezone=UTC&useSSL=false`)
- `FLIGHT_DB_USER` (default: `root`)
- `FLIGHT_DB_PASSWORD` (default: `admin123`)

Set them in Windows cmd if needed:

```cmd
set FLIGHT_DB_URL=jdbc:mysql://localhost:3306/flight_management?serverTimezone=UTC&useSSL=false
set FLIGHT_DB_USER=root
set FLIGHT_DB_PASSWORD=your_password
```

## Apply the database schema
Create the database and tables by applying the provided SQL schema file.

```cmd
rem Run from repo root
mysql -u root -p < "main\resources\sql\schema.sql"
```

If you prefer to open an interactive MySQL shell first:

```cmd
mysql -u root -p
USE mysql; -- optional
SOURCE main\resources\sql\schema.sql;
```

## (Optional) Create a default admin user quickly
If you want an admin account for first-run operations, insert a row (change email/password as needed):

```sql
USE flight_management;
INSERT INTO users (name, email, password, role) VALUES ('System Admin', 'admin@flight.com', 'admin123', 'ADMIN');
```

Note: The application supports PBKDF2 hashed passwords for security. Changing the admin password from within the Admin menu will store a hashed password.

## Build & Run (Windows cmd)
1. Compile sources:

```cmd
javac -cp lib\mysql-connector-j-9.4.0.jar -d out\classes src\flightmanagement\*.java
```

2. Run the app (recommended to enable UTF-8 in the console for emojis):

```cmd
chcp 65001
java -cp out\classes;lib\mysql-connector-j-9.4.0.jar flightmanagement.Main
```

If `chcp 65001` is not available or emojis render as question marks, try Windows Terminal or PowerShell with a Unicode-capable font.

## Quick usage
- From the main menu you can Login or Register.
- Choose role (Passenger or Admin) when logging in.
- Passenger flow: view flights, book, view/cancel bookings.
- Admin flow: add/update/cancel flights, view bookings and passengers, generate reports, change password.

## Troubleshooting
- "Database connection failed" — ensure MySQL is running and credentials (env vars or defaults) are correct.
- JDBC driver errors — confirm `lib\mysql-connector-j-9.4.0.jar` is present and on the classpath when compiling and running.
- Emoji/Unicode display — set console code page to UTF-8: `chcp 65001`, or use Windows Terminal / PowerShell with a UTF-8 font.

## Next recommended steps
- Add JUnit tests for `PasswordUtil` and `BookingService`.
- Add a small bootstrap that creates a default admin (hashed) if no admin exists.
- Improve input validation and add pagination for long lists.

---

For step-by-step user instructions, commands and menu walkthroughs see `USER_MANUAL.md`.

