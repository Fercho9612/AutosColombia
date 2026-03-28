package parqueadero.dao;

import parqueadero.DataBaseConnection.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DAO especializado en consultas de reportes y estado actual.
 */
public class ConsultaDAO {

    public void imprimirOcupacion() {
        String sql = "SELECT * FROM v_ocupacion";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println("Tipo: " + rs.getString("tipo") +
                        " | Libres: " + rs.getInt("disponibles") +
                        " | Ocupadas: " + rs.getInt("ocupadas"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
