import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// Removed: import java.util.StringTokenizer;

// NOTE: Ensure AuthService, PatientDashboardFrame, and DoctorDashboardFrame are available.

public class LoginFrame extends JFrame implements ActionListener {

    // UI Components
    private JTextField usernameField; 
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;

    // Colors for a modern look
    private final Color PRIMARY_COLOR = new Color(50, 150, 250); // Blue
    private final Color BACKGROUND_COLOR = new Color(240, 240, 240); // Light Gray

    public LoginFrame() {
        // 1. Frame Setup
        setTitle("Appointment System Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // 2. Create the Main Panel
        JPanel mainPanel = createMainPanel();

        // 3. Add panels to the frame
        add(createHeaderPanel(), BorderLayout.NORTH); 
        add(mainPanel, BorderLayout.CENTER);

        setVisible(true);
    }
    
    // Restored and Corrected Header Panel Implementation
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(400, 60));
        
        JLabel headerLabel = new JLabel("SECURE LOGIN");
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        
        headerPanel.add(headerLabel);
        return headerPanel;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Username Label and Field ---
        JLabel usernameLabel = new JLabel("Username:"); 
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(usernameLabel, gbc);

        usernameField = new JTextField(15); 
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(usernameField, gbc);

        // --- Password Label and Field ---
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordLabel, gbc);
        
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(passwordField, gbc);

        // --- Login Button ---
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setBackground(PRIMARY_COLOR);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(this); 

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.ipady = 8;
        panel.add(loginButton, gbc);

        // --- Status Label ---
        statusLabel = new JLabel("Enter your credentials.");
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.ipady = 0; 
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(statusLabel, gbc);

        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()); 
            passwordField.setText("");
            
            // --- 1. AUTHENTICATION (Returns "ID|TYPE" string) ---
            String authResult = AuthService.authenticateUser(username, password); 

            if (authResult != null) {
                
                // --- 2. PARSE THE RESULT STRING ---
                int userId;
                String userType;
                try {
                    // This parsing relies on AuthService returning "ID|TYPE"
                    String[] parts = authResult.split("\\|");
                    if (parts.length != 2) {
                        // This catch block handles the error if AuthService returned an incorrect format
                        throw new IllegalArgumentException("Invalid auth result format. Expected 'ID|TYPE'.");
                    }
                    
                    userId = Integer.parseInt(parts[0]); 
                    userType = parts[1]; 
                    
                } catch (Exception ex) {
                    statusLabel.setText("Login Failed: Internal data error after authentication.");
                    statusLabel.setForeground(Color.RED);
                    ex.printStackTrace(); // Uncomment for detailed debugging
                    return; 
                }

                // Login SUCCESSFUL
                statusLabel.setText("Login Successful! Opening Dashboard...");
                statusLabel.setForeground(new Color(0, 150, 0)); 
                
                // --- 3. OPEN DASHBOARD ---
                if (userType.equalsIgnoreCase("doctor")) { 
                    JOptionPane.showMessageDialog(this, "Doctor Login Successful! ID: " + userId, "Success", JOptionPane.INFORMATION_MESSAGE);
                    new DoctorDashboardFrame(userId); 
                    
                } else if (userType.equalsIgnoreCase("patient")) { 
                    JOptionPane.showMessageDialog(this, "Patient Login Successful! ID: " + userId, "Success", JOptionPane.INFORMATION_MESSAGE);
                    new PatientDashboardFrame(userId); 
                    
                } else {
                    statusLabel.setText("Login Failed: Unknown user type from database.");
                    statusLabel.setForeground(Color.RED);
                    return; 
                }

                dispose(); 

            } else {
                // Login FAILED (AuthService returned null)
                statusLabel.setText("Invalid Username or Password.");
                statusLabel.setForeground(Color.RED);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}