package com.restaurant.dao;

import com.restaurant.database.DatabaseConnection;
import com.restaurant.model.QuyenDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class quản lý thao tác database cho bảng QUYEN
 * Bao gồm: Lấy danh sách quyền, tìm quyền theo ID/tên
 * Business rule: Quyền cố định (Admin/Manager/Staff), không cho phép thêm/sửa/xóa
 */
public class QuyenDAO {

    /**
     * Lấy danh sách tất cả quyền
     *
     * Sử dụng khi:
     * - Load JComboBox chọn quyền trong StaffManagementPanel
     * - Hiển thị danh sách quyền trong hệ thống
     *
     * Quyền mặc định:
     * - MAQUYEN = 1: Admin (toàn quyền)
     * - MAQUYEN = 2: Manager (quản lý thực đơn, xem thống kê)
     * - MAQUYEN = 3: Staff (quản lý bàn, đặt món, thanh toán)
     *
     * @return List<QuyenDTO> danh sách tất cả quyền, empty list nếu không có
     */
    public List<QuyenDTO> getAllQuyen() {
        List<QuyenDTO> list = new ArrayList<>(); // Khởi tạo list rỗng để chứa kết quả
        String sql = "SELECT * FROM QUYEN"; // Câu lệnh SQL lấy tất cả quyền

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             Statement stmt = conn.createStatement(); // Tạo Statement (không cần tham số)
             ResultSet rs = stmt.executeQuery(sql)) { // Thực thi query và nhận kết quả

            while (rs.next()) { // Duyệt qua từng dòng kết quả
                QuyenDTO quyen = new QuyenDTO(); // Tạo object để chứa thông tin quyền
                quyen.setMaQuyen(rs.getInt("MAQUYEN")); // Gán mã quyền
                quyen.setTenQuyen(rs.getString("TENQUYEN")); // Gán tên quyền
                list.add(quyen); // Thêm vào list
            }

        } catch (SQLException e) {
            System.err.println("Error getting all roles: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return list; // Trả về list (có thể rỗng nếu không có quyền hoặc lỗi)
    }

    /**
     * Tìm quyền theo mã quyền (MAQUYEN)
     *
     * Sử dụng khi:
     * - Kiểm tra quyền có tồn tại không
     * - Lấy thông tin quyền để hiển thị
     * - Validate quyền khi thêm/sửa nhân viên
     *
     * @param maQuyen Mã quyền cần tìm (1=Admin, 2=Manager, 3=Staff)
     * @return QuyenDTO nếu tìm thấy, null nếu không tồn tại
     */
    public QuyenDTO getQuyenById(int maQuyen) {
        String sql = "SELECT * FROM QUYEN WHERE MAQUYEN = ?"; // Câu lệnh SQL tìm quyền theo MAQUYEN

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, maQuyen); // Gán mã quyền cần tìm vào tham số 1
            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            if (rs.next()) { // Nếu tìm thấy quyền
                QuyenDTO quyen = new QuyenDTO(); // Tạo object để chứa thông tin quyền
                quyen.setMaQuyen(rs.getInt("MAQUYEN")); // Gán mã quyền
                quyen.setTenQuyen(rs.getString("TENQUYEN")); // Gán tên quyền
                return quyen; // Trả về thông tin quyền
            }

        } catch (SQLException e) {
            System.err.println("Error getting role by ID: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return null; // Không tìm thấy hoặc lỗi
    }

    /**
     * Tìm quyền theo tên quyền (TENQUYEN)
     *
     * Sử dụng khi:
     * - Tìm kiếm quyền theo tên (ví dụ: "Admin", "Manager", "Staff")
     * - Validate tên quyền khi import dữ liệu
     *
     * @param tenQuyen Tên quyền cần tìm (ví dụ: "Admin")
     * @return QuyenDTO nếu tìm thấy, null nếu không tồn tại
     */
    public QuyenDTO getQuyenByName(String tenQuyen) {
        String sql = "SELECT * FROM QUYEN WHERE TENQUYEN = ?"; // Câu lệnh SQL tìm quyền theo TENQUYEN

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setString(1, tenQuyen); // Gán tên quyền cần tìm vào tham số 1
            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            if (rs.next()) { // Nếu tìm thấy quyền
                QuyenDTO quyen = new QuyenDTO(); // Tạo object để chứa thông tin quyền
                quyen.setMaQuyen(rs.getInt("MAQUYEN")); // Gán mã quyền
                quyen.setTenQuyen(rs.getString("TENQUYEN")); // Gán tên quyền
                return quyen; // Trả về thông tin quyền
            }

        } catch (SQLException e) {
            System.err.println("Error getting role by name: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return null; // Không tìm thấy hoặc lỗi
    }
}
