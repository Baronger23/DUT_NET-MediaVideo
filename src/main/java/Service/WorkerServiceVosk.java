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


public class WorkerServiceVosk implements Runnable {
    private TaskBO taskBO;
    private QueueManager queueManager;
    private VoskModelManager modelManager;
    private boolean isRunning = true;
    private String workerName;
    
    public WorkerServiceVosk(String workerName) {
        this.workerName = workerName;
        this.taskBO = new TaskBO();
        this.queueManager = QueueManager.getInstance();
        this.modelManager = VoskModelManager.getInstance();
        
        System.out.println("[" + workerName + "] ✅ Worker đã được khởi tạo (sử dụng shared Vosk models)");
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
        
        System.out.println("[" + workerName + "] Worker đã dừng");
    }
    
    /**
     * ✅ Graceful shutdown
     */
    public void shutdown() {
        this.isRunning = false;
        System.out.println("[" + workerName + "] Đang shutdown...");
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
        
        // ✅ Lấy model từ shared VoskModelManager
        Model selectedModel = modelManager.getModel(language);
        System.out.println("[" + workerName + "] Sử dụng model: " + (language.equals("vi") ? "Tiếng Việt" : "Tiếng Anh"));
        
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
                    
                    // ⚠️ FIX: Vosk native library trả về UTF-8 bytes nhưng Java String nhận nhầm là ISO-8859-1
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
            
            // ⚠️ FIX: Vosk native library trả về UTF-8 bytes nhưng Java String nhận nhầm là ISO-8859-1
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
     */
    private String extractTextFromJson(String json) {
        if (json == null || json.isEmpty()) {
            return "";
        }
        
        // Simple JSON parsing - Vosk đã trả về UTF-8 đúng
        int textIndex = json.indexOf("\"text\"");
        if (textIndex == -1) {
            return "";
        }
        
        int startQuote = json.indexOf("\"", textIndex + 6);
        if (startQuote == -1) {
            return "";
        }
        
        // Tìm endQuote, chú ý xử lý escaped quotes
        int endQuote = startQuote + 1;
        while (endQuote < json.length()) {
            char c = json.charAt(endQuote);
            if (c == '"' && json.charAt(endQuote - 1) != '\\') {
                break;
            }
            endQuote++;
        }
        
        if (endQuote >= json.length()) {
            return "";
        }
        
        String extractedText = json.substring(startQuote + 1, endQuote);
        
        // Xử lý escape characters
        extractedText = extractedText.replace("\\n", "\n")
                                   .replace("\\r", "\r")
                                   .replace("\\t", "\t")
                                   .replace("\\\"", "\"")
                                   .replace("\\\\", "\\");
        
        return extractedText;
    }
    
    /**
     * ⚠️ FIX LỖI ENCODING TỪ VOSK NATIVE LIBRARY
     * 
     * VẤN ĐỀ: Vosk native library (C++) trả về UTF-8 bytes,
     * nhưng JNI bridge tự động convert sang String với encoding ISO-8859-1 (default),
     * gây ra lỗi hiển thị tiếng Việt: "chÃ o má»«ng" thay vì "chào mừng"
     * 
     * GIẢI PHÁP: Convert lại từ ISO-8859-1 bytes sang UTF-8 String
     */
    private String fixVoskEncoding(String voskResult) {
        if (voskResult == null || voskResult.isEmpty()) {
            return voskResult;
        }
        
        try {
            // Vosk trả về UTF-8 bytes nhưng Java nhận nhầm là ISO-8859-1
            // Ta cần lấy lại bytes với encoding ISO-8859-1, rồi decode lại với UTF-8
            byte[] utf8Bytes = voskResult.getBytes("ISO-8859-1");
            String fixedResult = new String(utf8Bytes, "UTF-8");
            
            return fixedResult;
        } catch (Exception e) {
            System.err.println("[" + workerName + "] ⚠️ Lỗi khi fix encoding, trả về string gốc: " + e.getMessage());
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
}
