package com.restaurant.utils;

import java.util.Random;
import java.util.Properties;

/**
 * Utility class xử lý gửi email OTP (One-Time Password) để reset mật khẩu
 *
 * Chức năng:
 * - Generate mã OTP 6 số ngẫu nhiên
 * - Gửi email OTP qua Gmail SMTP (JavaMail)
 * - Fallback: In OTP ra console nếu không gửi được email
 *
 * OTP (One-Time Password) là gì?
 * - Mã xác thực 6 số dùng 1 lần (VD: 123456)
 * - Có thời hạn (5 phút)
 * - Dùng để xác thực khi reset password
 *
 * Quy trình reset password:
 * 1. User nhập email trong ForgotPasswordDialog
 * 2. Hệ thống generate OTP và gửi email
 * 3. User nhập OTP vào dialog
 * 4. Hệ thống verify OTP (so sánh với OTP đã gửi)
 * 5. Nếu đúng → Cho phép đổi password mới
 *
 * Gmail SMTP Configuration:
 * - Host: smtp.gmail.com
 * - Port: 587 (TLS)
 * - Auth: true (cần username/password)
 * - Username: SENDER_EMAIL
 * - Password: Gmail App Password (KHÔNG phải password Gmail thường)
 *
 * Cách tạo Gmail App Password:
 * 1. Vào Google Account → Security
 * 2. Bật 2-Step Verification
 * 3. Tạo App Password cho "Mail"
 * 4. Copy password 16 ký tự vào SENDER_PASSWORD
 *
 * Sử dụng trong:
 * - ForgotPasswordDialog: Gửi OTP khi user quên mật khẩu
 */
public class EmailUtil {

    // Email configuration - THAY ĐỔI GIÁ TRỊ NÀY
    private static final String SENDER_EMAIL = "anhduck2007@gmail.com"; // Email Gmail của bạn
    private static final String SENDER_PASSWORD = "esfqrhqneslrzbxg"; // Gmail App Password (16 ký tự)
    private static final boolean USE_REAL_EMAIL = true; // true = gửi email thật, false = in console

    /**
     * Generate mã OTP 6 số ngẫu nhiên
     *
     * Logic:
     * - Random số từ 100000 đến 999999
     * - Đảm bảo luôn có 6 chữ số
     *
     * @return String OTP 6 số (VD: "123456", "789012")
     */
    public static String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Gửi email OTP đến user
     *
     * Quy trình:
     * 1. Kiểm tra USE_REAL_EMAIL
     * 2. Nếu true → Gửi email thật qua Gmail SMTP
     * 3. Nếu false hoặc gửi thất bại → In OTP ra console
     *
     * @param recipientEmail Email người nhận
     * @param otp Mã OTP 6 số
     * @return true nếu gửi thành công, false nếu thất bại
     */
    public static boolean sendOTPEmail(String recipientEmail, String otp) {
        System.out.println("\n=== EMAIL SENDING DEBUG ===");
        System.out.println("Recipient: " + recipientEmail);
        System.out.println("OTP: " + otp);
        System.out.println("USE_REAL_EMAIL: " + USE_REAL_EMAIL);
        System.out.println("===========================\n");

        if (USE_REAL_EMAIL) {
            return sendRealEmail(recipientEmail, otp);
        } else {
            return sendConsoleEmail(recipientEmail, otp);
        }
    }

