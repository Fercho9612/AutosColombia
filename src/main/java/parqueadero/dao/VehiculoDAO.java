package parqueadero.dao;

import parqueadero.model.Vehiculo;
import parqueadero.DataBaseConnection.DatabaseConnection;

import java.sql.*;

/**
  * Gestiona las operaciones de base de datos
 * relacionadas con la entidad Vehiculo.

 */
public class VehiculoDAO {

    // ═══════════════════════════════════════════════
    // ITERACIÓN 1
    // ═══════════════════════════════════════════════

    /**
     * Inserta el vehículo si no existe, o actualiza
     * Usa INSERT ... ON DUPLICATE KEY UPDATE para
     * manejar ambos casos en una sola operación.
     *
     * @param v vehículo a guardar o actualizar
     */
    public void guardarOActualizar(Vehiculo v) throws SQLException {
        String sql = "INSERT INTO vehiculo (placa, tipo) " +
                "VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE tipo = VALUES(tipo)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getPlaca());
            ps.setString(2, v.getTipo());
            ps.executeUpdate();
        }
    }

}