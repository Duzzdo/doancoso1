package com.restaurant.utils;

/**
 * Utility class tính toán rank (hạng) nhân viên dựa trên doanh thu
 *
 * Chức năng:
 * - Tính rank dựa trên doanh thu tháng (Bronze/Silver/Gold/Platinum/Diamond)
 * - Lấy icon emoji cho từng rank
 * - Tính % bonus theo rank
 * - Tính doanh thu cần để lên rank tiếp theo
 * - Tính progress bar (%) đến rank tiếp theo
 *
 * Rank System (theo doanh thu tháng):
 * - 🥉 Đồng (Bronze): < 10,000,000 VNĐ → Bonus 10%
 * - 🥈 Bạc (Silver): 10,000,000 - 19,999,999 VNĐ → Bonus 15%
 * - 🥇 Vàng (Gold): 20,000,000 - 29,999,999 VNĐ → Bonus 20%
 * - 💎 Bạch kim (Platinum): 30,000,000 - 49,999,999 VNĐ → Bonus 25%
 * - 👑 Kim cương (Diamond): >= 50,000,000 VNĐ → Bonus 30%
 *
 * Tại sao có rank system?
 * - Động viên nhân viên tăng doanh thu
 * - Tạo cạnh tranh lành mạnh giữa nhân viên
 * - Tính bonus lương công bằng dựa trên hiệu suất
 * - Gamification: Nhân viên thấy progress và mục tiêu rõ ràng
 *
 * Sử dụng trong:
 * - StaffRankingDAO: Tính rank cho từng nhân viên
 * - LeaderboardPanel: Hiển thị rank và icon
 * - StaffRevenuePanel: Hiển thị rank cá nhân, progress bar
 * - AdminStatisticsPanel: Tính bonus lương theo rank
 */
public class RankUtil {

    // Ngưỡng doanh thu cho từng rank (VNĐ)
    public static final double DIAMOND_THRESHOLD = 50_000_000;  // 50 triệu
    public static final double PLATINUM_THRESHOLD = 30_000_000; // 30 triệu
    public static final double GOLD_THRESHOLD = 20_000_000;     // 20 triệu
    public static final double SILVER_THRESHOLD = 10_000_000;   // 10 triệu
    // Bronze: < 10 triệu (không cần threshold)

    // Tên rank (tiếng Việt)
    public static final String RANK_DIAMOND = "Kim cương";
    public static final String RANK_PLATINUM = "Bạch kim";
    public static final String RANK_GOLD = "Vàng";
    public static final String RANK_SILVER = "Bạc";
    public static final String RANK_BRONZE = "Đồng";

    // Icon emoji cho từng rank
    public static final String ICON_DIAMOND = "👑";   // Crown - cao nhất
    public static final String ICON_PLATINUM = "💎";  // Diamond
    public static final String ICON_GOLD = "🥇";      // Gold medal
    public static final String ICON_SILVER = "🥈";    // Silver medal
    public static final String ICON_BRONZE = "🥉";    // Bronze medal

    // % Bonus lương theo rank
    public static final double BONUS_DIAMOND = 0.30;   // 30%
    public static final double BONUS_PLATINUM = 0.25;  // 25%
    public static final double BONUS_GOLD = 0.20;      // 20%
    public static final double BONUS_SILVER = 0.15;    // 15%
    public static final double BONUS_BRONZE = 0.10;    // 10%

    /**
     * Tính rank dựa trên doanh thu tháng
     *
     * Logic:
     * - >= 50 triệu → Kim cương
     * - >= 30 triệu → Bạch kim
     * - >= 20 triệu → Vàng
     * - >= 10 triệu → Bạc
     * - < 10 triệu → Đồng
     *
     * @param doanhThu Doanh thu tháng (VNĐ)
     * @return Tên rank (String)
     */
    public static String calculateRank(double doanhThu) {
        if (doanhThu >= DIAMOND_THRESHOLD) {
            return RANK_DIAMOND;
        } else if (doanhThu >= PLATINUM_THRESHOLD) {
            return RANK_PLATINUM;
        } else if (doanhThu >= GOLD_THRESHOLD) {
            return RANK_GOLD;
        } else if (doanhThu >= SILVER_THRESHOLD) {
            return RANK_SILVER;
        } else {
            return RANK_BRONZE;
        }
    }

    /**
     * Lấy icon emoji dựa trên tên rank
     *
     * @param rank Tên rank (VD: "Kim cương", "Vàng")
     * @return Icon emoji (String), empty string nếu rank không hợp lệ
     */
    public static String getRankIcon(String rank) {
        switch (rank) {
            case RANK_DIAMOND:
                return ICON_DIAMOND;
            case RANK_PLATINUM:
                return ICON_PLATINUM;
            case RANK_GOLD:
                return ICON_GOLD;
            case RANK_SILVER:
                return ICON_SILVER;
            case RANK_BRONZE:
                return ICON_BRONZE;
            default:
                return "";
        }
    }

