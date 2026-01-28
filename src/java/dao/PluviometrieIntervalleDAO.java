package src.java.dao;

import src.java.models.PluviometrieIntervalle;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour gérer les intervalles de pluviométrie et leurs matériaux associés
 */
public class PluviometrieIntervalleDAO {

    /**
     * Récupère tous les intervalles de pluviométrie
     */
    public List<PluviometrieIntervalle> findAll() throws SQLException {
        List<PluviometrieIntervalle> intervalles = new ArrayList<>();
        String sql = "SELECT * FROM pluviometrie_intervalle ORDER BY quantite_min";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                PluviometrieIntervalle intervalle = new PluviometrieIntervalle(
                    rs.getInt("id"),
                    rs.getDouble("quantite_min"),
                    rs.getDouble("quantite_max"),
                    rs.getString("materiau")
                );
                intervalles.add(intervalle);
            }
        }
        
        return intervalles;
    }

    /**
     * Trouve le matériau correspondant à une quantité de pluie
     * MÉTHODE CRITIQUE pour la détermination automatique du matériau
     */
    public PluviometrieIntervalle findByQuantitePluie(double quantitePluie) throws SQLException {
        String sql = "SELECT * FROM pluviometrie_intervalle " +
                    "WHERE quantite_min <= ? AND quantite_max >= ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, quantitePluie);
            pstmt.setDouble(2, quantitePluie);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new PluviometrieIntervalle(
                        rs.getInt("id"),
                        rs.getDouble("quantite_min"),
                        rs.getDouble("quantite_max"),
                        rs.getString("materiau")
                    );
                }
            }
        }
        
        return null;
    }

    /**
     * Insère un nouvel intervalle
     */
    public void insert(PluviometrieIntervalle intervalle) throws SQLException {
        String sql = "INSERT INTO pluviometrie_intervalle (id, quantite_min, quantite_max, materiau) " +
                    "VALUES (pluviometrie_intervalle_seq.NEXTVAL, ?, ?, ?)";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, intervalle.getQuantiteMin());
            pstmt.setDouble(2, intervalle.getQuantiteMax());
            pstmt.setString(3, intervalle.getMateriau());
            pstmt.executeUpdate();
        }
    }

    /**
     * Insère plusieurs intervalles en une seule transaction
     */
    public void insertMultiple(List<PluviometrieIntervalle> intervalles) throws SQLException {
        String sql = "INSERT INTO pluviometrie_intervalle (id, quantite_min, quantite_max, materiau) " +
                    "VALUES (pluviometrie_intervalle_seq.NEXTVAL, ?, ?, ?)";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            
            for (PluviometrieIntervalle intervalle : intervalles) {
                pstmt.setDouble(1, intervalle.getQuantiteMin());
                pstmt.setDouble(2, intervalle.getQuantiteMax());
                pstmt.setString(3, intervalle.getMateriau());
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            conn.rollback();
            conn.setAutoCommit(true);
            throw e;
        }
    }

    /**
     * Met à jour un intervalle
     */
    public void update(PluviometrieIntervalle intervalle) throws SQLException {
        String sql = "UPDATE pluviometrie_intervalle SET quantite_min = ?, " +
                    "quantite_max = ?, materiau = ? WHERE id = ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, intervalle.getQuantiteMin());
            pstmt.setDouble(2, intervalle.getQuantiteMax());
            pstmt.setString(3, intervalle.getMateriau());
            pstmt.setInt(4, intervalle.getId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Supprime un intervalle
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM pluviometrie_intervalle WHERE id = ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}