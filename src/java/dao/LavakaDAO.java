package src.java.dao;

import src.java.models.Lavaka;
import src.java.models.Lalana;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LavakaDAO {

    public List<Lavaka> findAll() throws SQLException {
        List<Lavaka> lavakas = new ArrayList<>();
        String sql = "SELECT * FROM lavaka";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Lavaka lavaka = new Lavaka(
                    rs.getDouble("debut"),
                    rs.getDouble("fin"),
                    rs.getDouble("ralentissement")
                );
                lavakas.add(lavaka);
            }
        }
        
        return lavakas;
    }

    public List<Lavaka> findByLalana(String nomLalana) throws SQLException {
        List<Lavaka> lavakas = new ArrayList<>();
        String sql = "SELECT * FROM lavaka WHERE lalana_nom = ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomLalana);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Lavaka lavaka = new Lavaka(
                        rs.getDouble("debut"),
                        rs.getDouble("fin"),
                        rs.getDouble("ralentissement")
                    );
                    lavakas.add(lavaka);
                }
            }
        }
        
        return lavakas;
    }

    public void insert(Lavaka lavaka, String nomLalana) throws SQLException {
        String sql = "INSERT INTO lavaka (id, debut, fin, ralentissement, lalana_nom) VALUES (lavaka_seq.NEXTVAL, ?, ?, ?, ?)";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, lavaka.getDebut());
            pstmt.setDouble(2, lavaka.getFin());
            pstmt.setDouble(3, lavaka.getRalentissement());
            pstmt.setString(4, nomLalana);
            pstmt.executeUpdate();
        }
    }

    public void delete(double debut, String nomLalana) throws SQLException {
        String sql = "DELETE FROM lavaka WHERE debut = ? AND lalana_nom = ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, debut);
            pstmt.setString(2, nomLalana);
            pstmt.executeUpdate();
        }
    }

    public void chargerLavakasPourLalanas(List<Lalana> lalanas) throws SQLException {
        for (Lalana lalana : lalanas) {
            List<Lavaka> lavakas = findByLalana(lalana.getNom());
            for (Lavaka lavaka : lavakas) {
                lalana.ajouterLavaka(lavaka);
            }
        }
    }
}