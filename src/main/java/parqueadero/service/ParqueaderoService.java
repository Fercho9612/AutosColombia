package parqueadero.service;

import parqueadero.dao.*;
import parqueadero.model.Celda;
import parqueadero.model.Registro;
import parqueadero.model.Usuario;

import java.sql.SQLException;
import java.util.List;

/**
 * Servicio principal del parqueadero.
 * Orquesta los DAOs — NO contiene SQL directo.
 * Toda operación de BD se delega al DAO correspondiente.
 *
 * Equipo: Cadavid · Paternina · Marin — 2026
 */
public class ParqueaderoService {

    // ── DAOs ────────────────────────────────────────────
    private final UsuarioDAO  usuarioDAO  = new UsuarioDAO();
    private final CeldaDAO    celdaDAO    = new CeldaDAO();
    private final RegistroDAO registroDAO = new RegistroDAO();
    private final PagoDAO     pagoDAO     = new PagoDAO();
    private final VehiculoDAO vehiculoDAO = new VehiculoDAO();

    // ====================== AUTENTICACIÓN ======================

    /**
     * Valida credenciales y devuelve el Usuario autenticado.
     * @return Usuario si las credenciales son correctas, null si no.
     */
    public Usuario login(String nombre, String password) {
        try {
            return usuarioDAO.authenticate(nombre, password);
        } catch (SQLException e) {
            System.err.println("[login] Error SQL: " + e.getMessage());
            return null;
        }
    }

    /**
     * Restaura la contraseña verificando nombre + documento.
     * @throws Exception si el usuario no existe o hay error de BD.
     */
    public void restaurarPassword(String nombre, String documento, String nuevaPass) throws Exception {
        try {
            Usuario u = usuarioDAO.buscarPorNombre(nombre);
            if (u == null || !documento.equals(u.getDocumento())) {
                throw new Exception("Usuario no encontrado con esos datos.");
            }
            boolean ok = usuarioDAO.actualizarPassword(u.getId(), nuevaPass);
            if (!ok) throw new Exception("No se pudo actualizar la contraseña.");
        } catch (SQLException e) {
            throw new Exception("Error de base de datos: " + e.getMessage());
        }
    }

    // ====================== USUARIOS ======================

    /**
     * Busca usuarios activos por nombre parcial o documento exacto.
     * Si el filtro está vacío, retorna todos los usuarios activos.
     * @param filtro cadena de búsqueda (puede ser vacía, nunca null)
     */
    public List<Usuario> buscarUsuarios(String filtro) {
        try {
            return usuarioDAO.buscarPorCriterio(filtro == null ? "" : filtro);
        } catch (SQLException e) {
            System.err.println("[buscarUsuarios] Error SQL: " + e.getMessage());
            return List.of();   // Lista vacía segura — nunca null
        }
    }

