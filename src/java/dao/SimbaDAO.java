package dao;

import models.Lalana;
import models.Simba;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SimbaDAO {

    public List<Simba> findAll() throws SQLException {
        List<Simba> simbas = new ArrayList<>();
        String sql = "SELECT * FROM simba ORDER BY lalana_nom, pk";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Simba simba = new Simba(
                    rs.getInt("id"),
                    rs.getString("lalana_nom"),
                    rs.getDouble("pk"),
                    rs.getDouble("surface"),
                    rs.getDouble("profondeur")
                );
                simbas.add(simba);
            }
        }
        
        return simbas;
    }

    public List<Simba> findByLalana(String nomLalana) throws SQLException {
        List<Simba> simbas = new ArrayList<>();
        String sql = "SELECT * FROM simba WHERE lalana_nom = ? ORDER BY pk";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomLalana);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Simba simba = new Simba(
                        rs.getInt("id"),
                        rs.getString("lalana_nom"),
                        rs.getDouble("pk"),
                        rs.getDouble("surface"),
                        rs.getDouble("profondeur")
                    );
                    simbas.add(simba);
                }
            }
        }
        
        return simbas;
    }

    public void insert(Simba simba, String nomLalana) throws SQLException {
        String sql = "INSERT INTO simba (id, lalana_nom, pk, surface, profondeur) " +
                    "VALUES (simba_seq.NEXTVAL, ?, ?, ?, ?)";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomLalana);
            pstmt.setDouble(2, simba.getPk());
            pstmt.setDouble(3, simba.getSurface());
            pstmt.setDouble(4, simba.getProfondeur());
            pstmt.executeUpdate();
        }
    }

    public void update(Simba simba) throws SQLException {
        String sql = "UPDATE simba SET pk = ?, surface = ?, profondeur = ? WHERE id = ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, simba.getPk());
            pstmt.setDouble(2, simba.getSurface());
            pstmt.setDouble(3, simba.getProfondeur());
            pstmt.setInt(4, simba.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM simba WHERE id = ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public void chargerSimbasPourLalanas(List<Lalana> lalanas) throws SQLException {
        for (Lalana lalana : lalanas) {
            List<Simba> simbas = findByLalana(lalana.getNom());
            for (Simba simba : simbas) {
                lalana.ajouterSimba(simba);
            }
        }
    }
}