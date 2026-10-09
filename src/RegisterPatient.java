import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegisterPatient extends JFrame {

    private JTextField txtId, txtName, txtAge, txtContact, txtAddress, txtSearch;
    private JComboBox<String> cmbGender;
    private JTable patientTable;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch, btnBack;

    public RegisterPatient() {
        setTitle("Staff Portal - Patient Registration & Management");
        setSize(950, 620);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= 1. HEADER PANEL =================
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 152, 219)); // Blue Theme
        headerPanel.setPreferredSize(new Dimension(950, 50));

        JLabel lblTitle = new JLabel("REGISTER & MANAGE PATIENTS");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);

        add(headerPanel, BorderLayout.NORTH);

        // ================= 2. LEFT FORM PANEL =================
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 12));
        formPanel.setPreferredSize(new Dimension(360, 420));
        formPanel.setBorder(BorderFactory.createTitledBorder("Patient Details Form"));
        formPanel.setBackground(new Color(245, 247, 250));

        formPanel.add(new JLabel("Patient ID:"));
        txtId = new JTextField();
        txtId.setEditable(false);
        formPanel.add(txtId);

        formPanel.add(new JLabel("Full Name:"));
        txtName = new JTextField();
        formPanel.add(txtName);

        formPanel.add(new JLabel("Age:"));
        txtAge = new JTextField();
        formPanel.add(txtAge);

        formPanel.add(new JLabel("Gender:"));
        cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        formPanel.add(cmbGender);

        formPanel.add(new JLabel("Contact Number:"));
        txtContact = new JTextField();
        formPanel.add(txtContact);

        formPanel.add(new JLabel("Address:"));
        txtAddress = new JTextField();
        formPanel.add(txtAddress);

        // Buttons Grid
        btnAdd = new JButton("Register");
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

        // ================= 3. RIGHT TABLE & SEARCH PANEL =================
        JPanel rightPanel = new JPanel(new BorderLayout());

        // Search Sub-panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(new Color(236, 240, 241));
        searchPanel.add(new JLabel("Search Patient Name/Contact:"));
        txtSearch = new JTextField(15);
        searchPanel.add(txtSearch);

        btnSearch = new JButton("Search");
        btnSearch.setBackground(new Color(52, 73, 94));
        btnSearch.setForeground(Color.WHITE);
        searchPanel.add(btnSearch);

        rightPanel.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Name", "Age", "Gender", "Contact", "Address"};
        tableModel = new DefaultTableModel(columns, 0);
        patientTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(patientTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Registered Patients List"));
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
        loadPatients();

        // Table Row Selection
        patientTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = patientTable.getSelectedRow();
                txtId.setText(tableModel.getValueAt(selectedRow, 0).toString());
                txtName.setText(tableModel.getValueAt(selectedRow, 1).toString());
                txtAge.setText(tableModel.getValueAt(selectedRow, 2).toString());
                cmbGender.setSelectedItem(tableModel.getValueAt(selectedRow, 3).toString());
                txtContact.setText(tableModel.getValueAt(selectedRow, 4).toString());
                txtAddress.setText(tableModel.getValueAt(selectedRow, 5).toString());
            }
        });

        btnAdd.addActionListener(e -> registerPatient());
        btnUpdate.addActionListener(e -> updatePatient());
        btnDelete.addActionListener(e -> deletePatient());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> searchPatients());
        btnBack.addActionListener(e -> this.dispose());
    }

    // --- DATABASE LOGIC ---

    private void loadPatients() {
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

    private void registerPatient() {
        if (txtName.getText().trim().isEmpty() || txtContact.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill Full Name and Contact Number!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String query = "INSERT INTO patients (name, age, gender, contact, address) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, txtName.getText().trim());
            stmt.setInt(2, Integer.parseInt(txtAge.getText().trim().isEmpty() ? "0" : txtAge.getText().trim()));
            stmt.setString(3, cmbGender.getSelectedItem().toString());
            stmt.setString(4, txtContact.getText().trim());
            stmt.setString(5, txtAddress.getText().trim());

            if (stmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Patient Registered Successfully!");
                clearForm();
                loadPatients();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error registering patient: " + ex.getMessage());
        }
    }

    private void updatePatient() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a patient from table to update!");
            return;
        }

        String query = "UPDATE patients SET name = ?, age = ?, gender = ?, contact = ?, address = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, txtName.getText().trim());
            stmt.setInt(2, Integer.parseInt(txtAge.getText().trim()));
            stmt.setString(3, cmbGender.getSelectedItem().toString());
            stmt.setString(4, txtContact.getText().trim());
            stmt.setString(5, txtAddress.getText().trim());
            stmt.setInt(6, Integer.parseInt(txtId.getText()));

            if (stmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Patient Details Updated!");
                clearForm();
                loadPatients();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating patient: " + ex.getMessage());
        }
    }

    private void deletePatient() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a patient from table to delete!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this patient?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM patients WHERE id = ?")) {

                stmt.setInt(1, Integer.parseInt(txtId.getText()));
                if (stmt.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Patient Deleted!");
                    clearForm();
                    loadPatients();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting patient: " + ex.getMessage());
            }
        }
    }

    private void searchPatients() {
        String keyword = txtSearch.getText().trim();
        tableModel.setRowCount(0);

        String query = "SELECT * FROM patients WHERE name LIKE ? OR contact LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");

            ResultSet rs = stmt.executeQuery();
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

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtAge.setText("");
        cmbGender.setSelectedIndex(0);
        txtContact.setText("");
        txtAddress.setText("");
        txtSearch.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegisterPatient().setVisible(true));
    }
}