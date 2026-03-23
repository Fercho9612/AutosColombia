package parqueadero.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Representa a los operadores del sistema
 * clientes del parqueadero
 *  autenticación básica
 * gestión de clientes
 *  * Seguridad:
 */
@Data
//@NoArgsConstructor
@AllArgsConstructor

public class Usuario {

    // ── Iteración 1 ──────────────────────────────
    private int    id;
    private String nombre;
    private String rol;           // "admin" | "vigilante"
    private String password;      // hash SHA2-256 — nunca texto plano

    // ── Iteración 2 ──────────────────────────────
    private String  documento;
    private String  telefono;
    private String  correo;
    private String  tipoCliente;  // "mensual" | "visitante"
    private boolean activo;       // false = desactivado (reversible)
    private java.time.LocalDateTime fechaRegistro;

    public Usuario() {
        this.fechaRegistro = java.time.LocalDateTime.now();
    }
    public java.time.LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    // 4. Setter (opcional, pero recomendado)
    public void setFechaRegistro(java.time.LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }



}