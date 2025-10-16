# User Manual — Airport Flight Management System (Terminal)

This manual walks you through installing, configuring, and using the Airport Flight Management System from the command line (Windows cmd). It assumes you have already applied the SQL schema from `main/resources/sql/schema.sql`.

Contents
- Quick start (DB + compile + run)
- Main menu walkthrough
- Passenger flows
- Admin flows
- Troubleshooting and tips

---

Quick start (Windows cmd)
1. Ensure MySQL server is running and you can connect with your user/password.
2. Apply the schema:

```cmd
mysql -u root -p < "main\resources\sql\schema.sql"
```

3. (Optional) Create an admin quickly if you didn't insert one during schema creation:

```sql
USE flight_management;
INSERT INTO users (name, email, password, role) VALUES ('System Admin', 'admin@flight.com', 'admin123', 'ADMIN');
```

4. Compile Java sources (from repo root):

```cmd
javac -cp lib\mysql-connector-j-9.4.0.jar -d out\classes src\flightmanagement\*.java
```

5. Run the application (enable UTF-8 for nicer emoji support):

```cmd
chcp 65001
java -cp out\classes;lib\mysql-connector-j-9.4.0.jar flightmanagement.Main
```

Notes: If you get a DB connection error, verify the `FLIGHT_DB_*` environment variables or check the defaults in `DatabaseConnection.java`.

---

Main menu walkthrough

When the app starts you will see options:

1) Login — choose role (Passenger or Admin) and provide email and password.
2) Register — create a new Passenger account.
3) Exit — close the application.

Passenger flows

- Register (from main menu): supply Name, Email, Password, Passport Number, Contact Number.
  - On success you will see a green confirmation message.
- Login (choose Passenger): provide your email and password.
- Passenger Dashboard options:
  1) View Flights — lists all scheduled flights with IDs, route, departure, price, available seats.
  2) Book Flight — after viewing flights, type a Flight ID to reserve a seat.
     - Bookings are transactional; the system verifies seats and decrements available seats.
  3) View My Bookings — lists your bookings (Booking ID, Flight ID, date, status).
  4) Cancel Booking — supply the Booking ID to cancel (only permitted for the booking owner).
  5) Logout — return to main menu.

Admin flows

- Login (choose Admin): supply admin email and password.
- Admin Dashboard options:
  1) Add Flight — input flight number, name, source, destination, departure and arrival times (YYYY-MM-DD HH:MM:SS), price, seat capacity.
  2) Update Flight — update an existing flight by Flight ID, change fields including status.
  3) Cancel Flight — mark a flight as CANCELLED.
  4) View All Bookings — lists all bookings in the system.
  5) View Passengers — lists registered passengers (user id, name, email).
  6) Generate Reports — prints total flights, passengers, bookings counts.
  7) Change Password — change the admin's password (stored securely with PBKDF2).
  8) Logout — return to main menu.

Important business rules
- A booking reduces `available_seats` by 1; cancellation increments it back by 1.
- Bookings can only be made for flights with status `SCHEDULED` and available seats > 0.
- Deleting flights or users is not implemented in the UI; DB cascade rules exist for referential integrity.

Troubleshooting and tips

- Emoji shows as ? or squares: set the console encoding to UTF-8 and use a suitable font.
  ```cmd
  chcp 65001
  ```
  Use Windows Terminal or PowerShell with a font like "Segoe UI Emoji" for best results.

- Database connection failed: verify MySQL is running and that `FLIGHT_DB_URL`, `FLIGHT_DB_USER`, and `FLIGHT_DB_PASSWORD` environment variables (if used) match your server. Defaults are:
  - URL: jdbc:mysql://localhost:3306/flight_management?serverTimezone=UTC&useSSL=false
  - USER: root
  - PASSWORD: admin123

- Password formats: the system uses PBKDF2 hashed passwords for security. If you insert a plaintext password via SQL for quick testing (as shown above), you should change it from the Admin menu after login to ensure it's stored hashed.

- Adding sample data: use the `INSERT` statements shown in the README to add sample passengers and flights.

Security & production notes
- Do not store plaintext passwords in production. Use the Admin change password menu to hash passwords.
- Consider running MySQL with restricted network access and use strong credentials.
- For automated tests, refactor `DatabaseConnection` to use a configurable data source and add a test database or use an in-memory DB like H2 for unit tests.

Support
If you run into issues or want me to add automated tests, a default admin provisioning routine, or CI pipeline instructions, tell me which and I'll add them next.

