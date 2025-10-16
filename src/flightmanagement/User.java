// filepath: c:\Users\Pavi\OneDrive\Desktop\Flight_Management\src\flightmanagement\User.java
package flightmanagement;

public abstract class User {
    protected int userId;
    protected String name;
    protected String email;
    protected String password;
    protected String passportNumber;
    protected String contactNumber;
    protected String role; // PASSENGER or ADMIN

    public User() {}

    public User(int userId, String name, String email, String passportNumber, String contactNumber, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passportNumber = passportNumber;
        this.contactNumber = contactNumber;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassportNumber() { return passportNumber; }
    public String getContactNumber() { return contactNumber; }
    public String getRole() { return role; }
}

