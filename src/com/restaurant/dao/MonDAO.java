package com.restaurant.dao;

import com.restaurant.database.DatabaseConnection;
import com.restaurant.model.MonDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class quản lý thao tác database cho bảng MON
 * Bao gồm: CRUD món ăn, filter theo loại món, search theo tên, quản lý trạng thái còn/hết, xử lý hình ảnh BLOB
 * Business rule: Giá tiền > 0, hình ảnh resize trước khi lưu, món hết không hiển thị trong OrderDialog
 */
public class MonDAO {

    // Lấy danh sách tất cả món ăn: sắp xếp theo mã món
    public List<MonDTO> getAllMon() {
        List<MonDTO> list = new ArrayList<>(); // Khởi tạo list rỗng để chứa kết quả
        String sql = "SELECT * FROM MON ORDER BY MAMON"; // Câu lệnh SQL lấy tất cả món, sắp xếp theo mã món

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             Statement stmt = conn.createStatement(); // Tạo Statement (không cần tham số)
             ResultSet rs = stmt.executeQuery(sql)) { // Thực thi query và nhận kết quả

            while (rs.next()) { // Duyệt qua từng dòng kết quả
                MonDTO mon = new MonDTO(); // Tạo object để chứa thông tin món
                mon.setMaMon(rs.getInt("MAMON")); // Gán mã món
                mon.setTenMon(rs.getString("TENMON")); // Gán tên món
                mon.setGiaTien(rs.getDouble("GIATIEN")); // Gán giá tiền
                mon.setTinhTrang(rs.getString("TINHTRANG")); // Gán trạng thái ('true' = còn, 'false' = hết)
                mon.setHinhAnh(rs.getBytes("HINHANH")); // Gán hình ảnh (BLOB → byte[])
                mon.setMaLoai(rs.getInt("MALOAI")); // Gán mã loại món (FK)
                list.add(mon); // Thêm vào list
            }

        } catch (SQLException e) {
            System.err.println("Error getting all dishes: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return list; // Trả về list (có thể rỗng nếu không có món hoặc lỗi)
    }

    // Lấy danh sách món theo loại món: filter theo MALOAI
    public List<MonDTO> getMonByLoai(int maLoai) {
        List<MonDTO> list = new ArrayList<>(); // Khởi tạo list rỗng để chứa kết quả
        String sql = "SELECT * FROM MON WHERE MALOAI = ? ORDER BY MAMON"; // Câu lệnh SQL lấy món theo loại, sắp xếp theo mã món

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, maLoai); // Gán mã loại món vào tham số 1

            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            while (rs.next()) { // Duyệt qua từng dòng kết quả
                MonDTO mon = new MonDTO(); // Tạo object để chứa thông tin món
                mon.setMaMon(rs.getInt("MAMON")); // Gán mã món
                mon.setTenMon(rs.getString("TENMON")); // Gán tên món
                mon.setGiaTien(rs.getDouble("GIATIEN")); // Gán giá tiền
                mon.setTinhTrang(rs.getString("TINHTRANG")); // Gán trạng thái
                mon.setHinhAnh(rs.getBytes("HINHANH")); // Gán hình ảnh (BLOB)
                mon.setMaLoai(rs.getInt("MALOAI")); // Gán mã loại món
                list.add(mon); // Thêm vào list
            }

        } catch (SQLException e) {
            System.err.println("Error getting dishes by category: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return list; // Trả về list (có thể rỗng nếu không có món hoặc lỗi)
    }

    // Lấy danh sách món còn (available) theo loại món: chỉ lấy món có TINHTRANG = 'true'
    public List<MonDTO> getAvailableMonByLoai(int maLoai) {
        List<MonDTO> list = new ArrayList<>(); // Khởi tạo list rỗng để chứa kết quả
        String sql = "SELECT * FROM MON WHERE MALOAI = ? AND TINHTRANG = 'true' ORDER BY MAMON"; // Câu lệnh SQL lấy món còn theo loại

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, maLoai); // Gán mã loại món vào tham số 1

            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            while (rs.next()) { // Duyệt qua từng dòng kết quả
                MonDTO mon = new MonDTO(); // Tạo object để chứa thông tin món
                mon.setMaMon(rs.getInt("MAMON")); // Gán mã món
                mon.setTenMon(rs.getString("TENMON")); // Gán tên món
                mon.setGiaTien(rs.getDouble("GIATIEN")); // Gán giá tiền
                mon.setTinhTrang(rs.getString("TINHTRANG")); // Gán trạng thái (luôn là 'true')
                mon.setHinhAnh(rs.getBytes("HINHANH")); // Gán hình ảnh (BLOB)
                mon.setMaLoai(rs.getInt("MALOAI")); // Gán mã loại món
                list.add(mon); // Thêm vào list
            }

        } catch (SQLException e) {
            System.err.println("Error getting available dishes: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return list; // Trả về list (có thể rỗng nếu không có món còn hoặc lỗi)
    }

    // Tìm món ăn theo mã món: dùng để xem chi tiết hoặc kiểm tra món có tồn tại không
    public MonDTO getMonById(int maMon) {
        String sql = "SELECT * FROM MON WHERE MAMON = ?"; // Câu lệnh SQL tìm món theo MAMON

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, maMon); // Gán mã món cần tìm vào tham số 1

            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            if (rs.next()) { // Nếu tìm thấy món
                MonDTO mon = new MonDTO(); // Tạo object để chứa thông tin món
                mon.setMaMon(rs.getInt("MAMON")); // Gán mã món
                mon.setTenMon(rs.getString("TENMON")); // Gán tên món
                mon.setGiaTien(rs.getDouble("GIATIEN")); // Gán giá tiền
                mon.setTinhTrang(rs.getString("TINHTRANG")); // Gán trạng thái
                mon.setHinhAnh(rs.getBytes("HINHANH")); // Gán hình ảnh (BLOB)
                mon.setMaLoai(rs.getInt("MALOAI")); // Gán mã loại món
                return mon; // Trả về thông tin món
            }

        } catch (SQLException e) {
            System.err.println("Error getting dish by ID: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return null; // Không tìm thấy hoặc lỗi
    }

    // Thêm món ăn mới: insert vào database với hình ảnh BLOB
    public boolean insertMon(MonDTO mon) {
        String sql = "INSERT INTO MON (TENMON, GIATIEN, TINHTRANG, HINHANH, MALOAI) VALUES (?, ?, ?, ?, ?)"; // Câu lệnh SQL insert món mới (MAMON auto-increment)

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setString(1, mon.getTenMon()); // Gán tên món vào tham số 1
            pstmt.setDouble(2, mon.getGiaTien()); // Gán giá tiền vào tham số 2
            pstmt.setString(3, mon.getTinhTrang()); // Gán trạng thái vào tham số 3 ('true'/'false')
            pstmt.setBytes(4, mon.getHinhAnh()); // Gán hình ảnh vào tham số 4 (BLOB - byte[])
            pstmt.setInt(5, mon.getMaLoai()); // Gán mã loại món vào tham số 5 (FK)

            return pstmt.executeUpdate() > 0; // Thực thi insert và kiểm tra số dòng bị ảnh hưởng

        } catch (SQLException e) {
            System.err.println("Error inserting dish: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return false; // Insert thất bại
    }

    // Cập nhật thông tin món ăn: update toàn bộ thông tin món
    public boolean updateMon(MonDTO mon) {
        String sql = "UPDATE MON SET TENMON = ?, GIATIEN = ?, TINHTRANG = ?, HINHANH = ?, MALOAI = ? WHERE MAMON = ?"; // Câu lệnh SQL update toàn bộ thông tin món

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setString(1, mon.getTenMon()); // Gán tên món mới vào tham số 1
            pstmt.setDouble(2, mon.getGiaTien()); // Gán giá tiền mới vào tham số 2
            pstmt.setString(3, mon.getTinhTrang()); // Gán trạng thái mới vào tham số 3
            pstmt.setBytes(4, mon.getHinhAnh()); // Gán hình ảnh mới vào tham số 4 (hoặc giữ nguyên)
            pstmt.setInt(5, mon.getMaLoai()); // Gán loại món mới vào tham số 5
            pstmt.setInt(6, mon.getMaMon()); // Gán mã món vào tham số 6 (WHERE clause)

            return pstmt.executeUpdate() > 0; // Thực thi update và kiểm tra số dòng bị ảnh hưởng

        } catch (SQLException e) {
            System.err.println("Error updating dish: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return false; // Update thất bại
    }

    // Xóa món ăn: có thể vi phạm foreign key nếu món có trong CHITIETDONDAT
    public boolean deleteMon(int maMon) {
        String sql = "DELETE FROM MON WHERE MAMON = ?"; // Câu lệnh SQL xóa món theo MAMON

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setInt(1, maMon); // Gán mã món cần xóa vào tham số 1

            return pstmt.executeUpdate() > 0; // Thực thi delete và kiểm tra số dòng bị ảnh hưởng

        } catch (SQLException e) {
            System.err.println("Error deleting dish: " + e.getMessage()); // In thông báo lỗi (có thể là foreign key constraint)
            e.printStackTrace(); // In stack trace
        }

        return false; // Xóa thất bại
    }

    // Tìm kiếm món ăn theo tên: hỗ trợ partial match (LIKE %keyword%)
    public List<MonDTO> searchMon(String keyword) {
        List<MonDTO> list = new ArrayList<>(); // Khởi tạo list rỗng để chứa kết quả
        String sql = "SELECT * FROM MON WHERE TENMON LIKE ? ORDER BY MAMON"; // Câu lệnh SQL tìm kiếm trong TENMON

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setString(1, "%" + keyword + "%"); // Gán search pattern %keyword% vào tham số 1 (tìm kiếm partial match)

            ResultSet rs = pstmt.executeQuery(); // Thực thi query và nhận kết quả

            while (rs.next()) { // Duyệt qua từng dòng kết quả
                MonDTO mon = new MonDTO(); // Tạo object để chứa thông tin món
                mon.setMaMon(rs.getInt("MAMON")); // Gán mã món
                mon.setTenMon(rs.getString("TENMON")); // Gán tên món
                mon.setGiaTien(rs.getDouble("GIATIEN")); // Gán giá tiền
                mon.setTinhTrang(rs.getString("TINHTRANG")); // Gán trạng thái
                mon.setHinhAnh(rs.getBytes("HINHANH")); // Gán hình ảnh (BLOB)
                mon.setMaLoai(rs.getInt("MALOAI")); // Gán mã loại món
                list.add(mon); // Thêm vào list
            }

        } catch (SQLException e) {
            System.err.println("Error searching dishes: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return list; // Trả về list (có thể rỗng nếu không tìm thấy hoặc lỗi)
    }

    // Cập nhật trạng thái món: còn/hết (true/false)
    public boolean updateTinhTrangMon(int maMon, String tinhTrang) {
        String sql = "UPDATE MON SET TINHTRANG = ? WHERE MAMON = ?"; // Câu lệnh SQL chỉ update cột TINHTRANG

        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Lấy kết nối database
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Tạo PreparedStatement

            pstmt.setString(1, tinhTrang); // Gán trạng thái mới vào tham số 1 ('true' = còn, 'false' = hết)
            pstmt.setInt(2, maMon); // Gán mã món vào tham số 2 (WHERE clause)

            return pstmt.executeUpdate() > 0; // Thực thi update và kiểm tra số dòng bị ảnh hưởng

        } catch (SQLException e) {
            System.err.println("Error updating dish status: " + e.getMessage()); // In thông báo lỗi
            e.printStackTrace(); // In stack trace
        }

        return false; // Update thất bại
    }
}

