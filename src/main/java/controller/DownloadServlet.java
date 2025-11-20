package controller;

import model.bo.ConversionBO;
import model.bean.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;
import java.nio.file.Files;

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
    private ConversionBO conversionBO = new ConversionBO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        int conversionId = Integer.parseInt(request.getParameter("id"));
        
        Conversion conversion = conversionBO.getConversionDetails(conversionId);
        
        // Kiểm tra quyền
        if (conversion == null || conversion.getUserId() != user.getUserId()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        // Kiểm tra status
        if (!"COMPLETED".equals(conversion.getStatus())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                             "File not ready for download");
            return;
        }
        
        File file = new File(conversion.getDocFilePath());
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
            return;
        }
        
        // Download file
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", 
                          "attachment; filename=\"" + conversion.getOriginalFilename()
                          .replace(".pdf", ".docx") + "\"");
        
        try (InputStream in = Files.newInputStream(file.toPath());
             OutputStream out = response.getOutputStream()) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}