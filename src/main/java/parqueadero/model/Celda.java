package parqueadero.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Modelo: Celda
 *
 * Representa un espacio físico del parqueadero.
 * gestión del catálogo de celdas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Celda {

    // ── Iteración 1 ──────────────────────────────
    private int     id;
    private String  tipo;              // "CARRO" | "MOTO"
    private boolean disponible = true; // true=libre · false=ocupada
}