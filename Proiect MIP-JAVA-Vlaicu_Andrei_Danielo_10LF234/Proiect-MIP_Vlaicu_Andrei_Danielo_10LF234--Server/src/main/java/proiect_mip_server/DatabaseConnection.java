package proiect_mip_server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/* Dumi vrea sa vada si ca inchideti conexiunea la bd, o metoda de close */
public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/weather_app";
    private static final String USER = "postgres";  // Înlocuiește cu utilizatorul tău PostgreSQL
    private static final String PASSWORD = "1q2w3e"; // Înlocuiește cu parola ta

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null; // Resetează conexiunea pentru a permite reconectarea
            } catch (SQLException e) {
                System.err.println("Eroare la închiderea conexiunii: " + e.getMessage());
            }
        }
    }
}