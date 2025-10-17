import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PatientDashboardFrame extends JFrame implements ActionListener {

    private final Color PRIMARY_COLOR = new Color(34, 139, 34); // Forest Green
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245); // Off-White
    private final int patientId; // Added to store the ID for new frames

    private JButton bookAppointmentButton;
    private JButton viewAppointmentsButton;
    private JButton logoutButton;
    

    public PatientDashboardFrame(int patientId) {
        this.patientId = patientId; // Store patient ID
        
        // 1. Frame Setup
        setTitle("Patient Dashboard - " + patientId);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(550, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 2. Header Panel
        add(createHeaderPanel(patientId), BorderLayout.NORTH);

        // 3. Main Content Panel
        add(createMainContentPanel(), BorderLayout.CENTER);

        // 4. Footer/Logout Panel
        add(createFooterPanel(), BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createHeaderPanel(int patientId) {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(550, 70));

        JLabel titleLabel = new JLabel("Welcome to Your Health Portal, " + patientId);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 20));

        headerPanel.add(titleLabel);
        return headerPanel;
    }

    private JPanel createMainContentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Book Appointment Button ---
        bookAppointmentButton = new JButton("Book New Appointment");
        bookAppointmentButton.setFont(new Font("Arial", Font.BOLD, 18));
        bookAppointmentButton.setBackground(PRIMARY_COLOR);
        bookAppointmentButton.setForeground(Color.WHITE);
        bookAppointmentButton.setFocusPainted(false);
        bookAppointmentButton.setPreferredSize(new Dimension(300, 70));
        bookAppointmentButton.addActionListener(this);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(bookAppointmentButton, gbc);

        // --- View Appointments Button ---
        viewAppointmentsButton = new JButton("View/Cancel Appointments");
        viewAppointmentsButton.setFont(new Font("Arial", Font.BOLD, 18));
        viewAppointmentsButton.setBackground(new Color(100, 149, 237)); // Cornflower Blue
        viewAppointmentsButton.setForeground(Color.WHITE);
        viewAppointmentsButton.setFocusPainted(false);
        viewAppointmentsButton.setPreferredSize(new Dimension(300, 70));
        viewAppointmentsButton.addActionListener(this);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(viewAppointmentsButton, gbc);

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

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == logoutButton) {
            int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                dispose(); // Close dashboard
                // Since LoginFrame is assumed to exist:
                // new LoginFrame(); 
                System.exit(0); // Exit for this example
            }
        } else if (e.getSource() == bookAppointmentButton) {
            // Open the new appointment booking frame
            new BookAppointmentFrame(this.patientId);
        } else if (e.getSource() == viewAppointmentsButton) {
            // Open the new appointment viewing frame
            new ViewAppointmentsFrame(this.patientId);
        }
    }
}