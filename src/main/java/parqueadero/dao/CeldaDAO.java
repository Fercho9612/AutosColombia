package parqueadero.dao;

import parqueadero.model.Celda;
import parqueadero.DataBaseConnection.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CeldaDAO {

    // ═══════════════════════════════════════════════
    // ITERACIÓN 1 — Entrada/Salida vehículos

    /**
     * @param tipo "CARRO" | "MOTO"
     * @return primera celda libre o null si no hay
     */
    public Celda buscarCeldaDisponible(String tipo) throws SQLException {
        String sql = "SELECT * FROM celda " +
                "WHERE tipo = ? AND disponible = TRUE LIMIT 1";
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

    /**
     * Actualiza la disponibilidad de una celda.
     * libera celda al registrar salida.
     * ocupa celda al registrar entrada.
     *
     * @param id         ID de la celda
     * @param disponible true=libre · false=ocupada
     */
    public void actualizarDisponibilidad(int id, boolean disponible)
            throws SQLException {
        String sql = "UPDATE celda SET disponible = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, disponible);
            ps.setInt    (2, id);
            ps.executeUpdate();
        }
    }

    // ═══════════════════════════════════════════════
    // ITERACIÓN 2 — Gestión de Celdas
    // ═══════════════════════════════════════════════

    /**
     * Registra una nueva celda en el parqueadero.
     * @param id   identificador único de la celda
     * @param tipo "CARRO" | "MOTO"
     * @return true si se insertó correctamente
     */
    public boolean registrar(int id, String tipo) throws SQLException {
        String sql = "INSERT INTO celda (id, tipo, disponible) " +
                "VALUES (?, ?, TRUE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt   (1, id);
            ps.setString(2, tipo);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Verifica si un ID de celda ya está registrado.
     * evita duplicados antes de insertar.
     * @param id identificador a verificar
     * @return true si ya existe en BD
     */
    public boolean existeCelda(int id) throws SQLException {
        String sql = "SELECT id FROM celda WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeQuery().next();
        }
    }

    /**
     * Lista todas las celdas para el mapa visual.
     * @return lista completa con estado actual de cada celda
     */
    public List<Celda> listarTodasParaMapa() {
        List<Celda> lista = new ArrayList<>();

        //  Cruza celda con registro activo
        String sql = "SELECT c.id, c.tipo, " +
                "IF(r.id IS NULL, 1, 0) as disponible_real " + // 1=Verde, 0=Rojo
                "FROM celda c " +
                "LEFT JOIN registro r ON c.id = r.id_celda AND r.hora_salida IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Celda c = new Celda();
                c.setId(rs.getInt("id"));
                c.setTipo(rs.getString("tipo"));
                // Usamos el valor calculado por SQL, no el de la tabla celda
                c.setDisponible(rs.getBoolean("disponible_real"));
                lista.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Error en DAO al listar para mapa: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Modifica el tipo de una celda existente.
     * @param id   ID de la celda a modificar
     * @param tipo nuevo tipo: "CARRO" | "MOTO"
     * @return true si se actualizó correctamente
     */
    public boolean actualizarTipo(int id, String tipo)
            throws SQLException {
        String sql = "UPDATE celda SET tipo = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipo);
            ps.setInt   (2, id);
            return ps.executeUpdate() > 0;
        }
    }

        // ═══════════════════════════════════════════════
    // MAPPER PRIVADO
    // ═══════════════════════════════════════════════

    /**
     * Convierte un ResultSet en un objeto Celda.
     * Incluye todos los campos de It.1 e It.2.
     */


    public List<Celda> listarTodas() {
        List<Celda> lista = new ArrayList<>();

        // Esta es la consulta que cruza con 'registro'
        String sql = "SELECT c.id, c.tipo, " +
                "IF(r.id IS NULL, 1, 0) as disponible_real " +
                "FROM celda c " +
                "LEFT JOIN registro r ON c.id = r.id_celda AND r.hora_salida IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Celda c = new Celda();
                c.setId(rs.getInt("id"));
                c.setTipo(rs.getString("tipo"));
                c.setDisponible(rs.getBoolean("disponible_real"));
                lista.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
