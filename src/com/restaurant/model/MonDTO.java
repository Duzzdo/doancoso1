package com.restaurant.model;

/**
 * DTO (Data Transfer Object) class đại diện cho bảng MON trong database
 *
 * Chức năng:
 * - Lưu trữ thông tin món ăn trong thực đơn
 * - Hiển thị danh sách món trong OrderDialog
 * - Quản lý trạng thái còn/hết món
 *
 * Business Rules:
 * - MAMON: Auto-increment, primary key
 * - TENMON: Tên món hiển thị (VD: "Phở bò", "Cơm gà", "Trà đá")
 * - GIATIEN: Giá tiền (VNĐ), phải > 0
 * - TINHTRANG: "true" = còn món (available), "false" = hết món (out of stock)
 *   + Món hết không hiển thị trong OrderDialog
 *   + Có thể toggle trạng thái trong MenuManagementPanel
 * - HINHANH: Hình ảnh món ăn lưu dạng BLOB (byte[])
 *   + Upload từ máy tính qua JFileChooser
 *   + Chỉ chấp nhận JPG, JPEG, PNG
 *   + Resize trước khi lưu (ImageUtil.resizeImage)
 *   + Nếu không upload → Dùng default-food.png
 * - MALOAI: Foreign key → LOAIMON (phân loại món)
 *
 * Sử dụng trong:
 * - MenuManagementPanel: Quản lý món ăn (CRUD)
 * - OrderDialog: Hiển thị danh sách món để khách chọn
 * - ChiTietDonDatDAO: JOIN để lấy thông tin món trong đơn hàng
 */
public class MonDTO {
    private int maMon;          // Mã món (Primary Key, Auto-increment)
    private String tenMon;      // Tên món (VD: "Phở bò", "Cơm gà")
    private double giaTien;     // Giá tiền (VNĐ, phải > 0)
    private String tinhTrang;   // Trạng thái: "true" = còn món, "false" = hết món
    private byte[] hinhAnh;     // Hình ảnh món ăn (BLOB - byte[])
    private int maLoai;         // Mã loại món (FK → LOAIMON)

    // Constructors
    public MonDTO() {
    }

    public MonDTO(int maMon, String tenMon, double giaTien, String tinhTrang,
                  byte[] hinhAnh, int maLoai) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.giaTien = giaTien;
        this.tinhTrang = tinhTrang;
        this.hinhAnh = hinhAnh;
        this.maLoai = maLoai;
    }

    // Getters and Setters
    public int getMaMon() {
        return maMon;
    }

    public void setMaMon(int maMon) {
        this.maMon = maMon;
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
    }

    public String getTinhTrang() {
        return tinhTrang;
    }

    public void setTinhTrang(String tinhTrang) {
        this.tinhTrang = tinhTrang;
    }

    public byte[] getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(byte[] hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public int getMaLoai() {
        return maLoai;
    }

    public void setMaLoai(int maLoai) {
        this.maLoai = maLoai;
    }

    public boolean isAvailable() {
        return "true".equals(tinhTrang);
    }

    @Override
    public String toString() {
        return tenMon + " - " + String.format("%,.0f", giaTien) + " VNĐ";
    }
}
