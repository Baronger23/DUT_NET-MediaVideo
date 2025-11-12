package Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.vosk.Model;
import org.vosk.Recognizer;

import Model.Bean.Task;
import Model.BO.TaskBO;

/**
 * Worker Service với Vosk Speech-to-Text (Offline)
 * ✅ Không cần API key
 * ✅ Chạy offline (local)
 * ✅ Hỗ trợ tiếng Việt
 * ✅ PHẦN 30% ĐIỂM - Tính toán lớn
 */
public class WorkerServiceVosk implements Runnable {
    private TaskBO taskBO;
    private QueueManager queueManager;
    private boolean isRunning = true;
    private String workerName;
    private Model modelVietnamese; // Vosk model tiếng Việt
    private Model modelEnglish;    // Vosk model tiếng Anh
    
    public WorkerServiceVosk(String workerName) {
        this.workerName = workerName;
        this.taskBO = new TaskBO();
        this.queueManager = QueueManager.getInstance();
        
        // Load Vosk models khi khởi tạo Worker
        try {
            System.out.println("[" + workerName + "] Đang load Vosk models...");
            
            // Lấy đường dẫn tuyệt đối đến thư mục project
            String projectPath = System.getProperty("user.dir");
            
            // === Load model tiếng Việt ===
            String modelPathVi = projectPath + File.separator + "models" + File.separator + "vosk-model-vn-0.4";
            System.out.println("[" + workerName + "] Đường dẫn model tiếng Việt: " + modelPathVi);
            
            File modelDirVi = new File(modelPathVi);
            if (!modelDirVi.exists()) {
                // Thử đường dẫn khác (trong trường hợp chạy từ Eclipse)
                modelPathVi = "E:\\K1N3\\LTM\\DUT_NET-MediaVideo\\models\\vosk-model-vn-0.4";
                modelDirVi = new File(modelPathVi);
                System.out.println("[" + workerName + "] Thử đường dẫn thay thế: " + modelPathVi);
            }
            
            if (modelDirVi.exists()) {
                this.modelVietnamese = new Model(modelPathVi);
                System.out.println("[" + workerName + "] ✅ Đã load model tiếng Việt thành công");
            } else {
                System.err.println("[" + workerName + "] ⚠️ Không tìm thấy model tiếng Việt tại: " + modelPathVi);
            }
            
            // === Load model tiếng Anh (nếu có) ===
            String modelPathEn = projectPath + File.separator + "models" + File.separator + "vosk-model-small-en-us-0.15";
            System.out.println("[" + workerName + "] Đường dẫn model tiếng Anh: " + modelPathEn);
            
            File modelDirEn = new File(modelPathEn);
            if (!modelDirEn.exists()) {
                // Thử đường dẫn khác
                modelPathEn = "E:\\K1N3\\LTM\\DUT_NET-MediaVideo\\models\\vosk-model-small-en-us-0.15";
                modelDirEn = new File(modelPathEn);
                System.out.println("[" + workerName + "] Thử đường dẫn thay thế: " + modelPathEn);
            }
            
            if (modelDirEn.exists()) {
                this.modelEnglish = new Model(modelPathEn);
                System.out.println("[" + workerName + "] ✅ Đã load model tiếng Anh thành công");
            } else {
                System.err.println("[" + workerName + "] ⚠️ Không tìm thấy model tiếng Anh tại: " + modelPathEn);
                System.err.println("[" + workerName + "] 💡 Tải model từ: https://alphacephei.com/vosk/models");
                System.err.println("[" + workerName + "] 💡 Model đề xuất: vosk-model-small-en-us-0.15");
            }
            
            // Kiểm tra xem có ít nhất 1 model
            if (this.modelVietnamese == null && this.modelEnglish == null) {
                throw new IOException("Không có model nào được load. Hãy tải model từ: https://alphacephei.com/vosk/models");
            }
            
        } catch (IOException e) {
            System.err.println("[" + workerName + "] ❌ Không thể load Vosk models: " + e.getMessage());
            System.err.println("Hãy tải model từ: https://alphacephei.com/vosk/models");
            System.err.println("Model tiếng Việt: vosk-model-vn-0.4");
            System.err.println("Model tiếng Anh: vosk-model-small-en-us-0.15");
            System.err.println("Giải nén và đặt vào thư mục: models/");
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        System.out.println("[" + workerName + "] Worker đã khởi động và bắt đầu lắng nghe queue...");
        
        while (isRunning) {
            try {
                Integer taskId = queueManager.dequeue();
                
                if (taskId != null && taskId > 0) {
                    xuLyTask(taskId);
                }
                
            } catch (InterruptedException e) {
                System.out.println("[" + workerName + "] Worker bị interrupt");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("[" + workerName + "] Lỗi không mong muốn: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Cleanup - đóng tất cả models
        if (modelVietnamese != null) {
            modelVietnamese.close();
        }
        if (modelEnglish != null) {
            modelEnglish.close();
        }
        
        System.out.println("[" + workerName + "] Worker đã dừng");
    }
    
    private void xuLyTask(int taskId) {
        long startTime = System.currentTimeMillis();
        
        try {
            System.out.println("[" + workerName + "] Bắt đầu xử lý Task ID: " + taskId);
            taskBO.datTaskDangXuLy(taskId);
            
            Task task = taskBO.layThongTinTask(taskId);
            if (task == null) {
                System.err.println("[" + workerName + "] Không tìm thấy Task ID: " + taskId);
                return;
            }
            
            String filePath = task.getServerFilePath();
            String language = task.getLanguage() != null ? task.getLanguage() : "vi"; // Mặc định tiếng Việt
            
            System.out.println("[" + workerName + "] Đang xử lý file: " + filePath);
            System.out.println("[" + workerName + "] Ngôn ngữ: " + (language.equals("vi") ? "Tiếng Việt" : "Tiếng Anh"));
            
            // ===== PHẦN 30% ĐIỂM: SPEECH-TO-TEXT với VOSK =====
            String resultText = thucHienSpeechToTextVosk(filePath, language);
            
            long endTime = System.currentTimeMillis();
            int processingTimeMs = (int) (endTime - startTime);
            
            taskBO.hoanThanhTask(taskId, resultText, processingTimeMs);
            
            System.out.println("[" + workerName + "] ✓ Hoàn thành Task ID: " + taskId + " trong " + processingTimeMs + "ms");
            System.out.println("[" + workerName + "] Kết quả: " + resultText.substring(0, Math.min(100, resultText.length())) + "...");
            
        } catch (Exception e) {
            System.err.println("[" + workerName + "] Lỗi khi xử lý Task ID: " + taskId);
            e.printStackTrace();
            taskBO.datTaskThatBai(taskId, "Lỗi xử lý: " + e.getMessage());
        }
    }
    
    /**
     * ===== PHẦN 30% ĐIỂM: TÍNH TOÁN LỚN =====
     * Speech-to-Text với Vosk (Offline)
     * Hỗ trợ nhiều ngôn ngữ
     */
    private String thucHienSpeechToTextVosk(String filePath, String language) throws Exception {
        System.out.println("[" + workerName + "] ===== BẮT ĐẦU SPEECH-TO-TEXT với VOSK (30% ĐIỂM) =====");
        System.out.println("[" + workerName + "] Ngôn ngữ: " + language);
        
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File không tồn tại: " + filePath);
        }
        
        // Chọn model phù hợp với ngôn ngữ
        Model selectedModel = null;
        if (language.equals("vi")) {
            selectedModel = modelVietnamese;
            if (selectedModel == null) {
                throw new IllegalStateException("Model tiếng Việt chưa được load. Hãy tải từ https://alphacephei.com/vosk/models");
            }
        } else if (language.equals("en")) {
            selectedModel = modelEnglish;
            if (selectedModel == null) {
                throw new IllegalStateException("Model tiếng Anh chưa được load. Hãy tải từ https://alphacephei.com/vosk/models (vosk-model-small-en-us-0.15)");
            }
        } else {
            // Fallback to Vietnamese model
            selectedModel = modelVietnamese;
            if (selectedModel == null) {
                throw new IllegalStateException("Không có model nào được load.");
            }
        }
        
        // Kiểm tra định dạng file và chuyển đổi nếu cần
        String audioFilePath = filePath;
        boolean isConverted = false;
        
        String lowerPath = filePath.toLowerCase();
        
        // Kiểm tra nếu là video hoặc audio format không phải WAV
        if (lowerPath.endsWith(".mp4") || 
            lowerPath.endsWith(".avi") || 
            lowerPath.endsWith(".mov") ||
            lowerPath.endsWith(".mkv") ||
            lowerPath.endsWith(".m4a") ||  // Audio M4A
            lowerPath.endsWith(".mp3") ||  // Audio MP3
            lowerPath.endsWith(".aac") ||  // Audio AAC
            lowerPath.endsWith(".ogg") ||  // Audio OGG
            lowerPath.endsWith(".flac") || // Audio FLAC
            lowerPath.endsWith(".webm")) { // Video WEBM
            
            System.out.println("[" + workerName + "] File cần chuyển đổi sang WAV...");
            audioFilePath = chuyenDoiSangWav(filePath);
            isConverted = true;
        }
        
        StringBuilder result = new StringBuilder();
        File audioFile = new File(audioFilePath);
        
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(audioFile)) {
            // Tạo Recognizer với sample rate từ audio file và model đã chọn
            int sampleRate = (int) ais.getFormat().getSampleRate();
            Recognizer recognizer = new Recognizer(selectedModel, sampleRate);
            
            System.out.println("[" + workerName + "] Đang nhận dạng giọng nói (Sample rate: " + sampleRate + " Hz)...");
            System.out.println("[" + workerName + "] Model: " + (language.equals("vi") ? "Tiếng Việt" : "Tiếng Anh"));
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            // Đọc và xử lý audio từng chunk
            while ((bytesRead = ais.read(buffer)) != -1) {
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    String partialResult = recognizer.getResult();
                    
                    // ⚠️ FIX: Vosk native library trả về string với encoding sai
                    partialResult = fixVoskEncoding(partialResult);
                    
                    // Parse JSON result (format: {"text":"..."})
                    String text = extractTextFromJson(partialResult);
                    if (!text.isEmpty()) {
                        result.append(text).append(" ");
                    }
                }
            }
            
            // Lấy kết quả cuối cùng
            String finalResult = recognizer.getFinalResult();
            
            // ⚠️ FIX: Vosk native library trả về string với encoding sai
            finalResult = fixVoskEncoding(finalResult);
            
            String finalText = extractTextFromJson(finalResult);
            if (!finalText.isEmpty()) {
                result.append(finalText);
            }
            
            recognizer.close();
            
            String fullResult = result.toString().trim();
            
            if (fullResult.isEmpty()) {
                return "Không nhận dạng được giọng nói trong file. File có thể không chứa âm thanh hoặc chất lượng kém.";
            }
            
            System.out.println("[" + workerName + "] ✅ Nhận dạng hoàn tất. Độ dài kết quả: " + fullResult.length() + " ký tự");
            return fullResult;
            
        } catch (Exception e) {
            System.err.println("[" + workerName + "] ❌ Lỗi khi xử lý Speech-to-Text: " + e.getMessage());
            throw e;
        } finally {
            // Xóa file WAV tạm nếu đã chuyển đổi
            if (isConverted && audioFile.exists()) {
                audioFile.delete();
                System.out.println("[" + workerName + "] Đã xóa file WAV tạm");
            }
        }
    }
    
    /**
     * Chuyển đổi video/audio sang WAV bằng FFmpeg
     * Hỗ trợ: MP4, AVI, MOV, MKV, M4A, MP3, AAC, OGG, FLAC, WEBM
     */
    private String chuyenDoiSangWav(String inputPath) throws Exception {
        File inputFile = new File(inputPath);
        String outputPath = inputPath.substring(0, inputPath.lastIndexOf('.')) + "_audio.wav";
        
        // Tìm đường dẫn FFmpeg
        String ffmpegPath = timDuongDanFFmpeg();
        
        // Lệnh FFmpeg để trích xuất audio và chuyển sang WAV 44.1kHz mono
        // Tăng sample rate để giữ chất lượng audio tốt hơn
        String[] command = {
            ffmpegPath,
            "-i", inputPath,
            "-vn",                    // Không xử lý video
            "-acodec", "pcm_s16le",   // Codec WAV
            "-ar", "44100",           // Sample rate 44.1kHz (chất lượng cao)
            "-ac", "1",               // Mono channel
            "-af", "highpass=f=200,lowpass=f=3000,volume=2.0", // Filter để tăng chất lượng giọng nói
            "-y",                     // Overwrite nếu file tồn tại
            outputPath
        };
        
        System.out.println("[" + workerName + "] Đang chạy FFmpeg để chuyển đổi...");
        System.out.println("[" + workerName + "] FFmpeg path: " + ffmpegPath);
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        // Đọc output của FFmpeg
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // In log FFmpeg (có thể comment nếu quá nhiều)
                // System.out.println("[FFmpeg] " + line);
            }
        }
        
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            throw new Exception("FFmpeg không thể chuyển đổi file. Exit code: " + exitCode + 
                              ". Hãy cài đặt FFmpeg từ https://ffmpeg.org/download.html");
        }
        
        File outputFile = new File(outputPath);
        if (!outputFile.exists()) {
            throw new Exception("File WAV không được tạo sau khi chuyển đổi");
        }
        
        System.out.println("[" + workerName + "] ✓ Đã chuyển đổi video sang WAV: " + outputPath);
        return outputPath;
    }
    
    /**
     * Parse JSON result từ Vosk (format: {"text":"..."})
     * Xử lý đúng UTF-8 encoding cho tiếng Việt
     */
    private String extractTextFromJson(String json) {
        if (json == null || json.isEmpty()) {
            return "";
        }
        
        try {
            // Đảm bảo string được xử lý dưới dạng UTF-8
            byte[] utf8Bytes = json.getBytes("UTF-8");
            String utf8Json = new String(utf8Bytes, "UTF-8");
            
            // Simple JSON parsing với xử lý escape characters
            int textIndex = utf8Json.indexOf("\"text\"");
            if (textIndex == -1) {
                return "";
            }
            
            int startQuote = utf8Json.indexOf("\"", textIndex + 6);
            if (startQuote == -1) {
                return "";
            }
            
            // Tìm endQuote, chú ý xử lý escaped quotes
            int endQuote = startQuote + 1;
            while (endQuote < utf8Json.length()) {
                char c = utf8Json.charAt(endQuote);
                if (c == '"' && utf8Json.charAt(endQuote - 1) != '\\') {
                    break;
                }
                endQuote++;
            }
            
            if (endQuote >= utf8Json.length()) {
                return "";
            }
            
            String extractedText = utf8Json.substring(startQuote + 1, endQuote);
            
            // Xử lý escape characters
            extractedText = extractedText.replace("\\n", "\n")
                                       .replace("\\r", "\r")
                                       .replace("\\t", "\t")
                                       .replace("\\\"", "\"")
                                       .replace("\\\\", "\\");
            
            return extractedText;
            
        } catch (Exception e) {
            // Fallback về cách cũ nếu có lỗi
            int textIndex = json.indexOf("\"text\"");
            if (textIndex == -1) return "";
            int startQuote = json.indexOf("\"", textIndex + 6);
            if (startQuote == -1) return "";
            int endQuote = json.indexOf("\"", startQuote + 1);
            if (endQuote == -1) return "";
            return json.substring(startQuote + 1, endQuote);
        }
    }
    
    /**
     * ⚠️ FIX LỖI ENCODING TỪ VOSK NATIVE LIBRARY
     * Vosk (C++) trả về string với encoding sai trên Windows
     * Cần convert từ ISO-8859-1 hoặc Windows-1252 sang UTF-8
     */
    private String fixVoskEncoding(String voskResult) {
        if (voskResult == null || voskResult.isEmpty()) {
            return voskResult;
        }
        
        try {
            // Thử các phương pháp fix encoding phổ biến
            String[] encodings = {
                "ISO-8859-1",
                "Windows-1252",
                "Cp1252",
                "US-ASCII",
                "UTF-16",
                "UTF-16LE",
                "UTF-16BE"
            };
            
            for (String encoding : encodings) {
                try {
                    byte[] bytes = voskResult.getBytes(encoding);
                    String fixed = new String(bytes, "UTF-8");
                    
                    // Kiểm tra xem có hợp lệ không
                    // Kiểm tra có chứa ký tự tiếng Việt hợp lệ
                    boolean hasValidVietnamese = fixed.matches(".*[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ].*");
                    boolean noReplacementChar = !fixed.contains("�");
                    
                    if (hasValidVietnamese && noReplacementChar) {
                        return fixed;
                    }
                } catch (Exception e) {
                    // Encoding không khả dụng, thử tiếp
                }
            }
            
            // Phương pháp cuối cùng: Nếu string gốc đã là UTF-8
            if (voskResult.matches(".*[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ].*")) {
                return voskResult;
            }
            
            // Nếu tất cả đều thất bại, trả về original
            return voskResult;
            
        } catch (Exception e) {
            return voskResult;
        }
    }
    
    /**
     * Tìm đường dẫn FFmpeg từ nhiều nguồn
     */
    private String timDuongDanFFmpeg() throws Exception {
        // Danh sách các đường dẫn có thể có của FFmpeg
        String[] possiblePaths = {
            // FFmpeg từ WinGet
            System.getProperty("user.home") + "\\AppData\\Local\\Microsoft\\WinGet\\Packages\\Gyan.FFmpeg_Microsoft.Winget.Source_8wekyb3d8bbwe\\ffmpeg-8.0-full_build\\bin\\ffmpeg.exe",
            // FFmpeg từ PATH (thử gọi trực tiếp)
            "ffmpeg",
            // FFmpeg cài đặt thủ công
            "C:\\ffmpeg\\bin\\ffmpeg.exe",
            "C:\\Program Files\\ffmpeg\\bin\\ffmpeg.exe",
            // Chocolatey
            "C:\\ProgramData\\chocolatey\\bin\\ffmpeg.exe",
        };
        
        // Thử từng đường dẫn
        for (String path : possiblePaths) {
            File ffmpegFile = new File(path);
            if (ffmpegFile.exists() && ffmpegFile.isFile()) {
                System.out.println("[" + workerName + "] Tìm thấy FFmpeg tại: " + path);
                return path;
            }
        }
        
        // Nếu không tìm thấy, thử dùng lệnh "where" để tìm
        try {
            ProcessBuilder pb = new ProcessBuilder("where", "ffmpeg");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));
            String foundPath = reader.readLine();
            reader.close();
            
            if (foundPath != null && !foundPath.isEmpty()) {
                File ffmpegFile = new File(foundPath);
                if (ffmpegFile.exists()) {
                    System.out.println("[" + workerName + "] Tìm thấy FFmpeg qua 'where': " + foundPath);
                    return foundPath;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        
        // Không tìm thấy FFmpeg
        throw new Exception("Không tìm thấy FFmpeg. Hãy cài đặt FFmpeg và thêm vào PATH, " +
                          "hoặc đặt tại C:\\ffmpeg\\bin\\ffmpeg.exe");
    }
    
    public void shutdown() {
        System.out.println("[" + workerName + "] Đang dừng worker...");
        isRunning = false;
    }
}