    /**
     * Registra un nuevo usuario junto con su vehículo y primera entrada.
     * Flujo: insertar usuario → registrar entrada (SP) → resultado.
     *
     * @param nombre      Nombre completo del cliente
     * @param documento   Cédula / ID
     * @param telefono    Celular
     * @param correo      Email (puede ser vacío)
     * @param placa       Placa del vehículo (se normaliza a mayúsculas)
     * @param tipo        "Mensual" | "Visitante"
     * @param idCelda     No se usa directamente; el SP busca celda disponible
     * @param idUsuarioOp ID del vigilante/admin que opera el sistema
     * @return true si el registro fue exitoso
     * @throws Exception con mensaje legible si algo falla
     */
    public boolean registrarNuevoUsuarioCompleto(String nombre, String documento,
                                                 String telefono, String correo,
                                                 String placa, String tipo,
                                                 int idCelda, int idUsuarioOp) throws Exception {
        // 1. Verificar documento duplicado
        try {
            if (usuarioDAO.existeDocumento(documento)) {
                throw new Exception("El documento " + documento + " ya está registrado.");
            }
        } catch (SQLException e) {
            throw new Exception("Error al verificar documento: " + e.getMessage());
        }

        // 2. Construir objeto Usuario
        Usuario nuevo = new Usuario();
        nuevo.setNombre(nombre);
        nuevo.setDocumento(documento);
        nuevo.setTelefono(telefono);
        nuevo.setCorreo(correo);
        nuevo.setRol(Usuario.Rol.vigilante);         // Rol de cliente por defecto
        nuevo.setPassword("Temporal_2026");          // Contraseña temporal — cambiar en producción
        nuevo.setTipoCliente(Usuario.TipoCliente.valueOf(tipo.toLowerCase()));
        nuevo.setActivo(true);

        // 3. Insertar usuario en BD
        try {
            boolean insertado = usuarioDAO.registrar(nuevo);
            if (!insertado) throw new Exception("No se pudo insertar el usuario en la base de datos.");
        } catch (SQLException e) {
            throw new Exception("Error al guardar usuario: " + e.getMessage());
        }

        // 4. Registrar entrada del vehículo usando el SP (busca celda automáticamente)
        //    Determinamos el tipo de vehículo a partir de la celda seleccionada en la UI
        //    Si no hay celda disponible el SP devuelve "SIN_ESPACIO"
        try {
            // Obtener tipo de vehículo desde la celda seleccionada
            String tipoVehiculo = "CARRO"; // valor por defecto
            try {
                Celda celdaSel = listarCeldas().stream()
                        .filter(c -> c.getId() == idCelda)
                        .findFirst().orElse(null);
                if (celdaSel != null) {
                    tipoVehiculo = celdaSel.getTipo().name();
                }
            } catch (Exception ignored) { /* usa valor por defecto */ }

            // Recuperar el ID real del usuario recién insertado
            Usuario registrado = usuarioDAO.buscarPorNombre(nombre);
            int idNuevoUsuario = (registrado != null) ? registrado.getId() : idUsuarioOp;

            String resultado = registroDAO.registrarEntrada(placa, tipoVehiculo, idNuevoUsuario);

            if ("OK".equals(resultado)) {
                return true;
            } else if (resultado != null && resultado.startsWith("SIN_ESPACIO")) {
                throw new Exception("No hay celdas disponibles para ese tipo de vehículo.");
            } else if (resultado != null && resultado.startsWith("Vehículo ya")) {
                throw new Exception("El vehículo con placa " + placa + " ya se encuentra dentro.");
            } else {
                throw new Exception("Error al registrar entrada: " + resultado);
            }

        } catch (SQLException e) {
            throw new Exception("Error al registrar entrada del vehículo: " + e.getMessage());
        }
    }

    /**
     * Actualiza teléfono, correo y tipo de cliente de un usuario existente.
     * Requerido por PanelSalidas → guardarCambios().
     *
     * @param idUsuario    ID del usuario a modificar
     * @param telefono     Nuevo teléfono
     * @param correo       Nuevo correo
     * @param tipoCliente  "Activo" | "Inactivo" — se traduce a activo/inactivo en BD
     * @throws Exception si hay error de BD o usuario no encontrado
     */
    public void actualizarUsuario(int idUsuario, String telefono,
                                  String correo, String tipoCliente) throws Exception {
        try {
            // El tercer parámetro del DAO es tipo_cliente (mensual/visitante),
            // pero PanelSalidas envía el estado ("Activo"/"Inactivo").
            // Manejamos ambos casos para ser robustos.
            String tipoParaDB;
            if ("Activo".equalsIgnoreCase(tipoCliente) || "Inactivo".equalsIgnoreCase(tipoCliente)) {
                // El comboBox de estado no cambia tipo_cliente; actualizamos solo datos de contacto
                tipoParaDB = null; // Ver nota abajo
            } else {
                tipoParaDB = tipoCliente.trim().toLowerCase(); // "mensual" | "visitante"
            }

            // Si el panel envía un estado (Activo/Inactivo), actualizamos activo por separado
            if ("Inactivo".equalsIgnoreCase(tipoCliente)) {
                usuarioDAO.desactivar(idUsuario);
            }

            // Actualizar datos de contacto (DAO ignora tipo_cliente si es null → ponemos el valor actual)
            // Recuperamos el usuario para no pisar tipo_cliente con un valor incorrecto
            Usuario actual = null;
            try {
                actual = buscarUsuarioPorId(idUsuario);
            } catch (Exception ignored) {}

            String tipoFinal = (tipoParaDB != null) ? tipoParaDB
                    : (actual != null ? actual.getTipoCliente().name().toLowerCase() : "visitante");

            boolean ok = usuarioDAO.actualizar(idUsuario, telefono, correo, tipoFinal);
            if (!ok) throw new Exception("No se encontró el usuario con ID: " + idUsuario);

        } catch (SQLException e) {
            throw new Exception("Error al actualizar usuario: " + e.getMessage());
        }
    }

