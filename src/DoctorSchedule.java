import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DoctorSchedule extends JFrame {

    private int doctorId;
    private JComboBox<String> cmbDay, cmbStatus;
    private JTextField txtStartTime, txtEndTime, txtMaxPatients, txtScheduleId;
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnBack;

    public DoctorSchedule(int doctorId) {
        this.doctorId = doctorId;

        setTitle("Doctor Portal - Manage My Schedule");
        setSize(900, 580);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        //  HEADER PANEL
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(22, 160, 133)); // Teal Theme
        headerPanel.setPreferredSize(new Dimension(900, 50));

        JLabel lblTitle = new JLabel("MY WORKING SCHEDULE & AVAILABILITY");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);

        add(headerPanel, BorderLayout.NORTH);

        // ================= 2. LEFT FORM PANEL
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 15));
        formPanel.setPreferredSize(new Dimension(360, 400));
        formPanel.setBorder(BorderFactory.createTitledBorder("Schedule Details"));
        formPanel.setBackground(new Color(245, 247, 250));

        formPanel.add(new JLabel("Schedule ID:"));
        txtScheduleId = new JTextField();
        txtScheduleId.setEditable(false);
        formPanel.add(txtScheduleId);

        formPanel.add(new JLabel("Available Day:"));
        cmbDay = new JComboBox<>(new String[]{
                "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
        });
        formPanel.add(cmbDay);

        formPanel.add(new JLabel("Start Time (e.g. 09:00 AM):"));
        txtStartTime = new JTextField();
        formPanel.add(txtStartTime);

        formPanel.add(new JLabel("End Time (e.g. 01:00 PM):"));
        txtEndTime = new JTextField();
        formPanel.add(txtEndTime);

        formPanel.add(new JLabel("Max Patients:"));
        txtMaxPatients = new JTextField("20");
        formPanel.add(txtMaxPatients);

        formPanel.add(new JLabel("Status:"));
        cmbStatus = new JComboBox<>(new String[]{"Active", "Inactive"});
        formPanel.add(cmbStatus);

        // Buttons Grid
        btnAdd = new JButton("Add Schedule");
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.WHITE);

        btnUpdate = new JButton("Update");
        btnUpdate.setBackground(new Color(52, 152, 219));
        btnUpdate.setForeground(Color.WHITE);

        btnDelete = new JButton("Delete");
        btnDelete.setBackground(new Color(231, 76, 60));
        btnDelete.setForeground(Color.WHITE);

        btnClear = new JButton("Clear");
        btnClear.setBackground(new Color(230, 126, 34));
        btnClear.setForeground(Color.WHITE);

        JPanel pnlBtnTop = new JPanel(new GridLayout(1, 2, 5, 5));
        pnlBtnTop.setOpaque(false);
        pnlBtnTop.add(btnAdd);
        pnlBtnTop.add(btnUpdate);

        JPanel pnlBtnBottom = new JPanel(new GridLayout(1, 2, 5, 5));
        pnlBtnBottom.setOpaque(false);
        pnlBtnBottom.add(btnDelete);
        pnlBtnBottom.add(btnClear);

        formPanel.add(pnlBtnTop);
        formPanel.add(pnlBtnBottom);

        add(formPanel, BorderLayout.WEST);

        //  RIGHT TABLE PANEL =
        String[] columns = {"ID", "Day", "Start Time", "End Time", "Max Patients", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        scheduleTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Current Active Schedules"));
        add(scrollPane, BorderLayout.CENTER);


        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnBack = new JButton("Back to Dashboard");
        btnBack.setBackground(new Color(149, 165, 166));
        btnBack.setForeground(Color.WHITE);
        bottomPanel.add(btnBack);

        add(bottomPanel, BorderLayout.SOUTH);

        //  EVENTS & LOGIC
        loadSchedules();

        // Table Click Event
        scheduleTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = scheduleTable.getSelectedRow();
                txtScheduleId.setText(tableModel.getValueAt(selectedRow, 0).toString());
                cmbDay.setSelectedItem(tableModel.getValueAt(selectedRow, 1).toString());
                txtStartTime.setText(tableModel.getValueAt(selectedRow, 2).toString());
                txtEndTime.setText(tableModel.getValueAt(selectedRow, 3).toString());
                txtMaxPatients.setText(tableModel.getValueAt(selectedRow, 4).toString());
                cmbStatus.setSelectedItem(tableModel.getValueAt(selectedRow, 5).toString());
            }
        });

        btnAdd.addActionListener(e -> addSchedule());
        btnUpdate.addActionListener(e -> updateSchedule());
        btnDelete.addActionListener(e -> deleteSchedule());
        btnClear.addActionListener(e -> clearForm());
        btnBack.addActionListener(e -> this.dispose());
    }

    // DATABASE OPERATIONS

    private void loadSchedules() {
        tableModel.setRowCount(0);
        String query = "SELECT * FROM doctor_schedule WHERE doctor_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("available_day"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        rs.getInt("max_patients"),
                        rs.getString("status")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addSchedule() {
        if (txtStartTime.getText().trim().isEmpty() || txtEndTime.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Start Time and End Time!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String query = "INSERT INTO doctor_schedule (doctor_id, available_day, start_time, end_time, max_patients, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, doctorId);
            stmt.setString(2, cmbDay.getSelectedItem().toString());
            stmt.setString(3, txtStartTime.getText().trim());
            stmt.setString(4, txtEndTime.getText().trim());
            stmt.setInt(5, Integer.parseInt(txtMaxPatients.getText().trim()));
            stmt.setString(6, cmbStatus.getSelectedItem().toString());

            if (stmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Schedule Added Successfully!");
                clearForm();
                loadSchedules();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding schedule: " + ex.getMessage());
        }
    }

    private void updateSchedule() {
        if (txtScheduleId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a schedule to update!");
            return;
        }

        String query = "UPDATE doctor_schedule SET available_day = ?, start_time = ?, end_time = ?, max_patients = ?, status = ? WHERE id = ? AND doctor_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, cmbDay.getSelectedItem().toString());
            stmt.setString(2, txtStartTime.getText().trim());
            stmt.setString(3, txtEndTime.getText().trim());
            stmt.setInt(4, Integer.parseInt(txtMaxPatients.getText().trim()));
            stmt.setString(5, cmbStatus.getSelectedItem().toString());
            stmt.setInt(6, Integer.parseInt(txtScheduleId.getText()));
            stmt.setInt(7, doctorId);

            if (stmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Schedule Updated Successfully!");
                clearForm();
                loadSchedules();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating schedule: " + ex.getMessage());
        }
    }

    private void deleteSchedule() {
        if (txtScheduleId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a schedule to delete!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this schedule slot?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String query = "DELETE FROM doctor_schedule WHERE id = ? AND doctor_id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, Integer.parseInt(txtScheduleId.getText()));
                stmt.setInt(2, doctorId);

                if (stmt.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Schedule Deleted!");
                    clearForm();
                    loadSchedules();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting schedule: " + ex.getMessage());
            }
        }
    }

    private void clearForm() {
        txtScheduleId.setText("");
        cmbDay.setSelectedIndex(0);
        txtStartTime.setText("");
        txtEndTime.setText("");
        txtMaxPatients.setText("20");
        cmbStatus.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DoctorSchedule(1).setVisible(true));
    }
}