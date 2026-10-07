import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginForm extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginForm() {
        // Frame basic setup
        setTitle("Hospital Management System - Login");
        setSize(400, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // UI Components Manual Design (Fallback GUI)
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(245, 246, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel lblTitle = new JLabel("HMS LOGIN PORTAL", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(new Color(41, 128, 185));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(lblTitle, gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Username:"), gbc);

        txtUsername = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 1;
        mainPanel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Password:"), gbc);

        txtPassword = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 2;
        mainPanel.add(txtPassword, gbc);

        // Login Button
        btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        mainPanel.add(btnLogin, gbc);

        setContentPane(mainPanel);

        // Action Listener
        btnLogin.addActionListener((ActionEvent e) -> authenticateUser());
    }

    private void authenticateUser() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both Username and Password!",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this,
                        "Database Connection Failed!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String query = "SELECT role FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role"); // DB එකේ තියෙන role එක ගන්නවා (Admin / Doctor / Staff)

                JOptionPane.showMessageDialog(this,
                        "Login Successful as " + role + "!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                this.dispose(); // Login window එක වහනවා

                // Role එක අනුව අදාළ Dashboard එක Open කිරීම
                if (role.equalsIgnoreCase("Admin")) {
                    new AdminDashboard(username).setVisible(true);
                } else if (role.equalsIgnoreCase("Doctor")) {
                    new DoctorDashboard(username).setVisible(true);
                } else if (role.equalsIgnoreCase("Staff") || role.equalsIgnoreCase("Receptionist")) {
                    new StaffDashboard(username).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Unknown Role Type Assigned!");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}