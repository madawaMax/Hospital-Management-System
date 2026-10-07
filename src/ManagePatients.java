import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ManagePatients extends JFrame {

    private JTextField txtId, txtName, txtAge, txtContact, txtAddress;
    private JComboBox<String> cmbGender;
    private JTable patientTable;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnBack;

    public ManagePatients() {
        setTitle("Hospital Management System - Manage Patients");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= 1. HEADER PANEL =================
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(155, 89, 182)); // Purple Theme
        headerPanel.setPreferredSize(new Dimension(950, 50));

        JLabel lblTitle = new JLabel("PATIENT MANAGEMENT PORTAL");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);

        add(headerPanel, BorderLayout.NORTH);

        // ================= 2. FORM INPUT PANEL (LEFT) =================
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Patient Details"));
        formPanel.setPreferredSize(new Dimension(350, 550));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form Fields
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Patient ID:"), gbc);
        txtId = new JTextField(15);
        txtId.setEditable(false);
        gbc.gridx = 1;
        formPanel.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Name:"), gbc);
        txtName = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Age:"), gbc);
        txtAge = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtAge, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Gender:"), gbc);
        cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        gbc.gridx = 1;
        formPanel.add(cmbGender, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Contact No:"), gbc);
        txtContact = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtContact, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Address:"), gbc);
        txtAddress = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtAddress, gbc);

        // Buttons Panel inside Form
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        btnAdd = createButton("Add Patient", new Color(46, 204, 113));
        btnUpdate = createButton("Update", new Color(241, 196, 15));
        btnDelete = createButton("Delete", new Color(231, 76, 60));
        btnClear = createButton("Clear", new Color(149, 165, 166));
        btnBack = createButton("Back", new Color(52, 73, 94));

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnBack);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        add(formPanel, BorderLayout.WEST);

        // ================= 3. TABLE PANEL (RIGHT) =================
        String[] columns = {"ID", "Name", "Age", "Gender", "Contact", "Address"};
        tableModel = new DefaultTableModel(columns, 0);
        patientTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(patientTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Registered Patients List"));
        add(scrollPane, BorderLayout.CENTER);

        // ================= 4. EVENTS & DATABASE LOGIC =================

        loadPatientData();

        // Table Click Event -> Fill Text Fields
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

        btnAdd.addActionListener(e -> addPatient());
        btnUpdate.addActionListener(e -> updatePatient());
        btnDelete.addActionListener(e -> deletePatient());
        btnClear.addActionListener(e -> clearFields());
        btnBack.addActionListener(e -> this.dispose());
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        return btn;
    }

    // --- DATABASE OPERATIONS ---

    private void loadPatientData() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM patients";
            PreparedStatement stmt = conn.prepareStatement(query);
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

    private void addPatient() {
        if (txtName.getText().isEmpty() || txtAge.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill required fields!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(true);
            String query = "INSERT INTO patients (name, age, gender, contact, address) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, txtName.getText());
            stmt.setInt(2, Integer.parseInt(txtAge.getText()));
            stmt.setString(3, cmbGender.getSelectedItem().toString());
            stmt.setString(4, txtContact.getText());
            stmt.setString(5, txtAddress.getText());

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Patient Added Successfully!");
            clearFields();
            loadPatientData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void updatePatient() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a patient to update!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(true);
            String query = "UPDATE patients SET name=?, age=?, gender=?, contact=?, address=? WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, txtName.getText());
            stmt.setInt(2, Integer.parseInt(txtAge.getText()));
            stmt.setString(3, cmbGender.getSelectedItem().toString());
            stmt.setString(4, txtContact.getText());
            stmt.setString(5, txtAddress.getText());
            stmt.setInt(6, Integer.parseInt(txtId.getText()));

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Patient Updated!");
            clearFields();
            loadPatientData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deletePatient() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a patient to delete!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this patient?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(true);
                String query = "DELETE FROM patients WHERE id=?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, Integer.parseInt(txtId.getText()));

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Patient Deleted!");
                clearFields();
                loadPatientData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void clearFields() {
        txtId.setText("");
        txtName.setText("");
        txtAge.setText("");
        cmbGender.setSelectedIndex(0);
        txtContact.setText("");
        txtAddress.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManagePatients().setVisible(true));
    }
}