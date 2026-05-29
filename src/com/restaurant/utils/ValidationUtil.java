package com.restaurant.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Utility class xử lý validation input từ user
 *
 * Chức năng:
 * - Validate email format (RFC 5322 standard)
 * - Validate số điện thoại (10-11 số)
 * - Validate username (3-20 ký tự alphanumeric)
 * - Validate password (tối thiểu 6 ký tự, không có dấu tiếng Việt)
 * - Validate số dương (giá tiền, số lượng)
 * - Validate ngày tháng format
 * - Sanitize input (trim whitespace)
 *
 * Tại sao cần validation?
 * - Ngăn chặn SQL injection, XSS
 * - Đảm bảo data integrity trong database
 * - Tránh lỗi runtime (NumberFormatException, DateTimeParseException)
 * - Cải thiện UX (thông báo lỗi rõ ràng trước khi submit)
 *
 * Sử dụng trong:
 * - RegisterFrame: Validate form đăng ký
 * - LoginFrame: Validate username/password
 * - StaffManagementPanel: Validate form thêm/sửa nhân viên
 * - MenuManagementPanel: Validate giá tiền món ăn
 * - OrderDialog: Validate số lượng món
 */
public class ValidationUtil {

    // Regex pattern cho email (RFC 5322 simplified)
    // VD hợp lệ: user@example.com, test.user+tag@domain.co.uk
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Regex pattern cho số điện thoại (10-11 số)
    // VD hợp lệ: 0123456789, 01234567890
    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^\\d{10,11}$");

    // Regex pattern cho username (3-20 ký tự alphanumeric)
    // VD hợp lệ: user123, admin, staff01
    // VD không hợp lệ: ab (quá ngắn), user@123 (có ký tự đặc biệt)
    private static final Pattern USERNAME_PATTERN =
        Pattern.compile("^[a-zA-Z0-9]{3,20}$");

    /**
     * Validate email format
     *
     * Kiểm tra:
     * - Có @ và domain
     * - Domain có ít nhất 1 dấu chấm
     * - Extension tối thiểu 2 ký tự (.com, .vn, .co.uk)
     *
     * @param email Email cần validate
     * @return true nếu hợp lệ, false nếu không hợp lệ hoặc null
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate số điện thoại (10-11 số)
     *
     * Kiểm tra:
     * - Chỉ chứa số (0-9)
     * - Độ dài 10 hoặc 11 số
     *
     * VD hợp lệ: 0123456789, 01234567890
     * VD không hợp lệ: 012345678 (9 số), 012-345-6789 (có dấu gạch)
     *
     * @param phone Số điện thoại cần validate
     * @return true nếu hợp lệ, false nếu không hợp lệ hoặc null
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate username (3-20 ký tự alphanumeric)
     *
     * Kiểm tra:
     * - Chỉ chứa chữ cái (a-z, A-Z) và số (0-9)
     * - Độ dài 3-20 ký tự
     * - Không có ký tự đặc biệt, khoảng trắng, dấu tiếng Việt
     *
     * VD hợp lệ: admin, user123, staff01
     * VD không hợp lệ: ab (quá ngắn), user@123 (có @), nguyễn (có dấu)
     *
     * @param username Username cần validate
     * @return true nếu hợp lệ, false nếu không hợp lệ hoặc null
     */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validate password (tối thiểu 6 ký tự, không có dấu tiếng Việt)
     *
     * Kiểm tra:
     * - Độ dài tối thiểu 6 ký tự
     * - Không chứa dấu tiếng Việt (à, á, ạ, ả, ã, â, ă, è, é, ...)
     *
     * Tại sao không cho dấu tiếng Việt?
     * - BCrypt có thể xử lý Unicode nhưng dễ gây lỗi encoding
     * - User có thể quên mình đang bật/tắt bộ gõ tiếng Việt
     * - Best practice: Password chỉ dùng ASCII
     *
     * VD hợp lệ: 123456, password123, Admin@2024
     * VD không hợp lệ: 12345 (quá ngắn), mậtkhẩu123 (có dấu)
     *
     * @param password Password cần validate
     * @return true nếu hợp lệ, false nếu không hợp lệ hoặc null hoặc < 6 ký tự
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        // Regex pattern cho tất cả dấu tiếng Việt
        String vietnamesePattern = "[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ]";
        return !password.matches(".*" + vietnamesePattern + ".*");
    }

    /**
     * Kiểm tra string không null và không rỗng
     *
     * Sử dụng khi: Validate các field bắt buộc (họ tên, tên món, tên bàn)
     *
     * @param str String cần kiểm tra
     * @return true nếu không null và không rỗng (sau khi trim), false nếu null hoặc rỗng
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Validate số dương (double)
     *
     * Sử dụng khi: Validate giá tiền món ăn, tổng tiền đơn hàng
     *
     * Kiểm tra:
     * - Parse được thành double
     * - Giá trị > 0
     *
     * @param str String cần validate
     * @return true nếu là số dương, false nếu không phải số hoặc <= 0
     */
    public static boolean isPositiveNumber(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            double num = Double.parseDouble(str);
            return num > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate số nguyên dương (int)
     *
     * Sử dụng khi: Validate số lượng món, số bàn
     *
     * Kiểm tra:
     * - Parse được thành int
     * - Giá trị > 0
     *
     * @param str String cần validate
     * @return true nếu là số nguyên dương, false nếu không phải số hoặc <= 0
     */
    public static boolean isPositiveInteger(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            int num = Integer.parseInt(str);
            return num > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Sanitize input string (trim whitespace)
     *
     * Sử dụng khi: Xử lý input trước khi lưu vào database
     *
     * @param input String cần sanitize
     * @return String đã trim, empty string nếu input null
     */
    public static String sanitize(String input) {
        return input == null ? "" : input.trim();
    }

    /**
     * Validate date format (yyyy-MM-dd)
     *
     * Sử dụng khi: Validate ngày sinh, ngày đặt từ JTextField
     *
     * @param date String ngày cần validate
     * @return true nếu đúng format yyyy-MM-dd, false nếu sai format hoặc null/empty
     */
    public static boolean isValidDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate.parse(date, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
