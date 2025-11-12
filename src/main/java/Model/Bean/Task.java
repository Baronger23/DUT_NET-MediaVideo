package Model.Bean;

import java.sql.Timestamp;

public class Task {
    private int id;
    private int userId;
    private String fileName;
    private String serverFilePath;
    private String status; // Ví dụ: PENDING, PROCESSING, COMPLETED, FAILED
    private Timestamp submissionTime;
    private Timestamp completionTime;
    private String resultText; // Kết quả Speech-to-Text
    private Integer processingTimeMs; // Thời gian xử lý tính bằng mili-giây (Integer để cho phép NULL)
    private String language; // Ngôn ngữ: vi=Tiếng Việt, en=Tiếng Anh

    // 1. Constructor mặc định
    public Task() {
    }
    
    // 2. Constructor đầy đủ (Để bạn dễ hình dung, bạn có thể tự tạo các constructor khác)
    public Task(int id, int userId, String fileName, String serverFilePath, String status, 
                Timestamp submissionTime, Timestamp completionTime, String resultText, Integer processingTimeMs) {
        this.id = id;
        this.userId = userId;
        this.fileName = fileName;
        this.serverFilePath = serverFilePath;
        this.status = status;
        this.submissionTime = submissionTime;
        this.completionTime = completionTime;
        this.resultText = resultText;
        this.processingTimeMs = processingTimeMs;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getServerFilePath() {
        return serverFilePath;
    }

    public void setServerFilePath(String serverFilePath) {
        this.serverFilePath = serverFilePath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(Timestamp submissionTime) {
        this.submissionTime = submissionTime;
    }

    public Timestamp getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(Timestamp completionTime) {
        this.completionTime = completionTime;
    }

    public String getResultText() {
        return resultText;
    }

    public void setResultText(String resultText) {
        this.resultText = resultText;
    }

    public Integer getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Integer processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}