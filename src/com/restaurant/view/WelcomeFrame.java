package com.restaurant.view;

import javax.swing.*;
import java.awt.*;

/**
 * Màn hình chào mừng - Hiển thị logo và 2 nút: Đăng nhập / Đăng ký
 */
public class WelcomeFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    public WelcomeFrame() {
        setTitle("Chào mừng - Nhà hàng Hương Vị Việt");              // Tiêu đề cửa sổ
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);              // Đóng app khi tắt
        setSize(450, 600);                                           // Kích thước cửa sổ
        setLocationRelativeTo(null);                                 // Hiển thị giữa màn hình
        setResizable(false);                                         // Không cho resize

        JPanel mainPanel = new JPanel(new BorderLayout(0, 30));     // Panel chính với BorderLayout
        mainPanel.setBackground(Color.WHITE);                        // Màu nền trắng
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40)); // Padding 40px

        // Top panel - Logo + Title + Description
        JPanel topPanel = new JPanel();                              // Panel trên
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS)); // Layout dọc
        topPanel.setBackground(Color.WHITE);                         // Màu nền trắng

        // Logo panel - Vẽ circular logo với image hoặc text fallback
        JPanel logoPanel = new JPanel() {
            private Image logoImage;                                 // Biến lưu logo image

            {                                                        // Initializer block
                try {
                    ImageIcon icon = new ImageIcon("resources/images/logo.png"); // Load logo từ resources
                    if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) { // Nếu load thành công
                        logoImage = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH); // Scale về 80x80
                    }
                } catch (Exception e) {                              // Nếu không load được
                    logoImage = null;                                // Set null để dùng text fallback
                }
            }

            @Override
            protected void paintComponent(Graphics g) {              // Override để vẽ custom logo
                super.paintComponent(g);                             // Gọi super trước
                Graphics2D g2d = (Graphics2D) g;                     // Cast sang Graphics2D
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Anti-aliasing

                g2d.setColor(new Color(255, 240, 230));              // Màu cam nhạt
                g2d.fillOval(25, 0, 120, 120);                       // Vẽ hình tròn

                if (logoImage != null) {                             // Nếu có logo image
                    g2d.drawImage(logoImage, 45, 20, null);          // Vẽ image ở giữa
                } else {                                             // Nếu không có image
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 48)); // Font size 48
                    g2d.drawString("HVV", 40, 75);                   // Vẽ text "HVV" fallback
                }
            }

            @Override
            public Dimension getPreferredSize() {                    // Set kích thước ưu tiên
                return new Dimension(170, 120);                      // 170x120
            }
        };
        logoPanel.setBackground(Color.WHITE);                        // Màu nền trắng
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);         // Căn giữa

        // Title
        JLabel lblTitle = new JLabel("Quản lý");                    // Label tiêu đề
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));      // Font size 24
        lblTitle.setForeground(new Color(25, 25, 112));              // Màu Midnight Blue
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);          // Căn giữa

        // Subtitle
        JLabel lblSubtitle = new JLabel("Nhà Hàng Hương Vị Việt");  // Label phụ đề
        lblSubtitle.setFont(new Font("Segoe UI", Font.BOLD, 20));   // Font size 20
        lblSubtitle.setForeground(new Color(25, 25, 112));           // Màu Midnight Blue
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);       // Căn giữa

        // Description
        JLabel lblDescription = new JLabel("Thơm ngon đến từng giọt cuối cùng"); // Slogan
        lblDescription.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // Font size 13
        lblDescription.setForeground(new Color(128, 128, 128));      // Màu xám
        lblDescription.setAlignmentX(Component.CENTER_ALIGNMENT);    // Căn giữa

        topPanel.add(logoPanel);                                     // Thêm logo panel
        topPanel.add(Box.createVerticalStrut(20));                  // Khoảng cách 20px
        topPanel.add(lblTitle);                                      // Thêm title
        topPanel.add(Box.createVerticalStrut(5));                   // Khoảng cách 5px
        topPanel.add(lblSubtitle);                                   // Thêm subtitle
        topPanel.add(Box.createVerticalStrut(10));                  // Khoảng cách 10px
        topPanel.add(lblDescription);                                // Thêm description

        // Center panel - 2 buttons
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 15)); // Grid 2 hàng, spacing 15px
        centerPanel.setBackground(Color.WHITE);                      // Màu nền trắng
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50)); // Padding

        // Button Đăng nhập
        JButton btnLogin = new JButton("ĐĂNG NHẬP");                // Nút đăng nhập
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));      // Font size 14
        btnLogin.setBackground(new Color(135, 206, 235));            // Màu Sky Blue
        btnLogin.setForeground(Color.WHITE);                         // Màu chữ trắng
        btnLogin.setFocusPainted(false);                             // Không vẽ focus
        btnLogin.setBorderPainted(false);                            // Không vẽ border
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));         // Con trỏ tay khi hover
        btnLogin.setPreferredSize(new Dimension(250, 45));          // Kích thước 250x45
        btnLogin.addActionListener(e -> {                            // Xử lý click
            dispose();                                               // Đóng WelcomeFrame
            new LoginFrame();                                        // Mở LoginFrame
        });

        // Button Đăng ký
        JButton btnRegister = new JButton("ĐĂNG KÝ");               // Nút đăng ký
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));   // Font size 14
        btnRegister.setBackground(new Color(135, 206, 235));         // Màu Sky Blue
        btnRegister.setForeground(Color.WHITE);                      // Màu chữ trắng
        btnRegister.setFocusPainted(false);                          // Không vẽ focus
        btnRegister.setBorderPainted(false);                         // Không vẽ border
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));      // Con trỏ tay khi hover
        btnRegister.setPreferredSize(new Dimension(250, 45));       // Kích thước 250x45
        btnRegister.addActionListener(e -> {                         // Xử lý click
            dispose();                                               // Đóng WelcomeFrame
            new RegisterFrame();                                     // Mở RegisterFrame
        });

        centerPanel.add(btnLogin);                                   // Thêm nút đăng nhập
        centerPanel.add(btnRegister);                                // Thêm nút đăng ký

        // Bottom panel - Footer
        JPanel bottomPanel = new JPanel(new GridLayout(1, 1, 0, 5)); // Grid 1 hàng
        bottomPanel.setBackground(Color.WHITE);                      // Màu nền trắng

        JLabel lblVersion = new JLabel("Powered by Anh Duc & Du Hoc", SwingConstants.CENTER); // Footer text
        lblVersion.setFont(new Font("Segoe UI", Font.PLAIN, 11));   // Font size 11
        lblVersion.setForeground(new Color(128, 128, 128));          // Màu xám

        bottomPanel.add(lblVersion);                                 // Thêm footer text

        mainPanel.add(topPanel, BorderLayout.NORTH);                // Thêm panel trên
        mainPanel.add(centerPanel, BorderLayout.CENTER);            // Thêm panel giữa
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);             // Thêm panel dưới

        add(mainPanel);                                              // Thêm panel chính vào frame
        setVisible(true);                                            // Hiển thị frame
    }
}
