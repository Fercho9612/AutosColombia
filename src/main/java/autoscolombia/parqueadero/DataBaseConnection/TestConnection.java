package autoscolombia.parqueadero.DataBaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("✅ ¡Conectado con éxito como usuario UNI!");
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}
