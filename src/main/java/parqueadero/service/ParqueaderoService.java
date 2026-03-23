package parqueadero.service;

import parqueadero.dao.*;
import parqueadero.model.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio principal que coordina la lógica de negocio del parqueadero.
 * Gestiona la comunicación entre los DAOs para usuarios, celdas y vehículos.
 */
public class ParqueaderoService {

    // ── Instancias de DAOs ───────────────────────
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final VehiculoDAO vehiculoDAO = new VehiculoDAO();
    private final CeldaDAO celdaDAO = new CeldaDAO();
    private final RegistroDAO registroDAO = new RegistroDAO();
    private final AsignacionDAO asignacionDAO = new AsignacionDAO();
    //private final DatabaseConnection conexion = new DatabaseConnection();

    // ═══════════════════════════════════════════════
    // ITERACIÓN 1 — Autenticación

    /**
     * Valida el acceso de un usuario al sistema.
     * @param nombre Nombre del usuario.
     * @param password Contraseña en texto plano.
     * @return Usuario autenticado o null si falla.
     * @throws SQLException Si hay error en la base de datos.
     */
    public Usuario login(String nombre, String password)
            throws SQLException {
        return usuarioDAO.authenticate(nombre, password);
    }

    /**
     * Cambia la contraseña validando el documento como segundo factor.
     * @param nombre Nombre de la cuenta.
     * @param documento Cédula del usuario.
     * @param newPassword Nueva clave a asignar.
     * @throws Exception Si los datos no coinciden o el usuario no existe.
     */
    public void restaurarPassword(String nombre, String documento,
                                  String newPassword) throws Exception {
        // Paso 1: verificar que el usuario existe
        Usuario u = usuarioDAO.buscarPorNombre(nombre);
        if (u == null) {
            throw new Exception(
                    "Usuario no encontrado: " + nombre);
        }

        // Paso 2: verificar documento como segundo factor
        if (u.getDocumento() == null ||
                !u.getDocumento().equals(documento)) {
            throw new Exception(
                    "Documento incorrecto — no se puede restaurar");
        }

        // Paso 3: validar que la nueva contraseña no esté vacía
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new Exception(
                    "La nueva contraseña no puede estar vacía");
        }