    /**
     * Desactiva lógicamente un usuario (borrado lógico).
     * Requerido por PanelSalidas → confirmarDesactivacion().
     * @throws Exception si hay error de BD
     */
    public void desactivarUsuario(int idUsuario) throws Exception {
        try {
            boolean ok = usuarioDAO.eliminarUsuario(idUsuario); // Cierra entradas activas + desactiva
            if (!ok) throw new Exception("No se pudo desactivar el usuario con ID: " + idUsuario);
        } catch (SQLException e) {
            throw new Exception("Error al desactivar usuario: " + e.getMessage());
        }
    }

    // ====================== CELDAS ======================

    /**
     * Lista todas las celdas del parqueadero.
     * Requerido por PanelCeldas, PanelRegistro y PanelConsultaCeldas.
     * @throws SQLException si hay error de conexión
     */
    public List<Celda> listarCeldas() throws SQLException {
        return celdaDAO.listarTodas();  // Delega al DAO — no hay SQL duplicado aquí
    }

    /**
     * Registra una nueva celda física en el parqueadero.
     * @param id   Número identificador de la celda (ingresado por el admin)
     * @param tipo "CARRO" | "MOTO"
     * @throws Exception si el ID ya existe o hay error de BD
     */
    public void registrarCelda(int id, String tipo) throws Exception {
        try {
            if (celdaDAO.existeCelda(id)) {
                throw new Exception("Ya existe una celda con el ID: " + id);
            }
            boolean ok = celdaDAO.registrar(id, tipo);
            if (!ok) throw new Exception("No se pudo registrar la celda.");
        } catch (SQLException e) {
            throw new Exception("Error al registrar celda: " + e.getMessage());
        }
    }

