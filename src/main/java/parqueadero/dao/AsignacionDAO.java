package parqueadero.dao;

import parqueadero.model.AsignacionCelda;
import parqueadero.DataBaseConnection.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AsignacionDAO {

    // ═══════════════════════════════════════════════
    // ITERACIÓN 2 — Asignación de celdas mensuales

    /**
     * Registra una nueva asignación de celda para un usuario mensual.
     * * @param idUsuario El ID único del usuario (de la tabla usuario).
     * @param idCelda   El identificador de la celda (de la tabla celda).
     * @return {@code true} si la inserción en la base de datos fue exitosa;
     * {@code false} en caso contrario.
     * @throws SQLException Si ocurre un error al ejecutar la sentencia SQL.
     */

    public boolean asignar(int idUsuario, int idCelda)
            throws SQLException {
        String sql = "INSERT INTO asignacion_celda " +
                "(id_usuario, id_celda, fecha_asignacion, activa) " +
                "VALUES (?, ?, ?, TRUE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt (1, idUsuario);
            ps.setInt (2, idCelda);
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * @return AsignacionCelda activa o null si no tiene
     */
    public AsignacionCelda buscarActiva(int idUsuario)
            throws SQLException {
        String sql = "SELECT * FROM asignacion_celda " +
                "WHERE id_usuario = ? AND activa = TRUE LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        }
        return null;
    }

    /**
     * @return true si ya tiene asignación activa
     */
    public boolean tieneAsignacionActiva(int idUsuario)
            throws SQLException {
        String sql = "SELECT id FROM asignacion_celda " +
                "WHERE id_usuario = ? AND activa = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeQuery().next();
        }
    }

    /**
     * @return lista ordenada de más reciente a más antigua
     */
    public List<AsignacionCelda> listarPorUsuario(int idUsuario)
            throws SQLException {
        String sql = "SELECT * FROM asignacion_celda " +
                "WHERE id_usuario = ? " +
                "ORDER BY fecha_asignacion DESC";
        List<AsignacionCelda> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    /**
     * @return true si se desactivó correctamente
     */
    public boolean desactivar(int idUsuario) throws SQLException {
        String sql = "UPDATE asignacion_celda " +
                "SET activa = FALSE " +
                "WHERE id_usuario = ? AND activa = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    // ═══════════════════════════════════════════════
    // MAPPER PRIVADO
    // ═══════════════════════════════════════════════


    private AsignacionCelda mapear(ResultSet rs) throws SQLException {
        AsignacionCelda a = new AsignacionCelda();

        a.setId       (rs.getInt     ("id"));
        a.setIdUsuario(rs.getInt     ("id_usuario"));
        a.setIdCelda  (rs.getInt     ("id_celda"));
        a.setActiva   (rs.getBoolean ("activa"));

        // Convertir java.sql.Date → LocalDate de forma segura
        java.sql.Date fecha = rs.getDate("fecha_asignacion");
        if (fecha != null) {
            a.setFechaAsignacion(fecha.toLocalDate());
        }

        return a;
    }
}