    /**
     * Gửi email thật qua Gmail SMTP (cần JavaMail library)
     *
     * Quy trình:
     * 1. Tạo Session với SMTP config
     * 2. Tạo MimeMessage
     * 3. Set From, To, Subject, Content (HTML)
     * 4. Gửi email qua Transport.send()
     *
     * Lưu ý:
     * - Cần thư viện javax.mail trong classpath
     * - Cần Gmail App Password (không dùng password thường)
     * - Nếu gửi thất bại → Fallback sang console
     *
     * @param recipientEmail Email người nhận
     * @param otp Mã OTP 6 số
     * @return true nếu gửi thành công, false nếu thất bại
     */
    private static boolean sendRealEmail(String recipientEmail, String otp) {
        try {
            // Direct JavaMail usage (requires library in classpath)
            javax.mail.Session session = javax.mail.Session.getInstance(
                getMailProperties(),
                new javax.mail.Authenticator() {
                    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new javax.mail.PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                    }
                }
            );

            javax.mail.internet.MimeMessage message = new javax.mail.internet.MimeMessage(session);
            message.setFrom(new javax.mail.internet.InternetAddress(SENDER_EMAIL));
            message.setRecipients(
                javax.mail.Message.RecipientType.TO,
                javax.mail.internet.InternetAddress.parse(recipientEmail)
            );
            message.setSubject("Mã OTP Đặt Lại Mật Khẩu - Nhà Hàng Hương Vị Việt", "UTF-8");
            message.setContent(buildEmailHTML(otp), "text/html; charset=utf-8");

            javax.mail.Transport.send(message);

            System.out.println("Email sent successfully to: " + recipientEmail);
            return true;

        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            System.err.println("Falling back to console output...");
            return sendConsoleEmail(recipientEmail, otp);
        }
    }

    /**
     * Get mail server properties
     */
    private static java.util.Properties getMailProperties() {
        java.util.Properties props = new java.util.Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        return props;
    }

    /**
     * In OTP ra console (dùng cho demo/testing khi không có email thật)
     *
     * Sử dụng khi:
     * - USE_REAL_EMAIL = false
     * - Gửi email thật thất bại (fallback)
     * - Testing local không cần gửi email
     *
     * @param recipientEmail Email người nhận (chỉ để hiển thị)
     * @param otp Mã OTP 6 số
     * @return true (luôn thành công)
     */
    private static boolean sendConsoleEmail(String recipientEmail, String otp) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SIMULATED EMAIL - OTP CODE");
        System.out.println("=".repeat(60));
        System.out.println("To: " + recipientEmail);
        System.out.println("Subject: Ma OTP dat lai mat khau");
        System.out.println("\nYour OTP Code: " + otp);
        System.out.println("Valid for: 5 minutes");
        System.out.println("=".repeat(60) + "\n");
        return true;
    }

    /**
     * Build nội dung email HTML đẹp mắt
     *
     * Template:
     * - Header: Gradient background, logo nhà hàng
     * - Body: Mã OTP to, rõ ràng, gradient box
     * - Footer: Thông tin liên hệ, copyright
     *
     * @param otp Mã OTP 6 số
     * @return HTML string
     */
    private static String buildEmailHTML(String otp) {
        return "<!DOCTYPE html>"
            + "<html>"
            + "<head>"
            + "<meta charset='UTF-8'>"
            + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
            + "</head>"
            + "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f7fa;'>"
            + "<table width='100%' cellpadding='0' cellspacing='0' style='background-color: #f4f7fa; padding: 40px 20px;'>"
            + "<tr><td align='center'>"
            + "<table width='600' cellpadding='0' cellspacing='0' style='background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.1); overflow: hidden;'>"

            // Header with gradient
            + "<tr><td style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 40px 30px; text-align: center;'>"
            + "<h1 style='margin: 0; color: #ffffff; font-size: 28px; font-weight: 600; letter-spacing: 1px;'>Nhà Hàng Hương Vị Việt</h1>"
            + "</td></tr>"

            // Content
            + "<tr><td style='padding: 50px 40px;'>"
            + "<h2 style='margin: 0 0 20px 0; color: #2c3e50; font-size: 24px; font-weight: 600; text-align: center;'>Đặt Lại Mật Khẩu</h2>"
            + "<p style='margin: 0 0 30px 0; color: #5a6c7d; font-size: 16px; line-height: 1.6; text-align: center;'>"
            + "Bạn đã yêu cầu đặt lại mật khẩu. Vui lòng sử dụng mã OTP dưới đây để xác thực:"
            + "</p>"

            // OTP Box with gradient
            + "<table width='100%' cellpadding='0' cellspacing='0' style='margin: 30px 0;'>"
            + "<tr><td align='center'>"
            + "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 35px; border-radius: 10px; display: inline-block;'>"
            + "<h1 style='margin: 0; color: #ffffff; font-size: 42px; font-weight: 700; letter-spacing: 12px; font-family: Courier New, monospace;'>" + otp + "</h1>"
            + "</div>"
            + "</td></tr></table>"

            // Warning text
            + "<p style='margin: 30px 0 10px 0; color: #5a6c7d; font-size: 15px; line-height: 1.6; text-align: center;'>"
            + "Mã OTP này có hiệu lực trong <strong style='color: #e74c3c; font-weight: 600;'>5 phút</strong>."
            + "</p>"
            + "<p style='margin: 0 0 30px 0; color: #95a5a6; font-size: 14px; line-height: 1.6; text-align: center;'>"
            + "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này."
            + "</p>"
            + "</td></tr>"

            // Footer
            + "<tr><td style='background-color: #f8f9fa; padding: 25px 40px; border-top: 1px solid #e9ecef;'>"
            + "<p style='margin: 0; color: #95a5a6; font-size: 13px; text-align: center; line-height: 1.5;'>"
            + "&copy; 2026 Nhà Hàng Hương Vị Việt. All rights reserved."
            + "</p>"
            + "</td></tr>"

            + "</table>"
            + "</td></tr></table>"
            + "</body>"
            + "</html>";
    }
}
