package parqueadero.dao;

import parqueadero.model.Usuario;
import parqueadero.DataBaseConnection.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**

 * Iteración 1: autenticación y restauración de contraseña
 * Iteración 2: CRUD completo con borrado lógico

 */
public class UsuarioDAO {

    // ═══════════════════════════════════════════════

    /**
     * @param nombre
     * @param password
     * @return Usuario autenticado o null si credenciales incorrectas
     * @throws SQLException si hay error en BD
     */
    public Usuario authenticate(String nombre, String password)
            throws SQLException {
        String sql = "SELECT * FROM usuario " +
                "WHERE nombre = ? " +
                "AND password = SHA2(?, 256)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        }
        return null;
    }

    /**
     * @param id          ID del usuario
     * @param newPassword
     * @return true si se actualizó correctamente
     * @throws SQLException si hay error en BD
     */
    public boolean actualizarPassword(int id, String newPassword)
            throws SQLException {
        String sql = "UPDATE usuario " +
                "SET password = SHA2(?, 256) " +
                "WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt   (2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Busca un usuario por nombre (sin verificar password).
          * @param nombre nombre del usuario
     * @return Usuario encontrado o null si no existe
     *  @throws SQLException si hay error en BD
     */
    public Usuario buscarPorNombre(String nombre)
            throws SQLException {
        String sql = "SELECT * FROM usuario WHERE nombre = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        }
        return null;
    }

    // ═══════════════════════════════════════════════
    // ITERACIÓN 2

    /**
     * Registra un nuevo usuario en el sistema.
     * @param u objeto Usuario con todos los campos completos
     * @return true si se insertó correctamente
     * @throws SQLException si hay error en BD
     */
    public boolean registrar(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuario " +
                "(nombre, rol, password, documento, " +
                " telefono, correo, tipo_cliente, " +
                " activo, fecha_registro) " +
                "VALUES (?, ?, SHA2(?,256), ?, ?, ?, ?, TRUE, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString   (1, u.getNombre());
            ps.setString   (2, String.valueOf(u.getRol()));
            ps.setString   (3, u.getPassword());
            ps.setString   (4, u.getDocumento());
            ps.setString   (5, u.getTelefono());
            ps.setString   (6, u.getCorreo());
            ps.setString   (7, String.valueOf(u.getTipoCliente()));
            ps.setTimestamp(8, u.getFechaRegistro() != null
                    ? Timestamp.valueOf(u.getFechaRegistro())
                    : Timestamp.valueOf(java.time.LocalDateTime.now()));
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Verifica si un documento ya está registrado.
     * @param documento número de documento a verificar
     * @return true si el documento ya existe en BD
     * @throws SQLException si hay error en BD
     */
    public boolean existeDocumento(String documento)
            throws SQLException {
        String sql = "SELECT id FROM usuario WHERE documento = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, documento);
            return ps.executeQuery().next();
        }
    }

    /**
     * Busca usuarios activos por nombre parcial o documento exacto.
     * @param criterio nombre parcial o documento exacto
     * @return lista de usuarios que coinciden
     * @throws SQLException si hay error en BD
     */
    public List<Usuario> buscarPorCriterio(String criterio)
            throws SQLException {
        if (criterio == null) criterio = "NO ENCONTRADO";
        String sql = "SELECT * FROM usuario " +
                "WHERE (nombre LIKE ? OR documento = ?) " +
                "AND activo = TRUE " + "AND nombre != 'admin'";
        List<Usuario> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + criterio + "%");
            ps.setString(2, criterio);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    /**
     * @param id          ID del usuario a modificar
     * @param telefono    nuevo teléfono
     * @param correo      nuevo correo
     * @param tipoCliente nuevo tipo: "mensual" | "visitante"
     * @return true si se actualizó correctamente
     * @throws SQLException si hay error en BD
     */
    public boolean actualizar(int id, String telefono,
                              String correo, String tipoCliente)
            throws SQLException {
        String sql = "UPDATE usuario " +
                "SET telefono = ?, " +
                "    correo = ?, " +
                "    tipo_cliente = ? " +
                "WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, telefono);
            ps.setString(2, correo);
            ps.setString(3, tipoCliente.trim().toLowerCase());
            ps.setInt   (4, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Desactiva un usuario —
     * @param id del usuario a desactivar
     * @return true si se desactivó correctamente
     * */

    public boolean desactivar(int id) throws SQLException {
        String sql = "UPDATE usuario " +
                "SET activo = FALSE " +
                "WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Busca la placa del vehículo más reciente de un usuario.
     * @param idUsuario ID del usuario
     * @return placa o null si no tiene registros
     */
    public String buscarPlacaPorUsuario(int idUsuario)
            throws SQLException {
        String sql = "SELECT placa_vehiculo FROM registro " +
                "WHERE id_usuario_entrada = ? " +
                "ORDER BY hora_entrada DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("placa_vehiculo");
        }
        return null;
    }

    public boolean eliminarUsuario(int idUsuario) throws SQLException {
        String sqlRegistros = "UPDATE registro " +
                "SET hora_salida = CURRENT_TIMESTAMP, " +
                "    id_usuario_salida = id_usuario_entrada " +
                "WHERE id_usuario_entrada = ? " +
                "AND hora_salida IS NULL";

        // 2. Desactivar el usuario
        String sqlUsuario = "UPDATE usuario " +
                "SET activo = FALSE " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Iniciamos transacción segura
            try (PreparedStatement psReg = conn.prepareStatement(sqlRegistros);
                 PreparedStatement psUser = conn.prepareStatement(sqlUsuario)) {

                //Cerrar entradas activas
                psReg.setInt(1, idUsuario);
                psReg.executeUpdate();

                // Desactivar usuario
                psUser.setInt(1, idUsuario);
                int filasAfectadas = psUser.executeUpdate();

                conn.commit(); // Confirmamos los cambios en MySQL
                return filasAfectadas > 0;
            } catch (SQLException e) {
                conn.rollback(); // Si algo falla, deshacemos todo
                throw e;
            }
        }
    }

    // ═══════════════════════════════════════════════

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId          (rs.getInt    ("id"));
        u.setNombre      (rs.getString ("nombre"));
        u.setRol         (Usuario.Rol.valueOf(rs.getString ("rol")));
        u.setDocumento   (rs.getString ("documento"));
        u.setTelefono    (rs.getString ("telefono"));
        u.setCorreo      (rs.getString ("correo"));
        u.setTipoCliente (Usuario.TipoCliente.valueOf(rs.getString ("tipo_cliente")));
        u.setActivo      (rs.getBoolean("activo"));

        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) {
            u.setFechaRegistro(ts.toLocalDateTime());
        }
        return u;
    }
}