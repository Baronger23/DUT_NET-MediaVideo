package Service;

import java.io.File;
import java.io.IOException;
import org.vosk.Model;

/**
 * ✅ TỐI ƯU HÓA QUAN TRỌNG: Vosk Model Manager - Singleton
 * - Load model 1 lần duy nhất và share cho tất cả Worker
 * - Tiết kiệm RAM (mỗi model ~100-500MB)
 * - Thread-safe
 * 
 * TRƯỚC ĐÂY: Mỗi Worker load riêng model → 2 Worker = load 2 lần (1GB RAM)
 * SAU KHI TỐI ƯU: Load 1 lần duy nhất → Tiết kiệm 50% RAM
 */
public class VoskModelManager {
    private static volatile VoskModelManager instance;
    
    private Model modelVietnamese;
    private Model modelEnglish;
    private boolean isInitialized = false;
    
    private VoskModelManager() {
        // Private constructor for Singleton
    }
    
    /**
     * ✅ Thread-safe Singleton
     */
    public static VoskModelManager getInstance() {
        if (instance == null) {
            synchronized (VoskModelManager.class) {
                if (instance == null) {
                    instance = new VoskModelManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * ✅ Load models một lần duy nhất khi server khởi động
     * Được gọi từ WorkerInitializer
     */
    public synchronized void initializeModels() {
        if (isInitialized) {
            System.out.println("⚠️ Vosk models đã được load rồi, bỏ qua");
            return;
        }
        
        try {
            System.out.println("🚀 Đang load Vosk models...");
            
            String projectPath = System.getProperty("user.dir");
            
            // === Load model tiếng Việt ===
            String modelPathVi = projectPath + File.separator + "models" + File.separator + "vosk-model-vn-0.4";
            File modelDirVi = new File(modelPathVi);
            
            if (!modelDirVi.exists()) {
                modelPathVi = "E:\\K1N3\\LTM\\DUT_NET-MediaVideo\\models\\vosk-model-vn-0.4";
                modelDirVi = new File(modelPathVi);
            }
            
            if (modelDirVi.exists()) {
                this.modelVietnamese = new Model(modelPathVi);
                System.out.println("✅ Model tiếng Việt đã được load: " + modelPathVi);
            } else {
                System.err.println("⚠️ Không tìm thấy model tiếng Việt tại: " + modelPathVi);
            }
            
            // === Load model tiếng Anh ===
            String modelPathEn = projectPath + File.separator + "models" + File.separator + "vosk-model-small-en-us-0.15";
            File modelDirEn = new File(modelPathEn);
            
            if (!modelDirEn.exists()) {
                modelPathEn = "E:\\K1N3\\LTM\\DUT_NET-MediaVideo\\models\\vosk-model-small-en-us-0.15";
                modelDirEn = new File(modelPathEn);
            }
            
            if (modelDirEn.exists()) {
                this.modelEnglish = new Model(modelPathEn);
                System.out.println("✅ Model tiếng Anh đã được load: " + modelPathEn);
            } else {
                System.err.println("⚠️ Không tìm thấy model tiếng Anh. Tải từ: https://alphacephei.com/vosk/models");
            }
            
            if (this.modelVietnamese == null && this.modelEnglish == null) {
                throw new IOException("Không có model nào được load. Tải từ: https://alphacephei.com/vosk/models");
            }
            
            isInitialized = true;
            System.out.println("✅ Vosk Model Manager đã sẵn sàng");
            
        } catch (IOException e) {
            System.err.println("❌ Không thể load Vosk models: " + e.getMessage());
            System.err.println("💡 Hãy tải model từ: https://alphacephei.com/vosk/models");
            e.printStackTrace();
        }
    }
    
    /**
     * Lấy model theo ngôn ngữ (thread-safe)
     */
    public Model getModel(String language) {
        if (!isInitialized) {
            throw new IllegalStateException("Models chưa được khởi tạo. Gọi initializeModels() trước.");
        }
        
        if ("vi".equals(language)) {
            if (modelVietnamese == null) {
                throw new IllegalStateException("Model tiếng Việt không có. Tải từ: https://alphacephei.com/vosk/models");
            }
            return modelVietnamese;
        } else if ("en".equals(language)) {
            if (modelEnglish == null) {
                throw new IllegalStateException("Model tiếng Anh không có. Tải vosk-model-small-en-us-0.15");
            }
            return modelEnglish;
        } else {
            // Fallback to Vietnamese
            if (modelVietnamese == null) {
                throw new IllegalStateException("Không có model nào được load.");
            }
            return modelVietnamese;
        }
    }
    
    /**
     * Kiểm tra model có sẵn không
     */
    public boolean hasModel(String language) {
        if ("vi".equals(language)) {
            return modelVietnamese != null;
        } else if ("en".equals(language)) {
            return modelEnglish != null;
        }
        return false;
    }
    
    /**
     * Cleanup khi shutdown server
     */
    public synchronized void closeModels() {
        if (modelVietnamese != null) {
            modelVietnamese.close();
            modelVietnamese = null;
            System.out.println("✅ Model tiếng Việt đã được đóng");
        }
        
        if (modelEnglish != null) {
            modelEnglish.close();
            modelEnglish = null;
            System.out.println("✅ Model tiếng Anh đã được đóng");
        }
        
        isInitialized = false;
    }
    
    /**
     * Lấy trạng thái models
     */
    public String getModelsStatus() {
        return String.format("Vosk Models - Vietnamese: %s, English: %s, Initialized: %s",
            modelVietnamese != null ? "✓" : "✗",
            modelEnglish != null ? "✓" : "✗",
            isInitialized ? "✓" : "✗"
        );
    }
}
