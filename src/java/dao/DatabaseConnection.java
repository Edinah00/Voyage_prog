package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static Connection postgresConnection;
    private static Connection oracleConnection;

    public static Connection getPostgresConnection() throws SQLException {
        if (postgresConnection == null || postgresConnection.isClosed()) {
            try {
                Class.forName("org.postgresql.Driver");
                String url = "jdbc:postgresql://localhost:5432/voyage";
                String user = "postgres";
                String password = "123";
                postgresConnection = DriverManager.getConnection(url, user, password);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver PostgreSQL non trouve", e);
            }
        }
        return postgresConnection;
    }

    
    public static Connection getOracleConnection() throws SQLException {
        if (oracleConnection == null || oracleConnection.isClosed()) {
            try {
                Class.forName("oracle.jdbc.OracleDriver");
                String url = "jdbc:oracle:thin:@//localhost:1521/EE.oracle.docker";
                String user = "edinah";
                String password = "123";
                oracleConnection = DriverManager.getConnection(url, user, password);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver Oracle non trouve", e);
            }
        }
        return oracleConnection;
    }

    public static void closePostgresConnection() throws SQLException {
        if (postgresConnection != null && !postgresConnection.isClosed()) {
            postgresConnection.close();
        }
    }

    public static void closeOracleConnection() throws SQLException {
        if (oracleConnection != null && !oracleConnection.isClosed()) {
            oracleConnection.close();
        }
    }

    public static void closeAllConnections() throws SQLException {
        closePostgresConnection();
        closeOracleConnection();
    }
    
}