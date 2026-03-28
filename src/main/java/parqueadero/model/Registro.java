package parqueadero.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa un movimiento de entrada/salida
 * de un vehículo en el parqueadero.
 */
@Data              // Genera getters, setters, toString, etc.
@NoArgsConstructor // Necesario para frameworks de persistencia.
@AllArgsConstructor// Constructor para lecturas completas desde la DB.
@Builder

public class Registro {

    // ── Iteración 1 ──────────────────────────────
    private int id;
    private LocalDateTime horaEntrada;
    private LocalDateTime horaSalida;      // null = vehículo dentro
    private String placaVehiculo;
    private int idCelda;
    private int idUsuarioEntrada;
    private Integer idUsuarioSalida;
    private BigDecimal monto;

    /**
     * Constructor para crear una entrada nueva.
     * Los campos de salida y monto quedan pendientes.
     */
    public Registro(String placaVehiculo, int idCelda, int idUsuarioEntrada) {
        this.placaVehiculo = placaVehiculo;
        this.idCelda = idCelda;
        this.idUsuarioEntrada = idUsuarioEntrada;
        this.horaEntrada = LocalDateTime.now();
    }
}