        // Paso 4: actualizar contraseña
        boolean ok = usuarioDAO.actualizarPassword(
                u.getId(), newPassword);
        if (!ok) {
            throw new Exception(
                    "Error al actualizar la contraseña");
        }
    }

    // ═══════════════════════════════════════════════
    // ITERACIÓN 2 — Gestión de Usuarios

    /**
     * @param criterio nombre parcial o documento exacto
     * @return lista de usuarios que coinciden
     */
    public List<Usuario> buscarUsuarios(String criterio)
            throws SQLException {
        return usuarioDAO.buscarPorCriterio(criterio);
    }

    /**
     * @throws Exception si no se pudo actualizar
     */
    public void actualizarUsuario(int id, String telefono,
                                  String correo,
                                  String tipoCliente)
            throws Exception {
        if (!usuarioDAO.actualizar(id, telefono, correo, tipoCliente)) {
            throw new Exception(
                    "No se pudo actualizar el usuario id: " + id);
        }
    }

    /**
     * @throws Exception si no se pudo desactivar
     */
    public void desactivarUsuario(int id) throws Exception {
        // Liberar celda asignada si tiene una activa
        AsignacionCelda asignacion = asignacionDAO.buscarActiva(id);
        if (asignacion != null) {
            asignacionDAO.desactivar(id);
            celdaDAO.actualizarDisponibilidad(
                    asignacion.getIdCelda(), true);
        }

        if (!usuarioDAO.desactivar(id)) {
            throw new Exception(
                    "No se pudo desactivar el usuario id: " + id);
        }
    }

    /**
     * @param id   identificador único de la celda
     * @param tipo "CARRO" | "MOTO"
     * @throws Exception si el ID ya existe
     */
    public void registrarCelda(int id, String tipo)
            throws Exception {
        if (celdaDAO.existeCelda(id)) {
            throw new Exception(
                    "Identificador de celda ya existe: " + id);
        }
        if (!celdaDAO.registrar(id, tipo)) {
            throw new Exception("Error al registrar la celda");
        }
    }

    /**
     * Retorna todas las celdas para el mapa visual.
     *
     * @return lista completa de celdas con su estado
     */
    public List<Celda> listarCeldas() throws SQLException {
        return celdaDAO.listarTodas();
    }

    /**
     * Solo permite modificar celdas disponibles.
     *
     * @throws Exception si la celda está ocupada
     */
    public void actualizarTipoCelda(int id, String tipo)
            throws Exception {
        // Verificar que la celda existe y está disponible
        List<Celda> celdas = celdaDAO.listarTodas();
        Celda celda = celdas.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElseThrow(() ->
                        new Exception("Celda no encontrada: " + id));

        if (!celda.isDisponible()) {
            throw new Exception(
                    "No se puede modificar — celda con vehículo activo");
        }
        if (!celdaDAO.actualizarTipo(id, tipo)) {
            throw new Exception(
                    "Error al actualizar tipo de celda: " + id);
        }
    }


    // ═══════════════════════════════════════════════
    // ITERACIÓN 2 — Asignación celda mensual

    /**
     * @param idUsuario ID del usuario mensual
     * @param idCelda   ID de la celda a asignar
     * @throws Exception si la celda no está disponible
     *                   o el usuario ya tiene asignación
     */
    public void asignarCeldaMensual(int idUsuario, int idCelda) throws Exception {

        // ─── VALIDACIÓN DE LA NUEVA CELDA (Tu lógica original) ───
        List<Celda> celdas = celdaDAO.listarTodas();
        Celda celdaDestino = celdas.stream()
                .filter(c -> c.getId() == idCelda)
                .findFirst()
                .orElseThrow(() -> new Exception("Celda no encontrada: " + idCelda));

        if (!celdaDestino.isDisponible()) {
            throw new Exception("La celda C" + idCelda + " ya está ocupada por otro usuario.");
        }

        // ─── LÓGICA DE LIBERACIÓN AUTOMÁTICA  ───
        AsignacionCelda asignacionVieja = asignacionDAO.buscarActiva(idUsuario);

        if (asignacionVieja != null) {
            // 1. Usamos tu método 'desactivar' para poner activa = FALSE
            asignacionDAO.desactivar(idUsuario);
            // 2. Liberamos la celda vieja en el mapa (CeldaDAO)
            celdaDAO.actualizarDisponibilidad(asignacionVieja.getIdCelda(), true);

            System.out.println("LOG: Liberada celda C" + asignacionVieja.getIdCelda() + " por reubicación.");
        }

        // ─── REGISTRO DE LA NUEVA ASIGNACIÓN ───
        asignacionDAO.asignar(idUsuario, idCelda);
        celdaDAO.actualizarDisponibilidad(idCelda, false);
    }

    /**
     * @param nombre      Nombre del cliente
     * @param documento   Cédula/ID
     * @param telefono    Celular
     * @param correo      Email
     * @param placa       Placa del vehículo
     * @param tipoUsuario "Mensual" | "Visitante"
     * @param idCelda     ID de la celda a ocupar
     * @param idAdmin     ID del administrador que registra
     * @return true si todo se guardó en MySQL, false si falló
     */

    public boolean registrarNuevoUsuarioCompleto(String nombre, String documento, String telefono,
                                                 String correo, String placa, String tipoUsuario,
                                                 int idCelda, int idAdmin) {
        try {
            // 1. Registrar el Usuario
            Usuario u = new Usuario();
            u.setNombre(nombre);
            u.setDocumento(documento);
            u.setTelefono(telefono);
            u.setCorreo(correo);
            u.setTipoCliente(tipoUsuario.toLowerCase());
            u.setActivo(true);
            u.setRol("vigilante");
            u.setPassword(documento);

            // Validar duplicado antes de insertar
            if (usuarioDAO.existeDocumento(documento)) {
                System.err.println(
                        "Documento duplicado: " + documento);
                return false;

            }
            usuarioDAO.registrar(u);
            Usuario usuarioRecienCreado = usuarioDAO.buscarPorCriterio(documento).get(0);
            int idDelCliente = usuarioRecienCreado.getId();

            // 2. Registrar el Vehículo
            Vehiculo v = new Vehiculo();
            v.setPlaca(placa.toUpperCase().trim());
            v.setTipo("CARRO"); // O determinar según lógica
            vehiculoDAO.guardarOActualizar(v);

            // 3. Crear el Registro de Entrada
            Registro r = new Registro();
            r.setHoraEntrada(LocalDateTime.now());
            r.setPlacaVehiculo(v.getPlaca());
            r.setIdCelda(idCelda);
            //r.setIdUsuario(idAdmin);
            r.setIdUsuario(idDelCliente);
            registroDAO.registrarEntrada(r);

            // 4. Actualizar la Celda en MySQL para que salga OCUPADA
            celdaDAO.actualizarDisponibilidad(idCelda, false);

            return true;
        } catch (Exception e) {
            System.err.println("Error registrarNuevoUsuarioCompleto: "
                    + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retorna los vehículos registrados por un usuario.
     * Usados en "Vehículos asociados" del panel editar.
     *
     * @param idUsuario ID del usuario
     * @return lista de registros del usuario
     */
    public List<Registro> obtenerVehiculosPorUsuario(int idUsuario)
            throws SQLException {
        return registroDAO.obtenerRegistrosPorUsuario(idUsuario);
    }

    /**
     * @param idUsuario ID del usuario
     * @return true si había entrada activa y se cerró,
     * false si no había ninguna abierta
     */
    public boolean finalizarSalidaPorPlacaParaUsuario(int idUsuario)
            throws Exception {

        // Obtener todos los registros abiertos del sistema
        List<Registro> activos = registroDAO.obtenerVehiculosActivos();

        boolean salidaHecha = false;

        for (Registro r : activos) {
            // Verificar si el registro pertenece al usuario
            if (r.getIdUsuario() == idUsuario) {

                // Registrar hora de salida
                LocalDateTime salida = LocalDateTime.now();
                registroDAO.registrarSalida(r.getId(), salida);

                // Liberar celda
                celdaDAO.actualizarDisponibilidad(
                        r.getIdCelda(), true);

                salidaHecha = true;
            }
        }

        return salidaHecha;
    }

    /**
     * Obtiene la placa más reciente de un usuario para mostrarla en la tabla de gestión.
     *
     * @param idUsuario ID del usuario a consultar
     * @return La placa del vehículo o null si no tiene registros
     */
    public String obtenerPlacaPorUsuario(int idUsuario) throws SQLException {
        return usuarioDAO.buscarPlacaPorUsuario(idUsuario);
    }


}