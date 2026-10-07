import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JFrame {
    private JProgressBar progressBar;
    private JLabel lblStatus;

    public SplashScreen() {
        // Window Configurations
        setTitle("Hospital Management System");
        setSize(450, 250);
        setUndecorated(true); // Title bar (Close, Minimize buttons) අයින් කිරීමට
        setLocationRelativeTo(null); // Screen එකේ මැදට ගැනීමට
        setLayout(new BorderLayout());

        // Main Panel Setup
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(41, 128, 185)); // Blue Background Color
        mainPanel.setLayout(null);

        // Title Label
        JLabel lblTitle = new JLabel("HOSPITAL MANAGEMENT SYSTEM", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBounds(0, 50, 450, 30);
        mainPanel.add(lblTitle);

        // Status Label (e.g., "Loading Modules... 50%")
        lblStatus = new JLabel("Loading System...", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        lblStatus.setForeground(Color.WHITE);
        lblStatus.setBounds(0, 130, 450, 20);
        mainPanel.add(lblStatus);

        // Progress Bar Setup
        progressBar = new JProgressBar();
        progressBar.setBounds(50, 160, 350, 20);
        progressBar.setStringPainted(true); // % ප්‍රමාණය පෙනීමට
        progressBar.setForeground(new Color(46, 204, 113)); // Green Color Progress
        mainPanel.add(progressBar);

        add(mainPanel);
    }

    // Loading Animation Logic
    public void startLoading() {
        setVisible(true);

        try {
            for (int i = 0; i <= 100; i++) {
                Thread.sleep(30); // Loading speed එක (30ms per 1%)
                progressBar.setValue(i);

                // Status text එක වෙනස් කිරීම
                if (i == 20) lblStatus.setText("Connecting to Database...");
                if (i == 50) lblStatus.setText("Loading Application Modules...");
                if (i == 80) lblStatus.setText("Launching Login Portal...");
                if (i == 100) lblStatus.setText("Complete!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Loading එක 100% වුණු පසු Splash Screen එක Close කර Login Form එක Open කිරීම
        this.dispose();
        new LoginForm().setVisible(true);
    }

    public static void main(String[] args) {
        SplashScreen splash = new SplashScreen();
        splash.startLoading();
    }
}