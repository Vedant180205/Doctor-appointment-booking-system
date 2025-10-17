import java.sql.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// NOTE: This code assumes external classes Doctor and Appointment exist
// and have the required methods/constructors.

public class DBManager {

    // --- 1. Connection Details ---
    private static final String URL = "jdbc:mysql://localhost:3306/appointment_booking_db";
    private static final String USER = "root";
    private static final String PASS = "vedbhumi";

    // --- DSA Implementation for Caching and Queuing ---
    private static final Map<Integer, Doctor> DOCTORS_CACHE = new HashMap<>(); 
    private static final Queue<Integer> CANCELLATION_QUEUE = new LinkedList<>();

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            throw new SQLException("JDBC Driver not available.", e);
        }
        // Set connection timeout (e.g., 5 seconds) if supported by the driver, though this is usually done via driver properties.
        return DriverManager.getConnection(URL, USER, PASS); 
    }
    
    // ----------------------------------------------------------------------
    // DSA: PriorityQueue for Upcoming Appointments (Sorting/Priority)
    // ----------------------------------------------------------------------
    public static List<Appointment> getSortedAppointments(int patientId) {
        // CRITICAL FIX: Add patient_id to the SELECT list
        String sql = "SELECT appointment_id, patient_id, doctor_id, appointment_date, appointment_time, status " +
                     "FROM appointments WHERE patient_id = ? AND status = 'booked'"; 
        
        PriorityQueue<Appointment> sortedQueue = new PriorityQueue<>(
            Comparator.comparing(Appointment::getDateTime)
        );

        try (Connection conn = getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getString("appointment_date");
                    String time = rs.getString("appointment_time");

                    // Ensure the concatenation handles NULL gracefully 
                    String fullDateTime = (date != null && !date.trim().isEmpty() ? date : "0000-00-00") + " " + 
                                          (time != null && !time.trim().isEmpty() ? time : "00:00");

                    // Since we selected it, we can fetch patient_id from the result set
                    Appointment app = new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getInt("patient_id"), 
                        rs.getInt("doctor_id"), 
                        fullDateTime,
                        rs.getString("status")
                    );
                    sortedQueue.offer(app);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching sorted appointments: " + e.getMessage());
            e.printStackTrace();
        }

        List<Appointment> sortedList = new ArrayList<>();
        while (!sortedQueue.isEmpty()) {
            sortedList.add(sortedQueue.poll());
        }
        return sortedList;
    }
    // Inside DBManager.java

// ... (Existing methods like getSortedAppointments, getDoctorById, etc.) ...

/**
 * Fetches all 'booked' appointments for a specific Doctor ID on the current date.
 * @param doctorId The ID of the logged-in doctor.
 * @return A list of Appointment objects.
 */
public static List<Appointment> getDoctorsTodayAppointments(int doctorId) {
    // Get today's date in 'YYYY-MM-DD' format for SQL comparison
    String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    String sql = "SELECT appointment_id, patient_id, doctor_id, appointment_date, appointment_time, status " +
                 "FROM appointments WHERE doctor_id = ? AND appointment_date = ? AND status = 'booked' " +
                 "ORDER BY appointment_time ASC"; 
    
    List<Appointment> todaysAppointments = new ArrayList<>();

    try (Connection conn = getConnection(); 
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setInt(1, doctorId);
        pstmt.setString(2, today);
        
        System.out.println("DB_DEBUG: Fetching appointments for Dr. " + doctorId + " on " + today);

        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String date = rs.getString("appointment_date");
                String time = rs.getString("appointment_time");
                String fullDateTime = date + " " + time; // Date and time are guaranteed by the query
                
                Appointment app = new Appointment(
                    rs.getInt("appointment_id"),
                    rs.getInt("patient_id"), 
                    rs.getInt("doctor_id"), 
                    fullDateTime,
                    rs.getString("status")
                );
                todaysAppointments.add(app);
            }
        }
    } catch (SQLException e) {
        System.err.println("Error fetching doctor's appointments: " + e.getMessage());
        e.printStackTrace();
    }

    return todaysAppointments;
}

