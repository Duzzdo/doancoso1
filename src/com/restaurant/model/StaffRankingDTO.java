package com.restaurant.model;

/**
 * DTO (Data Transfer Object) class cho thông tin xếp hạng nhân viên
 *
 * Chức năng:
 * - Lưu trữ thông tin doanh thu và xếp hạng của nhân viên
 * - Hiển thị bảng xếp hạng trong LeaderboardPanel
 * - Tính toán rank (Bronze/Silver/Gold/Platinum/Diamond) dựa trên doanh thu
 *
 * Business Rules:
 * - Chỉ tính nhân viên có MAQUYEN = 3 (Staff)
 * - Chỉ tính đơn hàng completed (đã thanh toán)
 * - Xếp hạng theo doanh thu giảm dần
 * - Rank được tính theo RankUtil.calculateRank(doanhThu):
 *   + Bronze: < 5,000,000 VNĐ
 *   + Silver: 5,000,000 - 9,999,999 VNĐ
 *   + Gold: 10,000,000 - 19,999,999 VNĐ
 *   + Platinum: 20,000,000 - 49,999,999 VNĐ
 *   + Diamond: >= 50,000,000 VNĐ
 *
 * Sử dụng trong:
 * - LeaderboardPanel: Hiển thị bảng xếp hạng nhân viên theo tháng
 * - StaffRevenuePanel: Hiển thị rank và doanh thu cá nhân
 * - AdminStatisticsPanel: Thống kê hiệu suất nhân viên
 */
public class StaffRankingDTO {
    private int maNV;           // Mã nhân viên
    private String hoTenNV;     // Họ tên nhân viên
    private double doanhThu;    // Tổng doanh thu (VNĐ)
    private int soDon;          // Số đơn hàng đã hoàn thành
    private int xepHang;        // Vị trí xếp hạng (1, 2, 3...)
    private String rank;        // Rank: Bronze, Silver, Gold, Platinum, Diamond
    private String rankIcon;    // Icon emoji cho rank (🥉, 🥈, 🥇, 💎, 👑)

    public StaffRankingDTO() {
    }

    public StaffRankingDTO(int maNV, String hoTenNV, double doanhThu, int soDon) {
        this.maNV = maNV;
        this.hoTenNV = hoTenNV;
        this.doanhThu = doanhThu;
        this.soDon = soDon;
    }

    // Getters and Setters
    public int getMaNV() {
        return maNV;
    }

    public void setMaNV(int maNV) {
        this.maNV = maNV;
    }

    public String getHoTenNV() {
        return hoTenNV;
    }

    public void setHoTenNV(String hoTenNV) {
        this.hoTenNV = hoTenNV;
    }

    public double getDoanhThu() {
        return doanhThu;
    }

    public void setDoanhThu(double doanhThu) {
        this.doanhThu = doanhThu;
    }

    public int getSoDon() {
        return soDon;
    }

    public void setSoDon(int soDon) {
        this.soDon = soDon;
    }

    public int getXepHang() {
        return xepHang;
    }

    public void setXepHang(int xepHang) {
        this.xepHang = xepHang;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getRankIcon() {
        return rankIcon;
    }

    public void setRankIcon(String rankIcon) {
        this.rankIcon = rankIcon;
    }

    /**
     * Tính trung bình doanh thu mỗi đơn
     * @return Doanh thu trung bình/đơn, 0 nếu chưa có đơn nào
     */
    public double getTrungBinhDon() {
        return soDon > 0 ? doanhThu / soDon : 0;
    }
}
