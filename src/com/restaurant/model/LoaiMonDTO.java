package com.restaurant.model;

/**
 * DTO (Data Transfer Object) class đại diện cho bảng LOAIMON trong database
 *
 * Chức năng:
 * - Lưu trữ thông tin loại món ăn (Category)
 * - Phân loại món ăn trong thực đơn (VD: Khai vị, Món chính, Tráng miệng, Đồ uống)
 * - Hiển thị tabs loại món trong OrderDialog
 *
 * Business Rules:
 * - MALOAI: Auto-increment, primary key
 * - TENLOAI: Tên loại món hiển thị (VD: "Khai vị", "Món chính", "Đồ uống")
 * - HINHANH: Hình ảnh loại món lưu dạng BLOB (byte[])
 *   + Upload từ máy tính qua JFileChooser
 *   + Chỉ chấp nhận JPG, JPEG, PNG
 *   + Resize trước khi lưu (ImageUtil.resizeImage)
 *   + Nếu không upload → Dùng default-category.png
 * - Không cho xóa loại món đang có món ăn (LoaiMonDAO.hasDishes)
 *
 * Sử dụng trong:
 * - MenuManagementPanel: Quản lý loại món (CRUD)
 * - OrderDialog: Tabs loại món để filter món ăn
 * - JComboBox: Chọn loại món khi thêm/sửa món ăn
 */
public class LoaiMonDTO {
    private int maLoai;         // Mã loại món (Primary Key, Auto-increment)
    private String tenLoai;     // Tên loại món (VD: "Khai vị", "Món chính", "Đồ uống")
    private byte[] hinhAnh;     // Hình ảnh loại món (BLOB - byte[])

    // Constructors
    public LoaiMonDTO() {
    }

    public LoaiMonDTO(int maLoai, String tenLoai, byte[] hinhAnh) {
        this.maLoai = maLoai;
        this.tenLoai = tenLoai;
        this.hinhAnh = hinhAnh;
    }

    // Getters and Setters
    public int getMaLoai() {
        return maLoai;
    }

    public void setMaLoai(int maLoai) {
        this.maLoai = maLoai;
    }

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }

    public byte[] getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(byte[] hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    @Override
    public String toString() {
        return tenLoai;
    }
}
