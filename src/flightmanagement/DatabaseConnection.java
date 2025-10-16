package flightmanagement;
import java.sql.*;

public class DatabaseConnection {
    // Change these if your MySQL username or password differ
    // Defaults — can be overridden with environment variables:
    // FLIGHT_DB_URL, FLIGHT_DB_USER, FLIGHT_DB_PASSWORD
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/flight_management?serverTimezone=UTC&useSSL=false";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "admin123";

    private static final String URL = System.getenv().getOrDefault("FLIGHT_DB_URL", DEFAULT_URL);
    private static final String USER = System.getenv().getOrDefault("FLIGHT_DB_USER", DEFAULT_USER);
    private static final String PASSWORD = System.getenv().getOrDefault("FLIGHT_DB_PASSWORD", DEFAULT_PASSWORD);

    private static DatabaseConnection instance;

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL JDBC driver not found: " + e.getMessage());
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // Backwards compatible static method used in existing code
    public static Connection getConnection() {
        try {
            Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
            // Print a short success message (avoiding sensitive info)
            System.out.println(ConsoleColors.GREEN + "🔗 Connected to MySQL at " + URL + ConsoleColors.RESET);
            return con;
        } catch (SQLException e) {
            System.out.println(ConsoleColors.RED + "❌ Database connection failed: " + e.getMessage() + ConsoleColors.RESET);
            return null;
        }
    }

    // Optional convenience methods
    public static void closeQuietly(AutoCloseable ac) {
        if (ac == null) return;
        try { ac.close(); } catch (Exception ignored) {}
    }
}
