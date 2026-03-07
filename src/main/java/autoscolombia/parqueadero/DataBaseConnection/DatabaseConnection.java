package autoscolombia.parqueadero.DataBaseConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/parqueadero_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "UNI";
    private static final String PASSWORD = "Admin_2026";

    public static Connection getConnection() throws SQLException {
        try {
            // Registrar el driver (opcional desde Java 6)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establecer la conexión
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver de MySQL", e);
        }
    }

}
