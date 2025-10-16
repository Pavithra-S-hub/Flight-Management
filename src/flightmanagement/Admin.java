package flightmanagement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Admin extends User {

    public Admin() {
        this.role = "ADMIN";
    }

    public Admin(int userId, String name, String email, String passportNumber, String contactNumber) {
        super(userId, name, email, passportNumber, contactNumber, "ADMIN");
    }

    // Create an admin with default password 'admin' (hashed) — idempotent if email exists
    public static boolean createAdmin(String name, String email) {
        String checkSql = "SELECT COUNT(*) AS cnt FROM users WHERE email = ?";
        String insertSql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, 'ADMIN')";
        Connection con = DatabaseConnection.getConnection();
        if (con == null) {
            System.out.println(ConsoleColors.RED + "❌ Cannot create admin: no DB connection" + ConsoleColors.RESET);
            return false;
        }
        try (PreparedStatement ch = con.prepareStatement(checkSql)) {
            ch.setString(1, email);
            try (ResultSet rs = ch.executeQuery()) {
                if (rs.next() && rs.getInt("cnt") > 0) {
                    System.out.println(ConsoleColors.YELLOW + "⚠️ A user with that email already exists: " + email + ConsoleColors.RESET);
                    return false;
                }
            }

            String hashed = PasswordUtil.hashPassword("admin");
            try (PreparedStatement ins = con.prepareStatement(insertSql)) {
                ins.setString(1, name);
                ins.setString(2, email);
                ins.setString(3, hashed);
                ins.executeUpdate();
                System.out.println(ConsoleColors.GREEN + "✅ Admin created: " + email + " (initial password: admin). Please change it after first login." + ConsoleColors.RESET);
                return true;
            }
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "❌ Failed to create admin: " + e.getMessage() + ConsoleColors.RESET);
            return false;
        } finally {
            DatabaseConnection.closeQuietly(con);
        }
    }

    // Attempt to authenticate an admin; returns Admin instance or null
    public static Admin login(String email, String password) {
        String sql = "SELECT user_id, name, email, password, passport_number, contact_number FROM users WHERE email = ? AND role = 'ADMIN'";
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con == null) {
                System.out.println(ConsoleColors.RED + "❌ Cannot login as admin: no DB connection" + ConsoleColors.RESET);
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
                            Admin a = new Admin(rs.getInt("user_id"), rs.getString("name"), rs.getString("email"), rs.getString("passport_number"), rs.getString("contact_number"));
                            System.out.println(ConsoleColors.GREEN + "✅ Admin login successful! Hello, " + a.getName() + "!" + ConsoleColors.RESET);
                            return a;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "❌ Admin login failed: " + e.getMessage() + ConsoleColors.RESET);
        }
        System.out.println(ConsoleColors.RED + "❌ Invalid admin credentials." + ConsoleColors.RESET);
        return null;
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        String selectSql = "SELECT password FROM users WHERE user_id = ? AND role = 'ADMIN'";
        String updateSql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con == null) {
                System.out.println(ConsoleColors.RED + "❌ No DB connection for changePassword" + ConsoleColors.RESET);
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(selectSql)) {
                ps.setInt(1, this.userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String stored = rs.getString("password");
                        boolean ok = false;
                        if (stored != null && stored.startsWith("PBKDF2$")) {
                            ok = PasswordUtil.verifyPassword(stored, oldPassword);
                        } else {
                            ok = stored != null && stored.equals(oldPassword);
                        }
                        if (!ok) {
                            System.out.println(ConsoleColors.RED + "❌ Old password incorrect." + ConsoleColors.RESET);
                            return false;
                        }
                    } else {
                        System.out.println(ConsoleColors.RED + "❌ Admin user record not found." + ConsoleColors.RESET);
                        return false;
                    }
                }
            }

            String newHashed = PasswordUtil.hashPassword(newPassword);
            try (PreparedStatement ups = con.prepareStatement(updateSql)) {
                ups.setString(1, newHashed);
                ups.setInt(2, this.userId);
                ups.executeUpdate();
                System.out.println(ConsoleColors.GREEN + "✅ Password changed successfully." + ConsoleColors.RESET);
                return true;
            }
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "❌ Failed to change password: " + e.getMessage() + ConsoleColors.RESET);
            return false;
        }
    }

    // Admin operations delegating to BookingService
    public boolean addFlight(Flight f) {
        BookingService svc = new BookingService();
        return svc.addFlight(f);
    }

    public boolean updateFlight(Flight f) {
        BookingService svc = new BookingService();
        return svc.updateFlightDetails(f);
    }

    public boolean cancelFlight(int flightId) {
        BookingService svc = new BookingService();
        return svc.cancelFlight(flightId);
    }

    public List<Booking> viewAllBookings() {
        BookingService svc = new BookingService();
        return svc.getAllBookings();
    }

    public List<Passenger> viewPassengerList() {
        BookingService svc = new BookingService();
        return svc.getAllPassengers();
    }

    public void generateReports() {
        BookingService svc = new BookingService();
        svc.generateReport();
    }
}
