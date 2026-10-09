import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StaffDashboard extends JFrame {

    private String username;
    private JButton btnRegisterPatient, btnAppointments, btnBilling, btnLogout;
    private JLabel lblTotalPatients, lblTodayAppointments;

    public StaffDashboard(String username) {
        this.username = username;

        setTitle("Hospital Management System - Staff / Receptionist Portal");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= 1. HEADER PANEL =================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(241, 196, 15)); // Sun Yellow / Gold Header
        headerPanel.setPreferredSize(new Dimension(950, 60));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblTitle = new JLabel("STAFF & RECEPTIONIST PORTAL");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(new Color(44, 62, 80));

        btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(231, 76, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            this.dispose();
            new LoginForm().setVisible(true);
        });

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnLogout, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // ================= 2. SIDEBAR MENU =================
        JPanel sidebarPanel = new JPanel(new GridLayout(6, 1, 5, 10));
        sidebarPanel.setBackground(new Color(44, 62, 80));
        sidebarPanel.setPreferredSize(new Dimension(220, 540));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        btnRegisterPatient = createMenuButton("Register Patient");
        btnAppointments = createMenuButton("Book Appointment");
        btnBilling = createMenuButton("Patient Billing");

        sidebarPanel.add(btnRegisterPatient);
        sidebarPanel.add(btnAppointments);
        sidebarPanel.add(btnBilling);

        add(sidebarPanel, BorderLayout.WEST);

        // ================= 3. MAIN CONTENT AREA =================
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(new Color(236, 240, 241));
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Welcome Title
        JLabel lblWelcome = new JLabel("Welcome, " + username.toUpperCase());
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 22));
        lblWelcome.setForeground(new Color(44, 62, 80));
        mainContent.add(lblWelcome, BorderLayout.NORTH);

        // Summary Cards Panel
        JPanel cardsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 100, 0));

        lblTotalPatients = new JLabel("0", SwingConstants.CENTER);
        lblTodayAppointments = new JLabel("0", SwingConstants.CENTER);

        cardsPanel.add(createCard("Total Registered Patients", lblTotalPatients, new Color(52, 152, 219)));
        cardsPanel.add(createCard("Today's Appointments", lblTodayAppointments, new Color(155, 89, 182)));

        mainContent.add(cardsPanel, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        // ================= 4. ACTION LISTENERS =================

        btnRegisterPatient.addActionListener(e -> {
            RegisterPatient frame = new RegisterPatient();
            frame.setVisible(true);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    loadCounts(); // Register Window එක Close කළ පසු Counts Refresh වේ
                }
            });
        });

        btnAppointments.addActionListener(e -> {
            BookAppointment frame = new BookAppointment();
            frame.setVisible(true);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    loadCounts();
                }
            });
        });

        btnBilling.addActionListener(e -> {
            new PatientBilling().setVisible(true);
        });

        loadCounts();
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(52, 73, 94));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    private JPanel createCard(String title, JLabel countLabel, Color bgColor) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        countLabel.setFont(new Font("Arial", Font.BOLD, 36));
        countLabel.setForeground(Color.WHITE);

        card.add(lblTitle);
        card.add(countLabel);

        return card;
    }

    private void loadCounts() {
        try (Connection conn = DBConnection.getConnection()) {

            // 1. Total Patients Count
            PreparedStatement stmt1 = conn.prepareStatement("SELECT COUNT(*) FROM patients");
            ResultSet rs1 = stmt1.executeQuery();
            if (rs1.next()) {
                lblTotalPatients.setText(String.valueOf(rs1.getInt(1)));
            }

            // 2. Today's Appointments Count
            String today = java.time.LocalDate.now().toString();
            PreparedStatement stmt2 = conn.prepareStatement("SELECT COUNT(*) FROM appointments WHERE appointment_date = ?");
            stmt2.setString(1, today);
            ResultSet rs2 = stmt2.executeQuery();
            if (rs2.next()) {
                lblTodayAppointments.setText(String.valueOf(rs2.getInt(1)));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StaffDashboard("Staff_User").setVisible(true));
    }
}