// Inside DBManager.java

/**
 * Updates the status of a single appointment in the database.
 * @param appointmentId The ID of the appointment to update.
 * @param newStatus The new status (e.g., "completed", "cancelled", "in progress").
 * @return true if the update was successful, false otherwise.
 */
public static boolean updateAppointmentStatus(int appointmentId, String newStatus) {
    String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
    
    try (Connection conn = getConnection(); 
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, newStatus);
        pstmt.setInt(2, appointmentId);
        
        int rowsAffected = pstmt.executeUpdate();
        
        if (rowsAffected > 0) {
            System.out.println("DB_DEBUG: Appointment ID " + appointmentId + " status updated to " + newStatus);
            return true;
        }
        return false;

    } catch (SQLException e) {
        System.err.println("Error updating appointment status for ID " + appointmentId + ": " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

// Inside DBManager.java

// NOTE: You'll need to create a simple Patient class later to hold patient name/details.
// For now, we'll return a String array or list for simplicity.

/**
 * Counts the total number of appointments for a doctor on the current date, optionally filtered by status.
 * @param doctorId The ID of the doctor.
 * @param status The status to filter by ("booked", "completed", or null for all).
 * @return The count of matching appointments.
 */
public static int countAppointmentsByStatus(int doctorId, String status) {
    String today = java.time.LocalDate.now().format(DateTimeFormatter.ISO_DATE);
    String sql = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = ?";
    
    if (status != null && !status.isEmpty()) {
        sql += " AND status = ?";
    }
    
    int count = 0;
    try (Connection conn = getConnection(); 
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setInt(1, doctorId);
        pstmt.setString(2, today);
        
        if (status != null && !status.isEmpty()) {
            pstmt.setString(3, status);
        }
        
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        }
    } catch (SQLException e) {
        System.err.println("Error counting appointments: " + e.getMessage());
    }
    return count;
}


/**
 * Finds the soonest (next) scheduled appointment for a doctor today.
 * @param doctorId The ID of the doctor.
 * @return An Appointment object or null if none are found.
 */
public static Appointment getNextAppointment(int doctorId) {
    String today = java.time.LocalDate.now().format(DateTimeFormatter.ISO_DATE);
    
    // Order by time to find the next one, and limit to 1 result.
    String sql = "SELECT appointment_id, patient_id, doctor_id, appointment_date, appointment_time, status " +
                 "FROM appointments WHERE doctor_id = ? AND appointment_date = ? AND status = 'booked' " +
                 "ORDER BY appointment_time ASC LIMIT 1"; 
    
    Appointment nextApp = null;
    
    try (Connection conn = getConnection(); 
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setInt(1, doctorId);
        pstmt.setString(2, today);

        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                String date = rs.getString("appointment_date");
                String time = rs.getString("appointment_time");
                String fullDateTime = date + " " + time;
                
                nextApp = new Appointment(
                    rs.getInt("appointment_id"),
                    rs.getInt("patient_id"), 
                    rs.getInt("doctor_id"), 
                    fullDateTime,
                    rs.getString("status")
                );
            }
        }
    } catch (SQLException e) {
        System.err.println("Error fetching next appointment: " + e.getMessage());
    }
    return nextApp;
}

