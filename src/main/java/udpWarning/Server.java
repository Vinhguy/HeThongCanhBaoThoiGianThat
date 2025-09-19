package udpWarning;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class Server extends JFrame {
    public static final String GROUP_ADDRESS = "230.0.0.0";
    public static final int PORT = 4446;
    private static final String LOG_FILE = "server.log";

    private JTextPane logArea;
    private JTextField inputField;
    private JButton sendBtn, clearBtn, checkLogBtn;
    private JLabel clientCountLabel;
    private JComboBox<String> levelBox;

    private MulticastSocket socket;
    private InetAddress group;
    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private int clientCount = 0;
    private Set<String> ackedClients = ConcurrentHashMap.newKeySet();

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> resendTask;
    private String lastMessage;

    public Server() {
        super("UDP Warning Server");
        initUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 520);
        setLocationRelativeTo(null);

        try {
            socket = new MulticastSocket(PORT);
            group = InetAddress.getByName(GROUP_ADDRESS);
            socket.joinGroup(group);
            appendLog("Server ready. Lắng nghe trên " + GROUP_ADDRESS + ":" + PORT, Color.BLACK);

            new Thread(this::listenForClients).start();
        } catch (Exception e) {
            appendLog("Lỗi khi khởi tạo server: " + e.getMessage(), Color.RED);
        }
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

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        
        // Control Panel
        JPanel controlPanel = createControlPanel();

        // Log Panel
        JPanel logPanel = createLogPanel();

        // Footer Panel
        JPanel footerPanel = createFooterPanel();

        // Main content panel
        JPanel mainContentPanel = new JPanel(new BorderLayout(15, 15));
        mainContentPanel.setOpaque(false);
        mainContentPanel.add(controlPanel, BorderLayout.NORTH);
        mainContentPanel.add(logPanel, BorderLayout.CENTER);

        root.add(headerPanel, BorderLayout.NORTH);
        root.add(mainContentPanel, BorderLayout.CENTER);
        root.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(16, 185, 129), // Emerald-500
                    getWidth(), getHeight(), new Color(5, 150, 105) // Emerald-600
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        headerPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        JLabel titleLabel = new JLabel("UDP Warning Server");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel("Alert Broadcasting & Management System");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(209, 250, 229)); // Emerald-100
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        return headerPanel;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new BorderLayout(15, 15));
        controlPanel.setBackground(new Color(255, 255, 255));
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // Alert Level Selector
        JPanel levelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        levelPanel.setOpaque(false);
        
        JLabel levelLabel = new JLabel("Alert Level:");
        levelLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        levelLabel.setForeground(new Color(31, 41, 55)); // Gray-800
        
        String[] levels = {
                "-- Chọn mức độ --",
                "INFO: Thông báo chung",
                "WARNING: Nguy cơ tiềm ẩn",
                "CRITICAL: Cảnh báo khẩn cấp"
        };
        levelBox = new JComboBox<>(levels);
        levelBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        levelBox.setPreferredSize(new Dimension(250, 35));
        levelBox.addActionListener(e -> {
            String selected = (String) levelBox.getSelectedItem();
            if (selected != null && !selected.startsWith("--")) {
                inputField.setText(selected);
            }
        });
        
        levelPanel.add(levelLabel);
        levelPanel.add(levelBox);

        // Message Input
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setOpaque(false);
        
        JLabel inputLabel = new JLabel("Message:");
        inputLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputLabel.setForeground(new Color(31, 41, 55)); // Gray-800
        inputLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        
        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        inputField.setPreferredSize(new Dimension(0, 35));
        
        inputPanel.add(inputLabel, BorderLayout.NORTH);
        inputPanel.add(inputField, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        sendBtn = createStyledButton("Send Alert", new Color(16, 185, 129), Color.WHITE);
        sendBtn.addActionListener(e -> sendMessage());
        
        clearBtn = createStyledButton("Clear Log", new Color(107, 114, 128), Color.WHITE);
        clearBtn.addActionListener(e -> logArea.setText(""));
        
        checkLogBtn = createStyledButton("Check Log", new Color(59, 130, 246), Color.WHITE);
        checkLogBtn.addActionListener(e -> showLogViewer());
        
        buttonPanel.add(sendBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(checkLogBtn);

        controlPanel.add(levelPanel, BorderLayout.NORTH);
        controlPanel.add(inputPanel, BorderLayout.CENTER);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return controlPanel;
    }

    private JButton createStyledButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Hover effect
                if (getModel().isRollover()) {
                    g2d.setColor(bgColor.darker());
                } else {
                    g2d.setColor(bgColor);
                }
                
                // Rounded corners
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Text
                g2d.setColor(textColor);
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(getText(), x, y);
                
                g2d.dispose();
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(130, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
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
        
        JLabel logHeaderLabel = new JLabel("Server Activity Log");
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
        JPanel footerPanel = new JPanel(new BorderLayout(20, 10));
        footerPanel.setBackground(new Color(248, 250, 252));
        footerPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        // Client count
        clientCountLabel = new JLabel("Clients connected: 0");
        clientCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clientCountLabel.setForeground(new Color(16, 185, 129)); // Emerald-500
        
        // Server info
        JLabel serverInfoLabel = new JLabel("Server: " + GROUP_ADDRESS + ":" + PORT);
        serverInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        serverInfoLabel.setForeground(new Color(107, 114, 128)); // Gray-500
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.add(clientCountLabel, BorderLayout.WEST);
        infoPanel.add(serverInfoLabel, BorderLayout.EAST);
        
        footerPanel.add(infoPanel, BorderLayout.CENTER);
        
        return footerPanel;
    }

    private void sendMessage() {
        String line = inputField.getText().trim();
        if (line.isEmpty()) return;

        String decoratedLine = decorateMessage(line);
        lastMessage = "ALERT:" + line;

        Color color = getColorForMessage(line);

        try {
            byte[] buf = lastMessage.getBytes("UTF-8");
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
            socket.send(packet);
            appendLog("Đã gửi: " + decoratedLine, color);

            // Reset ACK list và start resend timer
            ackedClients.clear();
            if (resendTask != null && !resendTask.isCancelled()) {
                resendTask.cancel(true);
            }
            resendTask = scheduler.scheduleAtFixedRate(() -> {
                if (ackedClients.isEmpty()) {
                    try {
                        socket.send(new DatagramPacket(lastMessage.getBytes("UTF-8"),
                                lastMessage.length(), group, PORT));
                        appendLog("Resend: " + decoratedLine, color);
                    } catch (IOException ignored) {}
                } else {
                    resendTask.cancel(true);
                }
            }, 5, 5, TimeUnit.SECONDS);

        } catch (Exception e) {
            appendLog("Lỗi khi gửi: " + e.getMessage(), Color.RED);
        }
        inputField.setText("");
    }

    private String decorateMessage(String msg) {
        if (msg.contains("Cảnh báo khẩn cấp")) return "[CRITICAL] " + msg;
        if (msg.contains("Nguy cơ tiềm ẩn")) return "[WARNING] " + msg;
        if (msg.contains("Thông báo chung")) return "[INFO] " + msg;
        return msg;
    }

    private void listenForClients() {
        byte[] buf = new byte[2048];
        while (!socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String msg = new String(packet.getData(), packet.getOffset(), packet.getLength(), "UTF-8");

                if (msg.startsWith("JOIN:")) {
                    clientCount++;
                    updateClientCount();
                    appendLog("Client JOIN (" + msg.substring(5) + ")", Color.BLUE);
                } else if (msg.startsWith("LEAVE:")) {
                    clientCount = Math.max(0, clientCount - 1);
                    updateClientCount();
                    appendLog("Client LEAVE (" + msg.substring(6) + ")", Color.GRAY);
                } else if (msg.startsWith("ACK:")) {
                    ackedClients.add(msg.substring(4));
                    appendLog("ACK từ client " + msg.substring(4), new Color(0, 128, 0));
                }
            } catch (IOException ex) {
                break;
            }
        }
    }

    private void updateClientCount() {
        SwingUtilities.invokeLater(() -> clientCountLabel.setText("Clients connected: " + clientCount));
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
            String timestamp = LocalDateTime.now().format(fmt);
            fw.write(timestamp + " - " + s + "\n");
        } catch (IOException ignored) {}
    }

    private Color getColorForMessage(String msg) {
        if (msg.contains("Cảnh báo khẩn cấp")) return Color.RED;
        if (msg.contains("Nguy cơ tiềm ẩn")) return Color.ORANGE;
        if (msg.contains("Thông báo chung")) return new Color(0, 128, 0);
        return Color.BLACK;
    }

    // Hiển thị dialog log viewer
    private void showLogViewer() {
        LogViewerDialog logDialog = new LogViewerDialog(this);
        logDialog.showDialog();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Server server = new Server();
            server.setVisible(true);
        });
    }
}
