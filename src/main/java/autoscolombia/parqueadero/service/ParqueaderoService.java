package autoscolombia.parqueadero.service;

import autoscolombia.parqueadero.dao.*;
import autoscolombia.parqueadero.model.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ParqueaderoService {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final VehiculoDAO vehiculoDAO = new VehiculoDAO();
    private final CeldaDAO celdaDAO = new CeldaDAO();
    private final RegistroDAO registroDAO = new RegistroDAO();

    public Usuario login(String nombre, String password) throws SQLException {
        return usuarioDAO.authenticate(nombre, password);
    }

    public void registrarEntrada(String placa, String tipo, int idUsuario) throws Exception {
        if (!tipo.equals("CARRO") && !tipo.equals("MOTO")) {
            throw new Exception("Tipo de vehículo inválido");
        }
        placa = placa.trim().toUpperCase();

        Celda celda = celdaDAO.buscarCeldaDisponible(tipo);
        if (celda == null) {
            throw new Exception("No hay celdas disponibles para " + tipo);
        }

        Vehiculo v = new Vehiculo();
        v.setPlaca(placa);
        v.setTipo(tipo);
        vehiculoDAO.guardarOActualizar(v);

        Registro r = new Registro();
        r.setHoraEntrada(LocalDateTime.now());
        r.setPlacaVehiculo(placa);
        r.setIdCelda(celda.getId());
        r.setIdUsuario(idUsuario);
        registroDAO.registrarEntrada(r);

        celdaDAO.actualizarDisponibilidad(celda.getId(), false);
    }

    public long registrarSalida(String placa) throws Exception {
        placa = placa.trim().toUpperCase();
        Registro r = registroDAO.buscarEntradaAbierta(placa);
        if (r == null) {
            throw new Exception("No existe entrada abierta para la placa " + placa);
        }
        LocalDateTime salida = LocalDateTime.now();
        registroDAO.registrarSalida(r.getId(), salida);
        celdaDAO.actualizarDisponibilidad(r.getIdCelda(), true);
        return r.calcularMinutosPermanencia();
    }

    public List<Registro> getVehiculosActivos() throws SQLException {
        return registroDAO.obtenerVehiculosActivos();
    }
}