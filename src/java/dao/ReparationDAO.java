package src.java.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import src.java.models.Reparation;

public class ReparationDAO {

    public List<Reparation> findAll() throws SQLException {
        List<Reparation> reparations = new ArrayList<>();
        String sql = "SELECT * FROM reparation ORDER BY materiau, profondeur_min";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Reparation reparation = new Reparation(
                    rs.getInt("id"),
                    rs.getString("materiau"),
                    rs.getDouble("profondeur_min"),
                    rs.getDouble("profondeur_max"),
                    rs.getDouble("prix_par_m2")
                );
                reparations.add(reparation);
            }
        }
        
        return reparations;
    }

    public List<Reparation> findByMateriau(String materiau) throws SQLException {
        List<Reparation> reparations = new ArrayList<>();
        String sql = "SELECT * FROM reparation WHERE materiau = ? ORDER BY profondeur_min";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, materiau);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Reparation reparation = new Reparation(
                        rs.getInt("id"),
                        rs.getString("materiau"),
                        rs.getDouble("profondeur_min"),
                        rs.getDouble("profondeur_max"),
                        rs.getDouble("prix_par_m2")
                    );
                    reparations.add(reparation);
                }
            }
        }
        
        return reparations;
    }

    /**
     * CORRIGÉ: Intervalle ]profondeur_min, profondeur_max]
     * Ouvert à gauche (>) et fermé à droite (<=)
     * 
     * Exemples:
     * ]0.00 - 0.20] → 0.10 ✓ | 0.20 ✓ | 0.00 ✗
     * ]0.20 - 0.40] → 0.30 ✓ | 0.40 ✓ | 0.20 ✗
     */
    public Reparation findByMateriauAndProfondeur(String materiau, double profondeur) throws SQLException {
        // CORRIGÉ: profondeur > profondeur_min ET profondeur <= profondeur_max
        String sql = "SELECT * FROM reparation WHERE materiau = ? AND profondeur_min < ? AND profondeur_max >= ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, materiau);
            pstmt.setDouble(2, profondeur);
            pstmt.setDouble(3, profondeur);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Reparation(
                        rs.getInt("id"),
                        rs.getString("materiau"),
                        rs.getDouble("profondeur_min"),
                        rs.getDouble("profondeur_max"),
                        rs.getDouble("prix_par_m2")
                    );
                }
            }
        }
        
        return null;
    }

    public List<String> getAllMateriaux() throws SQLException {
        List<String> materiaux = new ArrayList<>();
        String sql = "SELECT DISTINCT materiau FROM reparation ORDER BY materiau";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                materiaux.add(rs.getString("materiau"));
            }
        }
        
        return materiaux;
    }

    public void insert(Reparation reparation) throws SQLException {
        String sql = "INSERT INTO reparation (id, materiau, profondeur_min, profondeur_max, prix_par_m2) " +
                    "VALUES (reparation_seq.NEXTVAL, ?, ?, ?, ?)";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, reparation.getMateriau());
            pstmt.setDouble(2, reparation.getProfondeurMin());
            pstmt.setDouble(3, reparation.getProfondeurMax());
            pstmt.setDouble(4, reparation.getPrixParM2());
            pstmt.executeUpdate();
        }
    }

    public void update(Reparation reparation) throws SQLException {
        String sql = "UPDATE reparation SET materiau = ?, profondeur_min = ?, profondeur_max = ?, prix_par_m2 = ? WHERE id = ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, reparation.getMateriau());
            pstmt.setDouble(2, reparation.getProfondeurMin());
            pstmt.setDouble(3, reparation.getProfondeurMax());
            pstmt.setDouble(4, reparation.getPrixParM2());
            pstmt.setInt(5, reparation.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM reparation WHERE id = ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}