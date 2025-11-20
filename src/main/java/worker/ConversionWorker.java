package worker;

import model.bo.ConversionBO;
import model.bo.ConversionQueueBO;
import model.bean.*;
import model.util.PDFConverter;

public class ConversionWorker implements Runnable {
    private ConversionQueueBO queueBO;
    private ConversionBO conversionBO;
    private volatile boolean running = true;
    
    public ConversionWorker() {
        this.queueBO = ConversionQueueBO.getInstance();
        this.conversionBO = new ConversionBO();
    }
    
    @Override
    public void run() {
        System.out.println("ConversionWorker started!");
        
        while (running) {
            try {
                // Lấy conversion từ queue (blocking)
                Conversion conversion = queueBO.takeFromQueue();
                
                System.out.println("Processing conversion: " + conversion.getConversionId());
                
                // Update status sang PROCESSING
                conversionBO.markAsProcessing(conversion.getConversionId());
                
                // Thực hiện convert
                String docPath = conversion.getPdfFilePath().replace(".pdf", ".docx");
                
                try {
                    PDFConverter.convertPDFtoDoc(conversion.getPdfFilePath(), docPath);
                    
                    // Update status sang COMPLETED
                    conversionBO.updateConversionResult(
                        conversion.getConversionId(), 
                        "COMPLETED", 
                        docPath, 
                        null
                    );
                    
                    System.out.println("Completed conversion: " + conversion.getConversionId());
                    
                } catch (Exception e) {
                    // Update status sang FAILED
                    conversionBO.updateConversionResult(
                        conversion.getConversionId(), 
                        "FAILED", 
                        null, 
                        e.getMessage()
                    );
                    
                    System.err.println("Failed conversion: " + conversion.getConversionId());
                    e.printStackTrace();
                }
                
            } catch (InterruptedException e) {
                System.out.println("Worker interrupted");
                break;
            }
        }
        
        System.out.println("ConversionWorker stopped!");
    }
    
    public void stop() {
        running = false;
    }
}