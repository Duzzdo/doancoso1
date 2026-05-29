package com.restaurant.model;

/**
 * DTO (Data Transfer Object) class đại diện cho bảng BAN trong database
 *
 * Chức năng:
 * - Lưu trữ thông tin bàn ăn trong nhà hàng
 * - Quản lý trạng thái bàn (trống/đang sử dụng)
 * - Hiển thị danh sách bàn trong TableManagementPanel
 *
 * Business Rules:
 * - MABAN: Auto-increment, primary key
 * - TENBAN: Tên bàn hiển thị (VD: "Bàn 1", "Bàn VIP 1")
 * - TINHTRANG: "true" = đang sử dụng (có khách), "false" = trống (sẵn sàng)
 * - Khi tạo đơn đặt mới → TINHTRANG = "true"
 * - Khi thanh toán xong → TINHTRANG = "false"
 * - Không cho xóa bàn đang có đơn hàng pending
 *
 * Sử dụng trong:
 * - TableManagementPanel: Hiển thị grid bàn ăn với màu sắc theo trạng thái
 * - OrderDialog: Chọn bàn khi tạo đơn đặt mới
 * - PaymentPanel: Hiển thị thông tin bàn khi thanh toán
 */
public class BanAnDTO {
    private int maBan;          // Mã bàn (Primary Key, Auto-increment)
    private String tenBan;      // Tên bàn hiển thị (VD: "Bàn 1", "Bàn VIP 1")
    private String tinhTrang;   // Trạng thái: "true" = đang sử dụng, "false" = trống

    // Constructors
    public BanAnDTO() {
    }

    public BanAnDTO(int maBan, String tenBan, String tinhTrang) {
        this.maBan = maBan;
        this.tenBan = tenBan;
        this.tinhTrang = tinhTrang;
    }

    // Getters and Setters
    public int getMaBan() {
        return maBan;
    }

    public void setMaBan(int maBan) {
        this.maBan = maBan;
    }

    public String getTenBan() {
        return tenBan;
    }

    public void setTenBan(String tenBan) {
        this.tenBan = tenBan;
    }

    public String getTinhTrang() {
        return tinhTrang;
    }

    public void setTinhTrang(String tinhTrang) {
        this.tinhTrang = tinhTrang;
    }

    public boolean isOccupied() {
        return "true".equals(tinhTrang);
    }

    @Override
    public String toString() {
        return tenBan + " (" + (isOccupied() ? "Đang sử dụng" : "Trống") + ")";
    }
}
