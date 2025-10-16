package flightmanagement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingService {

    // Add a flight to the database
    public boolean addFlight(Flight f) {
        String sql = "INSERT INTO flights (flight_number, flight_name, source, destination, departure_time, arrival_time, seat_capacity, available_seats, price, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection con = DatabaseConnection.getConnection();
        if (con == null) {
            System.out.println(ConsoleColors.RED + "❌ Failed to add flight: no DB connection" + ConsoleColors.RESET);
            return false;
        }
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, f.getFlightNumber());
            ps.setString(2, f.getName());
            ps.setString(3, f.getSource());
            ps.setString(4, f.getDestination());
            ps.setString(5, f.getDepartureTime());
            ps.setString(6, f.getArrivalTime());
            ps.setInt(7, f.getSeatCapacity());
            ps.setInt(8, f.getAvailableSeats());
            ps.setDouble(9, f.getPrice());
            ps.setString(10, f.getStatus());
            ps.executeUpdate();
            System.out.println(ConsoleColors.GREEN + "🛫 Flight added successfully!" + ConsoleColors.RESET);
            return true;
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "❌ Failed to add flight: " + e.getMessage() + ConsoleColors.RESET);
            return false;
        } finally {
            DatabaseConnection.closeQuietly(con);
        }
    }

    public boolean updateFlightDetails(Flight f) {
        String sql = "UPDATE flights SET flight_number = ?, flight_name = ?, source = ?, destination = ?, departure_time = ?, arrival_time = ?, seat_capacity = ?, available_seats = ?, price = ?, status = ? WHERE flight_id = ?";
        Connection con = DatabaseConnection.getConnection();
        if (con == null) {
            System.out.println(ConsoleColors.RED + "❌ Failed to update flight: no DB connection" + ConsoleColors.RESET);
            return false;
        }
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, f.getFlightNumber());
            ps.setString(2, f.getName());
            ps.setString(3, f.getSource());
            ps.setString(4, f.getDestination());
            ps.setString(5, f.getDepartureTime());
            ps.setString(6, f.getArrivalTime());
            ps.setInt(7, f.getSeatCapacity());
            ps.setInt(8, f.getAvailableSeats());
            ps.setDouble(9, f.getPrice());
            ps.setString(10, f.getStatus());
            ps.setInt(11, f.getFlightId());
            int updated = ps.executeUpdate();
            if (updated > 0) {
                System.out.println(ConsoleColors.YELLOW + "⚙️ Flight updated successfully." + ConsoleColors.RESET);
                return true;
            } else {
                System.out.println(ConsoleColors.RED + "❌ Flight not found." + ConsoleColors.RESET);
                return false;
            }
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "❌ Failed to update flight: " + e.getMessage() + ConsoleColors.RESET);
            return false;
        } finally {
            DatabaseConnection.closeQuietly(con);
        }
    }

    public boolean cancelFlight(int flightId) {
        String sql = "UPDATE flights SET status = 'CANCELLED' WHERE flight_id = ?";
        Connection con = DatabaseConnection.getConnection();
        if (con == null) {
            System.out.println(ConsoleColors.RED + "❌ Failed to cancel flight: no DB connection" + ConsoleColors.RESET);
            return false;
        }
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, flightId);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                System.out.println(ConsoleColors.YELLOW + "❌ Flight cancelled." + ConsoleColors.RESET);
                return true;
            }
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "❌ Failed to cancel flight: " + e.getMessage() + ConsoleColors.RESET);
        } finally {
            DatabaseConnection.closeQuietly(con);
        }
        return false;
    }

    // View flights
    public void viewFlights() {
        String sql = "SELECT flight_id, flight_number, flight_name, source, destination, departure_time, arrival_time, available_seats, price, status FROM flights WHERE status <> 'CANCELLED'";
        Connection con = DatabaseConnection.getConnection();
        if (con == null) {
            System.out.println(ConsoleColors.RED + "❌ Failed to fetch flights: no DB connection" + ConsoleColors.RESET);
            return;
        }
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            System.out.println(ConsoleColors.CYAN + "\n--- Available Flights ---" + ConsoleColors.RESET);
            while (rs.next()) {
                int id = rs.getInt("flight_id");
                String number = rs.getString("flight_number");
                String name = rs.getString("flight_name");
                String src = rs.getString("source");
                String dest = rs.getString("destination");
                String dep = rs.getString("departure_time");
                String price = String.valueOf(rs.getDouble("price"));
                int seats = rs.getInt("available_seats");
                String status = rs.getString("status");
                System.out.println(ConsoleColors.BLUE + id + " | " + (number == null ? name : number + " - " + name) + " | " + src + " -> " + dest + " | " + dep + " | ₹" + price + " | Seats: " + seats + " | " + status + ConsoleColors.RESET);
            }
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "❌ Failed to fetch flights: " + e.getMessage() + ConsoleColors.RESET);
        } finally {
            DatabaseConnection.closeQuietly(con);
        }
    }

    // Create booking with transactional seat decrement
    public boolean createBooking(int userId, int flightId) {
        String updateSeats = "UPDATE flights SET available_seats = available_seats - 1 WHERE flight_id = ?";
        String insertBooking = "INSERT INTO bookings (user_id, flight_id, booking_date, status) VALUES (?, ?, NOW(), 'CONFIRMED')";

        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            if (con == null) return false;
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement("SELECT available_seats, status FROM flights WHERE flight_id = ?")) {
                ps.setInt(1, flightId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println(ConsoleColors.RED + "❌ Flight not found." + ConsoleColors.RESET);
                        con.rollback();
                        return false;
                    }
                    int seats = rs.getInt("available_seats");
                    String status = rs.getString("status");
                    if (!"SCHEDULED".equalsIgnoreCase(status)) {
                        System.out.println(ConsoleColors.RED + "❌ Flight is not available for booking (status=" + status + ")." + ConsoleColors.RESET);
                        con.rollback();
                        return false;
                    }
                    if (seats <= 0) {
                        System.out.println(ConsoleColors.YELLOW + "⚠️ No seats available." + ConsoleColors.RESET);
                        con.rollback();
                        return false;
                    }
                }
            }

            try (PreparedStatement ups = con.prepareStatement(updateSeats)) {
                ups.setInt(1, flightId);
                ups.executeUpdate();
            }

            try (PreparedStatement ins = con.prepareStatement(insertBooking, Statement.RETURN_GENERATED_KEYS)) {
                ins.setInt(1, userId);
                ins.setInt(2, flightId);
                ins.executeUpdate();
                try (ResultSet keys = ins.getGeneratedKeys()) {
                    if (keys.next()) {
                        int bookingId = keys.getInt(1);
                        System.out.println(ConsoleColors.MAGENTA + "🎟️ Booking confirmed! ID: " + bookingId + ConsoleColors.RESET);
                    }
                }
            }

            con.commit();
            return true;
        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (Exception ignored) {}
            System.out.println(ConsoleColors.RED + "❌ Booking failed: " + e.getMessage() + ConsoleColors.RESET);
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (Exception ignored) {}
            DatabaseConnection.closeQuietly(con);
        }
    }

    // Cancel booking by user (ensure ownership)
    public boolean cancelBooking(int bookingId, int userId) {
        String selectSql = "SELECT booking_id, user_id, flight_id, status FROM bookings WHERE booking_id = ?";
        String updateBooking = "UPDATE bookings SET status = 'CANCELLED' WHERE booking_id = ?";
        String incrementSeat = "UPDATE flights SET available_seats = available_seats + 1 WHERE flight_id = ?";

        Connection con = DatabaseConnection.getConnection();
        if (con == null) {
            System.out.println(ConsoleColors.RED + "❌ Failed to cancel booking: no DB connection" + ConsoleColors.RESET);
            return false;
        }
        try {
            con.setAutoCommit(false);

            int flightId;
            try (PreparedStatement ps = con.prepareStatement(selectSql)) {
                ps.setInt(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println(ConsoleColors.RED + "❌ Booking not found." + ConsoleColors.RESET);
                        con.rollback();
                        return false;
                    }
                    int owner = rs.getInt("user_id");
                    if (owner != userId) {
                        System.out.println(ConsoleColors.RED + "❌ You are not authorized to cancel this booking." + ConsoleColors.RESET);
                        con.rollback();
                        return false;
                    }
                    String status = rs.getString("status");
                    flightId = rs.getInt("flight_id");
                    if ("CANCELLED".equalsIgnoreCase(status)) {
                        System.out.println(ConsoleColors.YELLOW + "⚠️ Booking already cancelled." + ConsoleColors.RESET);
                        con.rollback();
                        return false;
                    }
                }
            }

            try (PreparedStatement ups = con.prepareStatement(updateBooking)) {
                ups.setInt(1, bookingId);
                ups.executeUpdate();
            }

            try (PreparedStatement inc = con.prepareStatement(incrementSeat)) {
                inc.setInt(1, flightId);
                inc.executeUpdate();
            }

            con.commit();
            System.out.println(ConsoleColors.GREEN + "✅ Booking cancelled and seat released." + ConsoleColors.RESET);
            return true;
        } catch (Exception e) {
            try { con.rollback(); } catch (Exception ignored) {}
            System.out.println(ConsoleColors.RED + "❌ Failed to cancel booking: " + e.getMessage() + ConsoleColors.RESET);
            return false;
        } finally {
            try { con.setAutoCommit(true); } catch (Exception ignored) {}
            DatabaseConnection.closeQuietly(con);
        }
    }

    public List<Booking> getBookingsByUser(int userId) {
        List<Booking> out = new ArrayList<>();
        String sql = "SELECT booking_id, user_id, flight_id, booking_date, status FROM bookings WHERE user_id = ?";
        Connection con = DatabaseConnection.getConnection();
        if (con == null) {
            System.out.println(ConsoleColors.RED + "❌ Failed to fetch bookings: no DB connection" + ConsoleColors.RESET);
            return out;
        }
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = new Booking(rs.getInt("booking_id"), rs.getInt("user_id"), rs.getInt("flight_id"), rs.getString("booking_date"), rs.getString("status"));
                    out.add(b);
                }
            }
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "❌ Failed to fetch bookings: " + e.getMessage() + ConsoleColors.RESET);
        } finally {
            DatabaseConnection.closeQuietly(con);
        }
        return out;
    }

    public List<Booking> getAllBookings() {
        List<Booking> out = new ArrayList<>();
        String sql = "SELECT booking_id, user_id, flight_id, booking_date, status FROM bookings";
        Connection con = DatabaseConnection.getConnection();
        if (con == null) {
            System.out.println(ConsoleColors.RED + "❌ Failed to fetch all bookings: no DB connection" + ConsoleColors.RESET);
            return out;
        }
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Booking b = new Booking(rs.getInt("booking_id"), rs.getInt("user_id"), rs.getInt("flight_id"), rs.getString("booking_date"), rs.getString("status"));
                out.add(b);
            }
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "❌ Failed to fetch all bookings: " + e.getMessage() + ConsoleColors.RESET);
        } finally {
            DatabaseConnection.closeQuietly(con);
        }
        return out;
    }

    public List<Passenger> getAllPassengers() {
        List<Passenger> out = new ArrayList<>();
        String sql = "SELECT user_id, name, email, passport_number, contact_number FROM users WHERE role = 'PASSENGER'";
        Connection con = DatabaseConnection.getConnection();
        if (con == null) {
            System.out.println(ConsoleColors.RED + "❌ Failed to fetch passengers: no DB connection" + ConsoleColors.RESET);
            return out;
        }
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Passenger p = new Passenger(rs.getInt("user_id"), rs.getString("name"), rs.getString("email"), rs.getString("passport_number"), rs.getString("contact_number"));
                out.add(p);
            }
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "❌ Failed to fetch passengers: " + e.getMessage() + ConsoleColors.RESET);
        } finally {
            DatabaseConnection.closeQuietly(con);
        }
        return out;
    }

    public void generateReport() {
        String sqlFlights = "SELECT COUNT(*) AS cnt FROM flights";
        String sqlPassengers = "SELECT COUNT(*) AS cnt FROM users WHERE role = 'PASSENGER'";
        String sqlBookings = "SELECT COUNT(*) AS cnt FROM bookings";
        Connection con = DatabaseConnection.getConnection();
        if (con == null) {
            System.out.println(ConsoleColors.RED + "❌ Failed to generate report: no DB connection" + ConsoleColors.RESET);
            return;
        }
        try (Statement st = con.createStatement()) {
            try (ResultSet rf = st.executeQuery(sqlFlights)) { if (rf.next()) System.out.println(ConsoleColors.MAGENTA + "📊 Total Flights = " + rf.getInt("cnt") + ConsoleColors.RESET); }
            try (ResultSet ru = st.executeQuery(sqlPassengers)) { if (ru.next()) System.out.println(ConsoleColors.MAGENTA + "👥 Total Passengers = " + ru.getInt("cnt") + ConsoleColors.RESET); }
            try (ResultSet rb = st.executeQuery(sqlBookings)) { if (rb.next()) System.out.println(ConsoleColors.MAGENTA + "🧾 Total Bookings = " + rb.getInt("cnt") + ConsoleColors.RESET); }
            System.out.println(ConsoleColors.GREEN + "🧾 Report generated successfully!" + ConsoleColors.RESET);
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "❌ Failed to generate report: " + e.getMessage() + ConsoleColors.RESET);
        } finally {
            DatabaseConnection.closeQuietly(con);
        }
    }
}
