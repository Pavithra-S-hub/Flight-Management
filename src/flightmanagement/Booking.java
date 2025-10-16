package flightmanagement;

public class Booking {
    private int bookingId;
    private int userId;
    private int flightId;
    private String bookingDate;
    private String status; // CONFIRMED, CANCELLED

    public Booking(int bookingId, int userId, int flightId, String bookingDate, String status) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.flightId = flightId;
        this.bookingDate = bookingDate;
        this.status = status;
    }

    public Booking(int userId, int flightId, String bookingDate) {
        this.userId = userId;
        this.flightId = flightId;
        this.bookingDate = bookingDate;
        this.status = "CONFIRMED";
    }

    public int getBookingId() { return bookingId; }
    public int getUserId() { return userId; }
    public int getFlightId() { return flightId; }
    public String getBookingDate() { return bookingDate; }
    public String getStatus() { return status; }

    public void displayShort() {
        System.out.println(ConsoleColors.MAGENTA + "🎟️ Booking ID: " + bookingId + " | Flight: " + flightId + " | User: " + userId + " | " + bookingDate + " | " + status + ConsoleColors.RESET);
    }
}
