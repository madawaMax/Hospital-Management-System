import javax.swing.*;
import java.awt.*;

public class DoctorDashboard extends JFrame {

    public DoctorDashboard(String username) {
        setTitle("Hospital Management System - Doctor Portal");
        setSize(900, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(46, 204, 113)); // Green Header
        headerPanel.setPreferredSize(new Dimension(900, 60));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblTitle = new JLabel("DOCTOR PORTAL");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            this.dispose();
            new LoginForm().setVisible(true);
        });

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnLogout, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Sidebar Menu
        JPanel sidebarPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        sidebarPanel.setBackground(new Color(44, 62, 80));
        sidebarPanel.setPreferredSize(new Dimension(200, 490));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        sidebarPanel.add(createMenuButton("My Appointments"));
        sidebarPanel.add(createMenuButton("Patient History"));
        sidebarPanel.add(createMenuButton("Issue Prescriptions"));
        sidebarPanel.add(createMenuButton("My Schedule"));

        add(sidebarPanel, BorderLayout.WEST);

        // Main Content
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblWelcome = new JLabel("Welcome, Dr. " + username.toUpperCase());
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 20));
        mainContent.add(lblWelcome, BorderLayout.NORTH);

        add(mainContent, BorderLayout.CENTER);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(52, 73, 94));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }
}