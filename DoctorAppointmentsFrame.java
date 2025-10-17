import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class DoctorAppointmentsFrame extends JFrame implements ActionListener {
    
    private final int doctorId;
    private JTable appointmentTable;
    private DefaultTableModel tableModel;
    
    private PriorityQueue<Appointment> waitingQueue; 
    private JLabel nextPatientLabel; 
    private JButton processNextButton;

    public DoctorAppointmentsFrame(int doctorId) {
        this.doctorId = doctorId;
        setTitle("Today's Appointments & Queue - Dr. " + doctorId);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setSize(1000, 600);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout(10, 10));

        // 1. Setup Table
        setupAppointmentTable();

        // 2. Setup Queue Panel (THIS MUST COME BEFORE loadAppointmentsData)
        // This ensures nextPatientLabel and processNextButton are initialized.
        JPanel queuePanel = createQueuePanel(); 

        // 3. Load Data (now safe to call updateQueueDisplay)
        loadAppointmentsData();

        // 4. Assemble the UI
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                                             new JScrollPane(appointmentTable), 
                                             queuePanel); // Use the initialized queuePanel
        splitPane.setDividerLocation(600);
        
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        setVisible(true);
    }
    
    private JPanel createHeaderPanel() {
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        infoPanel.setBackground(new Color(240, 240, 255));
        infoPanel.add(new JLabel("Today's Date: " + 
            java.time.LocalDate.now().format(DateTimeFormatter.ISO_DATE)));
        return infoPanel;
    }

    private JPanel createQueuePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Real-Time Patient Queue (Prioritized by Time)"));
        panel.setBackground(Color.WHITE);

        nextPatientLabel = new JLabel("Queue Empty", SwingConstants.CENTER);
        nextPatientLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nextPatientLabel.setForeground(Color.RED);
        nextPatientLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        processNextButton = new JButton("Process Next Patient (Dequeue)");
        processNextButton.setFont(new Font("Arial", Font.BOLD, 14));
        processNextButton.setBackground(new Color(34, 139, 34)); // Green
        processNextButton.setForeground(Color.WHITE);
        processNextButton.addActionListener(this);

        panel.add(nextPatientLabel, BorderLayout.CENTER);
        panel.add(processNextButton, BorderLayout.SOUTH);

        return panel;
    }

    private void setupAppointmentTable() {
        String[] columnNames = {"Appt ID", "Time", "Patient ID", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appointmentTable = new JTable(tableModel);
        appointmentTable.setFont(new Font("Arial", Font.PLAIN, 14));
        appointmentTable.setRowHeight(25);
    }
    
    private void loadAppointmentsData() {
        tableModel.setRowCount(0);
        
        // DSA CONCEPT: Initialize Priority Queue. 
        // Comparator ensures appointments are sorted by DateTime (soonest first).
        waitingQueue = new PriorityQueue<>(
            Comparator.comparing(Appointment::getDateTime)
        );

        List<Appointment> appointments = DBManager.getDoctorsTodayAppointments(this.doctorId);

        if (appointments.isEmpty()) {
            updateQueueDisplay();
            return;
        }

        for (Appointment app : appointments) {
            // Add all fetched appointments (which are 'booked') to the Priority Queue
            waitingQueue.offer(app); 
            
            // Also add to the table view for the doctor to see the full list
            String timeOnly = app.getDateTime().substring(11);
            Object[] rowData = {
                app.getId(),
                timeOnly,
                app.getPatientId(),
                app.getStatus()
            };
            tableModel.addRow(rowData);
        }
        
        // Update the queue display after loading data
        updateQueueDisplay();
    }
    
    private void updateQueueDisplay() {
        if (waitingQueue.isEmpty()) {
            nextPatientLabel.setText("Queue Empty. Time for a coffee break!");
            nextPatientLabel.setForeground(Color.BLUE);
            processNextButton.setEnabled(false);
        } else {
            Appointment nextApp = waitingQueue.peek(); // DSA: peek() to see the head
            String time = nextApp.getDateTime().substring(11);
            String text = String.format("Next: Patient ID %d @ %s (Appt ID: %d)", 
                                        nextApp.getPatientId(), time, nextApp.getId());
            nextPatientLabel.setText(text);
            nextPatientLabel.setForeground(new Color(0, 100, 0)); // Dark Green
            processNextButton.setEnabled(true);
        }
    }

    private void updateTableRowStatus(int apptId, String newStatus) {
    // Iterate through all rows in the JTable model
    for (int i = 0; i < tableModel.getRowCount(); i++) {
        // TableModel column 0 holds the Appt ID (must match the column index in setupAppointmentTable)
        if ((int) tableModel.getValueAt(i, 0) == apptId) {
            
            // TableModel column 3 holds the Status
            tableModel.setValueAt(newStatus, i, 3);
            
            // Optional: Visually highlight the row (e.g., set background color)
            // Note: This often requires a custom TableCellRenderer, 
            // but simply changing the text status works for now.
            break; 
        }
    }
}
    
    @Override
public void actionPerformed(ActionEvent e) {
    if (e.getSource() == processNextButton) {
        // DSA: poll() to remove and get the highest priority item (soonest appointment)
        Appointment nextApp = waitingQueue.poll(); 
        
        if (nextApp != null) {
            // --- STEP 1: DATABASE SYNCHRONIZATION ---
            boolean dbSuccess = DBManager.updateAppointmentStatus(nextApp.getId(), "completed"); 

            if (dbSuccess) {
                // --- STEP 2: UI SYNCHRONIZATION ---
                
                // 2a. Update the list/JTable alongside
                updateTableRowStatus(nextApp.getId(), "Completed"); 
                
                // 2b. Show alert to doctor
                JOptionPane.showMessageDialog(this,
                    "Patient ID " + nextApp.getPatientId() + " successfully completed (Appt ID " + nextApp.getId() + ").",
                    "Appointment Completed", JOptionPane.INFORMATION_MESSAGE);
                
                // 2c. Update the visual display of the Priority Queue
                updateQueueDisplay();
                
            } else {
                // If DB fails, put the appointment back on the queue and alert the doctor
                waitingQueue.offer(nextApp); 
                JOptionPane.showMessageDialog(this, 
                    "ERROR: Failed to update database status. Please check connection.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
}