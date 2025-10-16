package flightmanagement;

import java.sql.*;
import java.util.List;

public class Passenger extends User {
    // password used only during registration/login flows
    public Passenger(String name, String email, String password, String passportNumber, String contactNumber) {
        super();
        this.name = name;
        this.email = email;
        this.password = password;
        this.passportNumber = passportNumber;
        this.contactNumber = contactNumber;
        this.role = "PASSENGER";
    }

    public Passenger(int userId, String name, String email, String passportNumber, String contactNumber) {
        super(userId, name, email, passportNumber, contactNumber, "PASSENGER");
    }

    // Registers this passenger into the `users` table
    public boolean registerUser() {
        String sql = "INSERT INTO users (name, email, password, passport_number, contact_number, role) VALUES (?, ?, ?, ?, ?, 'PASSENGER')";
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con == null) {
                System.out.println(ConsoleColors.RED + "❌ Cannot register user: no DB connection" + ConsoleColors.RESET);
                return false;
            }
            String hashed = PasswordUtil.hashPassword(this.password);
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, this.name);
                ps.setString(2, this.email);
                ps.setString(3, hashed);
                ps.setString(4, this.passportNumber);
                ps.setString(5, this.contactNumber);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) this.userId = keys.getInt(1);
                }
                System.out.println(ConsoleColors.GREEN + "✅ Registration successful! Welcome aboard, " + this.name + "!" + ConsoleColors.RESET);
                return true;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println(ConsoleColors.YELLOW + "⚠️ Email already registered: " + this.email + ConsoleColors.RESET);
            return false;
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "❌ Registration failed: " + e.getMessage() + ConsoleColors.RESET);
            return false;
        }
    }

    // Authenticate a passenger. Returns Passenger object if successful, otherwise null.
    public static Passenger loginUser(String email, String password) {
        String sql = "SELECT user_id, name, email, password, passport_number, contact_number FROM users WHERE email = ? AND role = 'PASSENGER'";
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con == null) {
                System.out.println(ConsoleColors.RED + "❌ Cannot login: no DB connection" + ConsoleColors.RESET);
                return null;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String stored = rs.getString("password");
                        boolean ok = false;
                        if (stored != null && stored.startsWith("PBKDF2$")) {
                            ok = PasswordUtil.verifyPassword(stored, password);
                        } else {
                            ok = stored != null && stored.equals(password);
                        }
                        if (ok) {
                            Passenger p = new Passenger(rs.getInt("user_id"), rs.getString("name"), rs.getString("email"), rs.getString("passport_number"), rs.getString("contact_number"));
                            System.out.println(ConsoleColors.GREEN + "✅ Login successful! Welcome back, " + p.getName() + "!" + ConsoleColors.RESET);
                            return p;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "❌ Login failed: " + e.getMessage() + ConsoleColors.RESET);
        }
        System.out.println(ConsoleColors.RED + "❌ Invalid email or password." + ConsoleColors.RESET);
        return null;
    }

    public void viewAvailableFlights() {
        BookingService service = new BookingService();
        service.viewFlights();
    }

    public boolean bookFlight(int flightId) {
        BookingService service = new BookingService();
        return service.createBooking(this.userId, flightId);
    }

    public boolean cancelBooking(int bookingId) {
        BookingService service = new BookingService();
        return service.cancelBooking(bookingId, this.userId);
    }

    public List<Booking> viewMyBookings() {
        BookingService service = new BookingService();
        return service.getBookingsByUser(this.userId);
    }
}
