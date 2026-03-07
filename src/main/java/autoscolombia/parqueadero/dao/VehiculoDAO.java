package autoscolombia.parqueadero.dao;

import autoscolombia.parqueadero.model.Vehiculo;
import autoscolombia.parqueadero.DataBaseConnection.DatabaseConnection;

import java.sql.*;

public class VehiculoDAO {
    public void guardarOActualizar(Vehiculo v) throws SQLException {
        String sql = "INSERT INTO vehiculo (placa, tipo) VALUES (?, ?) ON DUPLICATE KEY UPDATE tipo = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getPlaca().toUpperCase());
            ps.setString(2, v.getTipo());
            ps.setString(3, v.getTipo());
            ps.executeUpdate();
        }
    }
}