package flightmanagement;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println(ConsoleColors.GREEN + "=== 🌐 Airport Flight Management System ===" + ConsoleColors.RESET);

        // Ensure a default admin exists (idempotent)
        DatabaseInitializer.ensureDefaultAdmin();

        outer: while (true) {
            System.out.println("\n1️⃣  Login");
            System.out.println("2️⃣  Register");
            System.out.println("3️⃣  Exit");
            System.out.println("4️⃣  Create Admin (requires master password)");
            System.out.print(ConsoleColors.CYAN + "Enter choice: " + ConsoleColors.RESET);
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.println("Select role: 1) Passenger 2) Admin");
                    System.out.print(ConsoleColors.CYAN + "Role: " + ConsoleColors.RESET);
                    String roleChoice = sc.nextLine().trim();
                    if ("1".equals(roleChoice)) {
                        System.out.print("Email: ");
                        String email = sc.nextLine().trim();
                        System.out.print("Password: ");
                        String pass = sc.nextLine().trim();
                        Passenger p = Passenger.loginUser(email, pass);
                        if (p != null) passengerMenu(sc, p);
                    } else if ("2".equals(roleChoice)) {
                        System.out.print("Admin Email: ");
                        String email = sc.nextLine().trim();
                        System.out.print("Password: ");
                        String pass = sc.nextLine().trim();
                        Admin a = Admin.login(email, pass);
                        if (a != null) adminMenu(sc, a);
                    } else {
                        System.out.println(ConsoleColors.RED + "❌ Invalid role choice." + ConsoleColors.RESET);
                    }
                    break;

                case "2":
                    System.out.println(ConsoleColors.CYAN + "--- Passenger Registration ---" + ConsoleColors.RESET);
                    System.out.print("Name: "); String name = sc.nextLine().trim();
                    System.out.print("Email: "); String email = sc.nextLine().trim();
                    System.out.print("Password: "); String password = sc.nextLine().trim();
                    System.out.print("Passport Number: "); String passport = sc.nextLine().trim();
                    System.out.print("Contact Number: "); String phone = sc.nextLine().trim();
                    Passenger newP = new Passenger(name, email, password, passport, phone);
                    newP.registerUser();
                    break;

                case "3":
                    System.out.println(ConsoleColors.YELLOW + "👋 Exiting system. Goodbye!" + ConsoleColors.RESET);
                    break outer;

                case "4":
                    System.out.print(ConsoleColors.CYAN + "Enter master password to create admin: " + ConsoleColors.RESET);
                    String master = sc.nextLine().trim();
                    if ("admin".equals(master)) {
                        System.out.print("Admin Name: "); String aname = sc.nextLine().trim();
                        System.out.print("Admin Email: "); String aemail = sc.nextLine().trim();
                        Admin.createAdmin(aname, aemail);
                    } else {
                        System.out.println(ConsoleColors.RED + "❌ Incorrect master password." + ConsoleColors.RESET);
                    }
                    break;

                default:
                    System.out.println(ConsoleColors.RED + "❌ Invalid choice!" + ConsoleColors.RESET);
            }
        }

        sc.close();
    }

    private static void passengerMenu(Scanner sc, Passenger p) {
        BookingService svc = new BookingService();
        while (true) {
            System.out.println(ConsoleColors.CYAN + "\n🧍 Passenger Dashboard - " + p.getName() + ConsoleColors.RESET);
            System.out.println("1) View Flights");
            System.out.println("2) Book Flight");
            System.out.println("3) View My Bookings");
            System.out.println("4) Cancel Booking");
            System.out.println("5) Logout");
            System.out.print(ConsoleColors.CYAN + "Enter choice: " + ConsoleColors.RESET);
            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1":
                    p.viewAvailableFlights();
                    break;
                case "2":
                    p.viewAvailableFlights();
                    System.out.print("Enter Flight ID to book: ");
                    String fidStr = sc.nextLine().trim();
                    try {
                        int fid = Integer.parseInt(fidStr);
                        // Collect food preference
                        System.out.println("Food preferences: 1) Vegetarian 2) Non-Veg 3) Vegan 4) No preference");
                        System.out.print("Choose (1-4): ");
                        String foodChoice = sc.nextLine().trim();
                        String foodPref = null;
                        switch (foodChoice) {
                            case "1": foodPref = "Vegetarian"; break;
                            case "2": foodPref = "Non-Veg"; break;
                            case "3": foodPref = "Vegan"; break;
                            default: foodPref = null; break;
                        }
                        // Collect seat preference
                        System.out.println("Seat preferences: 1) Window 2) Aisle 3) Middle 4) No preference");
                        System.out.print("Choose (1-4): ");
                        String seatChoice = sc.nextLine().trim();
                        String seatPref = null;
                        switch (seatChoice) {
                            case "1": seatPref = "WINDOW"; break;
                            case "2": seatPref = "AISLE"; break;
                            case "3": seatPref = "MIDDLE"; break;
                            default: seatPref = null; break;
                        }

                        p.bookFlight(fid, foodPref, seatPref);
                    } catch (NumberFormatException e) {
                        System.out.println(ConsoleColors.RED + "❌ Invalid flight id." + ConsoleColors.RESET);
                    }
                    break;
                case "3":
                    List<Booking> bookings = p.viewMyBookings();
                    if (bookings.isEmpty()) System.out.println(ConsoleColors.YELLOW + "No bookings found." + ConsoleColors.RESET);
                    else for (Booking b : bookings) b.displayShort();
                    break;
                case "4":
                    System.out.print("Enter Booking ID to cancel: ");
                    String bidStr = sc.nextLine().trim();
                    try {
                        int bid = Integer.parseInt(bidStr);
                        p.cancelBooking(bid);
                    } catch (NumberFormatException e) {
                        System.out.println(ConsoleColors.RED + "❌ Invalid booking id." + ConsoleColors.RESET);
                    }
                    break;
                case "5":
                    System.out.println(ConsoleColors.YELLOW + "🔒 Logged out." + ConsoleColors.RESET);
                    return;
                default:
                    System.out.println(ConsoleColors.RED + "❌ Invalid choice." + ConsoleColors.RESET);
            }
        }
    }

    private static void adminMenu(Scanner sc, Admin a) {
        BookingService svc = new BookingService();
        while (true) {
            System.out.println(ConsoleColors.YELLOW + "\n🧑‍💼 Admin Dashboard - " + a.getName() + ConsoleColors.RESET);
            System.out.println("1) Add Flight");
            System.out.println("2) Update Flight");
            System.out.println("3) Cancel Flight");
            System.out.println("4) View All Bookings");
            System.out.println("5) View Passengers");
            System.out.println("6) Generate Reports");
            System.out.println("7) Change Password");
            System.out.println("8) Logout");
            System.out.print(ConsoleColors.CYAN + "Enter choice: " + ConsoleColors.RESET);
            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1":
                    System.out.print("Flight Number: "); String fn = sc.nextLine().trim();
                    System.out.print("Flight Name: "); String fname = sc.nextLine().trim();
                    System.out.print("Source: "); String src = sc.nextLine().trim();
                    System.out.print("Destination: "); String dest = sc.nextLine().trim();
                    System.out.print("Departure Time (YYYY-MM-DD HH:MM:SS): "); String dep = sc.nextLine().trim();
                    System.out.print("Arrival Time (YYYY-MM-DD HH:MM:SS): "); String arr = sc.nextLine().trim();
                    System.out.print("Price: "); String priceStr = sc.nextLine().trim();
                    System.out.print("Seat Capacity: "); String capStr = sc.nextLine().trim();
                    try {
                        double price = Double.parseDouble(priceStr);
                        int cap = Integer.parseInt(capStr);
                        Flight f = new Flight(fname, src, dest, dep, arr, price);
                        // set extra fields via reflection of available setters (we'll create full Flight with defaults)
                        // Use constructor that sets defaults and then update DB values when adding
                        f = new Flight(fname, src, dest, dep, arr, price);
                        // Sadly constructor doesn't accept flightNumber/seatCapacity; set via setters if available
                        // But to avoid modifying Flight further here, we will rely on DB defaults for seat counts.
                        // Instead create a Flight object with full fields using the alternative constructor
                        Flight full = new Flight(0, fn, fname, src, dest, dep, arr, price, cap, cap, "SCHEDULED");
                        a.addFlight(full);
                    } catch (Exception e) {
                        System.out.println(ConsoleColors.RED + "❌ Invalid numeric input: " + e.getMessage() + ConsoleColors.RESET);
                    }
                    break;
                case "2":
                    System.out.print("Enter Flight ID to update: "); String ufid = sc.nextLine().trim();
                    try {
                        int id = Integer.parseInt(ufid);
                        System.out.print("New Flight Number: "); String nfn = sc.nextLine().trim();
                        System.out.print("New Flight Name: "); String nn = sc.nextLine().trim();
                        System.out.print("New Source: "); String ns = sc.nextLine().trim();
                        System.out.print("New Destination: "); String nd = sc.nextLine().trim();
                        System.out.print("New Departure Time: "); String ndp = sc.nextLine().trim();
                        System.out.print("New Arrival Time: "); String nar = sc.nextLine().trim();
                        System.out.print("New Price: "); double nprice = Double.parseDouble(sc.nextLine().trim());
                        System.out.print("New Seat Capacity: "); int ncap = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("New Available Seats: "); int navail = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("New Status (SCHEDULED/CANCELLED): "); String nstatus = sc.nextLine().trim();
                        Flight uf = new Flight(id, nfn, nn, ns, nd, ndp, nar, nprice, ncap, navail, nstatus);
                        a.updateFlight(uf);
                    } catch (NumberFormatException e) {
                        System.out.println(ConsoleColors.RED + "❌ Invalid number." + ConsoleColors.RESET);
                    }
                    break;
                case "3":
                    System.out.print("Enter Flight ID to cancel: "); String cfid = sc.nextLine().trim();
                    try {
                        int id = Integer.parseInt(cfid);
                        a.cancelFlight(id);
                    } catch (NumberFormatException e) {
                        System.out.println(ConsoleColors.RED + "❌ Invalid id." + ConsoleColors.RESET);
                    }
                    break;
                case "4":
                    List<Booking> all = a.viewAllBookings();
                    if (all.isEmpty()) System.out.println(ConsoleColors.YELLOW + "No bookings found." + ConsoleColors.RESET);
                    else for (Booking b : all) b.displayShort();
                    break;
                case "5":
                    List<Passenger> ps = a.viewPassengerList();
                    if (ps.isEmpty()) System.out.println(ConsoleColors.YELLOW + "No passengers found." + ConsoleColors.RESET);
                    else for (Passenger p : ps) System.out.println(ConsoleColors.WHITE + p.getUserId() + " | " + p.getName() + " | " + p.getEmail() + ConsoleColors.RESET);
                    break;
                case "6":
                    a.generateReports();
                    break;
                case "7":
                    System.out.print("Enter old password: "); String oldP = sc.nextLine().trim();
                    System.out.print("Enter new password: "); String newP = sc.nextLine().trim();
                    a.changePassword(oldP, newP);
                    break;
                case "8":
                    System.out.println(ConsoleColors.YELLOW + "🔒 Admin logged out." + ConsoleColors.RESET);
                    return;
                default:
                    System.out.println(ConsoleColors.RED + "❌ Invalid choice." + ConsoleColors.RESET);
            }
        }
    }
}
