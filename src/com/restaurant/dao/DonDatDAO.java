package com.restaurant.dao;

import com.restaurant.database.DatabaseConnection;
import com.restaurant.model.DonDatDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class quản lý thao tác database cho bảng DONDAT
 * Bao gồm: CRUD đơn đặt, quản lý trạng thái (pending/completed/cancelled), thống kê doanh thu
 * Business rule: Mỗi bàn chỉ có tối đa 1 order pending, doanh thu chỉ tính order completed
 */
public class DonDatDAO {

    // Thêm đơn đặt mới: insert vào database và trả về MADONDAT vừa tạo để insert chi tiết đơn
    public int insertDonDat(DonDatDTO donDat) {
        String sql = "INSERT INTO DONDAT (MABAN, MANV, NGAYDAT, TONGTIEN, TINHTRANG) VALUES (?, ?, ?, ?, ?)"; // Câu lệnh SQL insert đơn mới (MADONDAT auto-increment)

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement để tránh SQL Injection

            pstmt.setInt(1, donDat.getMaBan()); // Gán mã bàn vào tham số 1 (FK đến bảng BAN)
            pstmt.setInt(2, donDat.getMaNV()); // Gán mã nhân viên vào tham số 2 (FK đến bảng NHANVIEN)
            pstmt.setString(3, donDat.getNgayDat()); // Gán ngày đặt vào tham số 3 (format: yyyy-MM-dd HH:mm:ss)
            pstmt.setDouble(4, donDat.getTongTien()); // Gán tổng tiền vào tham số 4
            pstmt.setString(5, donDat.getTinhTrang()); // Gán trạng thái vào tham số 5 (mặc định 'pending')

            int affectedRows = pstmt.executeUpdate(); // Thực thi insert và lấy số dòng bị ảnh hưởng

            if (affectedRows > 0) { // Nếu insert thành công (có dòng bị ảnh hưởng)
                Statement stmt = conn.createStatement(); // Tạo Statement để lấy ID vừa insert
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()"); // SQLite-specific: Lấy ID của row vừa insert
                if (rs.next()) { // Nếu có kết quả
                    return rs.getInt(1); // Trả về MADONDAT vừa được tạo (cột đầu tiên)
                }
            }

        } catch (SQLException e) {
            System.err.println("Error inserting order: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace để debug
        }

        return -1; // Insert thất bại: trả về -1
    }

