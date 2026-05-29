package com.restaurant.model;

/**
 * DTO (Data Transfer Object) class đại diện cho bảng CHITIETDONDAT trong database
 *
 * Chức năng:
 * - Lưu trữ chi tiết món ăn trong đơn đặt (Order Details)
 * - Quản lý số lượng và ghi chú cho từng món
 * - Tính toán thành tiền (số lượng × giá)
 *
 * Business Rules:
 * - Composite Primary Key: (MADONDAT, MAMON)
 * - MADONDAT: Foreign key → DONDAT (đơn hàng nào)
 * - MAMON: Foreign key → MON (món gì)
 * - SOLUONG: Số lượng món (phải > 0)
 * - GHICHU: Ghi chú đặc biệt (VD: "Không hành", "Ít cay", có thể null)
 * - Không thể thêm trùng món trong cùng đơn → Update số lượng thay vì insert
 *
 * Additional Fields (không có trong database):
 * - tenMon: Tên món (JOIN từ bảng MON)
 * - giaTien: Giá tiền (JOIN từ bảng MON)
 * - thanhTien: Thành tiền = soLuong × giaTien (tính toán)
 *
 * Quy trình xử lý:
 * 1. Thêm món vào đơn:
 *    - Kiểm tra món đã có trong đơn chưa (isDishInOrder)
 *    - Nếu có → Update số lượng (updateSoLuong)
 *    - Nếu chưa → Insert mới (insertChiTietDonDat)
 * 2. Xóa món: deleteChiTietDonDat
 * 3. Sau mỗi thay đổi → Cập nhật TONGTIEN trong DONDAT
 *
 * Sử dụng trong:
 * - OrderDialog: Hiển thị giỏ hàng, thêm/xóa/sửa món
 * - PaymentPanel: Hiển thị chi tiết đơn khi thanh toán
 * - InvoiceDialog: In hóa đơn chi tiết
 * - StatisticsPanel: Thống kê món bán chạy
 */
public class ChiTietDonDatDTO {
    // Database fields
    private int maDonDat;       // Mã đơn đặt (FK → DONDAT, part of composite PK)
    private int maMon;          // Mã món (FK → MON, part of composite PK)
    private int soLuong;        // Số lượng món (phải > 0)
    private String ghiChu;      // Ghi chú đặc biệt (VD: "Không hành", có thể null)

    // Additional fields for display purposes (not in database)
    private String tenMon;      // Tên món (JOIN từ bảng MON)
    private double giaTien;     // Giá tiền (JOIN từ bảng MON)
    private double thanhTien;   // Thành tiền = soLuong × giaTien (calculated)

    // Constructors
    public ChiTietDonDatDTO() {
    }

    public ChiTietDonDatDTO(int maDonDat, int maMon, int soLuong) {
        this.maDonDat = maDonDat;
        this.maMon = maMon;
        this.soLuong = soLuong;
    }

    // Getters and Setters
    public int getMaDonDat() {
        return maDonDat;
    }

    public void setMaDonDat(int maDonDat) {
        this.maDonDat = maDonDat;
    }

    public int getMaMon() {
        return maMon;
    }

    public void setMaMon(int maMon) {
        this.maMon = maMon;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
        calculateThanhTien(); // Tự động tính lại thành tiền khi số lượng thay đổi
    }

    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public double getGiaTien() {
        return giaTien;
    }

    public void setGiaTien(double giaTien) {
        this.giaTien = giaTien;
        calculateThanhTien(); // Tự động tính lại thành tiền khi giá thay đổi
    }

    public double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(double thanhTien) {
        this.thanhTien = thanhTien;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    /**
     * Tính toán thành tiền = số lượng × giá tiền
     * Được gọi tự động khi setSoLuong() hoặc setGiaTien()
     */
    private void calculateThanhTien() {
        this.thanhTien = this.giaTien * this.soLuong;
    }

    @Override
    public String toString() {
        return tenMon + " x" + soLuong + " = " + String.format("%,.0f", thanhTien) + " VNĐ";
    }
}