// ... (Rest of DBManager.java) ...

    // ----------------------------------------------------------------------
    // DSA: Hash Table for Doctor Lookup (Searching)
    // ----------------------------------------------------------------------
    public static Doctor getDoctorById(int doctorId) { 
        if (DOCTORS_CACHE.containsKey(doctorId)) {
            return DOCTORS_CACHE.get(doctorId);
        }

        String sql = "SELECT doctor_id, name, specialization, contact FROM doctors WHERE doctor_id = ?";
        Doctor doctor = null;

        try (Connection conn = getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, doctorId); 
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    doctor = new Doctor(
                        rs.getInt("doctor_id"),
                        rs.getString("name"),
                        rs.getString("specialization"),
                        rs.getString("contact")
                    );
                    DOCTORS_CACHE.put(doctorId, doctor);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor by ID: " + e.getMessage());
        }
        return doctor;
    }
    
    // ----------------------------------------------------------------------
    // DSA: Sorting Algorithm (Custom Comparator)
    // ----------------------------------------------------------------------
    public static List<Doctor> getAllDoctorsSorted() {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT doctor_id, name, specialization, contact FROM doctors";

        try (Connection conn = getConnection(); 
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                doctors.add(new Doctor(
                    rs.getInt("doctor_id"), 
                    rs.getString("name"),
                    rs.getString("specialization"),
                    rs.getString("contact")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all doctors: " + e.getMessage());
        }

        // DSA: Custom sorting logic
        Collections.sort(doctors, (d1, d2) -> {
            int specializationCompare = d1.getSpecialization().compareTo(d2.getSpecialization());
            if (specializationCompare != 0) {
                return specializationCompare;
            }
            return d1.getName().compareTo(d2.getName());
        });
        
        return doctors;
    }
    
    // ----------------------------------------------------------------------
    // DSA: Queue for Cancellations & Database Update
    // ----------------------------------------------------------------------
    public static boolean cancelAppointment(int appointmentId) {
        String sql = "UPDATE appointments SET status = 'cancelled' WHERE appointment_id = ? AND status = 'booked'";
        
        try (Connection conn = getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, appointmentId);
            
            if (pstmt.executeUpdate() > 0) {
                CANCELLATION_QUEUE.offer(appointmentId); 
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Error cancelling appointment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ----------------------------------------------------------------------
    // Core CRUD Operation: Book New Appointment (FIXED TRANSACTION CONTROL)
    // ----------------------------------------------------------------------
    public static Appointment bookNewAppointment(int patientId, int doctorId, String dateTime) {
        String date = dateTime.substring(0, 10);
        String time = dateTime.substring(11);

        String sql = "INSERT INTO appointments (doctor_id, patient_id, appointment_date, appointment_time, status) " +
                      "VALUES (?, ?, ?, ?, 'booked')"; 
        
        System.out.println("DEBUG: Attempting to INSERT Appointment (Transaction start):");
        
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            
            // CRITICAL FIX 1: Ensure auto-commit is OFF to control the transaction explicitly
            conn.setAutoCommit(false); 
            
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            // BINDING ORDER MUST MATCH SQL COLUMN LIST ORDER!
            pstmt.setInt(1, doctorId); 
            pstmt.setInt(2, patientId); 
            pstmt.setString(3, date); 
            pstmt.setString(4, time); 
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                
                // CRITICAL FIX 2: Commit the transaction to save changes permanently
                conn.commit(); 
                
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        System.out.println("DEBUG: SUCCESS! Transaction Committed. ID: " + newId);
                        return new Appointment(newId, patientId, doctorId, dateTime, "booked");
                    }
                }
            }
            
            // If execution reaches here without success, the finally block will ensure cleanup.

        } catch (SQLException e) {
            // If anything goes wrong, try to roll back the transaction
            if (conn != null) {
                try {
                    System.err.println("Transaction failed. Attempting rollback.");
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            System.err.println("DATABASE ERROR: Failed to book appointment! " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure connection and statement are closed
            try {
                if (pstmt != null) pstmt.close();
                // Reset auto-commit to true before closing the connection, though it usually resets on close.
                // if (conn != null && !conn.getAutoCommit()) conn.setAutoCommit(true); 
                if (conn != null) conn.close();
            } catch (SQLException closeEx) {
                System.err.println("Error closing resources: " + closeEx.getMessage());
            }
        }
        return null;
    }
}