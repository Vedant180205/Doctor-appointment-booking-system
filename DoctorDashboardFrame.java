import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// NOTE: Assuming DBManager and LoginFrame classes are accessible

public class DoctorDashboardFrame extends JFrame implements ActionListener {

    private final Color PRIMARY_COLOR = new Color(50, 150, 250); // Blue
    private final Color ACCENT_COLOR = new Color(25, 25, 112); // Midnight Blue

    private final int doctorId; // Stored ID for fetching personalized data
    private JButton manageAppointmentsButton;
    private JButton logoutButton;

    // UI Fields for Dynamic Stats
    private JLabel scheduledLabel;
    private JLabel completedLabel;
    private JLabel nextPatientLabel;

    public DoctorDashboardFrame(int doctorId) {
        this.doctorId = doctorId; // Initialize the ID
        
        // 1. Frame Setup
        setTitle("Doctor Dashboard - Dr. " + doctorId);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 2. Header Panel
        add(createHeaderPanel(doctorId), BorderLayout.NORTH);

        // 3. Main Content Panel (using GridBagLayout for layout)
        add(createMainContentPanel(), BorderLayout.CENTER);

        // 4. Footer/Logout Panel
        add(createFooterPanel(), BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createHeaderPanel(int doctorId) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ACCENT_COLOR);
        headerPanel.setPreferredSize(new Dimension(600, 70));

        JLabel titleLabel = new JLabel("Doctor Portal", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));

        // NOTE: In a real system, you'd fetch the doctor's name here using DBManager.getDoctorById(doctorId).getName()
        JLabel welcomeLabel = new JLabel("Welcome, Dr. " + doctorId, SwingConstants.CENTER);
        welcomeLabel.setForeground(Color.LIGHT_GRAY);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(welcomeLabel, BorderLayout.SOUTH);
        return headerPanel;
    }

    private JPanel createMainContentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Title/Instruction
        JLabel instruction = new JLabel("Select an Action:", SwingConstants.CENTER);
        instruction.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;

        // --- View & Manage Appointments Button ---
        manageAppointmentsButton = new JButton("View and Manage Appointments");
        manageAppointmentsButton.setFont(new Font("Arial", Font.BOLD, 18));
        manageAppointmentsButton.setBackground(PRIMARY_COLOR);
        manageAppointmentsButton.setForeground(Color.WHITE);
        manageAppointmentsButton.setFocusPainted(false);
        manageAppointmentsButton.setPreferredSize(new Dimension(350, 70));
        manageAppointmentsButton.addActionListener(this);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(manageAppointmentsButton, gbc);

        // --- Quick Stats Panel ---
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 20));
        statsPanel.setBackground(new Color(230, 230, 250)); // Lavender
        statsPanel.setBorder(BorderFactory.createTitledBorder("Quick Stats - Today"));

        // Initialize the actual JLabel objects 
        scheduledLabel = new JLabel("Remaining: Loading...");
        completedLabel = new JLabel("Completed: Loading...");
        nextPatientLabel = new JLabel("Next Patient: Loading..."); 
        
        statsPanel.add(scheduledLabel);
        statsPanel.add(completedLabel);
        statsPanel.add(nextPatientLabel);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(40, 20, 20, 20); // Add top padding
        panel.add(statsPanel, gbc);

        // CRITICAL: Load the live data immediately after initializing the JLabels
        updateQuickStats(); 
        
        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        footerPanel.setBackground(new Color(220, 220, 220)); 

        logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 14));
        logoutButton.setBackground(Color.RED);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(this);

        footerPanel.add(logoutButton);
        return footerPanel;
    }

    /**
     * Fetches the latest statistics from the DBManager and updates the JLabels.
     */
    private void updateQuickStats() {
        // 1. Fetch counts
        int scheduledCount = DBManager.countAppointmentsByStatus(this.doctorId, "booked");
        int completedCount = DBManager.countAppointmentsByStatus(this.doctorId, "completed");
        
        // 2. Fetch the next patient
        Appointment nextApp = DBManager.getNextAppointment(this.doctorId);
        
        // 3. Update JLabels
        scheduledLabel.setText("Remaining: " + scheduledCount);
        completedLabel.setText("Completed: " + completedCount);

        if (nextApp != null) {
            String nextPatient = "Patient ID " + nextApp.getPatientId() + 
                                 " @ " + nextApp.getDateTime().substring(11); // gets the HH:MM part
            nextPatientLabel.setText("Next Patient: " + nextPatient);
            nextPatientLabel.setForeground(new Color(0, 100, 0)); // Dark Green
        } else {
            nextPatientLabel.setText("Next Patient: Queue Empty");
            nextPatientLabel.setForeground(Color.BLUE);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == logoutButton) {
            int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                dispose(); 
                // new LoginFrame(); // Uncomment when LoginFrame is available
            }
        } else if (e.getSource() == manageAppointmentsButton) {
            // Action to open the Appointments Frame
            new DoctorAppointmentsFrame(this.doctorId);
        }
    }
}