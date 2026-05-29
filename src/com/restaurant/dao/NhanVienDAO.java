package com.restaurant.dao;

import com.restaurant.database.DatabaseConnection;
import com.restaurant.model.NhanVienDTO;
import com.restaurant.utils.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class quản lý thao tác database cho bảng NHANVIEN
 * Bao gồm: đăng nhập, CRUD nhân viên, tìm kiếm, quản lý mật khẩu
 */
public class NhanVienDAO {

    // Xác thực đăng nhập: kiểm tra username/password và trả về thông tin nhân viên nếu đúng
    public NhanVienDTO login(String tenDN, String matKhau) {
        String sql = "SELECT * FROM NHANVIEN WHERE TENDN = ?"; // Câu lệnh SQL tìm nhân viên theo tên đăng nhập

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement để tránh SQL Injection

            pstmt.setString(1, tenDN); // Gán giá trị username vào tham số thứ 1 của câu SQL
            ResultSet rs = pstmt.executeQuery(); // Thực thi câu query và nhận kết quả

            if (rs.next()) { // Nếu tìm thấy user trong database
                String hashedPassword = rs.getString("MATKHAU"); // Lấy mật khẩu đã hash từ cột MATKHAU

                if (PasswordUtil.verifyPassword(matKhau, hashedPassword)) { // So sánh password nhập vào với password đã hash
                    NhanVienDTO nv = new NhanVienDTO(); // Tạo object để chứa thông tin nhân viên
                    nv.setMaNV(rs.getInt("MANV")); // Gán mã nhân viên
                    nv.setHoTenNV(rs.getString("HOTENNV")); // Gán họ tên
                    nv.setTenDN(rs.getString("TENDN")); // Gán tên đăng nhập
                    nv.setMatKhau(hashedPassword); // Gán mật khẩu đã hash (không lưu plain text)
                    nv.setEmail(rs.getString("EMAIL")); // Gán email
                    nv.setSdt(rs.getString("SDT")); // Gán số điện thoại
                    nv.setGioiTinh(rs.getString("GIOITINH")); // Gán giới tính
                    nv.setNgaySinh(rs.getString("NGAYSINH")); // Gán ngày sinh
                    nv.setMaQuyen(rs.getInt("MAQUYEN")); // Gán mã quyền (Admin/Manager/Staff)
                    return nv; // Trả về thông tin nhân viên
                }
            }

        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace để debug
        }

