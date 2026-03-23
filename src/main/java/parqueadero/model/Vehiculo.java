package parqueadero.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Representa un vehículo que ha ingresado
 *  * El service aplica trim().toUpperCase() antes
 * de persistir — siempre en mayúsculas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehiculo {

    // ── Iteración 1 ──────────────────────────────
    private String placa;
    private String tipo;    // "CARRO" | "MOTO"
}