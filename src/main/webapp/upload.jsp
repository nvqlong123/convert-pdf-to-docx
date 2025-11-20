<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.bean.User" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>Upload PDF - PDF Converter</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div class="navbar">
        <h1>PDF to DOC Converter</h1>
        <div class="nav-links">
            <span>Welcome, <%= user.getFullName() %>!</span>
            <a href="dashboard">Dashboard</a>
            <a href="upload">Upload</a>
            <a href="logout">Logout</a>
        </div>
    </div>
    
    <div class="container">
        <div class="upload-box">
            <h2>Upload PDF File</h2>
            
            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-error">
                    <%= request.getAttribute("error") %>
                </div>
            <% } %>
            
            <% if (request.getAttribute("success") != null) { %>
                <div class="alert alert-success">
                    <%= request.getAttribute("success") %>
                </div>
            <% } %>
            
            <form action="upload" method="post" enctype="multipart/form-data">
                <div class="file-upload-wrapper">
                    <input type="file" id="pdfFile" name="pdfFile" 
                           accept=".pdf" required>
                    <label for="pdfFile">Choose PDF file</label>
                </div>
                
                <button type="submit" class="btn btn-primary">
                    Upload & Convert
                </button>
            </form>
            
            <div class="info-box">
                <h3>📌 Instructions:</h3>
                <ul>
                    <li>Only PDF files are accepted</li>
                    <li>Maximum file size: 10MB</li>
                    <li>Conversion will be processed in queue</li>
                    <li>You can check status in Dashboard</li>
                </ul>
            </div>
        </div>
    </div>
</body>
</html>