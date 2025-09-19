package udpWarning;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client extends JFrame {
    private static final String GROUP_ADDRESS = "230.0.0.0";
    private static final int PORT = 4446;
    private static final String LOG_FILE = "client.log";

    private JTextPane logArea;
    private JLabel statusLabel;

    private Thread listenerThread;
    private AtomicBoolean running = new AtomicBoolean(false);
    private MulticastSocket socket;

    private final String clientId = UUID.randomUUID().toString(); // unique ID

    public Client() {
        super("UDP Warning Client");
        initUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 450);
        setLocationRelativeTo(null);

        SwingUtilities.invokeLater(this::startListening);
    }

    private void initUI() {
        // Thiết lập Look and Feel hiện đại
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.setBackground(new Color(248, 250, 252)); // nền trắng xám nhạt

        // Header Panel với gradient effect
        JPanel headerPanel = createHeaderPanel();
        
        // Status Panel với icon
        JPanel statusPanel = createStatusPanel();

        // Log Panel với styling đẹp
        JPanel logPanel = createLogPanel();

        // Footer Panel
        JPanel footerPanel = createFooterPanel();

        root.add(headerPanel, BorderLayout.NORTH);
        root.add(statusPanel, BorderLayout.CENTER);
        root.add(logPanel, BorderLayout.CENTER);
        root.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(root);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                stopListening();
                sendMessage("LEAVE:" + clientId);
            }
        });
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(59, 130, 246), // Blue-600
                    getWidth(), getHeight(), new Color(37, 99, 235) // Blue-700
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        headerPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        JLabel titleLabel = new JLabel("UDP Warning Client");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel("Real-time Alert Monitoring System");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(219, 234, 254)); // Blue-100
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        return headerPanel;
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout(15, 15));
        statusPanel.setBackground(new Color(255, 255, 255));
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            new EmptyBorder(25, 25, 25, 25)
        ));

        // Status với icon và animation
        JPanel statusContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statusContainer.setOpaque(false);
        
        JLabel statusIcon = new JLabel("●");
        statusIcon.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        
        statusLabel = new JLabel("Status: Stopped");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statusLabel.setForeground(new Color(239, 68, 68)); // Red-500
        
        statusContainer.add(statusIcon);
        statusContainer.add(statusLabel);

        // Client ID info với styling đẹp
        JPanel clientInfoPanel = new JPanel(new BorderLayout());
        clientInfoPanel.setOpaque(false);
        clientInfoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            new EmptyBorder(10, 15, 10, 15)
        ));
        clientInfoPanel.setBackground(new Color(249, 250, 251));
        
        JLabel clientInfoLabel = new JLabel("Client ID: " + clientId.substring(0, 8) + "...");
        clientInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        clientInfoLabel.setForeground(new Color(107, 114, 128)); // Gray-500
        clientInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        clientInfoPanel.add(clientInfoLabel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new BorderLayout(20, 0));
        infoPanel.setOpaque(false);
        infoPanel.add(statusContainer, BorderLayout.WEST);
        infoPanel.add(clientInfoPanel, BorderLayout.EAST);

        statusPanel.add(infoPanel, BorderLayout.CENTER);
        
        return statusPanel;
    }

    private JPanel createLogPanel() {
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBackground(new Color(255, 255, 255));
        logPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // Log header với icon
        JPanel logHeaderPanel = new JPanel(new BorderLayout());
        logHeaderPanel.setOpaque(false);
        
        JLabel logHeaderLabel = new JLabel("Activity Log");
        logHeaderLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logHeaderLabel.setForeground(new Color(31, 41, 55)); // Gray-800
        
        JLabel logCountLabel = new JLabel("0 entries");
        logCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logCountLabel.setForeground(new Color(107, 114, 128)); // Gray-500
        
        logHeaderPanel.add(logHeaderLabel, BorderLayout.WEST);
        logHeaderPanel.add(logCountLabel, BorderLayout.EAST);

        // Log area với styling đẹp
        logArea = new JTextPane();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setBackground(new Color(249, 250, 251)); // Gray-50
        logArea.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
        scrollPane.setBackground(Color.WHITE);
        
        logPanel.add(logHeaderPanel, BorderLayout.NORTH);
        logPanel.add(scrollPane, BorderLayout.CENTER);
        
        return logPanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        footerPanel.setBackground(new Color(248, 250, 252));
        footerPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        JLabel footerLabel = new JLabel("Connected to: " + GROUP_ADDRESS + ":" + PORT);
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        footerLabel.setForeground(new Color(107, 114, 128)); // Gray-500
        
        footerPanel.add(footerLabel);
        
        return footerPanel;
    }

    private void startListening() {
        if (running.get()) return;
        running.set(true);
        statusLabel.setText("Status: Listening");
        statusLabel.setForeground(new Color(34, 197, 94)); // Green-500

        // Gửi JOIN
        sendMessage("JOIN:" + clientId);

        listenerThread = new Thread(() -> {
            try {
                InetAddress group = InetAddress.getByName(GROUP_ADDRESS);
                socket = new MulticastSocket(PORT);
                socket.joinGroup(group);

                appendLog("Joined group " + GROUP_ADDRESS + ":" + PORT, Color.BLUE);

                byte[] buf = new byte[2048];
                while (running.get()) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String received = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);

                    if (received.startsWith("ALERT:")) {
                        String content = received.substring(6);

                        appendLog("[ALERT] " + content, getColorForMessage(content));

                        // Gửi ACK ngay khi nhận
                        sendMessage("ACK:" + clientId);

                        // Hiển thị popup cảnh báo
                        showPopupAlert(content);
                    }
                }
            } catch (Exception ex) {
                if (running.get()) {
                    appendLog("Error: " + ex.getMessage(), Color.RED);
                }
            } finally {
                try {
                    if (socket != null) {
                        InetAddress group = InetAddress.getByName(GROUP_ADDRESS);
                        socket.leaveGroup(group);
                        socket.close();
                    }
                } catch (Exception ignore) {}
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Status: Stopped");
                    statusLabel.setForeground(new Color(239, 68, 68)); // Red-500
                });
            }
        });
        listenerThread.start();
    }



    private void stopListening() {
        if (!running.get()) return;
        running.set(false);
        if (socket != null && !socket.isClosed()) {
            try { socket.close(); } catch (Exception ignored) {}
        }
        statusLabel.setText("Status: Stopped");
        statusLabel.setForeground(new Color(239, 68, 68)); // Red-500
        appendLog("Stopped listening.", Color.BLACK);
    }

    private void sendMessage(String msg) {
        try {
            DatagramSocket ds = new DatagramSocket();
            byte[] buf = msg.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(buf, buf.length,
                    InetAddress.getByName(GROUP_ADDRESS), PORT);
            ds.send(packet);
            ds.close();
        } catch (Exception ignored) {}
    }

    private void appendLog(String s, Color color) {
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = logArea.getStyledDocument();
            Style style = logArea.addStyle("Style", null);
            StyleConstants.setForeground(style, color);
            try {
                doc.insertString(doc.getLength(), s + "\n", style);
            } catch (BadLocationException ignored) {}
        });
        writeToFile(LOG_FILE, s);
    }

    private void writeToFile(String file, String s) {
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write(s + "\n");
        } catch (IOException ignored) {}
    }

    private Color getColorForMessage(String msg) {
        if (msg.contains("Cảnh báo khẩn cấp")) return Color.RED;
        if (msg.contains("Nguy cơ tiềm ẩn")) return Color.ORANGE;
        if (msg.contains("Thông báo chung")) return new Color(0, 128, 0);
        return Color.BLACK;
    }

    // 🔔 Popup cảnh báo với design đẹp hơn
    private void showPopupAlert(String msg) {
        SwingUtilities.invokeLater(() -> {
            // Lấy màu sắc tương ứng với mức độ cảnh báo
            Color alertColor = getColorForMessage(msg);
            String colorHex = String.format("#%02x%02x%02x", 
                alertColor.getRed(), alertColor.getGreen(), alertColor.getBlue());

            JDialog newAlert = new JDialog(this, "🚨 Alert Notification", true);
            newAlert.setLayout(new BorderLayout());
            newAlert.setResizable(false);
            
            // Header panel với gradient
            JPanel headerPanel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    
                    // Gradient background
                    GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(239, 68, 68), // Red-500
                        getWidth(), getHeight(), new Color(220, 38, 38) // Red-600
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.dispose();
                }
            };
            headerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
            
            JLabel headerLabel = new JLabel("Alert Notification");
            headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            headerLabel.setForeground(Color.WHITE);
            headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
            headerPanel.add(headerLabel, BorderLayout.CENTER);
            
            // Content panel
            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.setBackground(Color.WHITE);
            contentPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
            
            JLabel label = new JLabel("<html><div style='text-align: center; font-family: Segoe UI; font-size: 16px; color: " + colorHex + "; line-height: 1.6; font-weight: 500; word-wrap: break-word; max-width: 400px;'>" + msg + "</div></html>", SwingConstants.CENTER);
            label.setForeground(alertColor);
            contentPanel.add(label, BorderLayout.CENTER);
            
            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
            buttonPanel.setBackground(Color.WHITE);
            buttonPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
            
            JButton okButton = new JButton("Acknowledge");
            okButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            okButton.setBackground(new Color(59, 130, 246)); // Blue-600
            okButton.setForeground(Color.WHITE);
            okButton.setFocusPainted(false);
            okButton.setBorderPainted(false);
            okButton.setPreferredSize(new Dimension(140, 40));
            okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            okButton.addActionListener(e -> newAlert.dispose());
            
            buttonPanel.add(okButton);
            
            newAlert.add(headerPanel, BorderLayout.NORTH);
            newAlert.add(contentPanel, BorderLayout.CENTER);
            newAlert.add(buttonPanel, BorderLayout.SOUTH);
            
            newAlert.setSize(500, 300);
            newAlert.setLocationRelativeTo(this);

            // Timeout tự đóng sau 10s
            new Timer(10000, e -> {
                if (newAlert.isShowing()) newAlert.dispose();
            }).start();

            newAlert.setVisible(true);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Client client = new Client();
            client.setVisible(true);
        });
    }
}
