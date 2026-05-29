package com.restaurant.dao;

import com.restaurant.database.DatabaseConnection;
import com.restaurant.model.NhanVienDTO;
import com.restaurant.model.StaffRankingDTO;
import com.restaurant.utils.RankUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class quản lý thao tác xếp hạng nhân viên
 * Bao gồm: Tính doanh thu theo tháng, xếp hạng nhân viên, tính rank (Bronze/Silver/Gold/Platinum/Diamond)
 * Business rule: Chỉ tính nhân viên Staff (MAQUYEN = 3), chỉ tính đơn completed
 */
public class StaffRankingDAO {

    private NhanVienDAO nhanVienDAO;
    private DonDatDAO donDatDAO;

    public StaffRankingDAO() {
        nhanVienDAO = new NhanVienDAO();
        donDatDAO = new DonDatDAO();
    }

    /**
     * Lấy bảng xếp hạng nhân viên theo tháng
     *
     * Sử dụng khi:
     * - Hiển thị LeaderboardPanel (bảng xếp hạng tháng)
     * - So sánh hiệu suất nhân viên trong AdminStatisticsPanel
     *
     * Query:
     * - LEFT JOIN để bao gồm cả nhân viên chưa có đơn nào (doanh thu = 0)
     * - COALESCE để xử lý NULL → 0
     * - WHERE MAQUYEN = 3: Chỉ lấy nhân viên Staff
     * - WHERE TINHTRANG = 'completed': Chỉ tính đơn đã thanh toán
     * - GROUP BY nhân viên
     * - ORDER BY doanh thu giảm dần
     *
     * Rank được tính theo RankUtil:
     * - Bronze: < 5,000,000 VNĐ
     * - Silver: 5,000,000 - 9,999,999 VNĐ
     * - Gold: 10,000,000 - 19,999,999 VNĐ
     * - Platinum: 20,000,000 - 49,999,999 VNĐ
     * - Diamond: >= 50,000,000 VNĐ
     *
     * @param yearMonth Tháng cần xếp hạng (format: YYYY-MM, VD: "2026-05")
     * @return List<StaffRankingDTO> danh sách xếp hạng, sắp xếp theo doanh thu giảm dần
     */
    public List<StaffRankingDTO> getStaffRankingByMonth(String yearMonth) {
        List<StaffRankingDTO> rankings = new ArrayList<>(); // Khởi tạo list rỗng để chứa kết quả
        String sql = "SELECT n.MANV, n.HOTENNV, " +
                     "COALESCE(SUM(d.TONGTIEN), 0) as DOANHTHU, " + // COALESCE xử lý NULL → 0
                     "COALESCE(COUNT(d.MADONDAT), 0) as SODON " +
                     "FROM NHANVIEN n " +
                     "LEFT JOIN DONDAT d ON n.MANV = d.MANV " + // LEFT JOIN để bao gồm nhân viên chưa có đơn
                     "AND strftime('%Y-%m', d.NGAYDAT) = ? " + // Filter theo tháng (YYYY-MM)
                     "AND d.TINHTRANG = 'completed' " + // Chỉ tính đơn đã thanh toán
                     "WHERE n.MAQUYEN = 3 " + // Chỉ lấy nhân viên Staff
                     "GROUP BY n.MANV, n.HOTENNV " + // Nhóm theo nhân viên
                     "ORDER BY DOANHTHU DESC, n.HOTENNV ASC"; // Sắp xếp theo doanh thu giảm dần, tên tăng dần

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setString(1, yearMonth); // Gán tháng vào tham số 1 (format: YYYY-MM)
            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            int position = 1; // Biến đếm vị trí xếp hạng (bắt đầu từ 1)
            while (rs.next()) { // Duyệt qua từng dòng kết quả
                int maNV = rs.getInt("MANV"); // Lấy mã nhân viên
                String hoTenNV = rs.getString("HOTENNV"); // Lấy họ tên
                double doanhThu = rs.getDouble("DOANHTHU"); // Lấy tổng doanh thu
                int soDon = rs.getInt("SODON"); // Lấy số đơn

                // Tạo object StaffRankingDTO
                StaffRankingDTO ranking = new StaffRankingDTO(maNV, hoTenNV, doanhThu, soDon);
                ranking.setXepHang(position++); // Gán vị trí xếp hạng và tăng biến đếm

                // Tính rank dựa trên doanh thu (Bronze/Silver/Gold/Platinum/Diamond)
                String rank = RankUtil.calculateRank(doanhThu);
                ranking.setRank(rank); // Gán rank
                ranking.setRankIcon(RankUtil.getRankIcon(rank)); // Gán icon emoji

                rankings.add(ranking); // Thêm vào list
            }

        } catch (SQLException e) {
            System.err.println("Error getting staff ranking: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return rankings; // Trả về list (có thể rỗng nếu không có nhân viên hoặc lỗi)
    }

    /**
     * Lấy thông tin xếp hạng của một nhân viên cụ thể trong tháng
     *
     * Sử dụng khi:
     * - Hiển thị StaffRevenuePanel (doanh thu cá nhân)
     * - Xem chi tiết hiệu suất của một nhân viên
     *
     * Quy trình:
     * 1. Lấy doanh thu và số đơn của nhân viên (từ DonDatDAO)
     * 2. Lấy thông tin nhân viên (từ NhanVienDAO)
     * 3. Tính rank dựa trên doanh thu (RankUtil)
     * 4. Tính vị trí xếp hạng bằng cách so sánh với tất cả nhân viên
     *
     * @param maNV Mã nhân viên cần xem
     * @param yearMonth Tháng cần xem (format: YYYY-MM)
     * @return StaffRankingDTO thông tin xếp hạng của nhân viên
     */
    public StaffRankingDTO getStaffRankingById(int maNV, String yearMonth) {
        // Bước 1: Lấy doanh thu và số đơn của nhân viên trong tháng
        double doanhThu = donDatDAO.getRevenueByStaffAndMonth(maNV, yearMonth);
        int soDon = donDatDAO.getOrderCountByStaffAndMonth(maNV, yearMonth);

        // Bước 2: Lấy thông tin nhân viên
        NhanVienDTO nhanVien = nhanVienDAO.getNhanVienById(maNV);
        String hoTenNV = nhanVien != null ? nhanVien.getHoTenNV() : "Unknown";

        // Bước 3: Tạo object StaffRankingDTO
        StaffRankingDTO ranking = new StaffRankingDTO(maNV, hoTenNV, doanhThu, soDon);

        // Bước 4: Tính rank dựa trên doanh thu
        String rank = RankUtil.calculateRank(doanhThu);
        ranking.setRank(rank);
        ranking.setRankIcon(RankUtil.getRankIcon(rank));

        // Bước 5: Tính vị trí xếp hạng bằng cách so sánh với tất cả nhân viên
        List<StaffRankingDTO> allRankings = getStaffRankingByMonth(yearMonth);

        // Nếu nhân viên chưa có đơn nào → Xếp cuối
        if (doanhThu == 0 || soDon == 0) {
            // Đếm tổng số nhân viên để xác định vị trí cuối
            int totalStaff = nhanVienDAO.getAllNhanVien().size();
            ranking.setXepHang(totalStaff);
        } else {
            // Tìm vị trí trong danh sách xếp hạng
            int position = allRankings.size() + 1; // Mặc định: cuối cùng trong danh sách có đơn
            for (int i = 0; i < allRankings.size(); i++) {
                if (allRankings.get(i).getMaNV() == maNV) {
                    position = i + 1; // Vị trí = index + 1 (vì index bắt đầu từ 0)
                    break;
                }
            }
            ranking.setXepHang(position);
        }

        return ranking; // Trả về thông tin xếp hạng
    }

    /**
     * Lấy top N nhân viên có doanh thu cao nhất trong tháng
     *
     * Sử dụng khi:
     * - Hiển thị Top 3/Top 5 trong Dashboard
     * - Báo cáo nhân viên xuất sắc
     *
     * @param yearMonth Tháng cần xem (format: YYYY-MM)
     * @param limit Số lượng nhân viên cần lấy (VD: 5 → Top 5), nếu <= 0 thì lấy tất cả
     * @return List<StaffRankingDTO> danh sách top N nhân viên
     */
    public List<StaffRankingDTO> getTopStaff(String yearMonth, int limit) {
        // Lấy danh sách xếp hạng đầy đủ (đã sắp xếp theo doanh thu giảm dần)
        List<StaffRankingDTO> allRankings = getStaffRankingByMonth(yearMonth);

        // Nếu limit <= 0 hoặc số nhân viên ít hơn limit → Trả về tất cả
        if (limit <= 0 || allRankings.size() <= limit) {
            return allRankings;
        }

        // Trả về top N nhân viên (subList từ 0 đến limit)
        return allRankings.subList(0, limit);
    }
}
