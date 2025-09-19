package udpWarning;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class LogViewerDialog extends JDialog {
    private static final String LOG_FILE = "server.log";
    
    // Class để lưu trữ thông tin log entry
    private static class LogEntry {
        String timestamp;
        String level;
        String content;
        
        LogEntry(String timestamp, String level, String content) {
            this.timestamp = timestamp;
            this.level = level;
            this.content = content;
        }
    }
    
    private JTable table;
    private DefaultTableModel model;
    private JLabel statsLabel;
    
    public LogViewerDialog(JFrame parent) {
        super(parent, "Server Log Viewer", true);
        initComponents();
        loadLogData();
    }
    
    private void initComponents() {
        setSize(900, 650);
        setLocationRelativeTo(getParent());
        
        // Tạo bảng
        String[] columnNames = {"Thời gian", "Loại", "Nội dung"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho edit
            }
        };
        
        table = new JTable(model);
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setGridColor(new Color(229, 231, 235));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        
        // Custom renderer để đổi màu text theo loại cảnh báo
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (value != null) {
                    String level = value.toString();
                    if (isSelected) {
                        c.setBackground(table.getSelectionBackground());
                        c.setForeground(table.getSelectionForeground());
                    } else {
                        c.setBackground(table.getBackground());
                        
                        // Đặt màu text theo loại cảnh báo
                        switch (level) {
                            case "CRITICAL":
                                c.setForeground(Color.RED);
                                break;
                            case "WARNING":
                                c.setForeground(Color.ORANGE);
                                break;
                            case "INFO":
                                c.setForeground(new Color(0, 128, 0));
                                break;
                            case "JOIN":
                                c.setForeground(Color.BLUE);
                                break;
                            case "LEAVE":
                                c.setForeground(Color.GRAY);
                                break;
                            case "ACK":
                                c.setForeground(new Color(0, 128, 0));
                                break;
                            case "RESEND":
                                c.setForeground(Color.MAGENTA);
                                break;
                            case "SENT":
                                c.setForeground(new Color(0, 100, 200));
                                break;
                            default:
                                c.setForeground(Color.BLACK);
                                break;
                        }
                    }
                }
                return c;
            }
        });
        
        // Tạo scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
        
        // Tạo panel chứa bảng và nút
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(248, 250, 252));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel nút với styling đẹp
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setOpaque(false);
        
        JButton refreshBtn = createStyledButton("Refresh", new Color(59, 130, 246), Color.WHITE);
        JButton exportBtn = createStyledButton("Export", new Color(16, 185, 129), Color.WHITE);
        JButton closeBtn = createStyledButton("Close", new Color(107, 114, 128), Color.WHITE);
        
        refreshBtn.addActionListener(e -> refreshLogData());
        exportBtn.addActionListener(e -> exportLogData());
        closeBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(exportBtn);
        buttonPanel.add(closeBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Thêm label thống kê với styling đẹp
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            new EmptyBorder(15, 20, 15, 20)
        ));
        statsPanel.setBackground(Color.WHITE);
        
        statsLabel = new JLabel("Total log entries: 0");
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statsLabel.setForeground(new Color(31, 41, 55));
        
        statsPanel.add(statsLabel, BorderLayout.WEST);
        mainPanel.add(statsPanel, BorderLayout.NORTH);
        
        add(mainPanel);
    }
    
    // Đọc và parse file log
    private List<LogEntry> parseLogFile() {
        List<LogEntry> entries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                // Parse log entry - format: "yyyy-MM-dd HH:mm:ss - [LEVEL] content"
                String[] parts = line.split(" - ", 2);
                if (parts.length >= 2) {
                    String timestamp = parts[0].trim();
                    String content = parts[1].trim();
                    
                    // Xác định level từ content
                    String level = "UNKNOWN";
                    if (content.contains("CRITICAL")) level = "CRITICAL";
                    else if (content.contains("WARNING")) level = "WARNING";
                    else if (content.contains("INFO")) level = "INFO";
                    else if (content.contains("Client JOIN")) level = "JOIN";
                    else if (content.contains("Client LEAVE")) level = "LEAVE";
                    else if (content.contains("ACK")) level = "ACK";
                    else if (content.contains("Resend")) level = "RESEND";
                    else if (content.contains("Đã gửi")) level = "SENT";
                    
                    entries.add(new LogEntry(timestamp, level, content));
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Lỗi đọc file log: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        return entries;
    }
    
    // Load dữ liệu log vào bảng
    private void loadLogData() {
        List<LogEntry> logEntries = parseLogFile();
        
        // Xóa dữ liệu cũ
        model.setRowCount(0);
        
        // Thêm dữ liệu mới
        for (LogEntry entry : logEntries) {
            Object[] row = {entry.timestamp, entry.level, entry.content};
            model.addRow(row);
        }
        
        // Cập nhật thống kê
        updateStatsLabel(logEntries.size());
    }
    
    // Refresh dữ liệu log
    private void refreshLogData() {
        loadLogData();
    }
    
    // Cập nhật label thống kê
    private void updateStatsLabel(int count) {
        statsLabel.setText("Total log entries: " + count);
    }
    
    // Tạo styled button
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
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    // Export log data
    private void exportLogData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Log Data");
        fileChooser.setSelectedFile(new java.io.File("server_log_export.txt"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.FileWriter writer = new java.io.FileWriter(fileChooser.getSelectedFile())) {
                writer.write("Server Log Export\n");
                writer.write("==================\n\n");
                
                for (int i = 0; i < model.getRowCount(); i++) {
                    writer.write(String.format("%s | %s | %s\n", 
                        model.getValueAt(i, 0),
                        model.getValueAt(i, 1),
                        model.getValueAt(i, 2)
                    ));
                }
                
                JOptionPane.showMessageDialog(this, 
                    "Log data exported successfully!", 
                    "Export Complete", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error exporting log data: " + e.getMessage(), 
                    "Export Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // Hiển thị dialog
    public void showDialog() {
        setVisible(true);
    }
}
