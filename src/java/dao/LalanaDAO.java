package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Lalana;

public class LalanaDAO {

    public List<Lalana> findAll() throws SQLException {
        List<Lalana> lalanas = new ArrayList<>();
        String sql = "SELECT * FROM lalana";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Lalana lalana = new Lalana(
                    rs.getString("nom"),
                    rs.getString("extremite_gauche"),
                    rs.getString("extremite_droite"),
                    rs.getDouble("distance"),
                    rs.getDouble("largeur")
                );
                lalanas.add(lalana);
            }
        }
        
        return lalanas;
    }

    public Lalana findByNom(String nom) throws SQLException {
        String sql = "SELECT * FROM lalana WHERE nom = ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Lalana(
                        rs.getString("nom"),
                        rs.getString("extremite_gauche"),
                        rs.getString("extremite_droite"),
                        rs.getDouble("distance"),
                        rs.getDouble("largeur")
                    );
                }
            }
        }
        
        return null;
    }

    public void insert(Lalana lalana) throws SQLException {
        String sql = "INSERT INTO lalana (nom, extremite_gauche, extremite_droite, distance, largeur) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, lalana.getNom());
            pstmt.setString(2, lalana.getExtremiteGauche());
            pstmt.setString(3, lalana.getExtremiteDroite());
            pstmt.setDouble(4, lalana.getDistance());
            pstmt.setDouble(5, lalana.getLargeur());
            pstmt.executeUpdate();
        }
    }

    public void update(Lalana lalana) throws SQLException {
        String sql = "UPDATE lalana SET extremite_gauche = ?, extremite_droite = ?, distance = ?, largeur = ? WHERE nom = ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, lalana.getExtremiteGauche());
            pstmt.setString(2, lalana.getExtremiteDroite());
            pstmt.setDouble(3, lalana.getDistance());
            pstmt.setDouble(4, lalana.getLargeur());
            pstmt.setString(5, lalana.getNom());
            pstmt.executeUpdate();
        }
    }

    public void delete(String nom) throws SQLException {
        String sql = "DELETE FROM lalana WHERE nom = ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            pstmt.executeUpdate();
        }
    }

    public List<String> getAllExtremites() throws SQLException {
        List<String> extremites = new ArrayList<>();
        String sql = "SELECT DISTINCT extremite_gauche FROM lalana UNION SELECT DISTINCT extremite_droite FROM lalana";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                extremites.add(rs.getString(1));
            }
        }
        
        return extremites;
    }
}