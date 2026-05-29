package com.restaurant.dao;

import com.restaurant.database.DatabaseConnection;
import com.restaurant.model.BanAnDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class quản lý thao tác database cho bảng BAN
 * Bao gồm: CRUD bàn ăn, quản lý trạng thái (trống/đang sử dụng), thống kê số bàn
 * Business rule: Không cho xóa bàn đang có khách, bàn mới tạo mặc định trạng thái trống
 */
public class BanAnDAO {

    /**
     * Lấy danh sách tất cả bàn ăn
     *
     * Sử dụng khi:
     * - Hiển thị grid bàn ăn trong TableManagementPanel
     * - Load dữ liệu cho dashboard
     * - Thống kê tổng số bàn
     *
     * @return List<BanAnDTO> danh sách tất cả bàn, sắp xếp theo MABAN, empty list nếu không có
     */
    public List<BanAnDTO> getAllBanAn() {
        // Khởi tạo list rỗng để chứa kết quả
        List<BanAnDTO> list = new ArrayList<>();

        // SQL query: Lấy tất cả bàn, sắp xếp theo mã bàn
        String sql = "SELECT * FROM BAN ORDER BY MABAN";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Duyệt qua từng dòng kết quả
            while (rs.next()) {
                // Tạo object BanAnDTO từ ResultSet
                BanAnDTO ban = new BanAnDTO();
                ban.setMaBan(rs.getInt("MABAN"));
                ban.setTenBan(rs.getString("TENBAN"));
                ban.setTinhTrang(rs.getString("TINHTRANG")); // 'false' hoặc 'true'
                list.add(ban);
            }

        } catch (SQLException e) {
            System.err.println("Error getting all tables: " + e.getMessage());
            e.printStackTrace();
        }

