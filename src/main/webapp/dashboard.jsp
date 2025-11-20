<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.bean.User" %>
<%@ page import="model.bean.Conversion" %>
<%@ page import="java.util.List" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login");
        return;
    }
    
    List<Conversion> conversions = (List<Conversion>) request.getAttribute("conversions");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Dashboard - PDF Converter</title>
    <link rel="stylesheet" href="css/style.css">
    <meta http-equiv="refresh" content="10"> <!-- Auto refresh every 10s -->
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
        <div class="dashboard-container">
            <h2>📊 My Conversions</h2>
            
            <% if (request.getAttribute("success") != null) { %>
                <div class="alert alert-success">
                    <%= request.getAttribute("success") %>
                </div>
            <% } %>
            
            <% if (conversions == null || conversions.isEmpty()) { %>
                <div class="empty-state">
                    <p>No conversions yet. <a href="upload">Upload your first PDF!</a></p>
                </div>
            <% } else { %>
                <table class="conversion-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Filename</th>
                            <th>Size</th>
                            <th>Status</th>
                            <th>Created</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Conversion conv : conversions) { %>
                        <tr>
                            <td>#<%= conv.getConversionId() %></td>
                            <td><%= conv.getOriginalFilename() %></td>
                            <td><%= String.format("%.2f MB", conv.getFileSize() / 1024.0 / 1024.0) %></td>
                            <td>
                                <span class="status status-<%= conv.getStatus().toLowerCase() %>">
                                    <%= conv.getStatus() %>
                                </span>
                            </td>
                            <td><%= conv.getCreatedAt() %></td>
                            <td>
                                <% if ("COMPLETED".equals(conv.getStatus())) { %>
                                    <a href="download?id=<%= conv.getConversionId() %>" 
                                       class="btn btn-download">
                                        📥 Download
                                    </a>
                                <% } else if ("FAILED".equals(conv.getStatus())) { %>
                                    <span class="error-text">
                                        Error: <%= conv.getErrorMessage() %>
                                    </span>
                                <% } else if ("PROCESSING".equals(conv.getStatus())) { %>
                                    <span class="processing-text">⏳ Processing...</span>
                                <% } else { %>
                                    <span class="pending-text">⏰ In Queue</span>
                                <% } %>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            <% } %>
            
            <div class="action-buttons">
                <a href="upload" class="btn btn-primary">➕ Upload New PDF</a>
            </div>
        </div>
    </div>
</body>
</html>