import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DoctorDashboard extends JFrame {

    private int doctorId;
    private String username;
    private JButton btnAppointments, btnPatientHistory, btnPrescriptions, btnSchedule, btnLogout;
    private JLabel lblTodayAppointments, lblPendingAppointments;

    public DoctorDashboard(int doctorId, String username) {
        this.doctorId = doctorId;
        this.username = username;

        setTitle("Hospital Management System - Doctor Portal");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= 1. HEADER PANEL =================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(46, 204, 113)); // Emerald Green Header
        headerPanel.setPreferredSize(new Dimension(950, 60));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblTitle = new JLabel("DOCTOR PORTAL");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

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

        btnAppointments = createMenuButton("My Appointments");
        btnPatientHistory = createMenuButton("Patient History");
        btnPrescriptions = createMenuButton("Issue Prescriptions");
        btnSchedule = createMenuButton("My Schedule");

        sidebarPanel.add(btnAppointments);
        sidebarPanel.add(btnPatientHistory);
        sidebarPanel.add(btnPrescriptions);
        sidebarPanel.add(btnSchedule);

        add(sidebarPanel, BorderLayout.WEST);

        // ================= 3. MAIN CONTENT AREA =================
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(new Color(236, 240, 241));
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Welcome Title
        JLabel lblWelcome = new JLabel("Welcome, Dr. " + username.toUpperCase());
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 22));
        lblWelcome.setForeground(new Color(44, 62, 80));
        mainContent.add(lblWelcome, BorderLayout.NORTH);

        // Summary Cards Panel
        JPanel cardsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 100, 0));

        lblTodayAppointments = new JLabel("0", SwingConstants.CENTER);
        lblPendingAppointments = new JLabel("0", SwingConstants.CENTER);

        cardsPanel.add(createCard("Today's Appointments", lblTodayAppointments, new Color(52, 152, 219)));
        cardsPanel.add(createCard("Pending Appointments", lblPendingAppointments, new Color(230, 126, 34)));

        mainContent.add(cardsPanel, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        // ================= 4. ACTION LISTENERS =================

        // My Appointments Click -> Open DoctorAppointments with doctorId
        btnAppointments.addActionListener(e -> {
            DoctorAppointments frame = new DoctorAppointments(this.doctorId);
            frame.setVisible(true);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    loadDoctorCounts(); // Appointments Close කළ පසු Counts Refresh වේ
                }
            });
        });

        btnPatientHistory.addActionListener(e -> {
            new PatientHistory(this.doctorId).setVisible(true);
        });
        btnPrescriptions.addActionListener(e -> {
            new IssuePrescription(this.doctorId).setVisible(true);
        });

        btnSchedule.addActionListener(e -> {
            new DoctorSchedule(this.doctorId).setVisible(true);
        });

        // Database Counts Load කිරීම
        loadDoctorCounts();
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

    // --- DATABASE LOGIC FOR DOCTOR STATS ---
    private void loadDoctorCounts() {
        try (Connection conn = DBConnection.getConnection()) {

            // 1. Today's Appointments Count for this Doctor
            String today = java.time.LocalDate.now().toString();
            String queryToday = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = ?";
            PreparedStatement stmtToday = conn.prepareStatement(queryToday);
            stmtToday.setInt(1, doctorId);
            stmtToday.setString(2, today);
            ResultSet rsToday = stmtToday.executeQuery();
            if (rsToday.next()) {
                lblTodayAppointments.setText(String.valueOf(rsToday.getInt(1)));
            }

            // 2. Pending Appointments Count for this Doctor
            String queryPending = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND status = 'Pending'";
            PreparedStatement stmtPending = conn.prepareStatement(queryPending);
            stmtPending.setInt(1, doctorId);
            ResultSet rsPending = stmtPending.executeQuery();
            if (rsPending.next()) {
                lblPendingAppointments.setText(String.valueOf(rsPending.getInt(1)));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Test Run (Doctor ID: 1, Name: Kamal)
        SwingUtilities.invokeLater(() -> new DoctorDashboard(1, "Kamal").setVisible(true));
    }
}