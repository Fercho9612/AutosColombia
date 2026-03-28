package parqueadero.dao;

import parqueadero.DataBaseConnection.DatabaseConnection;
import parqueadero.model.Vehiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Operaciones básicas para la entidad Vehículo.
 */
public class VehiculoDAO {

    /**
     * Busca un vehículo por su placa.
     */
    public Vehiculo buscarPorPlaca(String placa) {
        String sql = "SELECT * FROM vehiculo WHERE placa = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, placa.toUpperCase().trim());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Vehiculo(
                        rs.getString("placa"),
                        Vehiculo.TipoVehiculo.valueOf(rs.getString("tipo"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}