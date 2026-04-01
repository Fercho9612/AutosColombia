package parqueadero.dao;

import parqueadero.DataBaseConnection.DatabaseConnection;

import java.sql.*;

/**
 * Ejecuta la lógica central del parqueadero llamando a los SP de MySQL.
 */

public class RegistroDAO {

    /**
     * Ejecuta: CALL sp_registrarEntrada(placa, tipo, id_usuario, OUT id_reg, OUT id_cel, OUT msg)
     */
    public String registrarEntrada(String placa, String tipo, int idUsuario) throws SQLException {
        String sql = "{CALL sp_registrarEntrada(?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, placa.toUpperCase().trim());
            cs.setString(2, tipo.toUpperCase()); // 'CARRO' o 'MOTO'
            cs.setInt(3, idUsuario);

            // Parámetros de salida definidos en tu Script SQL
            cs.registerOutParameter(4, Types.INTEGER); // r_id_registro
            cs.registerOutParameter(5, Types.INTEGER); // r_id_celda
            cs.registerOutParameter(6, Types.VARCHAR); // r_mensaje

            cs.execute();

            // Retornamos el mensaje ("OK", "SIN_ESPACIO", "VEHICULO_DENTRO", etc.)
            return cs.getString(6);
        }
    }

}