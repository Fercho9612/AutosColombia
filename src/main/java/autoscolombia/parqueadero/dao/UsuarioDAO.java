package autoscolombia.parqueadero.dao;

import autoscolombia.parqueadero.model.Usuario;
import autoscolombia.parqueadero.DataBaseConnection.DatabaseConnection;

import java.sql.*;

public class UsuarioDAO {
    public Usuario authenticate(String nombre, String password) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE nombre = ? AND password = SHA2(?, 256)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setNombre(rs.getString("nombre"));
                u.setRol(rs.getString("rol"));
                return u;
            }
        }
        return null;
    }
}