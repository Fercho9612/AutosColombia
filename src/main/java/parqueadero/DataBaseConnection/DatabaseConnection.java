package parqueadero.DataBaseConnection;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final Properties properties = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
            // Trazabilidad: Cargar el driver explícitamente para evitar "No suitable driver found"
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("SISTEMA: Configuración y Driver cargados con éxito.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("CRÍTICO: Error de configuración inicial. " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
        );
    }
}