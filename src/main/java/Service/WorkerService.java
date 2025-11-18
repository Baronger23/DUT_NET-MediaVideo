//k.package Service;
//
//import java.io.File;
//import java.io.IOException;
//
//import Model.Bean.Task;
//import Model.BO.TaskBO;
//
///**
// * Worker Service - Xử lý tác vụ nặng (Speech-to-Text)
// * Chạy ngầm, lấy task từ Queue và xử lý
// * ĐÂY LÀ PHẦN 30% ĐIỂM - Tính toán lớn
// */
//public class WorkerService implements Runnable {
//    private TaskBO taskBO;
//    private QueueManager queueManager;
//    private boolean isRunning = true;
//    private String workerName;
//    
//    public WorkerService(String workerName) {
//        this.workerName = workerName;
//        this.taskBO = new TaskBO();
//        this.queueManager = QueueManager.getInstance();
//    }
//    
//    @Override
//    public void run() {
//        System.out.println("[" + workerName + "] Worker đã khởi động và bắt đầu lắng nghe queue...");
//        
//        while (isRunning) {
//            try {
//                // Bước 3: Worker Poll/Lắng nghe Queue, lấy Task_ID
//                Integer taskId = queueManager.dequeue(); // Blocking cho đến khi có task
//                
//                if (taskId != null && taskId > 0) {
//                    xuLyTask(taskId);
//                }
//                
//            } catch (InterruptedException e) {
//                System.out.println("[" + workerName + "] Worker bị interrupt");
//                Thread.currentThread().interrupt();
//                break;
//            } catch (Exception e) {
//                System.err.println("[" + workerName + "] Lỗi không mong muốn: " + e.getMessage());
//                e.printStackTrace();
//            }
//        }
//        
//        System.out.println("[" + workerName + "] Worker đã dừng");
//    }
//    
//    /**
//     * XỬ LÝ TASK - PHẦN 30% ĐIỂM
//     */
//    private void xuLyTask(int taskId) {
//        long startTime = System.currentTimeMillis();
//        
//        try {
//            // Bước 3: Cập nhật trạng thái PROCESSING
//            System.out.println("[" + workerName + "] Bắt đầu xử lý Task ID: " + taskId);
//            taskBO.datTaskDangXuLy(taskId);
//            
//            // Lấy thông tin task
//            Task task = taskBO.layThongTinTask(taskId);
//            if (task == null) {
//                System.err.println("[" + workerName + "] Không tìm thấy Task ID: " + taskId);
//                return;
//            }
//            
//            String filePath = task.getServerFilePath();
//            System.out.println("[" + workerName + "] Đang xử lý file: " + filePath);
//            
//            // ===== BƯỚC 4: THỰC HIỆN TÁC VỤ LỚN - 30% ĐIỂM =====
//            // Gọi thư viện Speech-to-Text (CMU Sphinx, Google Cloud API, etc.)
//            String resultText = thucHienSpeechToText(filePath);
//            
//            long endTime = System.currentTimeMillis();
//            int processingTimeMs = (int) (endTime - startTime);
//            
//            // Bước 5: Hoàn thành - Lưu kết quả vào CSDL
//            taskBO.hoanThanhTask(taskId, resultText, processingTimeMs);
//            
//            System.out.println("[" + workerName + "] ✓ Hoàn thành Task ID: " + taskId + " trong " + processingTimeMs + "ms");
//            System.out.println("[" + workerName + "] Kết quả: " + resultText.substring(0, Math.min(100, resultText.length())) + "...");
//            
//        } catch (Exception e) {
//            System.err.println("[" + workerName + "] Lỗi khi xử lý Task ID: " + taskId);
//            e.printStackTrace();
//            
//            // Cập nhật trạng thái FAILED
//            taskBO.datTaskThatBai(taskId, "Lỗi xử lý: " + e.getMessage());
//        }
//    }
//    
//    /**
//     * ===== PHẦN 30% ĐIỂM: TÁC VỤ TÍNH TOÁN LỚN =====
//     * Thực hiện Speech-to-Text từ file media
//     * 
//     * TODO: Tích hợp thư viện thực tế:
//     * - CMU Sphinx (Java)
//     * - Google Cloud Speech-to-Text API
//     * - Vosk API
//     * - Assembly AI API
//     */
//    private String thucHienSpeechToText(String filePath) throws IOException {
//        System.out.println("[" + workerName + "] ===== BÁT ĐẦU TÍNH TOÁN LỚN (30% ĐIỂM) =====");
//        
//        // Kiểm tra file tồn tại
//        File file = new File(filePath);
//        if (!file.exists()) {
//            throw new IOException("File không tồn tại: " + filePath);
//        }
//        
//        // ===== DEMO: Mô phỏng xử lý nặng =====
//        // Trong thực tế, đây là nơi gọi API/Thư viện Speech-to-Text
//        try {
//            // Mô phỏng thời gian xử lý (tốn CPU, tốn thời gian)
//            System.out.println("[" + workerName + "] Đang chuyển đổi audio thành text...");
//            Thread.sleep(5000); // Giả lập xử lý 5 giây
//            
//            // ===== TODO: THAY BẰNG CODE THỰC TẾ =====
//            /*
//            // Ví dụ với Google Cloud Speech-to-Text:
//            SpeechClient speechClient = SpeechClient.create();
//            RecognitionAudio audio = RecognitionAudio.newBuilder()
//                .setContent(ByteString.copyFrom(Files.readAllBytes(Paths.get(filePath))))
//                .build();
//            
//            RecognitionConfig config = RecognitionConfig.newBuilder()
//                .setEncoding(AudioEncoding.LINEAR16)
//                .setSampleRateHertz(16000)
//                .setLanguageCode("vi-VN")
//                .build();
//            
//            RecognizeResponse response = speechClient.recognize(config, audio);
//            
//            StringBuilder result = new StringBuilder();
//            for (SpeechRecognitionResult r : response.getResultsList()) {
//                result.append(r.getAlternatives(0).getTranscript());
//            }
//            
//            return result.toString();
//            */
//            
//            // DEMO: Trả về kết quả mẫu
//            String result = "Đây là kết quả demo Speech-to-Text. " +
//                          "Trong thực tế, đây sẽ là văn bản được trích xuất từ file audio/video. " +
//                          "File: " + file.getName() + " (" + file.length() + " bytes). " +
//                          "Nội dung mẫu: Xin chào, đây là bản ghi âm thử nghiệm. " +
//                          "Hệ thống đã xử lý thành công file media và chuyển đổi thành văn bản.";
//            
//            return result;
//            
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new IOException("Xử lý bị gián đoạn", e);
//        }
//    }
//    
//    /**
//     * Dừng worker
//     */
//    public void shutdown() {
//        System.out.println("[" + workerName + "] Đang dừng worker...");
//        isRunning = false;
//    }
//}
