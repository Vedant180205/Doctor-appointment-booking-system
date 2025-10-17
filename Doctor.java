// Doctor.java
public class Doctor {
    private final int id; // Changed from String to int
    private final String name;
    private final String specialization;
    private final String contact;

    public Doctor(int id, String name, String specialization, String contact) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.contact = contact;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }
    public String getContact() { return contact; }
    
    // For sorting and display
    public String toString() {
        return name + " (" + specialization + ") - ID: " + id;
    }
}