package parqueadero.dao;

import parqueadero.model.Registro;
import parqueadero.DataBaseConnection.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RegistroDAO {

    // ═══════════════════════════════════════════════
    // ITERACIÓN 1 — Entrada / Salida / Consulta

    /**
     * Registra la entrada de un vehículo al parqueadero.
     */
    
    public void registrarEntrada(Registro r) throws SQLException {
        String sql = "INSERT INTO registro " +
                "(hora_entrada, placa_vehiculo, " +
                " id_celda, id_usuario) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(r.getHoraEntrada()));
            ps.setString   (2, r.getPlacaVehiculo());
            ps.setInt      (3, r.getIdCelda());
            ps.setInt      (4, r.getIdUsuario());
            ps.executeUpdate();
        }
    }

    /**
     * Busca el registro de entrada abierto de una placa.
     * @param  placa placa del vehículo
     * @return Registro activo o null si no está en parqueadero
     */
    public Registro buscarEntradaAbierta(String placa)
            throws SQLException {
        String sql = "SELECT * FROM registro " +
                "WHERE placa_vehiculo = ? " +
                "AND hora_salida IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, placa.toUpperCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        }
        return null;
    }

    /**
     * @param idRegistro ID del registro a cerrar
     * @param salida     hora de salida
     */
    public void registrarSalida(int idRegistro,
                                LocalDateTime salida)
            throws SQLException {
        String sql = "UPDATE registro " +
                "SET hora_salida = ? " +
                "WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(salida));
            ps.setInt      (2, idRegistro);
            ps.executeUpdate();
        }
    }

    /**
     * Retorna todos los vehículos actualmente en el parqueadero.
     * Solo registros con hora_salida IS NULL.
     */
    public List<Registro> obtenerVehiculosActivos()
            throws SQLException {
        List<Registro> lista = new ArrayList<>();
        String sql = "SELECT * FROM registro " +
                "WHERE hora_salida IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // ═══════════════════════════════════════════════
    // ITERACIÓN 2 — requerido por ParqueaderoService

    /**
     * @param  idCelda ID de la celda a verificar
     * @return Registro activo o null si la celda está libre
     */
    public Registro buscarEntradaAbiertaPorCelda(int idCelda)
            throws SQLException {
        String sql = "SELECT * FROM registro " +
                "WHERE id_celda = ? " +
                "AND hora_salida IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCelda);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        }
        return null;
    }

    /**
     * Retorna todos los registros de entrada de un usuario.
     * @param idUsuario ID del usuario
     * @return lista de registros ordenados por fecha desc
     */
    public List<Registro> obtenerRegistrosPorUsuario(int idUsuario)
            throws SQLException {
        List<Registro> lista = new ArrayList<>();
        String sql = "SELECT * FROM registro " +
                "WHERE id_usuario = ? " +
                "ORDER BY hora_entrada DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // ═══════════════════════════════════════════════
    // MAPPER PRIVADO

    /**
     * Convierte un ResultSet en un objeto Registro.
     * Centraliza el mapeo para todos los métodos del DAO.
     */
    private Registro mapear(ResultSet rs) throws SQLException {
        Registro r = new Registro();
        r.setId            (rs.getInt      ("id"));
        r.setHoraEntrada   (rs.getTimestamp("hora_entrada")
                .toLocalDateTime());
        r.setPlacaVehiculo (rs.getString   ("placa_vehiculo"));
        r.setIdCelda       (rs.getInt      ("id_celda"));
        r.setIdUsuario     (rs.getInt      ("id_usuario"));

        // hora_salida puede ser NULL — verificar antes de mapear
        Timestamp salida = rs.getTimestamp("hora_salida");
        if (salida != null) {
            r.setHoraSalida(salida.toLocalDateTime());
        }
        return r;
    }


    public Registro buscarActivoPorUsuario(int idUsuario) {
        // Usamos 'id_usuario' y 'hora_salida' tal cual están en tu script SQL
        String sql = "SELECT * FROM registro WHERE id_usuario = ? AND hora_salida IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Registro r = new Registro();
                r.setId(rs.getInt("id"));
                r.setIdCelda(rs.getInt("id_celda"));
                return r;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public void registrarSalida(int idRegistro) throws SQLException {
        String sql = "UPDATE registro SET hora_salida = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idRegistro);
            pstmt.executeUpdate();
        }
    }
}