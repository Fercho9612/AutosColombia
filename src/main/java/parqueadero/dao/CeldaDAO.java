package parqueadero.dao;

import parqueadero.DataBaseConnection.DatabaseConnection;
import parqueadero.model.Celda;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CeldaDAO {

    /**
     * SELECT directo para el mapa de celdas (CELDAS ACTIVAS.jpg)
     */
    public List<Celda> listarTodas() throws SQLException {
        List<Celda> lista = new ArrayList<>();
        String sql = "SELECT id, tipo, disponible FROM celda ORDER BY id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Celda c = new Celda();
                c.setId(rs.getInt("id"));
                c.setTipo(Celda.TipoCelda.valueOf(rs.getString("tipo").toUpperCase()));
                c.setDisponible(rs.getBoolean("disponible"));
                lista.add(c);
            }
        }
        return lista;
    }

    /**
     * Verifica existencia para evitar errores antes de llamar al Script
     */
    public boolean existeCelda(int id) throws SQLException {
        String sql = "SELECT 1 FROM celda WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeQuery().next();
        }
    }

    /**
     * Registro simple
     */
    public boolean registrar(int id, String tipo) throws SQLException {
        String sql = "INSERT INTO celda (id, tipo, disponible) VALUES (?, ?, TRUE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, tipo.toUpperCase());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza el tipo y la disponibilidad de una celda existente.
     * Permite liberar (TRUE) o ocupar (FALSE) la celda.
     */
    public boolean actualizarEstadoCompleto(int id, String tipo, boolean disponible) throws SQLException {
        String sql = "UPDATE celda SET tipo = ?, disponible = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipo.toUpperCase());
            ps.setBoolean(2, disponible);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }
}