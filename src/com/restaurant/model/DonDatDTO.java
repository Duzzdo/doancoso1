package com.restaurant.model;

/**
 * DTO (Data Transfer Object) class đại diện cho bảng DONDAT trong database
 *
 * Chức năng:
 * - Lưu trữ thông tin đơn đặt món (Order)
 * - Quản lý trạng thái đơn hàng (pending/completed/cancelled)
 * - Tính toán tổng tiền đơn hàng
 *
 * Business Rules:
 * - MADONDAT: Auto-increment, primary key
 * - MABAN: Foreign key → BAN (bàn nào đặt món)
 * - MANV: Foreign key → NHANVIEN (nhân viên nào phục vụ)
 * - NGAYDAT: Thời gian tạo đơn (yyyy-MM-dd HH:mm:ss)
 * - TONGTIEN: Tổng tiền đơn hàng (tính từ CHITIETDONDAT)
 * - TINHTRANG: Trạng thái đơn hàng
 *   + "pending": Đơn đang chờ (khách đang ăn, chưa thanh toán)
 *   + "completed": Đơn đã thanh toán xong
 *   + "cancelled": Đơn bị hủy
 *
 * Quy trình xử lý đơn:
 * 1. Tạo đơn mới: TINHTRANG = "pending", cập nhật TINHTRANG bàn = "true"
 * 2. Thêm món: Insert vào CHITIETDONDAT, cập nhật TONGTIEN
 * 3. Thanh toán: TINHTRANG = "completed", cập nhật TINHTRANG bàn = "false"
 * 4. Hủy đơn: TINHTRANG = "cancelled", cập nhật TINHTRANG bàn = "false"
 *
 * Business Rule quan trọng:
 * - Mỗi bàn chỉ có tối đa 1 đơn pending tại một thời điểm
 * - Doanh thu chỉ tính đơn completed (không tính pending/cancelled)
 *
 * Sử dụng trong:
 * - OrderDialog: Tạo/sửa đơn đặt món
 * - PaymentPanel: Thanh toán đơn hàng
 * - StatisticsPanel: Thống kê doanh thu, lịch sử đơn hàng
 */
public class DonDatDTO {
    private int maDonDat;       // Mã đơn đặt (Primary Key, Auto-increment)
    private int maBan;          // Mã bàn (FK → BAN)
    private int maNV;           // Mã nhân viên phục vụ (FK → NHANVIEN)
    private String ngayDat;     // Ngày giờ đặt món (yyyy-MM-dd HH:mm:ss)
    private double tongTien;    // Tổng tiền đơn hàng (VNĐ)
    private String tinhTrang;   // Trạng thái: "pending", "completed", "cancelled"

    // Constructors
    public DonDatDTO() {
    }

    public DonDatDTO(int maDonDat, int maBan, int maNV, String ngayDat,
                     double tongTien, String tinhTrang) {
        this.maDonDat = maDonDat;
        this.maBan = maBan;
        this.maNV = maNV;
        this.ngayDat = ngayDat;
        this.tongTien = tongTien;
        this.tinhTrang = tinhTrang;
    }

    // Getters and Setters
    public int getMaDonDat() {
        return maDonDat;
    }

    public void setMaDonDat(int maDonDat) {
        this.maDonDat = maDonDat;
    }

    public int getMaBan() {
        return maBan;
    }

    public void setMaBan(int maBan) {
        this.maBan = maBan;
    }

    public int getMaNV() {
        return maNV;
    }

    public void setMaNV(int maNV) {
        this.maNV = maNV;
    }

    public String getNgayDat() {
        return ngayDat;
    }

    public void setNgayDat(String ngayDat) {
        this.ngayDat = ngayDat;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public String getTinhTrang() {
        return tinhTrang;
    }

    public void setTinhTrang(String tinhTrang) {
        this.tinhTrang = tinhTrang;
    }

    public boolean isPending() {
        return "pending".equals(tinhTrang);
    }

    public boolean isCompleted() {
        return "completed".equals(tinhTrang);
    }

    @Override
    public String toString() {
        return "Đơn #" + maDonDat + " - " + String.format("%,.0f", tongTien) + " VNĐ";
    }
}
