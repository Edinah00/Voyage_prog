package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Voiture;

public class VoitureDAO {

    public List<Voiture> findAll() throws SQLException {
        List<Voiture> voitures = new ArrayList<>();
        String sql = "SELECT * FROM voiture";
        
        Connection conn = DatabaseConnection.getPostgresConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Voiture voiture = new Voiture(
                    rs.getString("nom"),
                    rs.getString("type"),
                    rs.getDouble("vitesse_maximale"),
                    rs.getDouble("longueur"),
                    rs.getDouble("largeur"),
                    rs.getDouble("reservoir"),
                    rs.getDouble("consommation")
                );
                voitures.add(voiture);
            }
        }
        
        return voitures;
    }

    public Voiture findByNom(String nom) throws SQLException {
        String sql = "SELECT * FROM voiture WHERE nom = ?";
        
        Connection conn = DatabaseConnection.getPostgresConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Voiture(
                        rs.getString("nom"),
                        rs.getString("type"),
                        rs.getDouble("vitesse_maximale"),
                        rs.getDouble("longueur"),
                        rs.getDouble("largeur"),
                        rs.getDouble("reservoir"),
                        rs.getDouble("consommation")
                    );
                }
            }
        }
        
        return null;
    }

    public void insert(Voiture voiture) throws SQLException {
        String sql = "INSERT INTO voiture (nom, type, vitesse_maximale, longueur, largeur, reservoir, consommation) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = DatabaseConnection.getPostgresConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, voiture.getNom());
            pstmt.setString(2, voiture.getType());
            pstmt.setDouble(3, voiture.getVitesseMaximale());
            pstmt.setDouble(4, voiture.getLongueur());
            pstmt.setDouble(5, voiture.getLargeur());
            pstmt.setDouble(6, voiture.getReservoir());
            pstmt.setDouble(7, voiture.getConsommation());
            pstmt.executeUpdate();
        }
    }

    public void update(Voiture voiture) throws SQLException {
        String sql = "UPDATE voiture SET type = ?, vitesse_maximale = ?, longueur = ?, largeur = ?, reservoir = ?, consommation = ? WHERE nom = ?";
        
        Connection conn = DatabaseConnection.getPostgresConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, voiture.getType());
            pstmt.setDouble(2, voiture.getVitesseMaximale());
            pstmt.setDouble(3, voiture.getLongueur());
            pstmt.setDouble(4, voiture.getLargeur());
            pstmt.setDouble(5, voiture.getReservoir());
            pstmt.setDouble(6, voiture.getConsommation());
            pstmt.setString(7, voiture.getNom());
            pstmt.executeUpdate();
        }
    }

    public void delete(String nom) throws SQLException {
        String sql = "DELETE FROM voiture WHERE nom = ?";
        
        Connection conn = DatabaseConnection.getPostgresConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            pstmt.executeUpdate();
        }
    }
}