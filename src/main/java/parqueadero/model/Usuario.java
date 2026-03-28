package parqueadero.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Representa a los operadores del sistema
 * clientes del parqueadero
 *  autenticación básica
 * gestión de clientes
 *  * Seguridad:
 */
@Data // Genera getters, setters, toString, etc.
@NoArgsConstructor // Requerido por muchos frameworks
@AllArgsConstructor // Genera constructor con todos los campos
@Builder // Permite crear objetos con Usuario.builder()

public class Usuario {
    public enum Rol { vigilante, admin }
    public enum TipoCliente { mensual, visitante }

    private int    id;
    private String nombre;
    private Rol rol;
    private String password;
    private String  documento;
    private String  telefono;
    private String  correo;
    private TipoCliente tipoCliente;
    private boolean activo;       // false = desactivado (reversible)
    private java.time.LocalDateTime fechaRegistro;


    // Constructor para nuevos registros (donde el cliente ELIGE)
    public Usuario(String nombre, Rol rol, String password, TipoCliente tipo) {
        this.nombre = nombre;
        this.rol = rol;           // Aquí se asigna la elección manual
        this.password = password;
        this.tipoCliente = tipo;  // Aquí se asigna la elección manual
        this.activo = true;       // Por defecto activo al crear
        this.fechaRegistro = LocalDateTime.now();
    }


    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

}