    /**
     * Get rank icon directly from revenue
     */
    public static String getRankIconFromRevenue(double doanhThu) {
        String rank = calculateRank(doanhThu);
        return getRankIcon(rank);
    }

    /**
     * Tính doanh thu cần thêm để lên rank tiếp theo
     *
     * Ví dụ:
     * - Doanh thu hiện tại: 15,000,000 (Bạc)
     * - Rank tiếp theo: Vàng (20,000,000)
     * - Cần thêm: 5,000,000
     *
     * @param currentRevenue Doanh thu hiện tại (VNĐ)
     * @return Số tiền cần thêm (VNĐ), 0 nếu đã đạt rank cao nhất
     */
    public static double getRevenueToNextRank(double currentRevenue) {
        if (currentRevenue >= DIAMOND_THRESHOLD) {
            return 0; // Already at highest rank
        } else if (currentRevenue >= PLATINUM_THRESHOLD) {
            return DIAMOND_THRESHOLD - currentRevenue;
        } else if (currentRevenue >= GOLD_THRESHOLD) {
            return PLATINUM_THRESHOLD - currentRevenue;
        } else if (currentRevenue >= SILVER_THRESHOLD) {
            return GOLD_THRESHOLD - currentRevenue;
        } else {
            return SILVER_THRESHOLD - currentRevenue;
        }
    }

    /**
     * Get next rank name
     */
    public static String getNextRank(double currentRevenue) {
        if (currentRevenue >= DIAMOND_THRESHOLD) {
            return ""; // Already at highest rank
        } else if (currentRevenue >= PLATINUM_THRESHOLD) {
            return RANK_DIAMOND;
        } else if (currentRevenue >= GOLD_THRESHOLD) {
            return RANK_PLATINUM;
        } else if (currentRevenue >= SILVER_THRESHOLD) {
            return RANK_GOLD;
        } else {
            return RANK_SILVER;
        }
    }

    /**
     * Lấy % bonus lương dựa trên rank
     *
     * Sử dụng khi: Tính lương bonus cuối tháng
     *
     * Ví dụ:
     * - Rank: Vàng
     * - Bonus: 20%
     * - Doanh thu: 25,000,000
     * - Lương bonus = 25,000,000 × 0.20 = 5,000,000
     *
     * @param rank Tên rank
     * @return % bonus (0.10 - 0.30), 0 nếu rank không hợp lệ
     */
    public static double getBonusPercentage(String rank) {
        switch (rank) {
            case RANK_DIAMOND:
                return BONUS_DIAMOND;
            case RANK_PLATINUM:
                return BONUS_PLATINUM;
            case RANK_GOLD:
                return BONUS_GOLD;
            case RANK_SILVER:
                return BONUS_SILVER;
            case RANK_BRONZE:
                return BONUS_BRONZE;
            default:
                return 0;
        }
    }

    /**
     * Get bonus percentage from revenue
     */
    public static double getBonusPercentageFromRevenue(double revenue) {
        String rank = calculateRank(revenue);
        return getBonusPercentage(rank);
    }

    /**
     * Tính progress (tiến độ) đến rank tiếp theo (0.0 - 1.0)
     *
     * Sử dụng khi: Hiển thị progress bar trong StaffRevenuePanel
     *
     * Ví dụ:
     * - Doanh thu hiện tại: 15,000,000 (Bạc)
     * - Ngưỡng Bạc: 10,000,000
     * - Ngưỡng Vàng: 20,000,000
     * - Progress = (15M - 10M) / (20M - 10M) = 5M / 10M = 0.5 (50%)
     *
     * @param currentRevenue Doanh thu hiện tại (VNĐ)
     * @return Progress từ 0.0 (mới đạt rank) đến 1.0 (sắp lên rank tiếp theo)
     */
    public static double getProgressToNextRank(double currentRevenue) {
        if (currentRevenue >= DIAMOND_THRESHOLD) {
            return 1.0; // Already at max
        }

        double currentThreshold, nextThreshold;
        if (currentRevenue >= PLATINUM_THRESHOLD) {
            currentThreshold = PLATINUM_THRESHOLD;
            nextThreshold = DIAMOND_THRESHOLD;
        } else if (currentRevenue >= GOLD_THRESHOLD) {
            currentThreshold = GOLD_THRESHOLD;
            nextThreshold = PLATINUM_THRESHOLD;
        } else if (currentRevenue >= SILVER_THRESHOLD) {
            currentThreshold = SILVER_THRESHOLD;
            nextThreshold = GOLD_THRESHOLD;
        } else {
            currentThreshold = 0;
            nextThreshold = SILVER_THRESHOLD;
        }

        return (currentRevenue - currentThreshold) / (nextThreshold - currentThreshold);
    }
}