    // Cập nhật thông tin đơn đặt: chỉ update TONGTIEN và TINHTRANG (không sửa MABAN, MANV, NGAYDAT)
    public boolean updateDonDat(DonDatDTO donDat) {
        String sql = "UPDATE DONDAT SET TONGTIEN = ?, TINHTRANG = ? WHERE MADONDAT = ?"; // Câu lệnh SQL chỉ update 2 cột

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setDouble(1, donDat.getTongTien()); // Gán tổng tiền mới vào tham số 1
            pstmt.setString(2, donDat.getTinhTrang()); // Gán trạng thái mới vào tham số 2
            pstmt.setInt(3, donDat.getMaDonDat()); // Gán mã đơn vào tham số 3 (WHERE clause)

            return pstmt.executeUpdate() > 0; // Thực thi update và kiểm tra số dòng bị ảnh hưởng > 0

        } catch (SQLException e) {
            System.err.println("Error updating order: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return false; // Update thất bại
    }

    // Cập nhật trạng thái đơn đặt: pending/completed/cancelled
    public boolean updateTinhTrangDonDat(int maDonDat, String tinhTrang) {
        String sql = "UPDATE DONDAT SET TINHTRANG = ? WHERE MADONDAT = ?"; // Câu lệnh SQL chỉ update cột TINHTRANG

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setString(1, tinhTrang); // Gán trạng thái mới vào tham số 1 ('pending'/'completed'/'cancelled')
            pstmt.setInt(2, maDonDat); // Gán mã đơn vào tham số 2 (WHERE clause)

            return pstmt.executeUpdate() > 0; // Thực thi update và kiểm tra số dòng bị ảnh hưởng

        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return false; // Update thất bại
    }

    // Xóa đơn đặt: sẽ xóa cả chi tiết đơn trong CHITIETDONDAT (cascade delete)
    public boolean deleteDonDat(int maDonDat) {
        String sql = "DELETE FROM DONDAT WHERE MADONDAT = ?"; // Câu lệnh SQL xóa đơn theo MADONDAT

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, maDonDat); // Gán mã đơn cần xóa vào tham số 1

            return pstmt.executeUpdate() > 0; // Thực thi delete và kiểm tra số dòng bị ảnh hưởng

        } catch (SQLException e) {
            System.err.println("Error deleting order: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return false; // Xóa thất bại
    }

    // Tìm đơn đặt theo mã đơn: dùng để xem chi tiết đơn hoặc kiểm tra đơn có tồn tại không
    public DonDatDTO getDonDatById(int maDonDat) {
        String sql = "SELECT * FROM DONDAT WHERE MADONDAT = ?"; // Câu lệnh SQL tìm đơn theo MADONDAT

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, maDonDat); // Gán mã đơn cần tìm vào tham số 1

            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            if (rs.next()) { // Nếu tìm thấy đơn
                DonDatDTO donDat = new DonDatDTO(); // Tạo object để chứa thông tin đơn
                donDat.setMaDonDat(rs.getInt("MADONDAT")); // Gán mã đơn
                donDat.setMaBan(rs.getInt("MABAN")); // Gán mã bàn
                donDat.setMaNV(rs.getInt("MANV")); // Gán mã nhân viên
                donDat.setNgayDat(rs.getString("NGAYDAT")); // Gán ngày đặt
                donDat.setTongTien(rs.getDouble("TONGTIEN")); // Gán tổng tiền
                donDat.setTinhTrang(rs.getString("TINHTRANG")); // Gán trạng thái
                return donDat; // Trả về thông tin đơn
            }

        } catch (SQLException e) {
            System.err.println("Error getting order by ID: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return null; // Không tìm thấy hoặc lỗi
    }

    // Lấy đơn đang pending của bàn: mỗi bàn chỉ có tối đa 1 đơn pending
    public DonDatDTO getPendingDonDatByBan(int maBan) {
        String sql = "SELECT * FROM DONDAT WHERE MABAN = ? AND TINHTRANG = 'pending' ORDER BY MADONDAT DESC LIMIT 1"; // Tìm đơn pending của bàn, lấy đơn mới nhất

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, maBan); // Gán mã bàn vào tham số 1

            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            if (rs.next()) { // Nếu tìm thấy đơn pending
                DonDatDTO donDat = new DonDatDTO(); // Tạo object để chứa thông tin đơn
                donDat.setMaDonDat(rs.getInt("MADONDAT")); // Gán mã đơn
                donDat.setMaBan(rs.getInt("MABAN")); // Gán mã bàn
                donDat.setMaNV(rs.getInt("MANV")); // Gán mã nhân viên
                donDat.setNgayDat(rs.getString("NGAYDAT")); // Gán ngày đặt
                donDat.setTongTien(rs.getDouble("TONGTIEN")); // Gán tổng tiền
                donDat.setTinhTrang(rs.getString("TINHTRANG")); // Gán trạng thái (luôn là 'pending')
                return donDat; // Trả về thông tin đơn
            }

        } catch (SQLException e) {
            System.err.println("Error getting pending order: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return null; // Không có đơn pending hoặc lỗi
    }

    // Lấy đơn hiện tại của bàn: alias cho getPendingDonDatByBan
    public DonDatDTO getCurrentOrderByTable(int maBan) {
        return getPendingDonDatByBan(maBan); // Gọi method getPendingDonDatByBan (logic giống nhau)
    }

    // Lấy danh sách tất cả đơn đặt: sắp xếp mới nhất trước
    public List<DonDatDTO> getAllDonDat() {
        List<DonDatDTO> list = new ArrayList<>(); // Khởi tạo list rỗng để chứa kết quả
        String sql = "SELECT * FROM DONDAT ORDER BY MADONDAT DESC"; // Câu lệnh SQL lấy tất cả đơn, sắp xếp mới nhất trước

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             Statement stmt = conn.createStatement(); // Tạo Statement (không cần tham số)
             ResultSet rs = stmt.executeQuery(sql)) { // Thực thi query và nhận kết quả

            while (rs.next()) { // Duyệt qua từng dòng kết quả
                DonDatDTO donDat = new DonDatDTO(); // Tạo object để chứa thông tin đơn
                donDat.setMaDonDat(rs.getInt("MADONDAT")); // Gán mã đơn
                donDat.setMaBan(rs.getInt("MABAN")); // Gán mã bàn
                donDat.setMaNV(rs.getInt("MANV")); // Gán mã nhân viên
                donDat.setNgayDat(rs.getString("NGAYDAT")); // Gán ngày đặt
                donDat.setTongTien(rs.getDouble("TONGTIEN")); // Gán tổng tiền
                donDat.setTinhTrang(rs.getString("TINHTRANG")); // Gán trạng thái
                list.add(donDat); // Thêm vào list
            }

        } catch (SQLException e) {
            System.err.println("Error getting all orders: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return list; // Trả về list (có thể rỗng nếu không có đơn hoặc lỗi)
    }

    // Lấy danh sách đơn đặt theo ngày: filter theo ngày cụ thể
    public List<DonDatDTO> getDonDatByDate(String date) {
        List<DonDatDTO> list = new ArrayList<>(); // Khởi tạo list rỗng để chứa kết quả
        String sql = "SELECT * FROM DONDAT WHERE DATE(NGAYDAT) = DATE(?) ORDER BY MADONDAT DESC"; // Câu lệnh SQL lấy đơn theo ngày (so sánh DATE, bỏ qua giờ phút giây)

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setString(1, date); // Gán ngày cần lọc vào tham số 1

            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            while (rs.next()) { // Duyệt qua từng dòng kết quả
                DonDatDTO donDat = new DonDatDTO(); // Tạo object để chứa thông tin đơn
                donDat.setMaDonDat(rs.getInt("MADONDAT")); // Gán mã đơn
                donDat.setMaBan(rs.getInt("MABAN")); // Gán mã bàn
                donDat.setMaNV(rs.getInt("MANV")); // Gán mã nhân viên
                donDat.setNgayDat(rs.getString("NGAYDAT")); // Gán ngày đặt
                donDat.setTongTien(rs.getDouble("TONGTIEN")); // Gán tổng tiền
                donDat.setTinhTrang(rs.getString("TINHTRANG")); // Gán trạng thái
                list.add(donDat); // Thêm vào list
            }

        } catch (SQLException e) {
            System.err.println("Error getting orders by date: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return list; // Trả về list (có thể rỗng nếu không có đơn hoặc lỗi)
    }

    // Lấy danh sách đơn đặt theo khoảng thời gian: filter từ ngày X đến ngày Y
    public List<DonDatDTO> getDonDatByDateRange(String fromDate, String toDate) {
        List<DonDatDTO> list = new ArrayList<>(); // Khởi tạo list rỗng để chứa kết quả
        String sql = "SELECT * FROM DONDAT WHERE DATE(NGAYDAT) BETWEEN DATE(?) AND DATE(?) ORDER BY MADONDAT DESC"; // Câu lệnh SQL lấy đơn trong khoảng thời gian (BETWEEN)

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setString(1, fromDate); // Gán ngày bắt đầu vào tham số 1
            pstmt.setString(2, toDate); // Gán ngày kết thúc vào tham số 2

            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            while (rs.next()) { // Duyệt qua từng dòng kết quả
                DonDatDTO donDat = new DonDatDTO(); // Tạo object để chứa thông tin đơn
                donDat.setMaDonDat(rs.getInt("MADONDAT")); // Gán mã đơn
                donDat.setMaBan(rs.getInt("MABAN")); // Gán mã bàn
                donDat.setMaNV(rs.getInt("MANV")); // Gán mã nhân viên
                donDat.setNgayDat(rs.getString("NGAYDAT")); // Gán ngày đặt
                donDat.setTongTien(rs.getDouble("TONGTIEN")); // Gán tổng tiền
                donDat.setTinhTrang(rs.getString("TINHTRANG")); // Gán trạng thái
                list.add(donDat); // Thêm vào list
            }

        } catch (SQLException e) {
            System.err.println("Error getting orders by date range: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return list; // Trả về list (có thể rỗng nếu không có đơn hoặc lỗi)
    }

    // Tính tổng doanh thu theo ngày: chỉ tính đơn completed
    public double getTotalRevenueByDate(String date) {
        String sql = "SELECT SUM(TONGTIEN) FROM DONDAT WHERE DATE(NGAYDAT) = DATE(?) AND TINHTRANG = 'completed'"; // Câu lệnh SQL tính tổng tiền các đơn completed trong ngày

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setString(1, date); // Gán ngày cần tính vào tham số 1

            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            if (rs.next()) { // Nếu có kết quả
                return rs.getDouble(1); // Trả về tổng tiền (cột đầu tiên - SUM)
            }

        } catch (SQLException e) {
            System.err.println("Error getting revenue: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return 0; // Không có doanh thu hoặc lỗi
    }

    // Đếm số đơn hàng theo ngày: chỉ đếm đơn completed
    public int getOrderCountByDate(String date) {
        String sql = "SELECT COUNT(*) FROM DONDAT WHERE DATE(NGAYDAT) = DATE(?) AND TINHTRANG = 'completed'"; // Câu lệnh SQL đếm số đơn completed trong ngày

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setString(1, date); // Gán ngày cần đếm vào tham số 1

            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            if (rs.next()) { // Nếu có kết quả
                return rs.getInt(1); // Trả về số lượng đơn (cột đầu tiên - COUNT)
            }

        } catch (SQLException e) {
            System.err.println("Error getting order count: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return 0; // Không có đơn hoặc lỗi
    }

    // Tính doanh thu theo nhân viên và tháng: dùng cho xếp hạng nhân viên
    public double getRevenueByStaffAndMonth(int maNV, String yearMonth) {
        String sql = "SELECT SUM(TONGTIEN) FROM DONDAT WHERE MANV = ? AND strftime('%Y-%m', NGAYDAT) = ? AND TINHTRANG = 'completed'"; // Câu lệnh SQL tính tổng tiền của nhân viên trong tháng (format: YYYY-MM)

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, maNV); // Gán mã nhân viên vào tham số 1
            pstmt.setString(2, yearMonth); // Gán tháng cần tính vào tham số 2 (format: YYYY-MM)

            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            if (rs.next()) { // Nếu có kết quả
                return rs.getDouble(1); // Trả về tổng doanh thu (cột đầu tiên - SUM)
            }

        } catch (SQLException e) {
            System.err.println("Error getting staff revenue: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return 0; // Không có doanh thu hoặc lỗi
    }

    // Đếm số đơn hàng theo nhân viên và tháng: dùng cho xếp hạng nhân viên
    public int getOrderCountByStaffAndMonth(int maNV, String yearMonth) {
        String sql = "SELECT COUNT(*) FROM DONDAT WHERE MANV = ? AND strftime('%Y-%m', NGAYDAT) = ? AND TINHTRANG = 'completed'"; // Câu lệnh SQL đếm số đơn của nhân viên trong tháng

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, maNV); // Gán mã nhân viên vào tham số 1
            pstmt.setString(2, yearMonth); // Gán tháng cần đếm vào tham số 2 (format: YYYY-MM)

            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            if (rs.next()) { // Nếu có kết quả
                return rs.getInt(1); // Trả về số lượng đơn (cột đầu tiên - COUNT)
            }

        } catch (SQLException e) {
            System.err.println("Error getting staff order count: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return 0; // Không có đơn hoặc lỗi
    }
}

