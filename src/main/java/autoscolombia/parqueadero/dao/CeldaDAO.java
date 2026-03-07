package autoscolombia.parqueadero.dao;

import autoscolombia.parqueadero.model.Celda;
import autoscolombia.parqueadero.DataBaseConnection.DatabaseConnection;

import java.sql.*;

public class CeldaDAO {
    public Celda buscarCeldaDisponible(String tipo) throws SQLException {
        String sql = "SELECT * FROM celda WHERE tipo = ? AND disponible = TRUE LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Celda c = new Celda();
                c.setId(rs.getInt("id"));
                c.setTipo(rs.getString("tipo"));
                c.setDisponible(true);
                return c;
            }
        }
        return null;
    }

    public void actualizarDisponibilidad(int id, boolean disponible) throws SQLException {
        String sql = "UPDATE celda SET disponible = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, disponible);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }
}
