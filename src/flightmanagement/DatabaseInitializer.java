package flightmanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseInitializer {
    // Creates a default admin user if no admin exists. Idempotent and safe.
    public static void ensureDefaultAdmin() {
        final String defaultEmail = "admin@flightmanagement.com";
        final String defaultName = "System Admin";
        final String defaultPassword = "admin123"; // user should change on first login

        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            if (con == null) {
                System.out.println(ConsoleColors.YELLOW + "⚠️ Skipping default admin creation: no DB connection." + ConsoleColors.RESET);
                return;
            }

            String countSql = "SELECT COUNT(*) AS cnt FROM users WHERE role = 'ADMIN'";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(countSql)) {
                if (rs.next() && rs.getInt("cnt") > 0) {
                    // admin(s) already present
                    return;
                }
            }

            // No admin present — create default admin with hashed password
            String hashed = PasswordUtil.hashPassword(defaultPassword);
            String insertSql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, 'ADMIN')";
            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                ps.setString(1, defaultName);
                ps.setString(2, defaultEmail);
                ps.setString(3, hashed);
                ps.executeUpdate();
                System.out.println(ConsoleColors.GREEN + "ℹ️ Default admin created: " + defaultEmail + " (password: admin123). Please change it after first login." + ConsoleColors.RESET);
            }

        } catch (Exception e) {
            System.out.println(ConsoleColors.YELLOW + "⚠️ Could not ensure default admin: " + e.getMessage() + ConsoleColors.RESET);
        } finally {
            DatabaseConnection.closeQuietly(con);
        }
    }
}

