package com.restaurant.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class xử lý mã hóa và xác thực mật khẩu sử dụng BCrypt
 *
 * Chức năng:
 * - Mã hóa mật khẩu plain text thành hash BCrypt
 * - Xác thực mật khẩu người dùng nhập với hash trong database
 * - Kiểm tra mật khẩu có cần rehash không (khi thay đổi rounds)
 *
 * BCrypt là gì?
 * - Thuật toán hash mật khẩu an toàn, chống brute-force
 * - Tự động thêm salt (random string) vào mỗi hash
 * - Có cost factor (rounds) để điều chỉnh độ khó
 * - Hash khác nhau mỗi lần (vì salt khác nhau) nhưng vẫn verify được
 *
 * Ví dụ:
 * - Plain password: "123456"
 * - BCrypt hash: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
 * - Mỗi lần hash "123456" sẽ ra kết quả khác nhau (vì salt khác)
 *
 * Security:
 * - KHÔNG BAO GIỜ lưu plain password vào database
 * - Chỉ lưu hash BCrypt
 * - Khi login: hash password nhập vào và so sánh với hash trong DB
 *
 * Sử dụng trong:
 * - RegisterFrame: Hash password trước khi insert vào NHANVIEN
 * - LoginFrame: Verify password nhập vào với hash trong database
 * - StaffManagementPanel: Hash password khi thêm/sửa nhân viên
 * - ForgotPasswordDialog: Hash password mới sau khi reset
 */
public class PasswordUtil {

    // Cost factor cho BCrypt (10 = 2^10 = 1024 iterations)
    // Càng cao càng an toàn nhưng càng chậm (10 là cân bằng tốt)
    private static final int BCRYPT_ROUNDS = 10;

    /**
     * Mã hóa mật khẩu plain text thành BCrypt hash
     *
     * Quy trình:
     * 1. Generate random salt (BCrypt.gensalt)
     * 2. Hash password với salt và cost factor
     * 3. Trả về hash string (chứa cả salt và hash)
     *
     * Ví dụ:
     * - Input: "123456"
     * - Output: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
     *
     * @param plainPassword Mật khẩu plain text cần mã hóa
     * @return BCrypt hash string
     * @throws IllegalArgumentException nếu password null hoặc rỗng
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Xác thực mật khẩu plain text với BCrypt hash
     *
     * Quy trình:
     * 1. Extract salt từ hashedPassword
     * 2. Hash plainPassword với salt đó
     * 3. So sánh hash mới với hashedPassword
     * 4. Trả về true nếu khớp, false nếu không khớp
     *
     * Ví dụ:
     * - plainPassword: "123456"
     * - hashedPassword: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
     * - Return: true (nếu đúng password)
     *
     * @param plainPassword Mật khẩu plain text cần verify
     * @param hashedPassword BCrypt hash từ database
     * @return true nếu password đúng, false nếu sai hoặc hash không hợp lệ
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Hash format không hợp lệ (không phải BCrypt hash)
            return false;
        }
    }

    /**
     * Kiểm tra mật khẩu có cần rehash không
     *
     * Sử dụng khi:
     * - Thay đổi BCRYPT_ROUNDS (tăng security)
     * - Upgrade thuật toán BCrypt
     * - Sau khi user login thành công, kiểm tra và rehash nếu cần
     *
     * Lưu ý:
     * - Method này chỉ là ví dụ đơn giản (so sánh length)
     * - Production nên dùng BCrypt.getRounds() để kiểm tra chính xác
     *
     * @param hashedPassword BCrypt hash cần kiểm tra
     * @return true nếu cần rehash, false nếu không cần
     */
    public static boolean needsRehash(String hashedPassword) {
        try {
            return BCrypt.gensalt(BCRYPT_ROUNDS).length() != hashedPassword.length();
        } catch (Exception e) {
            return true;
        }
    }
}
