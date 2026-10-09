import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class IssuePrescription extends JFrame {

    private int doctorId;
    private JComboBox<String> cmbPatients;
    private JTextField txtMedicine, txtDosage, txtDuration;
    private JTextArea txtInstructions, txtPrescriptionPreview;
    private JTable medicineTable;
    private DefaultTableModel tableModel;
    private JButton btnAddMedicine, btnSavePrint, btnClear, btnBack;

    public IssuePrescription(int doctorId) {
        this.doctorId = doctorId;

        setTitle("Doctor Portal - Issue Prescription");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= 1. HEADER PANEL =================
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(142, 68, 173)); // Purple Theme
        headerPanel.setPreferredSize(new Dimension(950, 50));

        JLabel lblTitle = new JLabel("ISSUE NEW PRESCRIPTION");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);

        add(headerPanel, BorderLayout.NORTH);

        // ================= 2. LEFT PANEL (INPUT FORM) =================
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(450, 550));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Prescription Details"));
        leftPanel.setBackground(new Color(245, 247, 250));

        // Patient Selector
        JPanel pnlPatient = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlPatient.setOpaque(false);
        pnlPatient.add(new JLabel("Select Patient: "));
        cmbPatients = new JComboBox<>();
        cmbPatients.setPreferredSize(new Dimension(280, 25));
        pnlPatient.add(cmbPatients);
        leftPanel.add(pnlPatient);

        // Medicine Input Form
        JPanel pnlMedInput = new JPanel(new GridLayout(3, 2, 5, 5));
        pnlMedInput.setOpaque(false);
        pnlMedInput.setBorder(BorderFactory.createTitledBorder("Add Medicine"));

        pnlMedInput.add(new JLabel("Medicine Name:"));
        txtMedicine = new JTextField();
        pnlMedInput.add(txtMedicine);

        pnlMedInput.add(new JLabel("Dosage (e.g. 500mg - 1bd):"));
        txtDosage = new JTextField();
        pnlMedInput.add(txtDosage);

        pnlMedInput.add(new JLabel("Duration (e.g. 5 Days):"));
        txtDuration = new JTextField();
        pnlMedInput.add(txtDuration);

        leftPanel.add(pnlMedInput);

        btnAddMedicine = new JButton("Add Medicine to List");
        btnAddMedicine.setBackground(new Color(52, 152, 219));
        btnAddMedicine.setForeground(Color.WHITE);
        btnAddMedicine.setFocusPainted(false);
        leftPanel.add(btnAddMedicine);

        // Added Medicines Table
        String[] cols = {"Medicine Name", "Dosage", "Duration"};
        tableModel = new DefaultTableModel(cols, 0);
        medicineTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(medicineTable);
        tableScroll.setPreferredSize(new Dimension(400, 150));
        leftPanel.add(tableScroll);

        // Special Instructions Area
        JPanel pnlInst = new JPanel(new BorderLayout());
        pnlInst.setOpaque(false);
        pnlInst.setBorder(BorderFactory.createTitledBorder("Special Advice / Instructions"));
        txtInstructions = new JTextArea(3, 20);
        pnlInst.add(new JScrollPane(txtInstructions), BorderLayout.CENTER);
        leftPanel.add(pnlInst);

        add(leftPanel, BorderLayout.WEST);

        // ================= 3. RIGHT PANEL (PRESCRIPTION PREVIEW) =================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Prescription Slip Preview"));

        txtPrescriptionPreview = new JTextArea();
        txtPrescriptionPreview.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtPrescriptionPreview.setEditable(false);
        txtPrescriptionPreview.setBackground(new Color(254, 254, 235)); // Soft yellow paper color

        rightPanel.add(new JScrollPane(txtPrescriptionPreview), BorderLayout.CENTER);

        add(rightPanel, BorderLayout.CENTER);

        // ================= 4. BOTTOM PANEL (BUTTONS) =================
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnSavePrint = new JButton("Save & Print Prescription");
        btnSavePrint.setBackground(new Color(46, 204, 113));
        btnSavePrint.setForeground(Color.WHITE);
        btnSavePrint.setFocusPainted(false);

        btnClear = new JButton("Clear All");
        btnClear.setBackground(new Color(230, 126, 34));
        btnClear.setForeground(Color.WHITE);
        btnClear.setFocusPainted(false);

        btnBack = new JButton("Back");
        btnBack.setBackground(new Color(149, 165, 166));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);

        buttonPanel.add(btnSavePrint);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnBack);

        add(buttonPanel, BorderLayout.SOUTH);

        // ================= 5. EVENTS & LOGIC =================
        loadPatients();

        btnAddMedicine.addActionListener(e -> addMedicineToList());
        btnSavePrint.addActionListener(e -> saveAndPrintPrescription());
        btnClear.addActionListener(e -> clearForm());
        btnBack.addActionListener(e -> this.dispose());
    }

    private void loadPatients() {
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

    private void addMedicineToList() {
        String med = txtMedicine.getText().trim();
        String dosage = txtDosage.getText().trim();
        String duration = txtDuration.getText().trim();

        if (med.isEmpty() || dosage.isEmpty() || duration.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all medicine details!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.addRow(new Object[]{med, dosage, duration});

        txtMedicine.setText("");
        txtDosage.setText("");
        txtDuration.setText("");

        updatePreview();
    }

    private void updatePreview() {
        if (cmbPatients.getSelectedItem() == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("===============================================\n");
        sb.append("         HOSPITAL MANAGEMENT SYSTEM           \n");
        sb.append("               MEDICAL PRESCRIPTION            \n");
        sb.append("===============================================\n");
        sb.append("Date: ").append(java.time.LocalDate.now()).append("\n");
        sb.append("Patient: ").append(cmbPatients.getSelectedItem().toString()).append("\n");
        sb.append("-----------------------------------------------\n");
        sb.append(String.format("%-20s %-15s %-10s\n", "Medicine", "Dosage", "Duration"));
        sb.append("-----------------------------------------------\n");

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            sb.append(String.format("%-20s %-15s %-10s\n",
                    tableModel.getValueAt(i, 0),
                    tableModel.getValueAt(i, 1),
                    tableModel.getValueAt(i, 2)));
        }

        sb.append("-----------------------------------------------\n");
        sb.append("Instructions:\n").append(txtInstructions.getText()).append("\n");
        sb.append("===============================================\n");
        sb.append("Doctor's Signature: ___________________________\n");

        txtPrescriptionPreview.setText(sb.toString());
    }

    private void saveAndPrintPrescription() {
        if (cmbPatients.getSelectedItem() == null || tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Please add at least one medicine!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        updatePreview();

        int patientId = Integer.parseInt(cmbPatients.getSelectedItem().toString().split(" - ")[0]);
        String today = java.time.LocalDate.now().toString();

        StringBuilder medicines = new StringBuilder();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            medicines.append(tableModel.getValueAt(i, 0)).append(" (")
                    .append(tableModel.getValueAt(i, 1)).append(", ")
                    .append(tableModel.getValueAt(i, 2)).append("); ");
        }

        String query = "INSERT INTO prescriptions (patient_id, doctor_id, prescription_date, medicine_details, instructions) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setString(3, today);
            stmt.setString(4, medicines.toString());
            stmt.setString(5, txtInstructions.getText().trim());

            int inserted = stmt.executeUpdate();
            if (inserted > 0) {
                JOptionPane.showMessageDialog(this, "Prescription Saved to Database!");

                // Print Dialog Launching
                boolean printed = txtPrescriptionPreview.print();
                if (printed) {
                    JOptionPane.showMessageDialog(this, "Prescription Printed Successfully!");
                }
                clearForm();
            }
        } catch (PrinterException pe) {
            JOptionPane.showMessageDialog(this, "Printing Canceled/Failed: " + pe.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void clearForm() {
        tableModel.setRowCount(0);
        txtMedicine.setText("");
        txtDosage.setText("");
        txtDuration.setText("");
        txtInstructions.setText("");
        txtPrescriptionPreview.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new IssuePrescription(1).setVisible(true));
    }
}