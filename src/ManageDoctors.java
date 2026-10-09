import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ManageDoctors extends JFrame {

    private JTextField txtId, txtName, txtSpecialization, txtContact, txtEmail;
    private JTable doctorTable;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnBack;

    public ManageDoctors() {
        setTitle("Hospital Management System - Manage Doctors");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= 1. HEADER PANEL =================
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setPreferredSize(new Dimension(950, 50));

        JLabel lblTitle = new JLabel("DOCTOR MANAGEMENT PORTAL");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);

        add(headerPanel, BorderLayout.NORTH);

        // ================= 2. FORM INPUT PANEL (LEFT) =================
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Doctor Details"));
        formPanel.setPreferredSize(new Dimension(350, 550));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form Fields
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Doctor ID:"), gbc);
        txtId = new JTextField(15);
        txtId.setEditable(false); //
        gbc.gridx = 1;
        formPanel.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Name:"), gbc);
        txtName = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Specialization:"), gbc);
        txtSpecialization = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtSpecialization, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Contact No:"), gbc);
        txtContact = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtContact, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Email:"), gbc);
        txtEmail = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtEmail, gbc);

        // Buttons Panel inside Form
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        btnAdd = createButton("Add Doctor", new Color(46, 204, 113));
        btnUpdate = createButton("Update", new Color(241, 196, 15));
        btnDelete = createButton("Delete", new Color(231, 76, 60));
        btnClear = createButton("Clear", new Color(149, 165, 166));
        btnBack = createButton("Back to Dashboard", new Color(52, 73, 94));

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnBack);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        add(formPanel, BorderLayout.WEST);

        // ================= 3. TABLE PANEL (RIGHT) =================
        String[] columns = {"ID", "Name", "Specialization", "Contact", "Email"};
        tableModel = new DefaultTableModel(columns, 0);
        doctorTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(doctorTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Registered Doctors List"));
        add(scrollPane, BorderLayout.CENTER);

        // ================= 4. EVENTS AND LOGIC =================


        loadDoctorData();


        doctorTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = doctorTable.getSelectedRow();
                txtId.setText(tableModel.getValueAt(selectedRow, 0).toString());
                txtName.setText(tableModel.getValueAt(selectedRow, 1).toString());
                txtSpecialization.setText(tableModel.getValueAt(selectedRow, 2).toString());
                txtContact.setText(tableModel.getValueAt(selectedRow, 3).toString());
                txtEmail.setText(tableModel.getValueAt(selectedRow, 4).toString());
            }
        });

        // Add Button
        btnAdd.addActionListener(e -> addDoctor());

        // Update Button
        btnUpdate.addActionListener(e -> updateDoctor());

        // Delete Button
        btnDelete.addActionListener(e -> deleteDoctor());

        // Clear Button
        btnClear.addActionListener(e -> clearFields());

        // Back Button
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


    private void loadDoctorData() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM doctors";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

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


    private void addDoctor() {
        if (txtName.getText().isEmpty() || txtSpecialization.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill required fields!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO doctors (name, specialization, contact, email) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, txtName.getText());
            stmt.setString(2, txtSpecialization.getText());
            stmt.setString(3, txtContact.getText());
            stmt.setString(4, txtEmail.getText());

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Doctor Added Successfully!");
            clearFields();
            loadDoctorData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }


    private void updateDoctor() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to update!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "UPDATE doctors SET name=?, specialization=?, contact=?, email=? WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, txtName.getText());
            stmt.setString(2, txtSpecialization.getText());
            stmt.setString(3, txtContact.getText());
            stmt.setString(4, txtEmail.getText());
            stmt.setInt(5, Integer.parseInt(txtId.getText()));

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Doctor Details Updated!");
            clearFields();
            loadDoctorData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }


    private void deleteDoctor() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to delete!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this doctor?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String query = "DELETE FROM doctors WHERE id=?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, Integer.parseInt(txtId.getText()));

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Doctor Deleted!");
                clearFields();
                loadDoctorData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }


    private void clearFields() {
        txtId.setText("");
        txtName.setText("");
        txtSpecialization.setText("");
        txtContact.setText("");
        txtEmail.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManageDoctors().setVisible(true));
    }
}