        // Trả về list (có thể rỗng nếu không có bàn hoặc lỗi)
        return list;
    }

    /**
     * Tìm bàn ăn theo mã bàn (MABAN)
     *
     * Sử dụng khi:
     * - Kiểm tra bàn có tồn tại không
     * - Lấy thông tin bàn để hiển thị chi tiết
     * - Kiểm tra trạng thái bàn trước khi tạo order
     *
     * @param maBan Mã bàn cần tìm
     * @return BanAnDTO nếu tìm thấy, null nếu không tồn tại
     */
    public BanAnDTO getBanAnById(int maBan) {
        // SQL query: Tìm bàn theo MABAN
        String sql = "SELECT * FROM BAN WHERE MABAN = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set parameter: MABAN cần tìm
            pstmt.setInt(1, maBan);

            // Execute query và lấy kết quả
            ResultSet rs = pstmt.executeQuery();

            // Kiểm tra có tìm thấy không
            if (rs.next()) {
                // Tạo object BanAnDTO từ ResultSet
                BanAnDTO ban = new BanAnDTO();
                ban.setMaBan(rs.getInt("MABAN"));
                ban.setTenBan(rs.getString("TENBAN"));
                ban.setTinhTrang(rs.getString("TINHTRANG"));
                return ban;
            }

        } catch (SQLException e) {
            System.err.println("Error getting table by ID: " + e.getMessage());
            e.printStackTrace();
        }

        // Không tìm thấy hoặc lỗi
        return null;
    }

    /**
     * Thêm bàn ăn mới vào database
     *
     * Quy trình:
     * 1. Nhận tên bàn từ UI
     * 2. Insert vào database với trạng thái mặc định 'false' (trống)
     * 3. MABAN tự động tăng (auto-increment)
     *
     * Validation:
     * - Nên kiểm tra tên bàn không trống ở UI layer
     * - Có thể kiểm tra tên bàn trùng (tùy business logic)
     *
     * @param tenBan Tên bàn mới (ví dụ: "Bàn 1", "Bàn VIP 1")
     * @return true nếu insert thành công, false nếu thất bại
     */
    public boolean insertBanAn(String tenBan) {
        // SQL query: Insert bàn mới với trạng thái mặc định 'false' (trống)
        String sql = "INSERT INTO BAN (TENBAN, TINHTRANG) VALUES (?, 'false')";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set parameter: Tên bàn
            pstmt.setString(1, tenBan);

            // Execute update: Trả về số dòng bị ảnh hưởng
            // > 0 nghĩa là insert thành công
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error inserting table: " + e.getMessage());
            e.printStackTrace();
        }

        // Insert thất bại
        return false;
    }

    /**
     * Cập nhật tên bàn
     *
     * Sử dụng khi:
     * - Đổi tên bàn trong quản lý bàn ăn
     * - Sửa lỗi chính tả tên bàn
     *
     * Best practice:
     * - Chỉ cho sửa tên khi bàn đang trống (kiểm tra ở UI layer)
     * - Confirm trước khi sửa
     *
     * @param maBan Mã bàn cần sửa
     * @param tenBan Tên bàn mới
     * @return true nếu update thành công, false nếu thất bại
     */
    public boolean updateTenBan(int maBan, String tenBan) {
        // SQL query: Chỉ update cột TENBAN
        String sql = "UPDATE BAN SET TENBAN = ? WHERE MABAN = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set parameters
            pstmt.setString(1, tenBan);  // Tên bàn mới
            pstmt.setInt(2, maBan);      // Mã bàn (WHERE clause)

            // Execute update: Trả về số dòng bị ảnh hưởng
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating table name: " + e.getMessage());
            e.printStackTrace();
        }

        // Update thất bại
        return false;
    }

    /**
     * Cập nhật trạng thái bàn
     *
     * Sử dụng khi:
     * - Khách đặt món → Set 'true' (đang sử dụng)
     * - Khách thanh toán → Set 'false' (trống)
     * - Hủy order → Set 'false' (trống)
     *
     * Trạng thái:
     * - 'false' = Bàn trống (available) → Hiển thị màu xanh lá
     * - 'true' = Bàn đang sử dụng (occupied) → Hiển thị màu đỏ
     *
     * @param maBan Mã bàn cần cập nhật
     * @param tinhTrang Trạng thái mới ('false' hoặc 'true')
     * @return true nếu update thành công, false nếu thất bại
     */
    public boolean updateTinhTrangBan(int maBan, String tinhTrang) {
        // SQL query: Chỉ update cột TINHTRANG
        String sql = "UPDATE BAN SET TINHTRANG = ? WHERE MABAN = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set parameters
            pstmt.setString(1, tinhTrang); // Trạng thái mới ('false' hoặc 'true')
            pstmt.setInt(2, maBan);        // Mã bàn (WHERE clause)

            // Execute update: Trả về số dòng bị ảnh hưởng
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating table status: " + e.getMessage());
            e.printStackTrace();
        }

        // Update thất bại
        return false;
    }

    /**
     * Xóa bàn ăn khỏi database
     *
     * Business Rule:
     * - KHÔNG cho xóa bàn đang có khách (TINHTRANG = 'true')
     * - Kiểm tra trạng thái bàn trước khi xóa
     * - Confirm dialog trước khi xóa
     *
     * Lưu ý:
     * - Có thể vi phạm foreign key constraint nếu bàn có order trong DONDAT
     * - Cân nhắc "soft delete" (đánh dấu inactive) thay vì xóa hẳn
     *
     * @param maBan Mã bàn cần xóa
     * @return true nếu xóa thành công, false nếu thất bại hoặc bàn đang sử dụng
     */
    public boolean deleteBanAn(int maBan) {
        // Bước 1: Kiểm tra bàn có đang sử dụng không
        BanAnDTO ban = getBanAnById(maBan);

        // Nếu bàn đang có khách → Không cho xóa
        if (ban != null && ban.isOccupied()) {
            System.err.println("Cannot delete occupied table");
            return false;
        }

        // Bước 2: Xóa bàn (chỉ khi bàn trống)
        String sql = "DELETE FROM BAN WHERE MABAN = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set parameter: MABAN cần xóa
            pstmt.setInt(1, maBan);

            // Execute update: Trả về số dòng bị ảnh hưởng
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            // Lỗi có thể xảy ra:
            // - Foreign key constraint: Bàn có order trong DONDAT
            // - MABAN không tồn tại
            System.err.println("Error deleting table: " + e.getMessage());
            e.printStackTrace();
        }

        // Xóa thất bại
        return false;
    }

    /**
     * Đếm số bàn trống (available)
     *
     * Sử dụng khi:
     * - Hiển thị card "Bàn trống" trên dashboard
     * - Thống kê tổng quan
     * - Kiểm tra còn bàn trống để đặt không
     *
     * @return Số lượng bàn có TINHTRANG = 'false'
     */
    public int getAvailableTablesCount() {
        // SQL query: Đếm số bàn trống
        String sql = "SELECT COUNT(*) FROM BAN WHERE TINHTRANG = 'false'";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                // Lấy giá trị COUNT(*) - cột đầu tiên (index 1)
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error getting available tables count: " + e.getMessage());
            e.printStackTrace();
        }

        // Lỗi hoặc không có bàn → Trả về 0
        return 0;
    }

    /**
     * Đếm số bàn đang sử dụng (occupied)
     *
     * Sử dụng khi:
     * - Hiển thị card "Bàn đang dùng" trên dashboard
     * - Thống kê tổng quan
     * - Giám sát tình trạng nhà hàng
     *
     * @return Số lượng bàn có TINHTRANG = 'true'
     */
    public int getOccupiedTablesCount() {
        // SQL query: Đếm số bàn đang sử dụng
        String sql = "SELECT COUNT(*) FROM BAN WHERE TINHTRANG = 'true'";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                // Lấy giá trị COUNT(*) - cột đầu tiên (index 1)
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error getting occupied tables count: " + e.getMessage());
            e.printStackTrace();
        }

        // Lỗi hoặc không có bàn → Trả về 0
        return 0;
    }

    /**
     * Cập nhật toàn bộ thông tin bàn (full object update)
     *
     * Sử dụng khi:
     * - Cập nhật cả tên và trạng thái cùng lúc
     * - Sync dữ liệu từ object BanAnDTO
     *
     * Lưu ý:
     * - Thường dùng updateTenBan() hoặc updateTinhTrangBan() riêng lẻ
     * - Method này update cả 2 cột cùng lúc
     *
     * @param ban Object BanAnDTO chứa thông tin mới (MABAN để xác định record)
     * @return true nếu update thành công, false nếu thất bại
     */
    public boolean updateBanAn(BanAnDTO ban) {
        // SQL query: Update cả TENBAN và TINHTRANG
        String sql = "UPDATE BAN SET TENBAN = ?, TINHTRANG = ? WHERE MABAN = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set parameters theo thứ tự
            pstmt.setString(1, ban.getTenBan());      // Tên bàn mới
            pstmt.setString(2, ban.getTinhTrang());   // Trạng thái mới
            pstmt.setInt(3, ban.getMaBan());          // Mã bàn (WHERE clause)

            // Execute update: Trả về số dòng bị ảnh hưởng
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating table: " + e.getMessage());
            e.printStackTrace();
        }

        // Update thất bại
        return false;
    }
}
