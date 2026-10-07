import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ViewReports extends JFrame {

    private JComboBox<String> cmbReportType;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JButton btnGenerate, btnPrint, btnBack;

    public ViewReports() {
        setTitle("Hospital Management System - System Reports");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= 1. HEADER PANEL =================
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(44, 62, 80)); // Midnight Blue Theme
        headerPanel.setPreferredSize(new Dimension(950, 50));

        JLabel lblTitle = new JLabel("HOSPITAL SYSTEM REPORTS");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);

        add(headerPanel, BorderLayout.NORTH);

        // ================= 2. CONTROL PANEL (TOP) =================
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        controlPanel.setBackground(new Color(236, 240, 241));

        controlPanel.add(new JLabel("Select Report Type:"));
        cmbReportType = new JComboBox<>(new String[]{
                "Appointments Report",
                "Today's Appointments",
                "Doctors Summary",
                "Patients Summary"
        });
        controlPanel.add(cmbReportType);

        btnGenerate = new JButton("Generate Report");
        btnGenerate.setBackground(new Color(52, 152, 219));
        btnGenerate.setForeground(Color.WHITE);
        btnGenerate.setFocusPainted(false);

        btnPrint = new JButton("Print Report");
        btnPrint.setBackground(new Color(46, 204, 113));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFocusPainted(false);

        btnBack = new JButton("Back");
        btnBack.setBackground(new Color(149, 165, 166));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);

        controlPanel.add(btnGenerate);
        controlPanel.add(btnPrint);
        controlPanel.add(btnBack);

        add(controlPanel, BorderLayout.SOUTH);

        // ================= 3. TABLE PANEL (CENTER) =================
        tableModel = new DefaultTableModel();
        reportTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Report Output"));
        add(scrollPane, BorderLayout.CENTER);

        // ================= 4. EVENTS & LOGIC =================

        // Default Load First Report
        loadAppointmentsReport(false);

        btnGenerate.addActionListener(e -> generateSelectedReport());
        btnPrint.addActionListener(e -> printTable());
        btnBack.addActionListener(e -> this.dispose());
    }

    private void generateSelectedReport() {
        int selectedIndex = cmbReportType.getSelectedIndex();
        switch (selectedIndex) {
            case 0:
                loadAppointmentsReport(false); // All Appointments
                break;
            case 1:
                loadAppointmentsReport(true);  // Today's Appointments Only
                break;
            case 2:
                loadDoctorsReport();
                break;
            case 3:
                loadPatientsReport();
                break;
        }
    }

    // 1. Appointments Report (All / Today)
    private void loadAppointmentsReport(boolean todayOnly) {
        tableModel.setColumnIdentifiers(new String[]{"App. ID", "Patient Name", "Doctor Name", "Date", "Status"});
        tableModel.setRowCount(0);

        String query = "SELECT a.id, p.name AS patient_name, d.name AS doctor_name, a.appointment_date, a.status " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "JOIN doctors d ON a.doctor_id = d.id";

        if (todayOnly) {
            query += " WHERE a.appointment_date = CURDATE()";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

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

    // 2. Doctors Summary Report
    private void loadDoctorsReport() {
        tableModel.setColumnIdentifiers(new String[]{"Doctor ID", "Doctor Name", "Specialization", "Contact Number", "Email"});
        tableModel.setRowCount(0);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM doctors");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("specialization"),
                        rs.getString("contact"),
                        rs.getString("email")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // 3. Patients Summary Report
    private void loadPatientsReport() {
        tableModel.setColumnIdentifiers(new String[]{"Patient ID", "Patient Name", "Age", "Gender", "Contact", "Address"});
        tableModel.setRowCount(0);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM patients");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("contact"),
                        rs.getString("address")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // JTable Print Feature (PDF / Printer එකට කෙලින්ම Print දිය හැක)
    private void printTable() {
        try {
            boolean complete = reportTable.print(JTable.PrintMode.FIT_WIDTH,
                    new java.text.MessageFormat(cmbReportType.getSelectedItem().toString()),
                    new java.text.MessageFormat("Page {0}"));
            if (complete) {
                JOptionPane.showMessageDialog(this, "Printing Completed!", "Result", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Printing Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ViewReports().setVisible(true));
    }
}