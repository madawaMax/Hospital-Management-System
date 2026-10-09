import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BookAppointment extends JFrame {

    private JComboBox<String> cmbPatients, cmbDoctors, cmbStatus;
    private JTextField txtAppointmentId, txtDate, txtTime, txtSearch;
    private JTable appointmentTable;
    private DefaultTableModel tableModel;
    private JButton btnBook, btnUpdate, btnCancel, btnClear, btnSearch, btnBack;

    public BookAppointment() {
        setTitle("Staff Portal - Book Patient Appointment");
        setSize(980, 630);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= 1. HEADER PANEL =================
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(155, 89, 182)); // Amethyst Purple Theme
        headerPanel.setPreferredSize(new Dimension(980, 50));

        JLabel lblTitle = new JLabel("APPOINTMENT BOOKING & MANAGEMENT");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);

        add(headerPanel, BorderLayout.NORTH);

        // ================= 2. LEFT FORM PANEL =================
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 12));
        formPanel.setPreferredSize(new Dimension(380, 420));
        formPanel.setBorder(BorderFactory.createTitledBorder("Booking Details Form"));
        formPanel.setBackground(new Color(245, 247, 250));

        formPanel.add(new JLabel("Appointment ID:"));
        txtAppointmentId = new JTextField();
        txtAppointmentId.setEditable(false);
        formPanel.add(txtAppointmentId);

        formPanel.add(new JLabel("Select Patient:"));
        cmbPatients = new JComboBox<>();
        formPanel.add(cmbPatients);

        formPanel.add(new JLabel("Select Doctor:"));
        cmbDoctors = new JComboBox<>();
        formPanel.add(cmbDoctors);

        formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        txtDate = new JTextField(java.time.LocalDate.now().toString());
        formPanel.add(txtDate);

        formPanel.add(new JLabel("Time (e.g. 09:30 AM):"));
        txtTime = new JTextField();
        formPanel.add(txtTime);

        formPanel.add(new JLabel("Status:"));
        cmbStatus = new JComboBox<>(new String[]{"Pending", "Confirmed", "Completed", "Cancelled"});
        formPanel.add(cmbStatus);

        // Buttons Grid
        btnBook = new JButton("Book App.");
        btnBook.setBackground(new Color(46, 204, 113));
        btnBook.setForeground(Color.WHITE);

        btnUpdate = new JButton("Update");
        btnUpdate.setBackground(new Color(52, 152, 219));
        btnUpdate.setForeground(Color.WHITE);

        btnCancel = new JButton("Cancel App.");
        btnCancel.setBackground(new Color(231, 76, 60));
        btnCancel.setForeground(Color.WHITE);

        btnClear = new JButton("Clear");
        btnClear.setBackground(new Color(230, 126, 34));
        btnClear.setForeground(Color.WHITE);

        JPanel pnlBtnTop = new JPanel(new GridLayout(1, 2, 5, 5));
        pnlBtnTop.setOpaque(false);
        pnlBtnTop.add(btnBook);
        pnlBtnTop.add(btnUpdate);

        JPanel pnlBtnBottom = new JPanel(new GridLayout(1, 2, 5, 5));
        pnlBtnBottom.setOpaque(false);
        pnlBtnBottom.add(btnCancel);
        pnlBtnBottom.add(btnClear);

        formPanel.add(pnlBtnTop);
        formPanel.add(pnlBtnBottom);

        add(formPanel, BorderLayout.WEST);

        // ================= 3. RIGHT TABLE & SEARCH PANEL =================
        JPanel rightPanel = new JPanel(new BorderLayout());

        // Search Sub-panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(new Color(236, 240, 241));
        searchPanel.add(new JLabel("Search Patient/Doctor:"));
        txtSearch = new JTextField(15);
        searchPanel.add(txtSearch);

        btnSearch = new JButton("Search");
        btnSearch.setBackground(new Color(52, 73, 94));
        btnSearch.setForeground(Color.WHITE);
        searchPanel.add(btnSearch);

        rightPanel.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"App. ID", "Patient Name", "Doctor Name", "Date", "Time", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        appointmentTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("All Booked Appointments"));
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        add(rightPanel, BorderLayout.CENTER);

        // ================= 4. BOTTOM PANEL =================
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnBack = new JButton("Back to Dashboard");
        btnBack.setBackground(new Color(149, 165, 166));
        btnBack.setForeground(Color.WHITE);
        bottomPanel.add(btnBack);

        add(bottomPanel, BorderLayout.SOUTH);

        // ================= 5. EVENTS & LOGIC =================
        loadDropdownData();
        loadAppointments();

        // Table Selection Event
        appointmentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = appointmentTable.getSelectedRow();
                txtAppointmentId.setText(tableModel.getValueAt(selectedRow, 0).toString());
                txtDate.setText(tableModel.getValueAt(selectedRow, 3).toString());
                txtTime.setText(tableModel.getValueAt(selectedRow, 4).toString());
                cmbStatus.setSelectedItem(tableModel.getValueAt(selectedRow, 5).toString());
            }
        });

        btnBook.addActionListener(e -> bookAppointment());
        btnUpdate.addActionListener(e -> updateAppointment());
        btnCancel.addActionListener(e -> cancelAppointment());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> searchAppointments());
        btnBack.addActionListener(e -> this.dispose());
    }

    // --- DATABASE LOGIC ---

    private void loadDropdownData() {
        try (Connection conn = DBConnection.getConnection()) {
            // Load Patients
            cmbPatients.removeAllItems();
            PreparedStatement stmtP = conn.prepareStatement("SELECT id, name FROM patients");
            ResultSet rsP = stmtP.executeQuery();
            while (rsP.next()) {
                cmbPatients.addItem(rsP.getInt("id") + " - " + rsP.getString("name"));
            }

            // Load Doctors
            cmbDoctors.removeAllItems();
            PreparedStatement stmtD = conn.prepareStatement("SELECT id, name, specialization FROM doctors");
            ResultSet rsD = stmtD.executeQuery();
            while (rsD.next()) {
                cmbDoctors.addItem(rsD.getInt("id") + " - Dr. " + rsD.getString("name") + " (" + rsD.getString("specialization") + ")");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadAppointments() {
        tableModel.setRowCount(0);
        String query = "SELECT a.id, p.name AS patient_name, d.name AS doctor_name, a.appointment_date, a.appointment_time, a.status " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "JOIN doctors d ON a.doctor_id = d.id " +
                "ORDER BY a.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("patient_name"),
                        rs.getString("doctor_name"),
                        rs.getString("appointment_date"),
                        rs.getString("appointment_time"),
                        rs.getString("status")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void bookAppointment() {
        if (cmbPatients.getSelectedItem() == null || cmbDoctors.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select both Patient and Doctor!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (txtDate.getText().trim().isEmpty() || txtTime.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Date and Time!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int patientId = Integer.parseInt(cmbPatients.getSelectedItem().toString().split(" - ")[0]);
        int doctorId = Integer.parseInt(cmbDoctors.getSelectedItem().toString().split(" - ")[0]);

        String query = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setString(3, txtDate.getText().trim());
            stmt.setString(4, txtTime.getText().trim());
            stmt.setString(5, cmbStatus.getSelectedItem().toString());

            if (stmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Appointment Booked Successfully!");
                clearForm();
                loadAppointments();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error booking appointment: " + ex.getMessage());
        }
    }

    private void updateAppointment() {
        if (txtAppointmentId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select an appointment from the table to update!");
            return;
        }

        int patientId = Integer.parseInt(cmbPatients.getSelectedItem().toString().split(" - ")[0]);
        int doctorId = Integer.parseInt(cmbDoctors.getSelectedItem().toString().split(" - ")[0]);

        String query = "UPDATE appointments SET patient_id = ?, doctor_id = ?, appointment_date = ?, appointment_time = ?, status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setString(3, txtDate.getText().trim());
            stmt.setString(4, txtTime.getText().trim());
            stmt.setString(5, cmbStatus.getSelectedItem().toString());
            stmt.setInt(6, Integer.parseInt(txtAppointmentId.getText()));

            if (stmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Appointment Details Updated!");
                clearForm();
                loadAppointments();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating appointment: " + ex.getMessage());
        }
    }

    private void cancelAppointment() {
        if (txtAppointmentId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select an appointment from the table to cancel!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel this appointment?", "Confirm Cancel", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String query = "UPDATE appointments SET status = 'Cancelled' WHERE id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, Integer.parseInt(txtAppointmentId.getText()));

                if (stmt.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Appointment Status set to Cancelled!");
                    clearForm();
                    loadAppointments();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error cancelling appointment: " + ex.getMessage());
            }
        }
    }

    private void searchAppointments() {
        String keyword = txtSearch.getText().trim();
        tableModel.setRowCount(0);

        String query = "SELECT a.id, p.name AS patient_name, d.name AS doctor_name, a.appointment_date, a.appointment_time, a.status " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "JOIN doctors d ON a.doctor_id = d.id " +
                "WHERE p.name LIKE ? OR d.name LIKE ? " +
                "ORDER BY a.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("patient_name"),
                        rs.getString("doctor_name"),
                        rs.getString("appointment_date"),
                        rs.getString("appointment_time"),
                        rs.getString("status")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clearForm() {
        txtAppointmentId.setText("");
        txtDate.setText(java.time.LocalDate.now().toString());
        txtTime.setText("");
        cmbStatus.setSelectedIndex(0);
        txtSearch.setText("");
        if (cmbPatients.getItemCount() > 0) cmbPatients.setSelectedIndex(0);
        if (cmbDoctors.getItemCount() > 0) cmbDoctors.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BookAppointment().setVisible(true));
    }
}