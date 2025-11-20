package model.dao;

import model.bean.*;
import model.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConversionDAO {
    
    public int insertConversion(Conversion conversion) {
        String sql = "INSERT INTO conversions (user_id, original_filename, pdf_file_path, " +
                    "status, file_size) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, conversion.getUserId());
            ps.setString(2, conversion.getOriginalFilename());
            ps.setString(3, conversion.getPdfFilePath());
            ps.setString(4, conversion.getStatus());
            ps.setLong(5, conversion.getFileSize());
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public boolean updateConversionStatus(int conversionId, String status, 
                                         String docFilePath, String errorMessage) {
        String sql = "UPDATE conversions SET status = ?, doc_file_path = ?, " +
                    "error_message = ?, completed_at = ? WHERE conversion_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            ps.setString(2, docFilePath);
            ps.setString(3, errorMessage);
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            ps.setInt(5, conversionId);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateStartedAt(int conversionId) {
        String sql = "UPDATE conversions SET status = 'PROCESSING', started_at = ? " +
                    "WHERE conversion_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, conversionId);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Conversion> getConversionsByUserId(int userId) {
        List<Conversion> conversions = new ArrayList<>();
        String sql = "SELECT * FROM conversions WHERE user_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                conversions.add(mapResultSetToConversion(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversions;
    }
    
    public Conversion getConversionById(int conversionId) {
        String sql = "SELECT * FROM conversions WHERE conversion_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, conversionId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToConversion(rs);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Conversion> getPendingConversions() {
        List<Conversion> conversions = new ArrayList<>();
        String sql = "SELECT * FROM conversions WHERE status = 'PENDING' ORDER BY created_at ASC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                conversions.add(mapResultSetToConversion(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversions;
    }
    
    private Conversion mapResultSetToConversion(ResultSet rs) throws SQLException {
        Conversion conversion = new Conversion();
        conversion.setConversionId(rs.getInt("conversion_id"));
        conversion.setUserId(rs.getInt("user_id"));
        conversion.setOriginalFilename(rs.getString("original_filename"));
        conversion.setPdfFilePath(rs.getString("pdf_file_path"));
        conversion.setDocFilePath(rs.getString("doc_file_path"));
        conversion.setStatus(rs.getString("status"));
        conversion.setErrorMessage(rs.getString("error_message"));
        conversion.setFileSize(rs.getLong("file_size"));
        conversion.setCreatedAt(rs.getTimestamp("created_at"));
        conversion.setStartedAt(rs.getTimestamp("started_at"));
        conversion.setCompletedAt(rs.getTimestamp("completed_at"));
        return conversion;
    }
}