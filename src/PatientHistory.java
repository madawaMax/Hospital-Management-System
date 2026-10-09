import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PatientHistory extends JFrame {

    private int doctorId;
    private JComboBox<String> cmbPatients;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JTextArea txtDiagnosis, txtPrescription, txtNotes;
    private JButton btnAddRecord, btnClear, btnBack;

    public PatientHistory(int doctorId) {
        this.doctorId = doctorId;

        setTitle("Doctor Portal - Patient Medical History");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= 1. HEADER PANEL =================
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 73, 94)); // Dark Slate Blue
        headerPanel.setPreferredSize(new Dimension(950, 50));

        JLabel lblTitle = new JLabel("PATIENT MEDICAL HISTORY & RECORD MANAGEMENT");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);

        add(headerPanel, BorderLayout.NORTH);

        // ================= 2. PATIENT SELECTOR PANEL (TOP) =================
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setBackground(new Color(236, 240, 241));

        topPanel.add(new JLabel("Select Patient:"));
        cmbPatients = new JComboBox<>();
        cmbPatients.setPreferredSize(new Dimension(250, 25));
        topPanel.add(cmbPatients);

        JButton btnLoadHistory = new JButton("Load History");
        btnLoadHistory.setBackground(new Color(52, 152, 219));
        btnLoadHistory.setForeground(Color.WHITE);
        btnLoadHistory.setFocusPainted(false);
        topPanel.add(btnLoadHistory);

        add(topPanel, BorderLayout.NORTH);

        // Combine Header and Top Selector Panel
        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.add(headerPanel, BorderLayout.NORTH);
        northContainer.add(topPanel, BorderLayout.SOUTH);
        add(northContainer, BorderLayout.NORTH);

        // ================= 3. CENTER PANEL (SPLIT: HISTORY TABLE + ADD NEW RECORD FORM) =================
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        // Top Half: History Table
        String[] columns = {"Record ID", "Visit Date", "Doctor Name", "Diagnosis", "Prescription", "Notes"};
        tableModel = new DefaultTableModel(columns, 0);
        historyTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(historyTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Past Medical Records History"));
        splitPane.setTopComponent(tableScroll);

        // Bottom Half: Add New Medical Record Form
        JPanel formPanel = new JPanel(new GridLayout(1, 3, 10, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Diagnosis / Medical Record"));
        formPanel.setBackground(new Color(245, 247, 250));

        // Diagnosis Area
        JPanel panelDiag = new JPanel(new BorderLayout());
        panelDiag.add(new JLabel("Diagnosis / Symptoms:"), BorderLayout.NORTH);
        txtDiagnosis = new JTextArea(4, 20);
        panelDiag.add(new JScrollPane(txtDiagnosis), BorderLayout.CENTER);

        // Prescription Area
        JPanel panelPres = new JPanel(new BorderLayout());
        panelPres.add(new JLabel("Prescription / Medicines:"), BorderLayout.NORTH);
        txtPrescription = new JTextArea(4, 20);
        panelPres.add(new JScrollPane(txtPrescription), BorderLayout.CENTER);

        // Notes Area
        JPanel panelNotes = new JPanel(new BorderLayout());
        panelNotes.add(new JLabel("Doctor Notes / Remarks:"), BorderLayout.NORTH);
        txtNotes = new JTextArea(4, 20);
        panelNotes.add(new JScrollPane(txtNotes), BorderLayout.CENTER);

        formPanel.add(panelDiag);
        formPanel.add(panelPres);
        formPanel.add(panelNotes);

        splitPane.setBottomComponent(formPanel);
        add(splitPane, BorderLayout.CENTER);

        // ================= 4. BUTTONS PANEL (SOUTH) =================
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        btnAddRecord = new JButton("Save Record");
        btnAddRecord.setBackground(new Color(46, 204, 113));
        btnAddRecord.setForeground(Color.WHITE);
        btnAddRecord.setFocusPainted(false);

        btnClear = new JButton("Clear Fields");
        btnClear.setBackground(new Color(230, 126, 34));
        btnClear.setForeground(Color.WHITE);
        btnClear.setFocusPainted(false);

        btnBack = new JButton("Back");
        btnBack.setBackground(new Color(149, 165, 166));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);

        buttonPanel.add(btnAddRecord);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnBack);

        add(buttonPanel, BorderLayout.SOUTH);

        // ================= 5. EVENTS & LOGIC =================
        loadPatientDropdown();

        btnLoadHistory.addActionListener(e -> loadPatientHistory());
        btnAddRecord.addActionListener(e -> saveMedicalRecord());
        btnClear.addActionListener(e -> clearFields());
        btnBack.addActionListener(e -> this.dispose());
    }


    private void loadPatientDropdown() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM patients");
             ResultSet rs = stmt.executeQuery()) {

            cmbPatients.removeAllItems();
            while (rs.next()) {
                cmbPatients.addItem(rs.getInt("id") + " - " + rs.getString("name"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void loadPatientHistory() {
        tableModel.setRowCount(0);
        if (cmbPatients.getSelectedItem() == null) return;

        int patientId = getSelectedPatientId();

        String query = "SELECT h.id, h.visit_date, d.name AS doctor_name, h.diagnosis, h.prescription, h.notes " +
                "FROM patient_history h " +
                "JOIN doctors d ON h.doctor_id = d.id " +
                "WHERE h.patient_id = ? ORDER BY h.visit_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("visit_date"),
                        rs.getString("doctor_name"),
                        rs.getString("diagnosis"),
                        rs.getString("prescription"),
                        rs.getString("notes")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void saveMedicalRecord() {
        if (cmbPatients.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient first!");
            return;
        }

        if (txtDiagnosis.getText().trim().isEmpty() || txtPrescription.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Diagnosis and Prescription!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int patientId = getSelectedPatientId();
        String todayDate = java.time.LocalDate.now().toString();

        String query = "INSERT INTO patient_history (patient_id, doctor_id, visit_date, diagnosis, prescription, notes) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setString(3, todayDate);
            stmt.setString(4, txtDiagnosis.getText().trim());
            stmt.setString(5, txtPrescription.getText().trim());
            stmt.setString(6, txtNotes.getText().trim());

            int inserted = stmt.executeUpdate();
            if (inserted > 0) {
                JOptionPane.showMessageDialog(this, "Medical Record Saved Successfully!");
                clearFields();
                loadPatientHistory(); // Table එක Auto Refresh වේ
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving record: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getSelectedPatientId() {
        String selected = cmbPatients.getSelectedItem().toString();
        return Integer.parseInt(selected.split(" - ")[0]);
    }

    private void clearFields() {
        txtDiagnosis.setText("");
        txtPrescription.setText("");
        txtNotes.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PatientHistory(1).setVisible(true));
    }
}