    /**
     * Asigna una celda a un usuario mensual.
     * CORRECCIÓN: el primer parámetro es idUsuario (int), no el documento.
     *
     * @param idUsuario ID del usuario mensual (clave primaria en tabla usuario)
     * @param idCelda   ID de la celda a asignar
     * @throws Exception si la celda no existe, ya está ocupada o hay error de BD
     */
    public void asignarCeldaMensual(int idUsuario, int idCelda) throws Exception {
        // CORRECCIÓN: tabla correcta es "celda", no "celdas"
        // Delegamos al DAO de Registro vía el SP o una actualización directa.
        // Como el SQL script no tiene un SP específico para esto, usamos SQL directo aquí.
        String sql = "UPDATE celda SET disponible = FALSE WHERE id = ? AND disponible = TRUE";

        try (java.sql.Connection con = parqueadero.DataBaseConnection.DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCelda);
            int filas = ps.executeUpdate();

            if (filas == 0) {
                throw new Exception("La celda " + idCelda + " no existe o ya está ocupada.");
            }
        } catch (SQLException e) {
            throw new Exception("Error al asignar celda: " + e.getMessage());
        }
    }

    // ====================== REGISTROS / ENTRADAS-SALIDAS ======================

    /**
     * Registra la entrada de un vehículo al parqueadero.
     * Llama al stored procedure sp_registrarEntrada.
     *
     * @param placa       Placa del vehículo
     * @param tipoVehiculo "CARRO" | "MOTO"
     * @param idUsuario   ID del vigilante que registra
     * @return Mensaje del SP: "OK", "SIN_ESPACIO:...", "Vehículo ya está dentro"
     * @throws SQLException si hay error de BD
     */
    public String registrarEntrada(String placa, String tipoVehiculo, int idUsuario) throws SQLException {
        return registroDAO.registrarEntrada(placa, tipoVehiculo, idUsuario);
    }

    /**
     * Registra la salida de un vehículo y calcula el cobro.
     * Llama al stored procedure sp_registrarSalida.
     *
     * @param placa           Placa del vehículo
     * @param idUsuarioSalida ID del vigilante que procesa la salida
     * @return Resumen con monto y tiempo, o mensaje de error del SP
     * @throws SQLException si hay error de BD
     */
    public String registrarSalida(String placa, int idUsuarioSalida) throws SQLException {
        return registroDAO.registrarSalida(placa, idUsuarioSalida);
    }

    /**
     * Obtiene la placa del vehículo actualmente dentro del parqueadero
     * asociada al último registro de entrada de un usuario.
     * Requerido por PanelEntradas para mostrar la columna "PLACA".
     *
     * @param idUsuario ID del usuario
     * @return Placa o null si el usuario no tiene vehículo activo
     */
    public String obtenerPlacaPorUsuario(int idUsuario) {
        try {
            return usuarioDAO.buscarPlacaPorUsuario(idUsuario);
        } catch (SQLException e) {
            System.err.println("[obtenerPlacaPorUsuario] Error SQL: " + e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene el historial de registros (entradas/salidas) de un usuario.
     * Requerido por PanelSalidas → cargarVehiculosAsociados().
     *
     * @param idUsuario ID del usuario
     * @return Lista de registros (puede ser vacía, nunca null)
     */
    public List<Registro> obtenerVehiculosPorUsuario(int idUsuario) {
        // Consulta directa — el RegistroDAO no tiene este método aún,
        // se implementa aquí con SQL hasta que se añada al DAO.
        List<Registro> lista = new java.util.ArrayList<>();
        String sql = "SELECT id, placa_vehiculo, hora_entrada, hora_salida, id_celda " +
                "FROM registro WHERE id_usuario_entrada = ? ORDER BY hora_entrada DESC LIMIT 20";

        try (java.sql.Connection con = parqueadero.DataBaseConnection.DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            java.sql.ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Registro r = new Registro();
                r.setId(rs.getInt("id"));
                r.setPlacaVehiculo(rs.getString("placa_vehiculo"));
                r.setHoraEntrada(rs.getTimestamp("hora_entrada") != null
                        ? rs.getTimestamp("hora_entrada").toLocalDateTime() : null);
                r.setHoraSalida(rs.getTimestamp("hora_salida") != null
                        ? rs.getTimestamp("hora_salida").toLocalDateTime() : null);
                r.setIdCelda(rs.getInt("id_celda"));
                lista.add(r);
            }
        } catch (SQLException e) {
            System.err.println("[obtenerVehiculosPorUsuario] Error SQL: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Cierra todas las entradas activas de un usuario (sin calcular cobro).
     * Usado por PanelSalidas → guardarCambios() cuando se edita un usuario.
     * @throws Exception si hay error de BD
     */
    public void finalizarSalidaPorPlacaParaUsuario(int idUsuario) throws Exception {
        String sql = "UPDATE registro SET hora_salida = NOW(), id_usuario_salida = id_usuario_entrada " +
                "WHERE id_usuario_entrada = ? AND hora_salida IS NULL";

        try (java.sql.Connection con = parqueadero.DataBaseConnection.DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ps.executeUpdate();
            // No lanzamos excepción si no hay filas: el usuario puede no tener entradas activas

        } catch (SQLException e) {
            throw new Exception("Error al finalizar salida: " + e.getMessage());
        }
    }

    // ====================== PAGOS / TARIFAS ======================

    /**
     * Consulta el monto a cobrar por el tiempo en parqueadero de un vehículo.
     * Usa sp_calcularTarifa de MySQL.
     *
     * @param placa Placa del vehículo con entrada activa
     * @return Monto calculado en pesos, 0 si no hay entrada activa
     */
    public java.math.BigDecimal consultarLiquidacion(String placa) {
        try {
            return pagoDAO.consultarLiquidacion(placa);
        } catch (SQLException e) {
            System.err.println("[consultarLiquidacion] Error SQL: " + e.getMessage());
            return java.math.BigDecimal.ZERO;
        }
    }

    /**
     * Procesa el cobro final y registra la salida completa.
     * Llama a sp_registrarSalida que actualiza registro, celda y crea el pago.
     *
     * @param placa     Placa del vehículo
     * @param idUsuario ID del vigilante que cobra
     * @return true si el proceso fue exitoso
     */
    public boolean procesarCobroFinal(String placa, int idUsuario) {
        try {
            return pagoDAO.procesarCobroFinal(placa, idUsuario);
        } catch (SQLException e) {
            System.err.println("[procesarCobroFinal] Error SQL: " + e.getMessage());
            return false;
        }
    }

    // ====================== MÉTODOS AUXILIARES PRIVADOS ======================

    /**
     * Busca un usuario por su ID.
     * Método de apoyo interno para actualizarUsuario().
     */
    private Usuario buscarUsuarioPorId(int id) throws Exception {
        // Busca todos los usuarios y filtra por ID (alternativa a tener buscarPorId en DAO)
        // En una siguiente iteración se recomienda añadir buscarPorId(int) a UsuarioDAO.
        String sql = "SELECT * FROM usuario WHERE id = ?";
        try (java.sql.Connection con = parqueadero.DataBaseConnection.DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setNombre(rs.getString("nombre"));
                u.setTipoCliente(Usuario.TipoCliente.valueOf(rs.getString("tipo_cliente")));
                u.setActivo(rs.getBoolean("activo"));
                return u;
            }
        } catch (SQLException e) {
            throw new Exception("Error al buscar usuario por ID: " + e.getMessage());
        }
        return null;
    }
}