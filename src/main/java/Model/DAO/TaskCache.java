package Model.DAO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import Model.Bean.Task;

/**
 * ✅ TỐI ƯU HÓA: Task Cache để giảm database queries
 * - Cache task status để tránh query liên tục
 * - TTL (Time To Live) 30 giây
 * - Thread-safe với ConcurrentHashMap
 * - Auto-cleanup cho expired entries
 */
public class TaskCache {
    private static volatile TaskCache instance;
    
    // Cache: taskId -> CachedTask
    private final Map<Integer, CachedTask> cache;
    
    // Cache TTL: 30 seconds
    private static final long CACHE_TTL_MS = 30000;
    
    private TaskCache() {
        this.cache = new ConcurrentHashMap<>();
        
        // ✅ Background thread để cleanup expired entries
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000); // Cleanup mỗi 60 giây
                    cleanupExpiredEntries();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }
    
    public static TaskCache getInstance() {
        if (instance == null) {
            synchronized (TaskCache.class) {
                if (instance == null) {
                    instance = new TaskCache();
                }
            }
        }
        return instance;
    }
    
    /**
     * Lấy task từ cache
     */
    public Task get(int taskId) {
        CachedTask cached = cache.get(taskId);
        
        if (cached == null) {
            return null;
        }
        
        // Kiểm tra TTL
        if (System.currentTimeMillis() - cached.timestamp > CACHE_TTL_MS) {
            cache.remove(taskId);
            return null;
        }
        
        return cached.task;
    }
    
    /**
     * Lưu task vào cache
     */
    public void put(int taskId, Task task) {
        cache.put(taskId, new CachedTask(task));
    }
    
    /**
     * Xóa task khỏi cache (khi update status)
     */
    public void invalidate(int taskId) {
        cache.remove(taskId);
    }
    
    /**
     * Xóa toàn bộ cache
     */
    public void clear() {
        cache.clear();
    }
    
    /**
     * Cleanup expired entries
     */
    private void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> 
            now - entry.getValue().timestamp > CACHE_TTL_MS
        );
    }
    
    /**
     * Lấy cache stats
     */
    public String getStats() {
        return String.format("Cache Stats - Size: %d entries", cache.size());
    }
    
    /**
     * Wrapper class để lưu task + timestamp
     */
    private static class CachedTask {
        final Task task;
        final long timestamp;
        
        CachedTask(Task task) {
            this.task = task;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
