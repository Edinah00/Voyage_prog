package dao;

import models.TypeReparation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TypeReparationDAO {

    public List<TypeReparation> findAll() throws SQLException {
        List<TypeReparation> types = new ArrayList<>();
        String sql = "SELECT * FROM type_reparation ORDER BY nom";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                TypeReparation type = new TypeReparation(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("description")
                );
                types.add(type);
            }
        }
        
        return types;
    }

    public TypeReparation findById(int id) throws SQLException {
        String sql = "SELECT * FROM type_reparation WHERE id = ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new TypeReparation(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description")
                    );
                }
            }
        }
        
        return null;
    }

    public void insert(TypeReparation type) throws SQLException {
        String sql = "INSERT INTO type_reparation (id, nom, description) " +
                    "VALUES (type_reparation_seq.NEXTVAL, ?, ?)";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type.getNom());
            pstmt.setString(2, type.getDescription());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM type_reparation WHERE id = ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}