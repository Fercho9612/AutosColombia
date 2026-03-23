package parqueadero.DataBaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Prueba rápida para verificar la conexión a la base de datos.
 */
public class TestConnection {
    /**
     * Intenta conectar y muestra el resultado en consola.
     */
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("✅ ¡Conectado con éxito como usuario UNI!");
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}
//Admin_2026Ad