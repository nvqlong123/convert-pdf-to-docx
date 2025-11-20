package model.bo;

import model.dao.ConversionDAO;
import model.bean.Conversion;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConversionQueueBO {
    private static ConversionQueueBO instance;
    private BlockingQueue<Conversion> queue;
    private ConversionDAO conversionDAO;
    
    private ConversionQueueBO() {
        queue = new LinkedBlockingQueue<>();
        conversionDAO = new ConversionDAO();
        loadPendingConversions();
    }
    
    public static synchronized ConversionQueueBO getInstance() {
        if (instance == null) {
            instance = new ConversionQueueBO();
        }
        return instance;
    }
    
    private void loadPendingConversions() {
        // Load các conversion đang PENDING từ DB vào queue khi khởi động
        for (Conversion conversion : conversionDAO.getPendingConversions()) {
            try {
                queue.put(conversion);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void addToQueue(Conversion conversion) throws InterruptedException {
        queue.put(conversion);
    }
    
    public Conversion takeFromQueue() throws InterruptedException {
        return queue.take();
    }
    
    public int getQueueSize() {
        return queue.size();
    }
}