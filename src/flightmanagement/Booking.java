package flightmanagement;

public class Booking {
    private int bookingId;
    private int userId;
    private int flightId;
    private String bookingDate;
    private String status; // CONFIRMED, CANCELLED
    private String foodPreference; // e.g., Vegetarian, Non-Veg, Vegan
    private String seatPreference; // WINDOW, AISLE, MIDDLE
    private String seatNumber; // e.g., 12A

    // Flight-related details (for display convenience)
    private String flightNumber;
    private String flightName;
    private String source;
    private String destination;
    private String departureTime;
    private String arrivalTime;
    private double price;

    // Constructor used when loading full booking+flight info
    public Booking(int bookingId, int userId, int flightId, String bookingDate, String status, String foodPreference, String seatPreference, String seatNumber, String flightNumber, String flightName, String source, String destination, String departureTime, String arrivalTime, double price) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.flightId = flightId;
        this.bookingDate = bookingDate;
        this.status = status;
        this.foodPreference = foodPreference;
        this.seatPreference = seatPreference;
        this.seatNumber = seatNumber;
        this.flightNumber = flightNumber;
        this.flightName = flightName;
        this.source = source;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
    }

    // Lightweight constructor when only basic data is available
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
    public String getFoodPreference() { return foodPreference; }
    public String getSeatPreference() { return seatPreference; }
    public String getSeatNumber() { return seatNumber; }

    public String getFlightNumber() { return flightNumber; }
    public String getFlightName() { return flightName; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public double getPrice() { return price; }

    public void displayShort() {
        System.out.println(ConsoleColors.MAGENTA + "🎟️ Booking ID: " + bookingId + " | Flight: " + (flightNumber == null ? flightName : flightNumber + " - " + flightName) + " | " + source + " -> " + destination + " | Departs: " + departureTime + " | Status: " + status + ConsoleColors.RESET);
        if (seatNumber != null) System.out.println(ConsoleColors.CYAN + "   💺 Seat: " + seatNumber + " (" + (seatPreference == null ? "No preference" : seatPreference) + ")" + ConsoleColors.RESET);
        if (foodPreference != null) System.out.println(ConsoleColors.CYAN + "   🍽 Food preference: " + foodPreference + ConsoleColors.RESET);
    }
}
