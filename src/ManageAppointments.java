import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ManageAppointments extends JFrame {

    private JTextField txtId, txtDate;
    private JComboBox<String> cmbPatients, cmbDoctors, cmbStatus;
    private JTable appointmentTable;
    private DefaultTableModel tableModel;
    private JButton btnBook, btnUpdate, btnCancel, btnClear, btnBack;

    public ManageAppointments() {
        setTitle("Hospital Management System - Manage Appointments");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= 1. HEADER PANEL =================
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(230, 126, 34)); // Orange Theme
        headerPanel.setPreferredSize(new Dimension(1000, 50));

        JLabel lblTitle = new JLabel("APPOINTMENT MANAGEMENT PORTAL");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);

        add(headerPanel, BorderLayout.NORTH);

        // FORM INPUT PANEL (LEFT)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Appointment Details"));
        formPanel.setPreferredSize(new Dimension(380, 550));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form Fields
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Appointment ID:"), gbc);
        txtId = new JTextField(15);
        txtId.setEditable(false);
        gbc.gridx = 1;
        formPanel.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Select Patient:"), gbc);
        cmbPatients = new JComboBox<>();
        gbc.gridx = 1;
        formPanel.add(cmbPatients, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Select Doctor:"), gbc);
        cmbDoctors = new JComboBox<>();
        gbc.gridx = 1;
        formPanel.add(cmbDoctors, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        txtDate = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtDate, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Status:"), gbc);
        cmbStatus = new JComboBox<>(new String[]{"Pending", "Confirmed", "Cancelled", "Completed"});
        gbc.gridx = 1;
        formPanel.add(cmbStatus, gbc);

        // Buttons Panel inside Form
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        btnBook = createButton("Book Appointment", new Color(46, 204, 113));
        btnUpdate = createButton("Update Status", new Color(241, 196, 15));
        btnCancel = createButton("Delete/Cancel", new Color(231, 76, 60));
        btnClear = createButton("Clear", new Color(149, 165, 166));
        btnBack = createButton("Back", new Color(52, 73, 94));

        buttonPanel.add(btnBook);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnBack);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        add(formPanel, BorderLayout.WEST);

        // ================= 3. TABLE PANEL (RIGHT) =================
        String[] columns = {"ID", "Patient", "Doctor", "Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        appointmentTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Appointment Records"));
        add(scrollPane, BorderLayout.CENTER);

        // ================= 4. EVENTS & DATABASE LOGIC =================

        loadDropdownData();
        loadAppointmentData();

        // Table Click Event -> Fill Text Fields
        appointmentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = appointmentTable.getSelectedRow();
                txtId.setText(tableModel.getValueAt(selectedRow, 0).toString());
                cmbPatients.setSelectedItem(tableModel.getValueAt(selectedRow, 1).toString());
                cmbDoctors.setSelectedItem(tableModel.getValueAt(selectedRow, 2).toString());
                txtDate.setText(tableModel.getValueAt(selectedRow, 3).toString());
                cmbStatus.setSelectedItem(tableModel.getValueAt(selectedRow, 4).toString());
            }
        });

        btnBook.addActionListener(e -> bookAppointment());
        btnUpdate.addActionListener(e -> updateAppointment());
        btnCancel.addActionListener(e -> deleteAppointment());
        btnClear.addActionListener(e -> clearFields());
        btnBack.addActionListener(e -> this.dispose());
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setFocusPainted(false);
        return btn;
    }

    // --- DATABASE OPERATIONS ---

    // Patient සහ Doctor Names Dropdowns වලට Load කිරීම
    private void loadDropdownData() {
        try (Connection conn = DBConnection.getConnection()) {
            // Patients
            cmbPatients.removeAllItems();
            ResultSet rsP = conn.prepareStatement("SELECT id, name FROM patients").executeQuery();
            while (rsP.next()) {
                cmbPatients.addItem(rsP.getInt("id") + " - " + rsP.getString("name"));
            }

            // Doctors
            cmbDoctors.removeAllItems();
            ResultSet rsD = conn.prepareStatement("SELECT id, name FROM doctors").executeQuery();
            while (rsD.next()) {
                cmbDoctors.addItem(rsD.getInt("id") + " - " + rsD.getString("name"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadAppointmentData() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT a.id, p.name AS patient_name, d.name AS doctor_name, a.appointment_date, a.status " +
                    "FROM appointments a " +
                    "JOIN patients p ON a.patient_id = p.id " +
                    "JOIN doctors d ON a.doctor_id = d.id";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("patient_name"),
                        rs.getString("doctor_name"),
                        rs.getString("appointment_date"),
                        rs.getString("status")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void bookAppointment() {
        if (txtDate.getText().isEmpty() || cmbPatients.getSelectedItem() == null || cmbDoctors.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select Patient, Doctor, and Date!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int patientId = Integer.parseInt(cmbPatients.getSelectedItem().toString().split(" - ")[0]);
        int doctorId = Integer.parseInt(cmbDoctors.getSelectedItem().toString().split(" - ")[0]);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(true);
            String query = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, status) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setString(3, txtDate.getText());
            stmt.setString(4, cmbStatus.getSelectedItem().toString());

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Appointment Booked Successfully!");
            clearFields();
            loadAppointmentData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void updateAppointment() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select an appointment to update!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int patientId = Integer.parseInt(cmbPatients.getSelectedItem().toString().split(" - ")[0]);
        int doctorId = Integer.parseInt(cmbDoctors.getSelectedItem().toString().split(" - ")[0]);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(true);
            String query = "UPDATE appointments SET patient_id=?, doctor_id=?, appointment_date=?, status=? WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setString(3, txtDate.getText());
            stmt.setString(4, cmbStatus.getSelectedItem().toString());
            stmt.setInt(5, Integer.parseInt(txtId.getText()));

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Appointment Updated!");
            clearFields();
            loadAppointmentData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deleteAppointment() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select an appointment to delete!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this appointment?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(true);
                String query = "DELETE FROM appointments WHERE id=?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, Integer.parseInt(txtId.getText()));

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Appointment Deleted!");
                clearFields();
                loadAppointmentData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void clearFields() {
        txtId.setText("");
        txtDate.setText("");
        cmbStatus.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManageAppointments().setVisible(true));
    }
}