        return null; // Đăng nhập thất bại: trả về null
    }

    // Kiểm tra username đã tồn tại trong database chưa (dùng khi đăng ký tài khoản mới)
    public boolean isUsernameExists(String tenDN) {
        String sql = "SELECT COUNT(*) FROM NHANVIEN WHERE TENDN = ?"; // Đếm số lượng user có username này

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setString(1, tenDN); // Gán username vào tham số thứ 1
            ResultSet rs = pstmt.executeQuery(); // Thực thi query

            if (rs.next()) { // Nếu có kết quả
                return rs.getInt(1) > 0; // Lấy giá trị COUNT(*) và kiểm tra > 0 (có tồn tại)
            }

        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return false; // Mặc định trả về false (chưa tồn tại)
    }

    // Thêm nhân viên mới vào database (hash password trước khi lưu)
    public boolean insertNhanVien(NhanVienDTO nv) {
        String sql = "INSERT INTO NHANVIEN (HOTENNV, TENDN, MATKHAU, EMAIL, SDT, GIOITINH, NGAYSINH, MAQUYEN) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"; // Câu lệnh SQL insert (MANV tự động tăng)

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setString(1, nv.getHoTenNV()); // Gán họ tên vào tham số 1
            pstmt.setString(2, nv.getTenDN()); // Gán tên đăng nhập vào tham số 2
            pstmt.setString(3, PasswordUtil.hashPassword(nv.getMatKhau())); // Hash password và gán vào tham số 3
            pstmt.setString(4, nv.getEmail()); // Gán email vào tham số 4
            pstmt.setString(5, nv.getSdt()); // Gán số điện thoại vào tham số 5
            pstmt.setString(6, nv.getGioiTinh()); // Gán giới tính vào tham số 6
            pstmt.setString(7, nv.getNgaySinh()); // Gán ngày sinh vào tham số 7
            pstmt.setInt(8, nv.getMaQuyen()); // Gán mã quyền vào tham số 8

            return pstmt.executeUpdate() > 0; // Thực thi insert và kiểm tra số dòng bị ảnh hưởng > 0

        } catch (SQLException e) {
            System.err.println("Error inserting employee: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return false; // Insert thất bại
    }


    // Cập nhật thông tin nhân viên (có thể cập nhật cả password hoặc không)
    public boolean updateNhanVien(NhanVienDTO nv) {
        boolean updatePassword = nv.getMatKhau() != null && !nv.getMatKhau().isEmpty(); // Kiểm tra có cần update password không

        String sql; // Khai báo biến SQL query
        if (updatePassword) { // Nếu có password mới
            sql = "UPDATE NHANVIEN SET HOTENNV = ?, MATKHAU = ?, EMAIL = ?, SDT = ?, " +
                  "GIOITINH = ?, NGAYSINH = ?, MAQUYEN = ? WHERE MANV = ?"; // Query có cập nhật password
        } else { // Nếu không đổi password
            sql = "UPDATE NHANVIEN SET HOTENNV = ?, EMAIL = ?, SDT = ?, " +
                  "GIOITINH = ?, NGAYSINH = ?, MAQUYEN = ? WHERE MANV = ?"; // Query không có cột MATKHAU
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            int paramIndex = 1; // Biến đếm vị trí tham số (bắt đầu từ 1)

            pstmt.setString(paramIndex++, nv.getHoTenNV()); // Gán họ tên và tăng index

            if (updatePassword) { // Nếu có password mới
                pstmt.setString(paramIndex++, PasswordUtil.hashPassword(nv.getMatKhau())); // Hash password và gán vào tham số
            }

            pstmt.setString(paramIndex++, nv.getEmail()); // Gán email
            pstmt.setString(paramIndex++, nv.getSdt()); // Gán số điện thoại
            pstmt.setString(paramIndex++, nv.getGioiTinh()); // Gán giới tính
            pstmt.setString(paramIndex++, nv.getNgaySinh()); // Gán ngày sinh
            pstmt.setInt(paramIndex++, nv.getMaQuyen()); // Gán mã quyền
            pstmt.setInt(paramIndex, nv.getMaNV()); // Gán mã nhân viên (WHERE clause)

            return pstmt.executeUpdate() > 0; // Thực thi update và kiểm tra số dòng bị ảnh hưởng

        } catch (SQLException e) {
            System.err.println("Error updating employee: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return false; // Update thất bại
    }

    // Cập nhật mật khẩu cho nhân viên (dùng khi đổi password hoặc reset password)
    public boolean updatePassword(int maNV, String newPassword) {
        String sql = "UPDATE NHANVIEN SET MATKHAU = ? WHERE MANV = ?"; // Câu lệnh SQL chỉ update cột MATKHAU

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setString(1, PasswordUtil.hashPassword(newPassword)); // Hash password mới và gán vào tham số 1
            pstmt.setInt(2, maNV); // Gán mã nhân viên vào tham số 2 (WHERE clause)

            return pstmt.executeUpdate() > 0; // Thực thi update và kiểm tra số dòng bị ảnh hưởng

        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return false; // Update thất bại
    }

    // Xóa nhân viên khỏi database (cẩn thận với foreign key constraint)
    public boolean deleteNhanVien(int maNV) {
        String sql = "DELETE FROM NHANVIEN WHERE MANV = ?"; // Câu lệnh SQL xóa nhân viên

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, maNV); // Gán mã nhân viên vào tham số 1

            return pstmt.executeUpdate() > 0; // Thực thi delete và kiểm tra số dòng bị ảnh hưởng

        } catch (SQLException e) {
            System.err.println("Error deleting employee: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return false; // Xóa thất bại
    }

    // Lấy danh sách tất cả nhân viên (không trả về password)
    public List<NhanVienDTO> getAllNhanVien() {
        List<NhanVienDTO> list = new ArrayList<>(); // Khởi tạo list rỗng để chứa kết quả
        String sql = "SELECT * FROM NHANVIEN"; // Câu lệnh SQL lấy tất cả nhân viên

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             Statement stmt = conn.createStatement(); // Tạo Statement (không cần tham số)
             ResultSet rs = stmt.executeQuery(sql)) { // Thực thi query và nhận kết quả

            while (rs.next()) { // Duyệt qua từng dòng kết quả
                NhanVienDTO nv = new NhanVienDTO(); // Tạo object để chứa thông tin nhân viên
                nv.setMaNV(rs.getInt("MANV")); // Gán mã nhân viên
                nv.setHoTenNV(rs.getString("HOTENNV")); // Gán họ tên
                nv.setTenDN(rs.getString("TENDN")); // Gán tên đăng nhập
                // Không set password (bảo mật)
                nv.setEmail(rs.getString("EMAIL")); // Gán email
                nv.setSdt(rs.getString("SDT")); // Gán số điện thoại
                nv.setGioiTinh(rs.getString("GIOITINH")); // Gán giới tính
                nv.setNgaySinh(rs.getString("NGAYSINH")); // Gán ngày sinh
                nv.setMaQuyen(rs.getInt("MAQUYEN")); // Gán mã quyền
                list.add(nv); // Thêm vào list
            }

        } catch (SQLException e) {
            System.err.println("Error getting all employees: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return list; // Trả về list (có thể rỗng nếu không có nhân viên)
    }

    // Tìm nhân viên theo mã nhân viên
    public NhanVienDTO getNhanVienById(int maNV) {
        String sql = "SELECT * FROM NHANVIEN WHERE MANV = ?"; // Câu lệnh SQL tìm nhân viên theo mã

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, maNV); // Gán mã nhân viên vào tham số 1
            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            if (rs.next()) { // Nếu tìm thấy
                NhanVienDTO nv = new NhanVienDTO(); // Tạo object để chứa thông tin nhân viên
                nv.setMaNV(rs.getInt("MANV")); // Gán mã nhân viên
                nv.setHoTenNV(rs.getString("HOTENNV")); // Gán họ tên
                nv.setTenDN(rs.getString("TENDN")); // Gán tên đăng nhập
                // Không set password (bảo mật)
                nv.setEmail(rs.getString("EMAIL")); // Gán email
                nv.setSdt(rs.getString("SDT")); // Gán số điện thoại
                nv.setGioiTinh(rs.getString("GIOITINH")); // Gán giới tính
                nv.setNgaySinh(rs.getString("NGAYSINH")); // Gán ngày sinh
                nv.setMaQuyen(rs.getInt("MAQUYEN")); // Gán mã quyền
                return nv; // Trả về thông tin nhân viên
            }

        } catch (SQLException e) {
            System.err.println("Error getting employee by ID: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return null; // Không tìm thấy hoặc lỗi
    }

    // Tìm nhân viên theo username
    public NhanVienDTO getNhanVienByUsername(String tenDN) {
        String sql = "SELECT * FROM NHANVIEN WHERE TENDN = ?"; // Câu lệnh SQL tìm nhân viên theo username

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setString(1, tenDN); // Gán username vào tham số 1
            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            if (rs.next()) { // Nếu tìm thấy
                NhanVienDTO nv = new NhanVienDTO(); // Tạo object để chứa thông tin nhân viên
                nv.setMaNV(rs.getInt("MANV")); // Gán mã nhân viên
                nv.setHoTenNV(rs.getString("HOTENNV")); // Gán họ tên
                nv.setTenDN(rs.getString("TENDN")); // Gán tên đăng nhập
                // Không set password (bảo mật)
                nv.setEmail(rs.getString("EMAIL")); // Gán email
                nv.setSdt(rs.getString("SDT")); // Gán số điện thoại
                nv.setGioiTinh(rs.getString("GIOITINH")); // Gán giới tính
                nv.setNgaySinh(rs.getString("NGAYSINH")); // Gán ngày sinh
                nv.setMaQuyen(rs.getInt("MAQUYEN")); // Gán mã quyền
                return nv; // Trả về thông tin nhân viên
            }

        } catch (SQLException e) {
            System.err.println("Error getting employee by username: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return null; // Không tìm thấy hoặc lỗi
    }

    // Tìm nhân viên theo email (dùng cho forgot password hoặc kiểm tra email trùng)
    public NhanVienDTO getNhanVienByEmail(String email) {
        String sql = "SELECT * FROM NHANVIEN WHERE EMAIL = ?"; // Câu lệnh SQL tìm nhân viên theo email

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setString(1, email); // Gán email vào tham số 1
            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            if (rs.next()) { // Nếu tìm thấy
                NhanVienDTO nv = new NhanVienDTO(); // Tạo object để chứa thông tin nhân viên
                nv.setMaNV(rs.getInt("MANV")); // Gán mã nhân viên
                nv.setHoTenNV(rs.getString("HOTENNV")); // Gán họ tên
                nv.setTenDN(rs.getString("TENDN")); // Gán tên đăng nhập
                // Không set password (bảo mật)
                nv.setEmail(rs.getString("EMAIL")); // Gán email
                nv.setSdt(rs.getString("SDT")); // Gán số điện thoại
                nv.setGioiTinh(rs.getString("GIOITINH")); // Gán giới tính
                nv.setNgaySinh(rs.getString("NGAYSINH")); // Gán ngày sinh
                nv.setMaQuyen(rs.getInt("MAQUYEN")); // Gán mã quyền
                return nv; // Trả về thông tin nhân viên
            }

        } catch (SQLException e) {
            System.err.println("Error getting employee by email: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return null; // Không tìm thấy hoặc lỗi
    }

    // Tìm kiếm nhân viên theo tên hoặc username (dùng cho search box)
    public List<NhanVienDTO> searchNhanVien(String keyword) {
        List<NhanVienDTO> list = new ArrayList<>(); // Khởi tạo list rỗng để chứa kết quả
        String sql = "SELECT * FROM NHANVIEN WHERE HOTENNV LIKE ? OR TENDN LIKE ?"; // Tìm kiếm trong cột HOTENNV hoặc TENDN

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            String searchPattern = "%" + keyword + "%"; // Tạo pattern tìm kiếm: %keyword% (tìm kiếm partial match)

            pstmt.setString(1, searchPattern); // Gán pattern vào tham số 1 (HOTENNV LIKE ?)
            pstmt.setString(2, searchPattern); // Gán pattern vào tham số 2 (TENDN LIKE ?)

            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            while (rs.next()) { // Duyệt qua từng dòng kết quả
                NhanVienDTO nv = new NhanVienDTO(); // Tạo object để chứa thông tin nhân viên
                nv.setMaNV(rs.getInt("MANV")); // Gán mã nhân viên
                nv.setHoTenNV(rs.getString("HOTENNV")); // Gán họ tên
                nv.setTenDN(rs.getString("TENDN")); // Gán tên đăng nhập
                // Không set password (bảo mật)
                nv.setEmail(rs.getString("EMAIL")); // Gán email
                nv.setSdt(rs.getString("SDT")); // Gán số điện thoại
                nv.setGioiTinh(rs.getString("GIOITINH")); // Gán giới tính
                nv.setNgaySinh(rs.getString("NGAYSINH")); // Gán ngày sinh
                nv.setMaQuyen(rs.getInt("MAQUYEN")); // Gán mã quyền
                list.add(nv); // Thêm vào list
            }

        } catch (SQLException e) {
            System.err.println("Error searching employees: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return list; // Trả về list (có thể rỗng nếu không tìm thấy)
    }
}

