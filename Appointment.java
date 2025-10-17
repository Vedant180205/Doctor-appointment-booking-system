// Appointment.java (CLEANED)
public class Appointment {
    private final int id;
    private final int patientId; 
    private final int doctorId; 
    private final String dateTime; // Stored as "yyyy-MM-dd HH:mm" for sorting
    private String status;

    /**
     * The primary constructor for an Appointment object.
     */
    public Appointment(int id, int patientId, int doctorId, String dateTime, String status) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.dateTime = dateTime;
        this.status = status;
    }
    
    // REMOVED THE BROKEN CONSTRUCTOR:
    /*
    public Appointment(int patientId2, int int1, int int2, String dateTime2, String string) {
        // ... (This one was deleted)
    }
    */

    public int getId() { return id; }
    public int getPatientId() { return patientId; }
    public int getDoctorId() { return doctorId; }
    public String getDateTime() { return dateTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "App ID: " + id + ", Date: " + dateTime.substring(0, 10) + 
               ", Time: " + dateTime.substring(11) + ", Status: " + status;
    }
}