import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DoctorAppointments extends JFrame {

    private int doctorId;
    private JTable appointmentTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbStatusFilter, cmbUpdateStatus;
    private JTextField txtAppointmentId, txtPatientName, txtDate;
    private JButton btnUpdateStatus, btnRefresh, btnBack;

    public DoctorAppointments(int doctorId) {
        this.doctorId = doctorId;

        setTitle("Doctor Portal - My Appointments");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= 1. HEADER PANEL =================
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185)); // Blue Theme
        headerPanel.setPreferredSize(new Dimension(950, 50));

        JLabel lblTitle = new JLabel("MY APPOINTMENTS MANAGEMENT");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);

        add(headerPanel, BorderLayout.NORTH);

        // ================= 2. FILTER & TOP CONTROL PANEL =================
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setBackground(new Color(236, 240, 241));

        topPanel.add(new JLabel("Filter Status:"));
        cmbStatusFilter = new JComboBox<>(new String[]{"All", "Pending", "Confirmed", "Completed", "Cancelled"});
        topPanel.add(cmbStatusFilter);

        btnRefresh = new JButton("Refresh Table");
        btnRefresh.setBackground(new Color(52, 152, 219));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        topPanel.add(btnRefresh);

        add(topPanel, BorderLayout.NORTH);

        // Header සහ Filter Panel එකක් ලෙස ඒකාබද්ධ කිරීම
        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.add(headerPanel, BorderLayout.NORTH);
        northContainer.add(topPanel, BorderLayout.SOUTH);
        add(northContainer, BorderLayout.NORTH);

        // ================= 3. TABLE PANEL (CENTER) =================
        String[] columns = {"Appointment ID", "Patient Name", "Contact", "Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        appointmentTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Assigned Appointments List"));
        add(scrollPane, BorderLayout.CENTER);

        // ================= 4. UPDATE STATUS PANEL (SOUTH) =================
        JPanel updatePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        updatePanel.setBorder(BorderFactory.createTitledBorder("Manage Appointment Status"));
        updatePanel.setBackground(new Color(245, 247, 250));

        updatePanel.add(new JLabel("App. ID:"));
        txtAppointmentId = new JTextField(5);
        txtAppointmentId.setEditable(false);
        updatePanel.add(txtAppointmentId);

        updatePanel.add(new JLabel("Patient:"));
        txtPatientName = new JTextField(12);
        txtPatientName.setEditable(false);
        updatePanel.add(txtPatientName);

        updatePanel.add(new JLabel("Date:"));
        txtDate = new JTextField(8);
        txtDate.setEditable(false);
        updatePanel.add(txtDate);

        updatePanel.add(new JLabel("Change Status:"));
        cmbUpdateStatus = new JComboBox<>(new String[]{"Pending", "Confirmed", "Completed", "Cancelled"});
        updatePanel.add(cmbUpdateStatus);

        btnUpdateStatus = new JButton("Update Status");
        btnUpdateStatus.setBackground(new Color(46, 204, 113));
        btnUpdateStatus.setForeground(Color.WHITE);
        btnUpdateStatus.setFocusPainted(false);
        updatePanel.add(btnUpdateStatus);

        btnBack = new JButton("Back");
        btnBack.setBackground(new Color(149, 165, 166));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        updatePanel.add(btnBack);

        add(updatePanel, BorderLayout.SOUTH);

        // ================= 5. EVENTS & LOGIC =================

        loadDoctorAppointments();

        // Table Click Event -> Select Item
        appointmentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = appointmentTable.getSelectedRow();
                txtAppointmentId.setText(tableModel.getValueAt(selectedRow, 0).toString());
                txtPatientName.setText(tableModel.getValueAt(selectedRow, 1).toString());
                txtDate.setText(tableModel.getValueAt(selectedRow, 3).toString());
                cmbUpdateStatus.setSelectedItem(tableModel.getValueAt(selectedRow, 4).toString());
            }
        });

        cmbStatusFilter.addActionListener(e -> loadDoctorAppointments());
        btnRefresh.addActionListener(e -> loadDoctorAppointments());
        btnUpdateStatus.addActionListener(e -> updateStatus());
        btnBack.addActionListener(e -> this.dispose());
    }

    // --- DATABASE OPERATIONS ---

    private void loadDoctorAppointments() {
        tableModel.setRowCount(0);
        String selectedFilter = cmbStatusFilter.getSelectedItem().toString();

        String query = "SELECT a.id, p.name AS patient_name, p.contact, a.appointment_date, a.status " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "WHERE a.doctor_id = ?";

        if (!selectedFilter.equals("All")) {
            query += " AND a.status = ?";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, doctorId);
            if (!selectedFilter.equals("All")) {
                stmt.setString(2, selectedFilter);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("patient_name"),
                        rs.getString("contact"),
                        rs.getString("appointment_date"),
                        rs.getString("status")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateStatus() {
        if (txtAppointmentId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the table!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "UPDATE appointments SET status = ? WHERE id = ? AND doctor_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, cmbUpdateStatus.getSelectedItem().toString());
            stmt.setInt(2, Integer.parseInt(txtAppointmentId.getText()));
            stmt.setInt(3, doctorId);

            int updated = stmt.executeUpdate();
            if (updated > 0) {
                JOptionPane.showMessageDialog(this, "Appointment Status Updated Successfully!");
                clearFields();
                loadDoctorAppointments();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating status: " + ex.getMessage());
        }
    }

    private void clearFields() {
        txtAppointmentId.setText("");
        txtPatientName.setText("");
        txtDate.setText("");
        cmbUpdateStatus.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        // Sample Test Run with Doctor ID = 1
        SwingUtilities.invokeLater(() -> new DoctorAppointments(1).setVisible(true));
    }
}