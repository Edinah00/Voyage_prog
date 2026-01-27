package src.java.dao;

import src.java.models.PrixReparation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrixReparationDAO {

    public List<PrixReparation> findAll() throws SQLException {
        List<PrixReparation> prix = new ArrayList<>();
        String sql = "SELECT * FROM prix_reparation ORDER BY type_reparation_id, profondeur_min";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                PrixReparation p = new PrixReparation(
                    rs.getInt("id"),
                    rs.getInt("type_reparation_id"),
                    rs.getDouble("profondeur_min"),
                    rs.getDouble("profondeur_max"),
                    rs.getDouble("prix_par_m2")
                );
                prix.add(p);
            }
        }
        
        return prix;
    }

    public List<PrixReparation> findByTypeReparation(int typeReparationId) throws SQLException {
        List<PrixReparation> prix = new ArrayList<>();
        String sql = "SELECT * FROM prix_reparation WHERE type_reparation_id = ? " +
                    "ORDER BY profondeur_min";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, typeReparationId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PrixReparation p = new PrixReparation(
                        rs.getInt("id"),
                        rs.getInt("type_reparation_id"),
                        rs.getDouble("profondeur_min"),
                        rs.getDouble("profondeur_max"),
                        rs.getDouble("prix_par_m2")
                    );
                    prix.add(p);
                }
            }
        }
        
        return prix;
    }

    public PrixReparation findPrixPourProfondeur(int typeReparationId, double profondeur) 
            throws SQLException {
        String sql = "SELECT * FROM prix_reparation " +
                    "WHERE type_reparation_id = ? " +
                    "AND profondeur_min <= ? AND profondeur_max >= ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, typeReparationId);
            pstmt.setDouble(2, profondeur);
            pstmt.setDouble(3, profondeur);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new PrixReparation(
                        rs.getInt("id"),
                        rs.getInt("type_reparation_id"),
                        rs.getDouble("profondeur_min"),
                        rs.getDouble("profondeur_max"),
                        rs.getDouble("prix_par_m2")
                    );
                }
            }
        }
        
        return null;
    }

    public void insert(PrixReparation prix) throws SQLException {
        String sql = "INSERT INTO prix_reparation " +
                    "(id, type_reparation_id, profondeur_min, profondeur_max, prix_par_m2) " +
                    "VALUES (prix_reparation_seq.NEXTVAL, ?, ?, ?, ?)";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, prix.getTypeReparationId());
            pstmt.setDouble(2, prix.getProfondeurMin());
            pstmt.setDouble(3, prix.getProfondeurMax());
            pstmt.setDouble(4, prix.getPrixParM2());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM prix_reparation WHERE id = ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}