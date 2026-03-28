package parqueadero.dao;

import parqueadero.DataBaseConnection.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class PagoDAO {

    /**
     * Trazabilidad Total: Llama al SP que tú escribiste en el script.
     * r_monto será calculado según los minutos en la BD y el precio_por_minuto de la tabla tarifa.
     */
    public BigDecimal consultarLiquidacion(String placa) throws SQLException {
        String sql = "{CALL sp_calcularTarifa(?, ?)}";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, placa.toUpperCase());
            cs.registerOutParameter(2, Types.DECIMAL);
            cs.execute();

            return cs.getBigDecimal(2);
        }
    }

    /**
     * Ejecuta sp_registrarSalida que actualiza Registro, Celda y crea el Pago.
     */
    public boolean procesarCobroFinal(String placa, int idUsuario) throws SQLException {
        String sql = "{CALL sp_registrarSalida(?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, placa.toUpperCase());
            cs.setInt(2, idUsuario);
            // Parámetros de salida de tu procedimiento
            cs.registerOutParameter(3, Types.INTEGER); // r_minutos
            cs.registerOutParameter(4, Types.DECIMAL); // r_monto
            cs.registerOutParameter(5, Types.INTEGER); // r_id_celda
            cs.registerOutParameter(6, Types.VARCHAR); // r_mensaje

            cs.execute();
            return "OK".equalsIgnoreCase(cs.getString(6));
        }
    }
}