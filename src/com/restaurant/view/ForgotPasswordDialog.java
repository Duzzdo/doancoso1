package com.restaurant.view;

import com.restaurant.dao.NhanVienDAO;
import com.restaurant.model.NhanVienDTO;
import com.restaurant.utils.EmailUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog quên mật khẩu - 3 bước xác thực: Email → OTP → Đổi mật khẩu
 */
public class ForgotPasswordDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private static final Color CYAN_BG = new Color(160, 231, 229);
    private static final Color DARK_BLUE = new Color(0, 0, 139);
    private static final Color FIELD_BG = Color.WHITE;

    private NhanVienDAO nhanVienDAO;

    // Step 1: Email
    private JTextField txtEmail;
    private JButton btnSendOTP;

    // Step 2: OTP
    private JTextField txtOTP;
    private JButton btnVerify;

    // Step 3: Password
    private JPasswordField txtNewPassword;
    private JButton btnReset;

    // Data
    private String generatedOTP;                                         // Mã OTP 6 chữ số được generate
    private NhanVienDTO foundUser;                                       // User tìm được theo email

    public ForgotPasswordDialog(Frame parent) {
        super(parent, "Quên mật khẩu", true);
        nhanVienDAO = new NhanVienDAO();

        setSize(450, 550);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(CYAN_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Title
        JLabel lblTitle = new JLabel("Quên mật khẩu");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(DARK_BLUE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(30));

        // ==================== SECTION 1: EMAIL ====================
        JPanel emailHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        emailHeaderPanel.setBackground(CYAN_BG);
        emailHeaderPanel.setMaximumSize(new Dimension(440, 30));
        JLabel lblEmail = new JLabel("Email khôi phục");
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblEmail.setForeground(DARK_BLUE);
        emailHeaderPanel.add(lblEmail);
        mainPanel.add(emailHeaderPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Segoe UI", Font.ITALIC, 15));
        txtEmail.setForeground(Color.GRAY);
        txtEmail.setText("Nhập email khôi phục");
        txtEmail.setMaximumSize(new Dimension(440, 40));
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DARK_BLUE, 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txtEmail.setBackground(FIELD_BG);
        txtEmail.setAlignmentX(Component.CENTER_ALIGNMENT);
        addPlaceholder(txtEmail, "Nhập email khôi phục");
        mainPanel.add(txtEmail);
        mainPanel.add(Box.createVerticalStrut(8));

        btnSendOTP = new JButton("Gửi mã OTP");
        btnSendOTP.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSendOTP.setForeground(Color.WHITE);
        btnSendOTP.setBackground(DARK_BLUE);
        btnSendOTP.setPreferredSize(new Dimension(140, 35));
        btnSendOTP.setMaximumSize(new Dimension(140, 35));
        btnSendOTP.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSendOTP.setFocusPainted(false);
        btnSendOTP.setBorderPainted(false);
        btnSendOTP.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSendOTP.addActionListener(e -> handleSendOTP());
        mainPanel.add(btnSendOTP);
        mainPanel.add(Box.createVerticalStrut(25));

        // ==================== SECTION 2: OTP ====================
        JPanel otpHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        otpHeaderPanel.setBackground(CYAN_BG);
        otpHeaderPanel.setMaximumSize(new Dimension(440, 30));
        JLabel lblOTP = new JLabel("Nhận mã OTP");
        lblOTP.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblOTP.setForeground(DARK_BLUE);
        otpHeaderPanel.add(lblOTP);
        mainPanel.add(otpHeaderPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        txtOTP = new JTextField();
        txtOTP.setFont(new Font("Segoe UI", Font.ITALIC, 15));
        txtOTP.setForeground(Color.GRAY);
        txtOTP.setText("Nhập OTP");
        txtOTP.setMaximumSize(new Dimension(440, 40));
        txtOTP.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DARK_BLUE, 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txtOTP.setBackground(FIELD_BG);
        txtOTP.setEnabled(false);                                        // Disabled cho đến khi gửi OTP thành công
        txtOTP.setAlignmentX(Component.CENTER_ALIGNMENT);
        addPlaceholder(txtOTP, "Nhập OTP");
        mainPanel.add(txtOTP);
        mainPanel.add(Box.createVerticalStrut(8));

        btnVerify = new JButton("Verify");
        btnVerify.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnVerify.setForeground(Color.WHITE);
        btnVerify.setBackground(new Color(100, 100, 100));
        btnVerify.setPreferredSize(new Dimension(100, 35));
        btnVerify.setMaximumSize(new Dimension(100, 35));
        btnVerify.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVerify.setFocusPainted(false);
        btnVerify.setBorderPainted(false);
        btnVerify.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVerify.setEnabled(false);                                     // Disabled cho đến khi gửi OTP thành công
        btnVerify.addActionListener(e -> handleVerifyOTP());
        mainPanel.add(btnVerify);
        mainPanel.add(Box.createVerticalStrut(25));

        // ==================== SECTION 3: PASSWORD ====================
        JPanel passHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        passHeaderPanel.setBackground(CYAN_BG);
        passHeaderPanel.setMaximumSize(new Dimension(440, 30));
        JLabel lblNewPass = new JLabel("Nhận đổi mật khẩu");
        lblNewPass.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblNewPass.setForeground(DARK_BLUE);
        passHeaderPanel.add(lblNewPass);
        mainPanel.add(passHeaderPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        txtNewPassword = new JPasswordField();
        txtNewPassword.setFont(new Font("Segoe UI", Font.ITALIC, 15));
        txtNewPassword.setForeground(Color.GRAY);
        txtNewPassword.setText("Nhập mật khẩu mới");
        txtNewPassword.setEchoChar((char) 0);                            // Hiển thị rõ placeholder
        txtNewPassword.setMaximumSize(new Dimension(440, 40));
        txtNewPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DARK_BLUE, 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txtNewPassword.setBackground(FIELD_BG);
        txtNewPassword.setEnabled(false);                                // Disabled cho đến khi verify OTP thành công
        txtNewPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        addPasswordPlaceholder(txtNewPassword, "Nhập mật khẩu mới");
        mainPanel.add(txtNewPassword);
        mainPanel.add(Box.createVerticalStrut(8));

        btnReset = new JButton("Đổi mật khẩu");
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnReset.setForeground(Color.WHITE);
        btnReset.setBackground(new Color(100, 100, 100));
        btnReset.setPreferredSize(new Dimension(140, 35));
        btnReset.setMaximumSize(new Dimension(140, 35));
        btnReset.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnReset.setFocusPainted(false);
        btnReset.setBorderPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.setEnabled(false);                                      // Disabled cho đến khi verify OTP thành công
        btnReset.addActionListener(e -> handleResetPassword());
        mainPanel.add(btnReset);

        add(mainPanel);
        setVisible(true);
    }

    // Thêm placeholder behavior cho JTextField
    private void addPlaceholder(JTextField field, String placeholder) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(DARK_BLUE);
                    field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setFont(new Font("Segoe UI", Font.ITALIC, 15));
                    field.setText(placeholder);
                }
            }
        });
    }

    // Thêm placeholder behavior cho JPasswordField
    private void addPasswordPlaceholder(JPasswordField field, String placeholder) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                String text = new String(field.getPassword());
                if (text.equals(placeholder)) {
                    field.setText("");
                    field.setForeground(DARK_BLUE);
                    field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                    field.setEchoChar('•');                              // Ẩn password bằng dấu chấm
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (field.getPassword().length == 0) {
                    field.setForeground(Color.GRAY);
                    field.setFont(new Font("Segoe UI", Font.ITALIC, 15));
                    field.setText(placeholder);
                    field.setEchoChar((char) 0);                         // Hiển thị rõ placeholder
                }
            }
        });
    }

    // Xử lý gửi mã OTP qua email (Step 1)
    private void handleSendOTP() {
        String email = txtEmail.getText().trim();

        // Validation: Email không rỗng
        if (email.isEmpty() || email.equals("Nhập email khôi phục")) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập email!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validation: Email format hợp lệ
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Email không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validation: Email tồn tại trong database
        foundUser = nhanVienDAO.getNhanVienByEmail(email);
        if (foundUser == null) {
            JOptionPane.showMessageDialog(this, "Email không tồn tại trong hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Generate OTP 6 chữ số và gửi email
        generatedOTP = EmailUtil.generateOTP();
        boolean sent = EmailUtil.sendOTPEmail(email, generatedOTP);

        if (sent) {
            JOptionPane.showMessageDialog(this, "Mã OTP đã được gửi đến email của bạn!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            // Enable Step 2
            txtEmail.setEnabled(false);
            btnSendOTP.setEnabled(false);
            txtOTP.setEnabled(true);
            btnVerify.setEnabled(true);
            btnVerify.setForeground(Color.WHITE);
            btnVerify.setBackground(DARK_BLUE);
            txtOTP.requestFocus();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể gửi email. Vui lòng thử lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Xử lý verify mã OTP (Step 2)
    private void handleVerifyOTP() {
        String enteredOTP = txtOTP.getText().trim();

        // Validation: OTP không rỗng
        if (enteredOTP.isEmpty() || enteredOTP.equals("Nhập OTP")) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã OTP!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validation: OTP khớp với mã đã gửi
        if (!enteredOTP.equals(generatedOTP)) {
            JOptionPane.showMessageDialog(this, "Mã OTP không đúng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Xác thực thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        // Enable Step 3
        txtOTP.setEnabled(false);
        btnVerify.setEnabled(false);
        txtNewPassword.setEnabled(true);
        btnReset.setEnabled(true);
        btnReset.setForeground(Color.WHITE);
        btnReset.setBackground(DARK_BLUE);
        txtNewPassword.requestFocus();
    }

    // Xử lý đặt lại mật khẩu (Step 3)
    private void handleResetPassword() {
        String newPassword = new String(txtNewPassword.getPassword()).trim();

        // Validation: Password không rỗng
        if (newPassword.isEmpty() || newPassword.equals("Nhập mật khẩu mới")) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mật khẩu mới!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validation: Password min 6 ký tự
        if (newPassword.length() < 6) {
            JOptionPane.showMessageDialog(this, "Mật khẩu phải có ít nhất 6 ký tự!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update password vào database (sẽ được hash BCrypt trong DAO)
        boolean success = nhanVienDAO.updatePassword(foundUser.getMaNV(), newPassword);
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Đặt lại mật khẩu thành công!\nVui lòng đăng nhập lại.",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra. Vui lòng thử lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
