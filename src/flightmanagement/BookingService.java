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
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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

            // Optionally create seats for this flight by calling stored procedure (if exists)
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int flightId = keys.getInt(1);
                    // Try to call create_seats_for_flight if present (safe to ignore errors)
                    try (Statement st = con.createStatement()) {
                        st.execute("CALL create_seats_for_flight(" + flightId + ", " + f.getSeatCapacity() + ");");
                    } catch (Exception ignored) {}
                }
            }

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

    // Create booking with transactional seat allocation, seat and food preferences
    public boolean createBooking(int userId, int flightId, String foodPreference, String seatPreference) {
        String selectFlightSql = "SELECT available_seats, status FROM flights WHERE flight_id = ?";
        String allocateSeatSqlPreferred = "SELECT seat_number FROM flight_seats WHERE flight_id = ? AND is_available = 1 AND seat_type = ? ORDER BY seat_id LIMIT 1 FOR UPDATE";
        String allocateSeatSqlAny = "SELECT seat_number FROM flight_seats WHERE flight_id = ? AND is_available = 1 ORDER BY seat_id LIMIT 1 FOR UPDATE";
        String markSeatTakenSql = "UPDATE flight_seats SET is_available = 0 WHERE flight_id = ? AND seat_number = ?";
        String decrementSeats = "UPDATE flights SET available_seats = available_seats - 1 WHERE flight_id = ?";
        String insertBooking = "INSERT INTO bookings (user_id, flight_id, booking_date, status, food_preference, seat_preference, seat_number) VALUES (?, ?, NOW(), 'CONFIRMED', ?, ?, ?)";

        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            if (con == null) return false;
            con.setAutoCommit(false);

            // Check flight availability
            try (PreparedStatement ps = con.prepareStatement(selectFlightSql)) {
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

            // Allocate seat: try preferred type first, otherwise any available seat
            String chosenSeat = null;
            if (seatPreference != null && !seatPreference.isBlank()) {
                try (PreparedStatement ps = con.prepareStatement(allocateSeatSqlPreferred)) {
                    ps.setInt(1, flightId);
                    ps.setString(2, seatPreference);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) chosenSeat = rs.getString("seat_number");
                    }
                }
            }
            if (chosenSeat == null) {
                try (PreparedStatement ps = con.prepareStatement(allocateSeatSqlAny)) {
                    ps.setInt(1, flightId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) chosenSeat = rs.getString("seat_number");
                    }
                }
            }

            if (chosenSeat == null) {
                System.out.println(ConsoleColors.YELLOW + "⚠️ No seat could be allocated." + ConsoleColors.RESET);
                con.rollback();
                return false;
            }

            // Mark seat as taken
            try (PreparedStatement ps = con.prepareStatement(markSeatTakenSql)) {
                ps.setInt(1, flightId);
                ps.setString(2, chosenSeat);
                ps.executeUpdate();
            }

            // Decrement available seats
            try (PreparedStatement ups = con.prepareStatement(decrementSeats)) {
                ups.setInt(1, flightId);
                ups.executeUpdate();
            }

            // Insert booking with seat_number
            try (PreparedStatement ins = con.prepareStatement(insertBooking, Statement.RETURN_GENERATED_KEYS)) {
                ins.setInt(1, userId);
                ins.setInt(2, flightId);
                ins.setString(3, foodPreference);
                ins.setString(4, seatPreference);
                ins.setString(5, chosenSeat);
                ins.executeUpdate();
                try (ResultSet keys = ins.getGeneratedKeys()) {
                    if (keys.next()) {
                        int bookingId = keys.getInt(1);
                        System.out.println(ConsoleColors.MAGENTA + "🎟️ Booking confirmed! ID: " + bookingId + " | Seat: " + chosenSeat + ConsoleColors.RESET);
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

    // Cancel booking by user (ensure ownership) and release seat if assigned
    public boolean cancelBooking(int bookingId, int userId) {
        String selectSql = "SELECT booking_id, user_id, flight_id, status, seat_number FROM bookings WHERE booking_id = ?";
        String updateBooking = "UPDATE bookings SET status = 'CANCELLED' WHERE booking_id = ?";
        String incrementSeat = "UPDATE flights SET available_seats = available_seats + 1 WHERE flight_id = ?";
        String releaseSeat = "UPDATE flight_seats SET is_available = 1 WHERE flight_id = ? AND seat_number = ?";

        Connection con = DatabaseConnection.getConnection();
        if (con == null) {
            System.out.println(ConsoleColors.RED + "❌ Failed to cancel booking: no DB connection" + ConsoleColors.RESET);
            return false;
        }
        try {
            con.setAutoCommit(false);

            int flightId;
            String currentSeat = null;
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
                    currentSeat = rs.getString("seat_number");
                    if ("CANCELLED".equalsIgnoreCase(status)) {
                        System.out.println(ConsoleColors.YELLOW + "⚠️ Booking already cancelled." + ConsoleColors.RESET);
                        con.rollback();
                        return false;
                    }
                }
            }

            // Mark booking cancelled
            try (PreparedStatement ups = con.prepareStatement(updateBooking)) {
                ups.setInt(1, bookingId);
                ups.executeUpdate();
            }

            // Increment available seats in flights
            try (PreparedStatement inc = con.prepareStatement(incrementSeat)) {
                inc.setInt(1, flightId);
                inc.executeUpdate();
            }

            // Release seat if one was assigned
            if (currentSeat != null) {
                try (PreparedStatement rel = con.prepareStatement(releaseSeat)) {
                    rel.setInt(1, flightId);
                    rel.setString(2, currentSeat);
                    rel.executeUpdate();
                }
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
        String sql = "SELECT b.booking_id, b.user_id, b.flight_id, b.booking_date, b.status, b.food_preference, b.seat_preference, b.seat_number, f.flight_number, f.flight_name, f.source, f.destination, f.departure_time, f.arrival_time, f.price FROM bookings b JOIN flights f ON b.flight_id = f.flight_id WHERE b.user_id = ?";
        Connection con = DatabaseConnection.getConnection();
        if (con == null) {
            System.out.println(ConsoleColors.RED + "❌ Failed to fetch bookings: no DB connection" + ConsoleColors.RESET);
            return out;
        }
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = new Booking(
                        rs.getInt("booking_id"),
                        rs.getInt("user_id"),
                        rs.getInt("flight_id"),
                        rs.getString("booking_date"),
                        rs.getString("status"),
                        rs.getString("food_preference"),
                        rs.getString("seat_preference"),
                        rs.getString("seat_number"),
                        rs.getString("flight_number"),
                        rs.getString("flight_name"),
                        rs.getString("source"),
                        rs.getString("destination"),
                        rs.getString("departure_time"),
                        rs.getString("arrival_time"),
                        rs.getDouble("price")
                    );
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
        String sql = "SELECT b.booking_id, b.user_id, b.flight_id, b.booking_date, b.status, b.food_preference, b.seat_preference, b.seat_number, f.flight_number, f.flight_name, f.source, f.destination, f.departure_time, f.arrival_time, f.price FROM bookings b JOIN flights f ON b.flight_id = f.flight_id";
        Connection con = DatabaseConnection.getConnection();
        if (con == null) {
            System.out.println(ConsoleColors.RED + "❌ Failed to fetch all bookings: no DB connection" + ConsoleColors.RESET);
            return out;
        }
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Booking b = new Booking(
                    rs.getInt("booking_id"),
                    rs.getInt("user_id"),
                    rs.getInt("flight_id"),
                    rs.getString("booking_date"),
                    rs.getString("status"),
                    rs.getString("food_preference"),
                    rs.getString("seat_preference"),
                    rs.getString("seat_number"),
                    rs.getString("flight_number"),
                    rs.getString("flight_name"),
                    rs.getString("source"),
                    rs.getString("destination"),
                    rs.getString("departure_time"),
                    rs.getString("arrival_time"),
                    rs.getDouble("price")
                );
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
