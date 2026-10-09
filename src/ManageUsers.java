import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ManageUsers extends JFrame {

    private JTextField txtId, txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRole;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnBack;

    public ManageUsers() {
        setTitle("Hospital Management System - Manage System Users");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= 1. HEADER PANEL =================
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 73, 94)); // Dark Gray/Blue Theme
        headerPanel.setPreferredSize(new Dimension(950, 50));

        JLabel lblTitle = new JLabel("USER ACCOUNT MANAGEMENT PORTAL");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);

        add(headerPanel, BorderLayout.NORTH);

        // ================= 2. FORM INPUT PANEL (LEFT) =================
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("User Account Details"));
        formPanel.setPreferredSize(new Dimension(350, 550));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form Fields
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("User ID:"), gbc);
        txtId = new JTextField(15);
        txtId.setEditable(false);
        gbc.gridx = 1;
        formPanel.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Username:"), gbc);
        txtUsername = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Password:"), gbc);
        txtPassword = new JPasswordField(15);
        gbc.gridx = 1;
        formPanel.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Role:"), gbc);
        cmbRole = new JComboBox<>(new String[]{"Admin", "Doctor", "Staff"});
        gbc.gridx = 1;
        formPanel.add(cmbRole, gbc);

        // Buttons Panel inside Form
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        btnAdd = createButton("Add User", new Color(46, 204, 113));
        btnUpdate = createButton("Update", new Color(241, 196, 15));
        btnDelete = createButton("Delete", new Color(231, 76, 60));
        btnClear = createButton("Clear", new Color(149, 165, 166));
        btnBack = createButton("Back", new Color(41, 128, 185));

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnBack);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        add(formPanel, BorderLayout.WEST);

        // ================= 3. TABLE PANEL (RIGHT) =================
        String[] columns = {"ID", "Username", "Role"};
        tableModel = new DefaultTableModel(columns, 0);
        userTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("System Users List"));
        add(scrollPane, BorderLayout.CENTER);

        // ================= 4. EVENTS & DATABASE LOGIC =================

        loadUserData();

        // Table Click Event -> Fill Text Fields
        userTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = userTable.getSelectedRow();
                txtId.setText(tableModel.getValueAt(selectedRow, 0).toString());
                txtUsername.setText(tableModel.getValueAt(selectedRow, 1).toString());
                cmbRole.setSelectedItem(tableModel.getValueAt(selectedRow, 2).toString());
                txtPassword.setText(""); // Security සඳහා Password එක Empty තබයි
            }
        });

        btnAdd.addActionListener(e -> addUser());
        btnUpdate.addActionListener(e -> updateUser());
        btnDelete.addActionListener(e -> deleteUser());
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

    private void loadUserData() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT user_id, username, role FROM users";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addUser() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Username and Password!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(true);
            String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, cmbRole.getSelectedItem().toString());

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "User Created Successfully!");
            clearFields();
            loadUserData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void updateUser() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a user to update!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String password = new String(txtPassword.getPassword());

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(true);
            String query;
            PreparedStatement stmt;


            if (password.isEmpty()) {
                query = "UPDATE users SET username=?, role=? WHERE user_id=?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, txtUsername.getText());
                stmt.setString(2, cmbRole.getSelectedItem().toString());
                stmt.setInt(3, Integer.parseInt(txtId.getText()));
            } else {
                query = "UPDATE users SET username=?, password=?, role=? WHERE user_id=?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, txtUsername.getText());
                stmt.setString(2, password);
                stmt.setString(3, cmbRole.getSelectedItem().toString());
                stmt.setInt(4, Integer.parseInt(txtId.getText()));
            }

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "User Updated!");
            clearFields();
            loadUserData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deleteUser() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a user to delete!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this user?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(true);
                String query = "DELETE FROM users WHERE user_id=?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, Integer.parseInt(txtId.getText()));

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "User Deleted!");
                clearFields();
                loadUserData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void clearFields() {
        txtId.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        cmbRole.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManageUsers().setVisible(true));
    }
}