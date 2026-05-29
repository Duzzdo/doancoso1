package com.restaurant.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class xử lý format và parse ngày tháng
 *
 * Chức năng:
 * - Format LocalDate/LocalDateTime thành String (cho database hoặc hiển thị)
 * - Parse String thành LocalDate/LocalDateTime (từ database hoặc input)
 * - Lấy ngày giờ hiện tại
 * - Validate format ngày tháng
 *
 * Format sử dụng:
 * - Database format: yyyy-MM-dd (VD: 2026-05-15)
 * - Database datetime format: yyyy-MM-dd HH:mm:ss (VD: 2026-05-15 14:30:00)
 * - Display format: dd/MM/yyyy (VD: 15/05/2026)
 * - Display datetime format: dd/MM/yyyy HH:mm:ss (VD: 15/05/2026 14:30:00)
 *
 * Tại sao cần 2 format?
 * - Database format: Chuẩn ISO 8601, dễ sort, dễ query SQL
 * - Display format: Dễ đọc cho người Việt (ngày/tháng/năm)
 *
 * Sử dụng trong:
 * - DAO classes: Format ngày trước khi insert/update database
 * - View classes: Format ngày để hiển thị trong JTable, JLabel
 * - StatisticsPanel: Filter theo khoảng thời gian
 * - InvoiceDialog: Hiển thị ngày giờ trên hóa đơn
 */
public class DateUtil {

    // Formatter cho database (yyyy-MM-dd)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Formatter cho database datetime (yyyy-MM-dd HH:mm:ss)
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Formatter cho hiển thị (dd/MM/yyyy)
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Formatter cho hiển thị datetime (dd/MM/yyyy HH:mm:ss)
    private static final DateTimeFormatter DISPLAY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Format LocalDate thành database format (yyyy-MM-dd)
     *
     * Sử dụng khi: Insert/update ngày sinh, ngày đặt vào database
     *
     * @param date LocalDate cần format
     * @return String format yyyy-MM-dd, null nếu date null
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return null;
        return date.format(DATE_FORMATTER);
    }

    /**
     * Format LocalDateTime thành database format (yyyy-MM-dd HH:mm:ss)
     *
     * Sử dụng khi: Insert/update ngày giờ đặt món vào database
     *
     * @param dateTime LocalDateTime cần format
     * @return String format yyyy-MM-dd HH:mm:ss, null nếu dateTime null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * Format LocalDate thành display format (dd/MM/yyyy)
     *
     * Sử dụng khi: Hiển thị ngày trong JTable, JLabel, hóa đơn
     *
     * @param date LocalDate cần format
     * @return String format dd/MM/yyyy, null nếu date null
     */
    public static String formatDateForDisplay(LocalDate date) {
        if (date == null) return null;
        return date.format(DISPLAY_DATE_FORMATTER);
    }

    /**
     * Format LocalDateTime thành display format (dd/MM/yyyy HH:mm:ss)
     *
     * Sử dụng khi: Hiển thị ngày giờ trong JTable, hóa đơn
     *
     * @param dateTime LocalDateTime cần format
     * @return String format dd/MM/yyyy HH:mm:ss, null nếu dateTime null
     */
    public static String formatDateTimeForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DISPLAY_DATETIME_FORMATTER);
    }

    /**
     * Parse String thành LocalDate (từ database format yyyy-MM-dd)
     *
     * Sử dụng khi: Đọc ngày từ database ResultSet
     *
     * @param dateStr String format yyyy-MM-dd
     * @return LocalDate object, null nếu parse lỗi hoặc dateStr null/empty
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            System.err.println("Error parsing date: " + dateStr);
            return null;
        }
    }

    /**
     * Parse String thành LocalDateTime (từ database format yyyy-MM-dd HH:mm:ss)
     *
     * Sử dụng khi: Đọc ngày giờ từ database ResultSet
     *
     * @param dateTimeStr String format yyyy-MM-dd HH:mm:ss
     * @return LocalDateTime object, null nếu parse lỗi hoặc dateTimeStr null/empty
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) return null;
        try {
            return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
        } catch (Exception e) {
            System.err.println("Error parsing datetime: " + dateTimeStr);
            return null;
        }
    }

    /**
     * Lấy ngày hiện tại dạng String (yyyy-MM-dd)
     *
     * Sử dụng khi: Insert ngày sinh mặc định, filter "hôm nay"
     *
     * @return String ngày hiện tại format yyyy-MM-dd
     */
    public static String getCurrentDate() {
        return formatDate(LocalDate.now());
    }

    /**
     * Lấy ngày giờ hiện tại dạng String (yyyy-MM-dd HH:mm:ss)
     *
     * Sử dụng khi: Insert NGAYDAT khi tạo đơn mới
     *
     * @return String ngày giờ hiện tại format yyyy-MM-dd HH:mm:ss
     */
    public static String getCurrentDateTime() {
        return formatDateTime(LocalDateTime.now());
    }

    /**
     * Kiểm tra String có đúng format ngày không (yyyy-MM-dd)
     *
     * Sử dụng khi: Validate input ngày từ JTextField
     *
     * @param dateStr String cần validate
     * @return true nếu đúng format, false nếu sai format hoặc null/empty
     */
    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return false;
        try {
            LocalDate.parse(dateStr, DATE_FORMATTER);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
