package parqueadero.dto;

import lombok.Data;

/**
 * DTO para representar el estado actual del parqueadero.
 * Mapea la vista 'v_ocupacion'.
 */
@Data
public class OcupacionDTO {
    private String tipo;      // CARRO o MOTO
    private int totalCeldas;
    private int ocupadas;
    private int disponibles;
}