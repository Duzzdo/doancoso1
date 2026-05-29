package com.restaurant.view;

import com.restaurant.dao.NhanVienDAO;
import com.restaurant.model.NhanVienDTO;
import com.restaurant.utils.SessionManager;
import com.restaurant.utils.ValidationUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Màn hình đăng nhập - Xác thực username/password với BCrypt, lưu session và chuyển sang MainFrame
 */
public class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    // Input fields
    private JTextField txtUsername;                                      // Field nhập username
    private JPasswordField txtPassword;                                  // Field nhập password
    private JCheckBox chkRemember;                                       // Checkbox "Hiện mật khẩu"
    private NhanVienDAO nhanVienDAO;                                     // DAO xử lý đăng nhập

    // Color scheme - Modern cyan theme
    private static final Color CYAN_BG = new Color(160, 231, 229);      // Background cyan nhạt
    private static final Color DARK_BLUE = new Color(0, 0, 139);        // Text/border xanh đậm
    private static final Color FIELD_BG = new Color(200, 245, 244);     // Field background cyan rất nhạt

    public LoginFrame() {
        nhanVienDAO = new NhanVienDAO();                                 // Khởi tạo DAO

        setTitle("Đăng nhập - Nhà hàng Hương Vị Việt");                 // Tiêu đề cửa sổ
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);                  // Đóng app khi tắt
        setSize(450, 600);                                               // Kích thước cửa sổ
        setLocationRelativeTo(null);                                     // Hiển thị giữa màn hình
        setResizable(false);                                             // Không cho resize

        JPanel mainPanel = new JPanel(new BorderLayout(0, 30));         // Panel chính với BorderLayout
        mainPanel.setBackground(CYAN_BG);                                // Màu nền cyan
        mainPanel.setBorder(BorderFactory.createEmptyBorder(80, 40, 40, 40)); // Padding

        // Top panel - Back button and title
        JPanel topPanel = new JPanel(new GridBagLayout());              // Panel trên với GridBagLayout
        topPanel.setBackground(CYAN_BG);                                 // Màu nền cyan
        GridBagConstraints gbc = new GridBagConstraints();               // Constraints cho layout

        JButton btnBack = new JButton("←");                             // Nút quay lại
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 24));           // Font size 24
        btnBack.setForeground(DARK_BLUE);                                // Màu chữ xanh đậm
        btnBack.setBackground(CYAN_BG);                                  // Màu nền cyan
        btnBack.setBorderPainted(false);                                 // Không vẽ border
        btnBack.setFocusPainted(false);                                  // Không vẽ focus
        btnBack.setContentAreaFilled(false);                             // Không fill background
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));              // Con trỏ tay khi hover
        btnBack.addActionListener(e -> {                                 // Xử lý click
            dispose();                                                   // Đóng LoginFrame
            new WelcomeFrame();                                          // Mở WelcomeFrame
        });

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP");                      // Label tiêu đề
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));          // Font size 32
        lblTitle.setForeground(DARK_BLUE);                               // Màu chữ xanh đậm

        // Back button on the left
        gbc.gridx = 0;                                                   // Cột 0
        gbc.gridy = 0;                                                   // Hàng 0
        gbc.anchor = GridBagConstraints.WEST;                            // Căn trái
        gbc.weightx = 0;                                                 // Không giãn ngang
        topPanel.add(btnBack, gbc);                                      // Thêm nút back

        // Title in the center
        gbc.gridx = 1;                                                   // Cột 1
        gbc.anchor = GridBagConstraints.CENTER;                          // Căn giữa
        gbc.weightx = 1.0;                                               // Giãn ngang
        topPanel.add(lblTitle, gbc);                                     // Thêm tiêu đề

        // Empty space on the right to balance
        gbc.gridx = 2;                                                   // Cột 2
        gbc.anchor = GridBagConstraints.EAST;                            // Căn phải
        gbc.weightx = 0;                                                 // Không giãn ngang
        JLabel lblEmpty = new JLabel("   ");                            // Label rỗng để cân bằng
        topPanel.add(lblEmpty, gbc);                                     // Thêm label rỗng

        // Center panel - Form
        JPanel formPanel = new JPanel();                                // Panel form
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS)); // Layout dọc
        formPanel.setBackground(CYAN_BG);                                // Màu nền cyan

        // Username field with icon INSIDE
        JPanel usernamePanel = new JPanel(new BorderLayout(0, 0));      // Panel username
        usernamePanel.setBackground(FIELD_BG);                           // Màu nền field
        usernamePanel.setBorder(BorderFactory.createCompoundBorder(      // Border kép
            BorderFactory.createLineBorder(DARK_BLUE, 2),               // Border xanh đậm 2px
            BorderFactory.createEmptyBorder(0, 10, 0, 10)               // Padding trong
        ));
        usernamePanel.setMaximumSize(new Dimension(690, 60));           // Kích thước tối đa
        usernamePanel.setAlignmentX(Component.CENTER_ALIGNMENT);         // Căn giữa

        JLabel lblUserIcon = new JLabel("👤");                          // Icon user
        lblUserIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24)); // Font emoji size 24
        lblUserIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 15)); // Padding

        txtUsername = new JTextField();                                  // Field nhập username
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 16));      // Font size 16
        txtUsername.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10)); // Padding
        txtUsername.setBackground(FIELD_BG);                             // Màu nền field
        addPlaceholder(txtUsername, "Nhập tên tài khoản");              // Thêm placeholder

        usernamePanel.add(lblUserIcon, BorderLayout.WEST);              // Thêm icon bên trái
        usernamePanel.add(txtUsername, BorderLayout.CENTER);            // Thêm field ở giữa

        // Password field with icon INSIDE
        JPanel passwordPanel = new JPanel(new BorderLayout(0, 0));      // Panel password
        passwordPanel.setBackground(FIELD_BG);                           // Màu nền field
        passwordPanel.setBorder(BorderFactory.createCompoundBorder(      // Border kép
            BorderFactory.createLineBorder(DARK_BLUE, 2),               // Border xanh đậm 2px
            BorderFactory.createEmptyBorder(0, 10, 0, 10)               // Padding trong
        ));
        passwordPanel.setMaximumSize(new Dimension(690, 60));           // Kích thước tối đa
        passwordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);         // Căn giữa

        JLabel lblLockIcon = new JLabel("🔒");                          // Icon khóa
        lblLockIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24)); // Font emoji size 24
        lblLockIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 15)); // Padding

        txtPassword = new JPasswordField();                              // Field nhập password
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 16));      // Font size 16
        txtPassword.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Padding
        txtPassword.setBackground(FIELD_BG);                             // Màu nền field
        addPasswordPlaceholder(txtPassword, "Nhập mật khẩu");           // Thêm placeholder

        passwordPanel.add(lblLockIcon, BorderLayout.WEST);              // Thêm icon bên trái
        passwordPanel.add(txtPassword, BorderLayout.CENTER);            // Thêm field ở giữa

        // Checkbox for show/hide password and Forgot Password on same line
        JPanel checkboxForgotPanel = new JPanel(new BorderLayout(10, 0)); // Panel checkbox + forgot
        checkboxForgotPanel.setBackground(CYAN_BG);                      // Màu nền cyan
        checkboxForgotPanel.setMaximumSize(new Dimension(690, 30));     // Kích thước tối đa
        checkboxForgotPanel.setAlignmentX(Component.CENTER_ALIGNMENT);   // Căn giữa

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); // Panel trái
        leftPanel.setBackground(CYAN_BG);                                // Màu nền cyan

        chkRemember = new JCheckBox("Hiện mật khẩu");                   // Checkbox hiện password
        chkRemember.setFont(new Font("Segoe UI", Font.PLAIN, 15));      // Font size 15
        chkRemember.setForeground(DARK_BLUE);                            // Màu chữ xanh đậm
        chkRemember.setBackground(CYAN_BG);                              // Màu nền cyan
        chkRemember.setFocusPainted(false);                              // Không vẽ focus
        chkRemember.addActionListener(e -> {                             // Xử lý click
            if (chkRemember.isSelected()) {                              // Nếu được chọn
                txtPassword.setEchoChar((char) 0);                       // Hiện password
            } else {                                                     // Nếu không chọn
                txtPassword.setEchoChar('•');                            // Ẩn password
            }
        });

        leftPanel.add(chkRemember);                                      // Thêm checkbox

        JLabel lblForgot = new JLabel("QUÊN MẬT KHẨU");                 // Label quên mật khẩu
        lblForgot.setFont(new Font("Segoe UI", Font.BOLD, 15));         // Font size 15
        lblForgot.setForeground(DARK_BLUE);                              // Màu chữ xanh đậm
        lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));            // Con trỏ tay khi hover
        lblForgot.addMouseListener(new java.awt.event.MouseAdapter() {  // Xử lý mouse events
            public void mouseClicked(java.awt.event.MouseEvent evt) {   // Khi click
                new ForgotPasswordDialog(LoginFrame.this);               // Mở dialog quên mật khẩu
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {   // Khi hover vào
                lblForgot.setText("<html><u>QUÊN MẬT KHẨU</u></html>"); // Gạch chân
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {    // Khi hover ra
                lblForgot.setText("QUÊN MẬT KHẨU");                      // Bỏ gạch chân
            }
        });

        checkboxForgotPanel.add(leftPanel, BorderLayout.WEST);          // Thêm panel trái
        checkboxForgotPanel.add(lblForgot, BorderLayout.EAST);          // Thêm label quên MK bên phải

        // Login button
        JButton btnLogin = new JButton("ĐĂNG NHẬP");                    // Nút đăng nhập
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 18));          // Font size 18
        btnLogin.setForeground(Color.WHITE);                             // Màu chữ trắng
        btnLogin.setBackground(DARK_BLUE);                               // Màu nền xanh đậm
        btnLogin.setFocusPainted(false);                                 // Không vẽ focus
        btnLogin.setBorderPainted(false);                                // Không vẽ border
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));             // Con trỏ tay khi hover
        btnLogin.setMaximumSize(new Dimension(690, 60));                // Kích thước tối đa
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);              // Căn giữa
        btnLogin.addActionListener(e -> handleLogin());                  // Xử lý click

        // Register link
        JLabel lblRegister = new JLabel("ĐĂNG KÝ");                     // Label đăng ký
        lblRegister.setFont(new Font("Segoe UI", Font.BOLD, 16));       // Font size 16
        lblRegister.setForeground(DARK_BLUE);                            // Màu chữ xanh đậm
        lblRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));          // Con trỏ tay khi hover
        lblRegister.setAlignmentX(Component.CENTER_ALIGNMENT);           // Căn giữa
        lblRegister.addMouseListener(new java.awt.event.MouseAdapter() { // Xử lý mouse events
            public void mouseClicked(java.awt.event.MouseEvent evt) {   // Khi click
                dispose();                                               // Đóng LoginFrame
                new RegisterFrame();                                     // Mở RegisterFrame
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {   // Khi hover vào
                lblRegister.setText("<html><u>ĐĂNG KÝ</u></html>");     // Gạch chân
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {    // Khi hover ra
                lblRegister.setText("ĐĂNG KÝ");                         // Bỏ gạch chân
            }
        });

        formPanel.add(usernamePanel);                                    // Thêm panel username
        formPanel.add(Box.createVerticalStrut(25));                     // Khoảng cách 25px
        formPanel.add(passwordPanel);                                    // Thêm panel password
        formPanel.add(Box.createVerticalStrut(20));                     // Khoảng cách 20px
        formPanel.add(checkboxForgotPanel);                              // Thêm panel checkbox + forgot
        formPanel.add(Box.createVerticalStrut(40));                     // Khoảng cách 40px
        formPanel.add(btnLogin);                                         // Thêm nút đăng nhập
        formPanel.add(Box.createVerticalStrut(25));                     // Khoảng cách 25px
        formPanel.add(lblRegister);                                      // Thêm label đăng ký

        mainPanel.add(topPanel, BorderLayout.NORTH);                    // Thêm panel trên
        mainPanel.add(formPanel, BorderLayout.CENTER);                  // Thêm panel form ở giữa

        add(mainPanel);                                                  // Thêm panel chính vào frame
        setVisible(true);                                                // Hiển thị frame

        // Enter key listener
        txtPassword.addActionListener(e -> handleLogin());              // Enter trong password field → Login
    }

    // Thêm placeholder cho JTextField (text gợi ý màu xám khi field rỗng)
    private void addPlaceholder(JTextField textField, String placeholder) {
        textField.setForeground(Color.GRAY);                             // Màu chữ xám
        textField.setText(placeholder);                                  // Set text placeholder

        textField.addFocusListener(new java.awt.event.FocusAdapter() {  // Lắng nghe focus events
            public void focusGained(java.awt.event.FocusEvent evt) {    // Khi focus vào field
                if (textField.getText().equals(placeholder)) {           // Nếu đang là placeholder
                    textField.setText("");                               // Xóa text
                    textField.setForeground(Color.BLACK);                // Đổi màu chữ đen
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {      // Khi focus ra khỏi field
                if (textField.getText().isEmpty()) {                     // Nếu field rỗng
                    textField.setForeground(Color.GRAY);                 // Đổi màu chữ xám
                    textField.setText(placeholder);                      // Hiện lại placeholder
                }
            }
        });
    }

    // Thêm placeholder cho JPasswordField (text gợi ý màu xám khi field rỗng)
    private void addPasswordPlaceholder(JPasswordField passwordField, String placeholder) {
        passwordField.setForeground(Color.GRAY);                         // Màu chữ xám
        passwordField.setText(placeholder);                              // Set text placeholder
        passwordField.setEchoChar((char) 0);                             // Hiện rõ placeholder (không ẩn)

        passwordField.addFocusListener(new java.awt.event.FocusAdapter() { // Lắng nghe focus events
            public void focusGained(java.awt.event.FocusEvent evt) {    // Khi focus vào field
                if (new String(passwordField.getPassword()).equals(placeholder)) { // Nếu đang là placeholder
                    passwordField.setText("");                           // Xóa text
                    passwordField.setForeground(Color.BLACK);            // Đổi màu chữ đen
                    passwordField.setEchoChar('•');                      // Ẩn password bằng dấu •
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {      // Khi focus ra khỏi field
                if (new String(passwordField.getPassword()).isEmpty()) { // Nếu field rỗng
                    passwordField.setForeground(Color.GRAY);             // Đổi màu chữ xám
                    passwordField.setText(placeholder);                  // Hiện lại placeholder
                    passwordField.setEchoChar((char) 0);                 // Hiện rõ placeholder
                }
            }
        });
    }

    // Xử lý đăng nhập: Validate input → Xác thực với database → Lưu session → Mở MainFrame
    private void handleLogin() {
        String username = txtUsername.getText().trim();                  // Lấy username và trim khoảng trắng
        String password = new String(txtPassword.getPassword());         // Lấy password

        // Remove placeholder check
        if (username.equals("Nhập tên tài khoản")) {                     // Nếu đang là placeholder
            username = "";                                               // Set rỗng
        }
        if (password.equals("Nhập mật khẩu")) {                          // Nếu đang là placeholder
            password = "";                                               // Set rỗng
        }

        // Validation
        if (!ValidationUtil.isNotEmpty(username)) {                      // Nếu username rỗng
            showError("Vui lòng nhập tên tài khoản!");                   // Hiển thị lỗi
            txtUsername.requestFocus();                                  // Focus vào username field
            return;                                                      // Dừng xử lý
        }

        if (!ValidationUtil.isNotEmpty(password)) {                      // Nếu password rỗng
            showError("Vui lòng nhập mật khẩu!");                        // Hiển thị lỗi
            txtPassword.requestFocus();                                  // Focus vào password field
            return;                                                      // Dừng xử lý
        }

        // Authenticate
        NhanVienDTO user = nhanVienDAO.login(username, password);        // Xác thực với database (BCrypt)

        if (user != null) {                                              // Nếu đăng nhập thành công
            // Save session
            SessionManager.getInstance().login(user);                    // Lưu thông tin user vào session

            // Show success message
            JOptionPane.showMessageDialog(this,                          // Hiển thị thông báo thành công
                    "Đăng nhập thành công!\nChào mừng " + user.getHoTenNV(),
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            // Open main frame
            dispose();                                                   // Đóng LoginFrame
            new MainFrame();                                             // Mở MainFrame
        } else {                                                         // Nếu đăng nhập thất bại
            showError("Tên đăng nhập hoặc mật khẩu không đúng!");        // Hiển thị lỗi
            txtPassword.setText("");                                     // Xóa password field
            addPasswordPlaceholder(txtPassword, "Nhập mật khẩu");       // Thêm lại placeholder
            txtPassword.requestFocus();                                  // Focus vào password field
        }
    }

    // Hiển thị dialog lỗi
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE); // Dialog lỗi
    }
}

