package src.java.dao;

import src.java.models.Pause;
import src.java.models.Lalana;
import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PauseDAO {

    public List<Pause> findAll() throws SQLException {
        List<Pause> pauses = new ArrayList<>();
        String sql = "SELECT * FROM pause";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Pause pause = new Pause(
                    rs.getInt("id"),
                    rs.getString("lalana_nom"),
                    rs.getDouble("position"),
                    LocalTime.parse(rs.getString("heure_debut"), DateTimeFormatter.ofPattern("HH:mm")),
                    LocalTime.parse(rs.getString("heure_fin"), DateTimeFormatter.ofPattern("HH:mm"))
                );
                pauses.add(pause);
            }
        }
        
        return pauses;
    }

    public List<Pause> findByLalana(String nomLalana) throws SQLException {
        List<Pause> pauses = new ArrayList<>();
        String sql = "SELECT * FROM pause WHERE lalana_nom = ? ORDER BY position";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomLalana);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Pause pause = new Pause(
                        rs.getInt("id"),
                        rs.getString("lalana_nom"),
                        rs.getDouble("position"),
                        LocalTime.parse(rs.getString("heure_debut"), DateTimeFormatter.ofPattern("HH:mm")),
                        LocalTime.parse(rs.getString("heure_fin"), DateTimeFormatter.ofPattern("HH:mm"))
                    );
                    pauses.add(pause);
                }
            }
        }
        
        return pauses;
    }

    public void insert(Pause pause, String nomLalana) throws SQLException {
        String sql = "INSERT INTO pause (id, lalana_nom, position, heure_debut, heure_fin) " +
                    "VALUES (pause_seq.NEXTVAL, ?, ?, ?, ?)";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomLalana);
            pstmt.setDouble(2, pause.getPosition());
            pstmt.setString(3, pause.getHeureDebutFormatee());
            pstmt.setString(4, pause.getHeureFinFormatee());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM pause WHERE id = ?";
        
        Connection conn = DatabaseConnection.getOracleConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public void chargerPausesPourLalanas(List<Lalana> lalanas) throws SQLException {
        for (Lalana lalana : lalanas) {
            List<Pause> pauses = findByLalana(lalana.getNom());
            for (Pause pause : pauses) {
                lalana.ajouterPause(pause);
            }
        }
    }
}