import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AdminDashboard extends JFrame {

    // Dynamic Counts පෙන්වීමට Labels සාදාගැනීම
    private JLabel lblDoctorCount;
    private JLabel lblPatientCount;
    private JLabel lblAppointmentCount;

    public AdminDashboard(String username) {
        setTitle("Hospital Management System - Admin Dashboard");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= 1. TOP HEADER PANEL =================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185)); // Dark Blue
        headerPanel.setPreferredSize(new Dimension(1000, 60));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblTitle = new JLabel("HOSPITAL MANAGEMENT SYSTEM - ADMIN");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JButton btnLogout = new JButton("Logout");
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

        // ================= 2. LEFT SIDEBAR MENU =================
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new GridLayout(8, 1, 5, 5));
        sidebarPanel.setBackground(new Color(52, 73, 94));
        sidebarPanel.setPreferredSize(new Dimension(200, 540));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnDoctors = createMenuButton("Manage Doctors");
        JButton btnPatients = createMenuButton("Manage Patients");
        JButton btnAppointments = createMenuButton("Appointments");
        JButton btnUsers = createMenuButton("Manage Users");
        JButton btnReports = createMenuButton("Reports");

        sidebarPanel.add(btnDoctors);
        sidebarPanel.add(btnPatients);
        sidebarPanel.add(btnAppointments);
        sidebarPanel.add(btnUsers);
        sidebarPanel.add(btnReports);

        add(sidebarPanel, BorderLayout.WEST);

        // Navigation Action Listeners
        btnDoctors.addActionListener(e -> {
            ManageDoctors frame = new ManageDoctors();
            frame.setVisible(true);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    loadDashboardCounts(); // Manage Window එක close වෙද්දී counts refresh වේ
                }
            });
        });

        // 2. Manage Patients Button Action
        btnPatients.addActionListener(e -> {
            ManagePatients frame = new ManagePatients();
            frame.setVisible(true);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    loadDashboardCounts(); // Patient Window එක Close වුණාම Counts Refresh වේ
                }
            });
        });

// 3. Manage Appointments Button Action
        btnAppointments.addActionListener(e -> {
            ManageAppointments frame = new ManageAppointments();
            frame.setVisible(true);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    loadDashboardCounts(); // Appointment Window එක Close වුණාම Counts Refresh වේ
                }
            });
        });
        btnUsers.addActionListener(e -> new ManageUsers().setVisible(true));

        btnReports.addActionListener(e -> {
            new ViewReports().setVisible(true);
        });

        // ================= 3. MAIN CONTENT AREA =================
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(new Color(236, 240, 241));
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Welcome Message
        JLabel lblWelcome = new JLabel("Welcome back, " + username.toUpperCase() + "!");
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 20));
        mainContentPanel.add(lblWelcome, BorderLayout.NORTH);

        // Summary Cards Panel
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Dynamic Count Labels මුලින් "0" ලෙස සකසයි
        lblDoctorCount = new JLabel("0", SwingConstants.CENTER);
        lblPatientCount = new JLabel("0", SwingConstants.CENTER);
        lblAppointmentCount = new JLabel("0", SwingConstants.CENTER);

        cardsPanel.add(createCard("Total Doctors", lblDoctorCount, new Color(46, 204, 113)));
        cardsPanel.add(createCard("Total Patients", lblPatientCount, new Color(155, 89, 182)));
        cardsPanel.add(createCard("Appointments Today", lblAppointmentCount, new Color(230, 126, 34)));

        mainContentPanel.add(cardsPanel, BorderLayout.CENTER);
        add(mainContentPanel, BorderLayout.CENTER);

        // Database එකෙන් Counts Load කිරීම
        loadDashboardCounts();
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(44, 62, 80));
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

        countLabel.setFont(new Font("Arial", Font.BOLD, 32));
        countLabel.setForeground(Color.WHITE);

        card.add(lblTitle);
        card.add(countLabel);

        return card;
    }

    // --- DATABASE COUNTS FETCHING LOGIC ---
    public void loadDashboardCounts() {
        try (Connection conn = DBConnection.getConnection()) {

            // 1. Total Doctors Count
            PreparedStatement stmtDoc = conn.prepareStatement("SELECT COUNT(*) FROM doctors");
            ResultSet rsDoc = stmtDoc.executeQuery();
            if (rsDoc.next()) {
                lblDoctorCount.setText(String.valueOf(rsDoc.getInt(1)));
            }

            // 2. Total Patients Count
            PreparedStatement stmtPat = conn.prepareStatement("SELECT COUNT(*) FROM patients");
            ResultSet rsPat = stmtPat.executeQuery();
            if (rsPat.next()) {
                lblPatientCount.setText(String.valueOf(rsPat.getInt(1)));
            }

            // 3. Appointments Today Count (අද දිනයට අදාළ appointments පමණක්)
            String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            PreparedStatement stmtApp = conn.prepareStatement("SELECT COUNT(*) FROM appointments WHERE appointment_date = ?");
            stmtApp.setString(1, todayDate);
            ResultSet rsApp = stmtApp.executeQuery();
            if (rsApp.next()) {
                lblAppointmentCount.setText(String.valueOf(rsApp.getInt(1)));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboard("Admin").setVisible(true));
    }
}