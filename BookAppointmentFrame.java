import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookAppointmentFrame extends JFrame implements ActionListener {
    
    private final int patientId;
    private JComboBox<String> doctorComboBox;
    private JTextField dateField;
    private JTextField timeField;
    private JButton bookButton;

    public BookAppointmentFrame(int patientId) {
        this.patientId = patientId;
        setTitle("Book New Appointment - " + patientId);
        setSize(450, 350);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        
        setupHeader();
        setupForm();

        setVisible(true);
    }
    
    private void setupHeader() {
        // ... (Header code) ...
    }

    // Inside BookAppointmentFrame.java

private void setupForm() {
    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // 1. Doctor Selection - Uses Sorted Doctors from DBManager
    JLabel doctorLabel = new JLabel("Select Doctor:");
    doctorLabel.setFont(new Font("Arial", Font.PLAIN, 14));
    gbc.gridx = 0; gbc.gridy = 0;
    formPanel.add(doctorLabel, gbc);

    List<Doctor> doctors = DBManager.getAllDoctorsSorted();
    String[] doctorNames = new String[doctors.size()];
    for (int i = 0; i < doctors.size(); i++) {
        Doctor d = doctors.get(i);
        // Using the updated Doctor toString format: "Name (Specialization) - ID: 1"
        doctorNames[i] = d.toString(); 
    }
    doctorComboBox = new JComboBox<>(doctorNames);
    gbc.gridx = 1; gbc.gridy = 0;
    formPanel.add(doctorComboBox, gbc);

    // 2. Date Input
    JLabel dateLabel = new JLabel("Date (YYYY-MM-DD):");
    gbc.gridx = 0; gbc.gridy = 1;
    formPanel.add(dateLabel, gbc);
    dateField = new JTextField("2025-12-30", 10);
    gbc.gridx = 1; gbc.gridy = 1;
    formPanel.add(dateField, gbc);

    // 3. Time Input
    JLabel timeLabel = new JLabel("Time (HH:MM):");
    gbc.gridx = 0; gbc.gridy = 2;
    formPanel.add(timeLabel, gbc);
    timeField = new JTextField("14:30", 10);
    gbc.gridx = 1; gbc.gridy = 2;
    formPanel.add(timeField, gbc);

    // 4. Book Button
    bookButton = new JButton("Confirm Booking");
    bookButton.setBackground(new Color(34, 139, 34));
    bookButton.setForeground(Color.WHITE);
    bookButton.setFont(new Font("Arial", Font.BOLD, 16));
    bookButton.addActionListener(this);
    gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
    gbc.insets = new Insets(30, 10, 10, 10);
    formPanel.add(bookButton, gbc);

    // FIX: This line MUST be here to add the panel to the JFrame
    add(formPanel, BorderLayout.CENTER);
}

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == bookButton) {
            String selectedDoctorInfo = (String) doctorComboBox.getSelectedItem();
            if (selectedDoctorInfo == null) {
                JOptionPane.showMessageDialog(this, "Please select a doctor.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // --- CRITICAL FIX: Extract Doctor ID as INT ---
            int doctorId;
            try {
                // Find "ID: " and extract the subsequent integer
                String idString = selectedDoctorInfo.substring(
                    selectedDoctorInfo.lastIndexOf("ID: ") + 4
                );
                doctorId = Integer.parseInt(idString.trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error parsing Doctor ID. Selection format is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // --- END CRITICAL FIX ---
            
            String date = dateField.getText().trim();
            String time = timeField.getText().trim();
            String dateTimeString = date + " " + time;
            
            try {
                // Validation
                LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid Date/Time format. Use YYYY-MM-DD and HH:MM.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Book the appointment (Pass doctorId as INT)
            Appointment newApp = DBManager.bookNewAppointment(this.patientId, doctorId, dateTimeString);
            
            if (newApp != null) {
                Doctor bookedDoctor = DBManager.getDoctorById(doctorId);
                String doctorName = (bookedDoctor != null) ? bookedDoctor.getName() : "Unknown Doctor";

                // 1. Show Success Message
                JOptionPane.showMessageDialog(this, 
                    "Appointment successfully booked!\n\nID: " + newApp.getId() + 
                    "\nDoctor: " + doctorName + 
                    "\nDate: " + date + "\nTime: " + time, 
                    "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
                
                // 2. Close the current (booking) window
                dispose(); 
                
            } else {
                 JOptionPane.showMessageDialog(this, "Failed to book appointment. Check the console for database errors.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}