package flightmanagement;

public class Flight {
    private int flightId;
    private String flightNumber; // new
    private String name, source, destination, departureTime, arrivalTime;
    private double price;
    private int seatCapacity;
    private int availableSeats;
    private String status; // e.g., SCHEDULED, CANCELLED

    public Flight(String name, String source, String destination, String dep, String arr, double price) {
        this.name = name;
        this.source = source;
        this.destination = destination;
        this.departureTime = dep;
        this.arrivalTime = arr;
        this.price = price;
        this.seatCapacity = 100; // default
        this.availableSeats = 100;
        this.status = "SCHEDULED";
    }

    // New full constructor
    public Flight(int flightId, String flightNumber, String name, String source, String destination, String dep, String arr, double price, int seatCapacity, int availableSeats, String status) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.name = name;
        this.source = source;
        this.destination = destination;
        this.departureTime = dep;
        this.arrivalTime = arr;
        this.price = price;
        this.seatCapacity = seatCapacity;
        this.availableSeats = availableSeats;
        this.status = status;
    }

    public int getFlightId() { return flightId; }
    public String getFlightNumber() { return flightNumber; }
    public String getName() { return name; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public double getPrice() { return price; }
    public int getSeatCapacity() { return seatCapacity; }
    public int getAvailableSeats() { return availableSeats; }
    public String getStatus() { return status; }

    public void setAvailableSeats(int seats) { this.availableSeats = seats; }
    public void setStatus(String status) { this.status = status; }

    public void displayShort() {
        System.out.println(ConsoleColors.CYAN + "✈️ " + (flightNumber == null ? name : flightNumber + " - " + name) + " | " + source + " -> " + destination + " | " + departureTime + " | ₹" + price + " | Seats: " + availableSeats + ConsoleColors.RESET);
    }
}
