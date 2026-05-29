package com.restaurant.dao;

import com.restaurant.database.DatabaseConnection;
import com.restaurant.model.LoaiMonDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class quản lý thao tác database cho bảng LOAIMON
 * Bao gồm: CRUD loại món, xử lý hình ảnh BLOB, kiểm tra ràng buộc trước khi xóa
 * Business rule: Không cho xóa loại món đang có món ăn, hình ảnh upload từ máy tính và lưu dạng BLOB
 */
public class LoaiMonDAO {

    /**
     * Lấy danh sách tất cả loại món
     *
     * Sử dụng khi:
     * - Hiển thị danh sách loại món trong MenuManagementPanel
     * - Load JComboBox chọn loại món khi thêm/sửa món ăn
     * - Hiển thị tabs loại món trong OrderDialog
     *
     * @return List<LoaiMonDTO> danh sách tất cả loại món, sắp xếp theo MALOAI, empty list nếu không có
     */
    public List<LoaiMonDTO> getAllLoaiMon() {
        List<LoaiMonDTO> list = new ArrayList<>(); // Khởi tạo list rỗng để chứa kết quả
        String sql = "SELECT * FROM LOAIMON ORDER BY MALOAI"; // Câu lệnh SQL lấy tất cả loại món, sắp xếp theo mã loại

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             Statement stmt = conn.createStatement(); // Tạo Statement (không cần tham số)
             ResultSet rs = stmt.executeQuery(sql)) { // Thực thi query và nhận kết quả

            while (rs.next()) { // Duyệt qua từng dòng kết quả
                LoaiMonDTO loai = new LoaiMonDTO(); // Tạo object để chứa thông tin loại món
                loai.setMaLoai(rs.getInt("MALOAI")); // Gán mã loại
                loai.setTenLoai(rs.getString("TENLOAI")); // Gán tên loại
                loai.setHinhAnh(rs.getBytes("HINHANH")); // Gán hình ảnh (BLOB → byte[])
                list.add(loai); // Thêm vào list
            }

        } catch (SQLException e) {
            System.err.println("Error getting all categories: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return list; // Trả về list (có thể rỗng nếu không có loại món hoặc lỗi)
    }

    /**
     * Tìm loại món theo mã loại (MALOAI)
     *
     * Sử dụng khi:
     * - Kiểm tra loại món có tồn tại không
     * - Lấy thông tin loại món để hiển thị chi tiết
     * - Validate loại món khi thêm/sửa món ăn
     *
     * @param maLoai Mã loại món cần tìm
     * @return LoaiMonDTO nếu tìm thấy, null nếu không tồn tại
     */
    public LoaiMonDTO getLoaiMonById(int maLoai) {
        String sql = "SELECT * FROM LOAIMON WHERE MALOAI = ?"; // Câu lệnh SQL tìm loại món theo MALOAI

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, maLoai); // Gán mã loại cần tìm vào tham số 1
            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            if (rs.next()) { // Nếu tìm thấy loại món
                LoaiMonDTO loai = new LoaiMonDTO(); // Tạo object để chứa thông tin loại món
                loai.setMaLoai(rs.getInt("MALOAI")); // Gán mã loại
                loai.setTenLoai(rs.getString("TENLOAI")); // Gán tên loại
                loai.setHinhAnh(rs.getBytes("HINHANH")); // Gán hình ảnh (BLOB)
                return loai; // Trả về thông tin loại món
            }

        } catch (SQLException e) {
            System.err.println("Error getting category by ID: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return null; // Không tìm thấy hoặc lỗi
    }

    /**
     * Thêm loại món mới vào database
     *
     * Quy trình:
     * 1. Nhận tên loại và hình ảnh từ UI
     * 2. Hình ảnh được upload từ máy tính qua JFileChooser
     * 3. Resize hình ảnh trước khi lưu (ImageUtil.resizeImage)
     * 4. Convert hình ảnh thành byte[] và lưu dạng BLOB
     * 5. Nếu không upload hình, dùng default-category.png
     *
     * Validation:
     * - Tên loại không trống (kiểm tra ở UI layer)
     * - File upload phải là ảnh hợp lệ (JPG, JPEG, PNG)
     *
     * @param loai Object LoaiMonDTO chứa thông tin loại món mới (MALOAI auto-increment)
     * @return true nếu insert thành công, false nếu thất bại
     */
    public boolean insertLoaiMon(LoaiMonDTO loai) {
        String sql = "INSERT INTO LOAIMON (TENLOAI, HINHANH) VALUES (?, ?)"; // Câu lệnh SQL insert loại món mới (MALOAI auto-increment)

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setString(1, loai.getTenLoai()); // Gán tên loại vào tham số 1
            pstmt.setBytes(2, loai.getHinhAnh()); // Gán hình ảnh vào tham số 2 (BLOB - byte[])
            return pstmt.executeUpdate() > 0; // Thực thi insert và kiểm tra số dòng bị ảnh hưởng

        } catch (SQLException e) {
            System.err.println("Error inserting category: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return false; // Insert thất bại
    }

    /**
     * Cập nhật thông tin loại món
     *
     * Sử dụng khi:
     * - Đổi tên loại món
     * - Thay đổi hình ảnh loại món
     *
     * Lưu ý:
     * - Nếu không đổi hình ảnh, truyền byte[] cũ vào
     * - Nếu đổi hình ảnh, upload file mới và resize trước khi lưu
     *
     * @param loai Object LoaiMonDTO chứa thông tin mới (MALOAI để xác định record)
     * @return true nếu update thành công, false nếu thất bại
     */
    public boolean updateLoaiMon(LoaiMonDTO loai) {
        String sql = "UPDATE LOAIMON SET TENLOAI = ?, HINHANH = ? WHERE MALOAI = ?"; // Câu lệnh SQL update toàn bộ thông tin loại món

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setString(1, loai.getTenLoai()); // Gán tên loại mới vào tham số 1
            pstmt.setBytes(2, loai.getHinhAnh()); // Gán hình ảnh mới vào tham số 2 (hoặc giữ nguyên)
            pstmt.setInt(3, loai.getMaLoai()); // Gán mã loại vào tham số 3 (WHERE clause)
            return pstmt.executeUpdate() > 0; // Thực thi update và kiểm tra số dòng bị ảnh hưởng

        } catch (SQLException e) {
            System.err.println("Error updating category: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return false; // Update thất bại
    }

    /**
     * Xóa loại món khỏi database
     *
     * Business Rule:
     * - KHÔNG cho xóa loại món đang có món ăn
     * - Kiểm tra bằng method hasDishes() trước khi xóa
     * - Confirm dialog trước khi xóa (ở UI layer)
     *
     * Quy trình:
     * 1. Kiểm tra loại món có món ăn không (hasDishes)
     * 2. Nếu có món ăn → Từ chối xóa, hiển thị thông báo
     * 3. Nếu không có món ăn → Xóa loại món
     *
     * @param maLoai Mã loại món cần xóa
     * @return true nếu xóa thành công, false nếu thất bại hoặc loại món có món ăn
     */
    public boolean deleteLoaiMon(int maLoai) {
        // Bước 1: Kiểm tra loại món có món ăn không
        if (hasDishes(maLoai)) {
            System.err.println("Cannot delete category with existing dishes"); // In thông báo lỗi
            return false; // Không cho xóa
        }

        // Bước 2: Xóa loại món (chỉ khi không có món ăn)
        String sql = "DELETE FROM LOAIMON WHERE MALOAI = ?"; // Câu lệnh SQL xóa loại món

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, maLoai); // Gán mã loại cần xóa vào tham số 1
            return pstmt.executeUpdate() > 0; // Thực thi delete và kiểm tra số dòng bị ảnh hưởng

        } catch (SQLException e) {
            System.err.println("Error deleting category: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return false; // Xóa thất bại
    }

    /**
     * Kiểm tra loại món có món ăn không
     *
     * Sử dụng khi:
     * - Trước khi xóa loại món (trong deleteLoaiMon)
     * - Validate trước khi cho phép xóa (ở UI layer)
     *
     * Logic:
     * - Đếm số món ăn có MALOAI = maLoai
     * - Nếu COUNT(*) > 0 → Có món ăn
     * - Nếu COUNT(*) = 0 → Không có món ăn
     *
     * @param maLoai Mã loại món cần kiểm tra
     * @return true nếu có món ăn, false nếu không có món ăn hoặc lỗi
     */
    private boolean hasDishes(int maLoai) {
        String sql = "SELECT COUNT(*) FROM MON WHERE MALOAI = ?"; // Câu lệnh SQL đếm số món ăn của loại món

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, maLoai); // Gán mã loại vào tham số 1
            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            if (rs.next()) { // Nếu có kết quả
                return rs.getInt(1) > 0; // Trả về true nếu COUNT(*) > 0 (có món ăn)
            }

        } catch (SQLException e) {
            System.err.println("Error checking dishes: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return false; // Không có món ăn hoặc lỗi
    }
}
