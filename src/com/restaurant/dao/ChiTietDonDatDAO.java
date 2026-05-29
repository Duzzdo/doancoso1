package com.restaurant.dao;

import com.restaurant.database.DatabaseConnection;
import com.restaurant.model.ChiTietDonDatDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class quản lý thao tác database cho bảng CHITIETDONDAT
 * Bao gồm: CRUD chi tiết đơn đặt, thống kê món bán chạy, tính toán doanh thu theo món
 * Business rule: Composite primary key (MADONDAT, MAMON), tự động tính thành tiền = số lượng × giá
 */
public class ChiTietDonDatDAO {

    /**
     * Thêm chi tiết đơn đặt mới vào database
     *
     * Sử dụng khi:
     * - Thêm món vào đơn hàng trong OrderDialog
     * - Lưu giỏ hàng sau khi khách đặt món
     *
     * Quy trình:
     * 1. Nhận thông tin: MADONDAT, MAMON, SOLUONG, GHICHU
     * 2. Insert vào bảng CHITIETDONDAT
     * 3. Thành tiền được tính ở UI layer hoặc khi query (SOLUONG × GIATIEN)
     *
     * Lưu ý:
     * - Composite primary key: (MADONDAT, MAMON) → Không thể thêm trùng món trong cùng đơn
     * - Nếu món đã có trong đơn → Update số lượng thay vì insert
     *
     * @param chiTiet Object ChiTietDonDatDTO chứa thông tin chi tiết đơn
     * @return true nếu insert thành công, false nếu thất bại
     */
    public boolean insertChiTietDonDat(ChiTietDonDatDTO chiTiet) {
        String sql = "INSERT INTO CHITIETDONDAT (MADONDAT, MAMON, SOLUONG, GHICHU) VALUES (?, ?, ?, ?)"; // Câu lệnh SQL insert chi tiết đơn

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, chiTiet.getMaDonDat()); // Gán mã đơn đặt vào tham số 1 (FK)
            pstmt.setInt(2, chiTiet.getMaMon()); // Gán mã món vào tham số 2 (FK)
            pstmt.setInt(3, chiTiet.getSoLuong()); // Gán số lượng vào tham số 3
            pstmt.setString(4, chiTiet.getGhiChu()); // Gán ghi chú vào tham số 4 (có thể null)
            return pstmt.executeUpdate() > 0; // Thực thi insert và kiểm tra số dòng bị ảnh hưởng

        } catch (SQLException e) {
            System.err.println("Error inserting order detail: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return false; // Insert thất bại
    }

    /**
     * Cập nhật số lượng món trong chi tiết đơn
     *
     * Sử dụng khi:
     * - Khách thay đổi số lượng món trong OrderDialog
     * - Tăng/giảm số lượng món đã đặt
     *
     * Lưu ý:
     * - Chỉ update cột SOLUONG, không update GHICHU
     * - Thành tiền tự động thay đổi khi query (SOLUONG × GIATIEN)
     * - Nếu số lượng = 0 → Nên xóa món khỏi đơn thay vì update
     *
     * @param maDonDat Mã đơn đặt
     * @param maMon Mã món cần update
     * @param soLuong Số lượng mới
     * @return true nếu update thành công, false nếu thất bại
     */
    public boolean updateSoLuong(int maDonDat, int maMon, int soLuong) {
        String sql = "UPDATE CHITIETDONDAT SET SOLUONG = ? WHERE MADONDAT = ? AND MAMON = ?"; // Câu lệnh SQL chỉ update cột SOLUONG

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, soLuong); // Gán số lượng mới vào tham số 1
            pstmt.setInt(2, maDonDat); // Gán mã đơn vào tham số 2 (WHERE clause)
            pstmt.setInt(3, maMon); // Gán mã món vào tham số 3 (WHERE clause)
            return pstmt.executeUpdate() > 0; // Thực thi update và kiểm tra số dòng bị ảnh hưởng

        } catch (SQLException e) {
            System.err.println("Error updating quantity: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return false; // Update thất bại
    }

    /**
     * Xóa một món khỏi chi tiết đơn
     *
     * Sử dụng khi:
     * - Khách muốn bỏ món đã đặt trong OrderDialog
     * - Xóa món khỏi giỏ hàng trước khi thanh toán
     *
     * Lưu ý:
     * - Sau khi xóa, cần cập nhật lại TONGTIEN trong bảng DONDAT
     * - Nếu xóa hết món → Đơn hàng trống, cân nhắc xóa luôn đơn hoặc giữ lại
     *
     * @param maDonDat Mã đơn đặt
     * @param maMon Mã món cần xóa
     * @return true nếu xóa thành công, false nếu thất bại
     */
    public boolean deleteChiTietDonDat(int maDonDat, int maMon) {
        String sql = "DELETE FROM CHITIETDONDAT WHERE MADONDAT = ? AND MAMON = ?"; // Câu lệnh SQL xóa chi tiết đơn theo composite key

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, maDonDat); // Gán mã đơn vào tham số 1 (WHERE clause)
            pstmt.setInt(2, maMon); // Gán mã món vào tham số 2 (WHERE clause)
            return pstmt.executeUpdate() > 0; // Thực thi delete và kiểm tra số dòng bị ảnh hưởng

        } catch (SQLException e) {
            System.err.println("Error deleting order detail: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return false; // Xóa thất bại
    }

    /**
     * Xóa tất cả chi tiết của một đơn đặt
     *
     * Sử dụng khi:
     * - Hủy đơn hàng (xóa toàn bộ chi tiết trước khi xóa đơn)
     * - Reset đơn hàng (xóa hết món để đặt lại từ đầu)
     *
     * Lưu ý:
     * - Thường được gọi trước khi xóa đơn trong DONDAT
     * - Hoặc database có cascade delete thì tự động xóa
     *
     * @param maDonDat Mã đơn đặt cần xóa chi tiết
     * @return true nếu xóa thành công (>= 0 dòng bị ảnh hưởng), false nếu lỗi
     */
    public boolean deleteAllByDonDat(int maDonDat) {
        String sql = "DELETE FROM CHITIETDONDAT WHERE MADONDAT = ?"; // Câu lệnh SQL xóa tất cả chi tiết của một đơn

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, maDonDat); // Gán mã đơn vào tham số 1
            return pstmt.executeUpdate() >= 0; // Thực thi delete và kiểm tra (>= 0 vì có thể xóa 0 dòng nếu đơn rỗng)

        } catch (SQLException e) {
            System.err.println("Error deleting order details: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return false; // Xóa thất bại
    }

    /**
     * Lấy danh sách chi tiết đơn theo mã đơn đặt
     *
     * Sử dụng khi:
     * - Hiển thị giỏ hàng trong OrderDialog
     * - Xem chi tiết đơn hàng trong PaymentPanel
     * - Hiển thị hóa đơn khi thanh toán
     *
     * Query:
     * - JOIN với bảng MON để lấy TENMON và GIATIEN
     * - Tính THANHTIEN = SOLUONG × GIATIEN
     * - Trả về đầy đủ thông tin để hiển thị
     *
     * @param maDonDat Mã đơn đặt cần lấy chi tiết
     * @return List<ChiTietDonDatDTO> danh sách chi tiết đơn, empty list nếu không có
     */
    public List<ChiTietDonDatDTO> getChiTietByDonDat(int maDonDat) {
        List<ChiTietDonDatDTO> list = new ArrayList<>(); // Khởi tạo list rỗng để chứa kết quả
        String sql = "SELECT ct.*, m.TENMON, m.GIATIEN " +
                     "FROM CHITIETDONDAT ct " +
                     "JOIN MON m ON ct.MAMON = m.MAMON " +
                     "WHERE ct.MADONDAT = ?"; // Câu lệnh SQL JOIN để lấy đầy đủ thông tin món

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, maDonDat); // Gán mã đơn vào tham số 1
            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            while (rs.next()) { // Duyệt qua từng dòng kết quả
                ChiTietDonDatDTO chiTiet = new ChiTietDonDatDTO(); // Tạo object để chứa thông tin chi tiết
                chiTiet.setMaDonDat(rs.getInt("MADONDAT")); // Gán mã đơn
                chiTiet.setMaMon(rs.getInt("MAMON")); // Gán mã món
                chiTiet.setSoLuong(rs.getInt("SOLUONG")); // Gán số lượng
                chiTiet.setGhiChu(rs.getString("GHICHU")); // Gán ghi chú
                chiTiet.setTenMon(rs.getString("TENMON")); // Gán tên món (từ bảng MON)
                chiTiet.setGiaTien(rs.getDouble("GIATIEN")); // Gán giá tiền (từ bảng MON)
                chiTiet.setThanhTien(rs.getDouble("GIATIEN") * rs.getInt("SOLUONG")); // Tính thành tiền = giá × số lượng
                list.add(chiTiet); // Thêm vào list
            }

        } catch (SQLException e) {
            System.err.println("Error getting order details: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return list; // Trả về list (có thể rỗng nếu không có chi tiết hoặc lỗi)
    }

    /**
     * Kiểm tra món có trong đơn không
     *
     * Sử dụng khi:
     * - Trước khi thêm món vào đơn (kiểm tra trùng)
     * - Nếu món đã có → Update số lượng thay vì insert
     * - Nếu món chưa có → Insert mới
     *
     * @param maDonDat Mã đơn đặt
     * @param maMon Mã món cần kiểm tra
     * @return true nếu món đã có trong đơn, false nếu chưa có hoặc lỗi
     */
    public boolean isDishInOrder(int maDonDat, int maMon) {
        String sql = "SELECT COUNT(*) FROM CHITIETDONDAT WHERE MADONDAT = ? AND MAMON = ?"; // Câu lệnh SQL đếm số lượng record

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, maDonDat); // Gán mã đơn vào tham số 1
            pstmt.setInt(2, maMon); // Gán mã món vào tham số 2
            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            if (rs.next()) { // Nếu có kết quả
                return rs.getInt(1) > 0; // Trả về true nếu COUNT(*) > 0 (món đã có trong đơn)
            }

        } catch (SQLException e) {
            System.err.println("Error checking dish in order: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return false; // Món chưa có trong đơn hoặc lỗi
    }

    /**
     * Lấy danh sách món bán chạy nhất (Top N)
     *
     * Sử dụng khi:
     * - Hiển thị báo cáo "Món bán chạy" trong StatisticsPanel
     * - Dashboard hiển thị Top 5 món bán chạy
     *
     * Query:
     * - GROUP BY món ăn
     * - SUM(SOLUONG) để tính tổng số lượng đã bán
     * - SUM(SOLUONG × GIATIEN) để tính tổng doanh thu
     * - Chỉ tính đơn completed (đã thanh toán)
     * - ORDER BY TOTAL_SOLD DESC để sắp xếp giảm dần
     * - LIMIT N để lấy top N món
     *
     * @param limit Số lượng món cần lấy (ví dụ: 10 → Top 10)
     * @return List<ChiTietDonDatDTO> danh sách món bán chạy, empty list nếu không có
     */
    public List<ChiTietDonDatDTO> getBestSellingDishes(int limit) {
        List<ChiTietDonDatDTO> list = new ArrayList<>(); // Khởi tạo list rỗng để chứa kết quả
        String sql = "SELECT m.MAMON, m.TENMON, m.GIATIEN, SUM(ct.SOLUONG) as TOTAL_SOLD, " +
                     "SUM(ct.SOLUONG * m.GIATIEN) as TOTAL_REVENUE " +
                     "FROM CHITIETDONDAT ct " +
                     "JOIN MON m ON ct.MAMON = m.MAMON " +
                     "JOIN DONDAT d ON ct.MADONDAT = d.MADONDAT " +
                     "WHERE d.TINHTRANG = 'completed' " + // Chỉ tính đơn đã thanh toán
                     "GROUP BY m.MAMON, m.TENMON, m.GIATIEN " + // Nhóm theo món
                     "ORDER BY TOTAL_SOLD DESC " + // Sắp xếp giảm dần theo số lượng bán
                     "LIMIT ?"; // Giới hạn số lượng kết quả

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, limit); // Gán limit vào tham số 1
            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            while (rs.next()) { // Duyệt qua từng dòng kết quả
                ChiTietDonDatDTO chiTiet = new ChiTietDonDatDTO(); // Tạo object để chứa thông tin
                chiTiet.setMaMon(rs.getInt("MAMON")); // Gán mã món
                chiTiet.setTenMon(rs.getString("TENMON")); // Gán tên món
                chiTiet.setGiaTien(rs.getDouble("GIATIEN")); // Gán giá tiền
                chiTiet.setSoLuong(rs.getInt("TOTAL_SOLD")); // Gán tổng số lượng đã bán
                chiTiet.setThanhTien(rs.getDouble("TOTAL_REVENUE")); // Gán tổng doanh thu
                list.add(chiTiet); // Thêm vào list
            }

        } catch (SQLException e) {
            System.err.println("Error getting best selling dishes: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return list; // Trả về list (có thể rỗng nếu không có dữ liệu hoặc lỗi)
    }

    /**
     * Lấy danh sách món bán chạy nhất theo khoảng thời gian (Top N)
     *
     * Sử dụng khi:
     * - Báo cáo món bán chạy theo ngày/tháng/năm trong StatisticsPanel
     * - Filter theo khoảng thời gian cụ thể
     *
     * Query:
     * - Tương tự getBestSellingDishes() nhưng có thêm filter theo ngày
     * - WHERE DATE(NGAYDAT) BETWEEN fromDate AND toDate
     * - Chỉ tính đơn completed (đã thanh toán)
     *
     * @param fromDate Ngày bắt đầu (format: yyyy-MM-dd)
     * @param toDate Ngày kết thúc (format: yyyy-MM-dd)
     * @param limit Số lượng món cần lấy (ví dụ: 10 → Top 10)
     * @return List<ChiTietDonDatDTO> danh sách món bán chạy trong khoảng thời gian, empty list nếu không có
     */
    public List<ChiTietDonDatDTO> getBestSellingDishesByDateRange(String fromDate, String toDate, int limit) {
        List<ChiTietDonDatDTO> list = new ArrayList<>(); // Khởi tạo list rỗng để chứa kết quả
        String sql = "SELECT m.MAMON, m.TENMON, m.GIATIEN, SUM(ct.SOLUONG) as TOTAL_SOLD, " +
                     "SUM(ct.SOLUONG * m.GIATIEN) as TOTAL_REVENUE " +
                     "FROM CHITIETDONDAT ct " +
                     "JOIN MON m ON ct.MAMON = m.MAMON " +
                     "JOIN DONDAT d ON ct.MADONDAT = d.MADONDAT " +
                     "WHERE d.TINHTRANG = 'completed' " + // Chỉ tính đơn đã thanh toán
                     "AND DATE(d.NGAYDAT) BETWEEN DATE(?) AND DATE(?) " + // Filter theo khoảng thời gian
                     "GROUP BY m.MAMON, m.TENMON, m.GIATIEN " + // Nhóm theo món
                     "ORDER BY TOTAL_SOLD DESC " + // Sắp xếp giảm dần theo số lượng bán
                     "LIMIT ?"; // Giới hạn số lượng kết quả

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setString(1, fromDate); // Gán ngày bắt đầu vào tham số 1
            pstmt.setString(2, toDate); // Gán ngày kết thúc vào tham số 2
            pstmt.setInt(3, limit); // Gán limit vào tham số 3
            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            while (rs.next()) { // Duyệt qua từng dòng kết quả
                ChiTietDonDatDTO chiTiet = new ChiTietDonDatDTO(); // Tạo object để chứa thông tin
                chiTiet.setMaMon(rs.getInt("MAMON")); // Gán mã món
                chiTiet.setTenMon(rs.getString("TENMON")); // Gán tên món
                chiTiet.setGiaTien(rs.getDouble("GIATIEN")); // Gán giá tiền
                chiTiet.setSoLuong(rs.getInt("TOTAL_SOLD")); // Gán tổng số lượng đã bán
                chiTiet.setThanhTien(rs.getDouble("TOTAL_REVENUE")); // Gán tổng doanh thu
                list.add(chiTiet); // Thêm vào list
            }

        } catch (SQLException e) {
            System.err.println("Error getting best selling dishes by date range: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return list; // Trả về list (có thể rỗng nếu không có dữ liệu hoặc lỗi)
    }
}
