import javax.swing.*;

// NOTE: Delete this dummy class if you have the real LoginFrame class defined.
// class LoginFrame extends JFrame {} 

// NOTE: You must ensure PatientDashboardFrame is available.

public class MainApp {
    public static void main(String[] args) {
        // Run the GUI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            
            // --- SIMULATION FOR TESTING DASHBOARD ---
            // 1. Define the patientId as an INTEGER (e.g., 3, 4, 7 from your appointments table image).
            int patientId = 3; 
            
            // 2. Launch the Patient Dashboard directly.
            new PatientDashboardFrame(patientId);
            
            // Note: This bypasses the LoginFrame, but uses the correct INT ID.
        });
    }
}