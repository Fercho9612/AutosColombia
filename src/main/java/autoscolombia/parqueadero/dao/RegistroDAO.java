package autoscolombia.parqueadero.dao;

import autoscolombia.parqueadero.model.Registro;
import autoscolombia.parqueadero.DataBaseConnection.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RegistroDAO {
    public void registrarEntrada(Registro r) throws SQLException {
        String sql = "INSERT INTO registro (hora_entrada, placa_vehiculo, id_celda, id_usuario) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(r.getHoraEntrada()));
            ps.setString(2, r.getPlacaVehiculo());
            ps.setInt(3, r.getIdCelda());
            ps.setInt(4, r.getIdUsuario());
            ps.executeUpdate();
        }
    }

    public Registro buscarEntradaAbierta(String placa) throws SQLException {
        String sql = "SELECT * FROM registro WHERE placa_vehiculo = ? AND hora_salida IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, placa.toUpperCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Registro r = new Registro();
                r.setId(rs.getInt("id"));
                r.setHoraEntrada(rs.getTimestamp("hora_entrada").toLocalDateTime());
                r.setPlacaVehiculo(rs.getString("placa_vehiculo"));
                r.setIdCelda(rs.getInt("id_celda"));
                r.setIdUsuario(rs.getInt("id_usuario"));
                return r;
            }
        }
        return null;
    }

    public void registrarSalida(int idRegistro, LocalDateTime salida) throws SQLException {
        String sql = "UPDATE registro SET hora_salida = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(salida));
            ps.setInt(2, idRegistro);
            ps.executeUpdate();
        }
    }

    public List<Registro> obtenerVehiculosActivos() throws SQLException {
        List<Registro> lista = new ArrayList<>();
        String sql = "SELECT * FROM registro WHERE hora_salida IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Registro r = new Registro();
                r.setId(rs.getInt("id"));
                r.setHoraEntrada(rs.getTimestamp("hora_entrada").toLocalDateTime());
                r.setPlacaVehiculo(rs.getString("placa_vehiculo"));
                r.setIdCelda(rs.getInt("id_celda"));
                r.setIdUsuario(rs.getInt("id_usuario"));
                lista.add(r);
            }
        }
        return lista;
    }
}