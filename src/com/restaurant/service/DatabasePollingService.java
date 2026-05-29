package com.restaurant.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Service tự động polling database để phát hiện thay đổi
 * Polling interval: 7 giây
 *
 * Cách dùng:
 * DatabasePollingService.getInstance().addListener(() -> {
 *     // Code refresh UI ở đây
 *     loadData();
 * });
 */
public class DatabasePollingService {
    private static DatabasePollingService instance;
    private Timer timer;
    private List<DataChangeListener> listeners;
    private boolean isRunning = false;

    // Polling interval: 7 giây
    private static final long POLLING_INTERVAL = 7000;

    /**
     * Interface cho listener khi có thay đổi dữ liệu
     */
    public interface DataChangeListener {
        void onDataChanged();
    }

    private DatabasePollingService() {
        listeners = new ArrayList<>();
    }

    /**
     * Singleton instance
     */
    public static synchronized DatabasePollingService getInstance() {
        if (instance == null) {
            instance = new DatabasePollingService();
        }
        return instance;
    }

    /**
     * Thêm listener để nhận thông báo khi có thay đổi
     */
    public void addListener(DataChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Xóa listener
     */
    public void removeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Bắt đầu polling
     */
    public void start() {
        if (isRunning) {
            return;                                                      // Đã chạy rồi
        }

        timer = new Timer("DatabasePollingService", true);              // Daemon thread
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkForChanges();                                       // Check database mỗi 7 giây
            }
        }, POLLING_INTERVAL, POLLING_INTERVAL);                         // Delay 7s, repeat mỗi 7s

        isRunning = true;
        System.out.println("DatabasePollingService started (interval: " + POLLING_INTERVAL + "ms)");
    }

    /**
     * Dừng polling
     */
    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        isRunning = false;
        System.out.println("DatabasePollingService stopped");
    }

    /**
     * Check database có thay đổi không
     * Hiện tại: Luôn notify listeners (simple approach)
     * TODO: Có thể optimize bằng cách check timestamp hoặc checksum
     */
    private void checkForChanges() {
        // Notify tất cả listeners
        for (DataChangeListener listener : listeners) {
            try {
                listener.onDataChanged();                                // Gọi callback
            } catch (Exception e) {
                System.err.println("Error in listener: " + e.getMessage());
            }
        }
    }

    /**
     * Force refresh ngay lập tức (không đợi polling interval)
     */
    public void forceRefresh() {
        checkForChanges();
    }
}
