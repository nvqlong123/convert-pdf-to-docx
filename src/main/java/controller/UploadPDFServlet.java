package controller;

import model.bo.ConversionBO;
import model.bo.ConversionQueueBO;
import model.bean.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.IOException;

@WebServlet("/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB
    maxFileSize = 1024 * 1024 * 10,      // 10MB
    maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class UploadPDFServlet extends HttpServlet {
    private ConversionBO conversionBO = new ConversionBO();
    private ConversionQueueBO queueBO = ConversionQueueBO.getInstance();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login");
            return;
        }
        
        request.getRequestDispatcher("upload.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        
        try {
            Part filePart = request.getPart("pdfFile");
            String fileName = getFileName(filePart);
            
            if (!fileName.toLowerCase().endsWith(".pdf")) {
                request.setAttribute("error", "Please upload a PDF file!");
                request.getRequestDispatcher("upload.jsp").forward(request, response);
                return;
            }
            
            // Tạo thư mục uploads nếu chưa có
            String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // Tạo tên file unique
            String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
            String filePath = uploadPath + File.separator + uniqueFileName;
            
            // Lưu file
            filePart.write(filePath);
            long fileSize = filePart.getSize();
            
            // Tạo conversion record
            int conversionId = conversionBO.createConversion(
                user.getUserId(), 
                fileName, 
                filePath, 
                fileSize
            );
            
            if (conversionId > 0) {
                // Lấy conversion object và thêm vào queue
                Conversion conversion = conversionBO.getConversionDetails(conversionId);
                queueBO.addToQueue(conversion);
                
                request.setAttribute("success", "File uploaded successfully! " +
                                               "Your file is in queue for conversion.");
                request.getRequestDispatcher("dashboard").forward(request, response);
            } else {
                request.setAttribute("error", "Failed to save conversion information!");
                request.getRequestDispatcher("upload.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Upload failed: " + e.getMessage());
            request.getRequestDispatcher("upload.jsp").forward(request, response);
        }
    }
    
    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "";
    }
}