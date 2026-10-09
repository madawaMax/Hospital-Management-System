import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PatientBilling extends JFrame {

    private JComboBox<String> cmbPatients;
    private JTextField txtDoctorFee, txtHospitalFee, txtMedicineFee, txtTotal, txtPaid, txtBalance;
    private JTextArea txtInvoicePreview;
    private JTable billTable;
    private DefaultTableModel tableModel;
    private JButton btnCalculate, btnSavePrint, btnClear, btnBack;

    public PatientBilling() {
        setTitle("Staff Portal - Patient Billing & Invoice System");
        setSize(980, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= 1. HEADER PANEL =================
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(39, 174, 96)); // Green Theme
        headerPanel.setPreferredSize(new Dimension(980, 50));

        JLabel lblTitle = new JLabel("PATIENT BILLING & INVOICE MANAGEMENT");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);

        add(headerPanel, BorderLayout.NORTH);

        // ================= 2. LEFT PANEL (BILLING FORM) =================
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(420, 550));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Billing Details"));
        leftPanel.setBackground(new Color(245, 247, 250));

        // Patient Selector
        JPanel pnlPatient = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlPatient.setOpaque(false);
        pnlPatient.add(new JLabel("Select Patient: "));
        cmbPatients = new JComboBox<>();
        cmbPatients.setPreferredSize(new Dimension(260, 25));
        pnlPatient.add(cmbPatients);
        leftPanel.add(pnlPatient);

        // Fees Grid
        JPanel pnlFees = new JPanel(new GridLayout(6, 2, 5, 8));
        pnlFees.setOpaque(false);
        pnlFees.setBorder(BorderFactory.createTitledBorder("Charges Breakdown (LKR)"));

        pnlFees.add(new JLabel("Doctor Fee:"));
        txtDoctorFee = new JTextField("1500.00");
        pnlFees.add(txtDoctorFee);

        pnlFees.add(new JLabel("Hospital / Service Fee:"));
        txtHospitalFee = new JTextField("1000.00");
        pnlFees.add(txtHospitalFee);

        pnlFees.add(new JLabel("Medicine Fee:"));
        txtMedicineFee = new JTextField("0.00");
        pnlFees.add(txtMedicineFee);

        pnlFees.add(new JLabel("Total Amount:"));
        txtTotal = new JTextField("0.00");
        txtTotal.setEditable(false);
        txtTotal.setFont(new Font("Arial", Font.BOLD, 14));
        pnlFees.add(txtTotal);

        pnlFees.add(new JLabel("Paid Amount:"));
        txtPaid = new JTextField("0.00");
        pnlFees.add(txtPaid);

        pnlFees.add(new JLabel("Balance:"));
        txtBalance = new JTextField("0.00");
        txtBalance.setEditable(false);
        txtBalance.setFont(new Font("Arial", Font.BOLD, 14));
        pnlFees.add(txtBalance);

        leftPanel.add(pnlFees);

        btnCalculate = new JButton("Calculate Total");
        btnCalculate.setBackground(new Color(52, 152, 219));
        btnCalculate.setForeground(Color.WHITE);
        btnCalculate.setFocusPainted(false);
        leftPanel.add(btnCalculate);

        // Past Bills History Table
        String[] cols = {"Bill ID", "Patient", "Total", "Paid", "Date"};
        tableModel = new DefaultTableModel(cols, 0);
        billTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(billTable);
        tableScroll.setPreferredSize(new Dimension(400, 150));
        tableScroll.setBorder(BorderFactory.createTitledBorder("Recent Invoices"));
        leftPanel.add(tableScroll);

        add(leftPanel, BorderLayout.WEST);

        // ================= 3. RIGHT PANEL (INVOICE PREVIEW) =================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Invoice Receipt Preview"));

        txtInvoicePreview = new JTextArea();
        txtInvoicePreview.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtInvoicePreview.setEditable(false);
        txtInvoicePreview.setBackground(new Color(254, 254, 235));

        rightPanel.add(new JScrollPane(txtInvoicePreview), BorderLayout.CENTER);

        add(rightPanel, BorderLayout.CENTER);

        // ================= 4. BOTTOM PANEL =================
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnSavePrint = new JButton("Save & Print Invoice");
        btnSavePrint.setBackground(new Color(46, 204, 113));
        btnSavePrint.setForeground(Color.WHITE);

        btnClear = new JButton("Clear");
        btnClear.setBackground(new Color(230, 126, 34));
        btnClear.setForeground(Color.WHITE);

        btnBack = new JButton("Back to Dashboard");
        btnBack.setBackground(new Color(149, 165, 166));
        btnBack.setForeground(Color.WHITE);

        buttonPanel.add(btnSavePrint);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnBack);

        add(buttonPanel, BorderLayout.SOUTH);

        // ================= 5. EVENTS & LOGIC =================
        loadPatients();
        loadBillHistory();

        btnCalculate.addActionListener(e -> calculateTotals());
        btnSavePrint.addActionListener(e -> saveAndPrintInvoice());
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

    private void calculateTotals() {
        try {
            double docFee = Double.parseDouble(txtDoctorFee.getText().trim().isEmpty() ? "0" : txtDoctorFee.getText().trim());
            double hospFee = Double.parseDouble(txtHospitalFee.getText().trim().isEmpty() ? "0" : txtHospitalFee.getText().trim());
            double medFee = Double.parseDouble(txtMedicineFee.getText().trim().isEmpty() ? "0" : txtMedicineFee.getText().trim());

            double total = docFee + hospFee + medFee;
            txtTotal.setText(String.format("%.2f", total));

            double paid = Double.parseDouble(txtPaid.getText().trim().isEmpty() ? "0" : txtPaid.getText().trim());
            double balance = paid - total;
            txtBalance.setText(String.format("%.2f", balance));

            generatePreview();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric amounts!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generatePreview() {
        if (cmbPatients.getSelectedItem() == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("===============================================\n");
        sb.append("         HOSPITAL MANAGEMENT SYSTEM           \n");
        sb.append("                PAYMENT RECEIPT                \n");
        sb.append("===============================================\n");
        sb.append("Date: ").append(java.time.LocalDate.now()).append("\n");
        sb.append("Patient: ").append(cmbPatients.getSelectedItem().toString()).append("\n");
        sb.append("-----------------------------------------------\n");
        sb.append(String.format("%-25s : LKR %10s\n", "Doctor Fee", txtDoctorFee.getText()));
        sb.append(String.format("%-25s : LKR %10s\n", "Hospital / Service Fee", txtHospitalFee.getText()));
        sb.append(String.format("%-25s : LKR %10s\n", "Medicine Fee", txtMedicineFee.getText()));
        sb.append("-----------------------------------------------\n");
        sb.append(String.format("%-25s : LKR %10s\n", "TOTAL AMOUNT", txtTotal.getText()));
        sb.append(String.format("%-25s : LKR %10s\n", "PAID AMOUNT", txtPaid.getText()));
        sb.append(String.format("%-25s : LKR %10s\n", "BALANCE", txtBalance.getText()));
        sb.append("===============================================\n");
        sb.append("           Thank you for visiting!             \n");

        txtInvoicePreview.setText(sb.toString());
    }

    private void saveAndPrintInvoice() {
        calculateTotals();

        if (cmbPatients.getSelectedItem() == null) return;

        int patientId = Integer.parseInt(cmbPatients.getSelectedItem().toString().split(" - ")[0]);
        String today = java.time.LocalDate.now().toString();

        String query = "INSERT INTO billing (patient_id, bill_date, doctor_fee, hospital_fee, medicine_fee, total_amount, paid_amount, balance) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, patientId);
            stmt.setString(2, today);
            stmt.setDouble(3, Double.parseDouble(txtDoctorFee.getText()));
            stmt.setDouble(4, Double.parseDouble(txtHospitalFee.getText()));
            stmt.setDouble(5, Double.parseDouble(txtMedicineFee.getText()));
            stmt.setDouble(6, Double.parseDouble(txtTotal.getText()));
            stmt.setDouble(7, Double.parseDouble(txtPaid.getText()));
            stmt.setDouble(8, Double.parseDouble(txtBalance.getText()));

            if (stmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Invoice Saved Successfully!");

                // Print Receipt
                txtInvoicePreview.print();
                clearForm();
                loadBillHistory();
            }
        } catch (PrinterException pe) {
            JOptionPane.showMessageDialog(this, "Printing Error: " + pe.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving invoice: " + ex.getMessage());
        }
    }

    private void loadBillHistory() {
        tableModel.setRowCount(0);
        String query = "SELECT b.id, p.name, b.total_amount, b.paid_amount, b.bill_date " +
                "FROM billing b JOIN patients p ON b.patient_id = p.id ORDER BY b.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("total_amount"),
                        rs.getDouble("paid_amount"),
                        rs.getString("bill_date")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clearForm() {
        txtDoctorFee.setText("1500.00");
        txtHospitalFee.setText("1000.00");
        txtMedicineFee.setText("0.00");
        txtTotal.setText("0.00");
        txtPaid.setText("0.00");
        txtBalance.setText("0.00");
        txtInvoicePreview.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PatientBilling().setVisible(true));
    }
}