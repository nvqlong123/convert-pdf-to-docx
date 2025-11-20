package model.bean;

import java.sql.Timestamp;

public class Conversion {
    private int conversionId;
    private int userId;
    private String originalFilename;
    private String pdfFilePath;
    private String docFilePath;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private String errorMessage;
    private long fileSize;
    private Timestamp createdAt;
    private Timestamp startedAt;
    private Timestamp completedAt;

    // Constructors
    public Conversion() {}
    
    public Conversion(int userId, String originalFilename, String pdfFilePath, long fileSize) {
        this.userId = userId;
        this.originalFilename = originalFilename;
        this.pdfFilePath = pdfFilePath;
        this.fileSize = fileSize;
        this.status = "PENDING";
    }

    // Getters and Setters (tất cả các field)
    public int getConversionId() { return conversionId; }
    public void setConversionId(int conversionId) { this.conversionId = conversionId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { 
        this.originalFilename = originalFilename; 
    }
    
    public String getPdfFilePath() { return pdfFilePath; }
    public void setPdfFilePath(String pdfFilePath) { this.pdfFilePath = pdfFilePath; }
    
    public String getDocFilePath() { return docFilePath; }
    public void setDocFilePath(String docFilePath) { this.docFilePath = docFilePath; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getStartedAt() { return startedAt; }
    public void setStartedAt(Timestamp startedAt) { this.startedAt = startedAt; }
    
    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }
}