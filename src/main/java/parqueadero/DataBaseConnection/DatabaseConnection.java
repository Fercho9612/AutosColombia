package parqueadero.DataBaseConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestiona la conexión central con la base de datos MySQL.
 * Configura credenciales, zona horaria de Colombia y tiempos de espera (timeouts).
 */
public class DatabaseConnection {

    // ── Credenciales de conexión ─────────────────
    private static final String USER     = "UNI";
    private static final String PASSWORD = "Admin_2026";

    // ── URL con parámetros corregidos ────────────
    // serverTimezone: America/Bogota (Colombia UTC-5)
    // connectTimeout/socketTimeout: RNF08 ≤ 3s
    private static final String URL =
            "jdbc:mysql://localhost:3306/parqueadero_db" +
                    "?useSSL=false" +
                    "&allowPublicKeyRetrieval=true" +
                    "&serverTimezone=America/Bogota" +  // ← corregido de UTC
                    "&connectTimeout=3000" +            // ← nuevo RNF08
                    "&socketTimeout=3000";              // ← nuevo RNF08

    /**
     * Establece y retorna una conexión activa con el servidor de base de datos.
     * @return Objeto Connection para ejecutar sentencias SQL.
     * @throws SQLException Si el driver no existe o la conexión falla tras 3 segundos.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Registrar driver MySQL (requerido antes de Java 6,
            // opcional desde Java 6+ pero sin daño dejarlo)
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "Driver MySQL no encontrado — " +
                            "verificar mysql-connector-j en pom.xml", e);
        }
    }
}