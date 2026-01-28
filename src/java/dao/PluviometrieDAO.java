package src.java.dao;

import src.java.models.Pluviometrie;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour gérer les zones de pluviométrie
 */
public class PluviometrieDAO {

    /**
     * Récupère toutes les zones de pluviométrie
     */
    public List<Pluviometrie> findAll() throws SQLException {
        List<Pluviometrie> pluviometries = new ArrayList<>();
        String sql = "SELECT * FROM pluviometrie ORDER BY lalana_nom, debut_lalana";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Pluviometrie pluvio = new Pluviometrie(
                    rs.getInt("id"),
                    rs.getString("lalana_nom"),
                    rs.getDouble("debut_lalana"),
                    rs.getDouble("fin_lalana"),
                    rs.getDouble("quantite_pluie")
                );
                pluviometries.add(pluvio);
            }
        }
        
        return pluviometries;
    }

    /**
     * Récupère les zones de pluviométrie d'une route spécifique
     */
    public List<Pluviometrie> findByLalana(String nomLalana) throws SQLException {
        List<Pluviometrie> pluviometries = new ArrayList<>();
        String sql = "SELECT * FROM pluviometrie WHERE lalana_nom = ? ORDER BY debut_lalana";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomLalana);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Pluviometrie pluvio = new Pluviometrie(
                        rs.getInt("id"),
                        rs.getString("lalana_nom"),
                        rs.getDouble("debut_lalana"),
                        rs.getDouble("fin_lalana"),
                        rs.getDouble("quantite_pluie")
                    );
                    pluviometries.add(pluvio);
                }
            }
        }
        
        return pluviometries;
    }

    /**
     * Trouve la zone de pluviométrie pour un PK donné sur une route
     * MÉTHODE CRITIQUE pour la détermination automatique du matériau
     */
    public Pluviometrie findByPK(String nomLalana, double pk) throws SQLException {
        String sql = "SELECT * FROM pluviometrie WHERE lalana_nom = ? " +
                    "AND debut_lalana <= ? AND fin_lalana >= ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomLalana);
            pstmt.setDouble(2, pk);
            pstmt.setDouble(3, pk);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Pluviometrie(
                        rs.getInt("id"),
                        rs.getString("lalana_nom"),
                        rs.getDouble("debut_lalana"),
                        rs.getDouble("fin_lalana"),
                        rs.getDouble("quantite_pluie")
                    );
                }
            }
        }
        
        return null;
    }

    /**
     * Insère une nouvelle zone de pluviométrie
     */
    public void insert(Pluviometrie pluviometrie) throws SQLException {
        String sql = "INSERT INTO pluviometrie (id, lalana_nom, debut_lalana, fin_lalana, quantite_pluie) " +
                    "VALUES (pluviometrie_seq.NEXTVAL, ?, ?, ?, ?)";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pluviometrie.getLalanaNom());
            pstmt.setDouble(2, pluviometrie.getDebutLalana());
            pstmt.setDouble(3, pluviometrie.getFinLalana());
            pstmt.setDouble(4, pluviometrie.getQuantitePluie());
            pstmt.executeUpdate();
        }
    }

    /**
     * Insère plusieurs zones de pluviométrie en une seule transaction
     */
    public void insertMultiple(List<Pluviometrie> pluviometries) throws SQLException {
        String sql = "INSERT INTO pluviometrie (id, lalana_nom, debut_lalana, fin_lalana, quantite_pluie) " +
                    "VALUES (pluviometrie_seq.NEXTVAL, ?, ?, ?, ?)";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            
            for (Pluviometrie pluvio : pluviometries) {
                pstmt.setString(1, pluvio.getLalanaNom());
                pstmt.setDouble(2, pluvio.getDebutLalana());
                pstmt.setDouble(3, pluvio.getFinLalana());
                pstmt.setDouble(4, pluvio.getQuantitePluie());
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
     * Met à jour une zone de pluviométrie
     */
    public void update(Pluviometrie pluviometrie) throws SQLException {
        String sql = "UPDATE pluviometrie SET lalana_nom = ?, debut_lalana = ?, " +
                    "fin_lalana = ?, quantite_pluie = ? WHERE id = ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pluviometrie.getLalanaNom());
            pstmt.setDouble(2, pluviometrie.getDebutLalana());
            pstmt.setDouble(3, pluviometrie.getFinLalana());
            pstmt.setDouble(4, pluviometrie.getQuantitePluie());
            pstmt.setInt(5, pluviometrie.getId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Supprime une zone de pluviométrie
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM pluviometrie WHERE id = ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
    public void delete(String nomLalana, double pkDebut, double pkFin) throws SQLException {
    String sql = "DELETE FROM pluviometrie WHERE nom_lalana = ? AND pk_debut = ? AND pk_fin = ?";

    try (Connection conn = DatabaseConnection.getOracleConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, nomLalana);
        ps.setDouble(2, pkDebut);
        ps.setDouble(3, pkFin);

        ps.executeUpdate();
    }
}

}