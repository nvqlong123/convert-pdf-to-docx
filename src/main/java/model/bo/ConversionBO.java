package model.bo;

import model.dao.ConversionDAO;
import model.bean.Conversion;

import java.util.List;

public class ConversionBO {
    private ConversionDAO conversionDAO = new ConversionDAO();
    
    public int createConversion(int userId, String originalFilename, 
                                String pdfFilePath, long fileSize) {
        Conversion conversion = new Conversion(userId, originalFilename, pdfFilePath, fileSize);
        return conversionDAO.insertConversion(conversion);
    }
    
    public List<Conversion> getUserConversions(int userId) {
        return conversionDAO.getConversionsByUserId(userId);
    }
    
    public Conversion getConversionDetails(int conversionId) {
        return conversionDAO.getConversionById(conversionId);
    }
    
    public boolean updateConversionResult(int conversionId, String status, 
                                         String docFilePath, String errorMessage) {
        return conversionDAO.updateConversionStatus(conversionId, status, 
                                                   docFilePath, errorMessage);
    }
    
    public boolean markAsProcessing(int conversionId) {
        return conversionDAO.updateStartedAt(conversionId);
    }
}