import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ViewAppointmentsFrame extends JFrame implements ActionListener {
    
    private final int patientId; // Corrected to int
    private JList<String> appointmentList;
    private DefaultListModel<String> listModel; // <-- This is the variable that was null
    private JButton cancelButton;

    public ViewAppointmentsFrame(int patientId) { // Constructor takes int
        this.patientId = patientId;
        setTitle("View/Cancel Appointments - " + patientId);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        
        // 1. SETUP METHODS (MUST be called before loadAppointments())
        setupHeader();
        setupAppointmentList(); // <-- This method now MUST execute listModel = new DefaultListModel<>()
        setupFooter();
        
        // 2. DATA LOAD
        loadAppointments(); 

        setVisible(true);
    }
    
    private void setupHeader() {
        JPanel header = new JPanel();
        header.setBackground(new Color(100, 149, 237)); // Cornflower Blue
        JLabel title = new JLabel("Your Scheduled Appointments (Sorted by Date/Time)");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        header.add(title);
        add(header, BorderLayout.NORTH);
    }

    private void setupAppointmentList() {
        // *** CRITICAL FIX: INITIALIZE listModel HERE ***
        listModel = new DefaultListModel<>(); 
        
        appointmentList = new JList<>(listModel);
        appointmentList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(appointmentList);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void setupFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cancelButton = new JButton("Cancel Selected Appointment");
        cancelButton.setBackground(Color.RED);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.addActionListener(this);
        footer.add(cancelButton);
        add(footer, BorderLayout.SOUTH);
    }

    /**
     * Loads and displays appointments. Includes diagnostic checks for data integrity.
     */
    private void loadAppointments() {
        // This is the line that was crashing, but it is now safe because setupAppointmentList() ran first.
        listModel.clear(); 
        
        List<Appointment> appointments = null;
        try {
            appointments = DBManager.getSortedAppointments(this.patientId); 
        } catch (Exception e) {
            listModel.addElement("!!! CRITICAL ERROR: Could not fetch appointments from database !!!");
            System.err.println("Database fetch failed for patient ID " + this.patientId);
            e.printStackTrace(); 
            return; 
        }
        
        if (appointments == null || appointments.isEmpty()) {
            listModel.addElement("You have no scheduled appointments.");
        } else {
            System.out.println("--- DEBUG: Rendering " + appointments.size() + " Appointments ---");
            
            for (Appointment app : appointments) {
                
               // Inside ViewAppointmentsFrame.java -> loadAppointments()

// Find this line (around L89):
// System.out.printf("APPOINTMENT DATA: ID=%d, PatientID=%d, DoctorID=%d, DateTime='%s', Status=%s%n",

// Replace it with this corrected version:

// DIAGNOSTIC LOG: Print the raw Appointment data
System.out.printf("APPOINTMENT DATA: ID=%d, PatientID=%d, DoctorID=%d, DateTime='%s', Status='%s'%n",
    app.getId(), 
    app.getDoctorId(),
    app.getPatientId(), 
    app.getDateTime(), 
    app.getStatus()
);
                
                // 1. Get Doctor Name (O(1) Hash Table Lookup)
                Doctor doctor = DBManager.getDoctorById(app.getDoctorId()); 
                String doctorName = (doctor != null) ? doctor.getName() : "Unknown Doctor (ID: " + app.getDoctorId() + ")"; 
                
                // 2. Format Date/Time Safely
                String fullDateTime = app.getDateTime();
                String datePart = "N/A (Error)";
                String timePart = "N/A (Error)";

                // Check for the minimum expected length of "YYYY-MM-DD HH:MM" (16 chars)
                if (fullDateTime != null && fullDateTime.length() >= 16) { 
                    datePart = fullDateTime.substring(0, 10);
                    timePart = fullDateTime.substring(11); 
                } else if (fullDateTime != null) {
                    datePart = fullDateTime;
                    timePart = "";
                }
                
                // 3. Build the List Item
                String listItem = String.format("ID: %d | Date: %s | Time: %s | Doctor: %s | Status: %s",
                    app.getId(),
                    datePart, 
                    timePart, 
                    doctorName,
                    app.getStatus()
                );
                listModel.addElement(listItem);
            }
            System.out.println("--- DEBUG: Rendering Complete ---");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelButton) {
            
            String selectedItem = appointmentList.getSelectedValue();
             if (selectedItem == null || selectedItem.startsWith("You have")) {
                JOptionPane.showMessageDialog(this, "Please select an appointment to cancel.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Safely parse the appointment ID from the string
                int appointmentId = Integer.parseInt(selectedItem.substring(selectedItem.indexOf("ID: ") + 4, selectedItem.indexOf(" |")).trim());
                
                int result = JOptionPane.showConfirmDialog(this, "Confirm cancellation for appointment ID " + appointmentId + "?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
                
                if (result == JOptionPane.YES_OPTION) {
                    if (DBManager.cancelAppointment(appointmentId)) { 
                        JOptionPane.showMessageDialog(this, "Appointment ID " + appointmentId + " has been requested for cancellation. List will now refresh.", "Cancellation Pending", JOptionPane.INFORMATION_MESSAGE);
                        loadAppointments(); // Refresh the list
                    } else {
                        JOptionPane.showMessageDialog(this, "Cancellation failed. Appointment may not be found or is already cancelled/completed.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error processing selection: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}