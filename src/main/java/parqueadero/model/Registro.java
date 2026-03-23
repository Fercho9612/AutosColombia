package parqueadero.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.time.Duration;

/**
  * Representa un movimiento de entrada/salida
 * de un vehículo en el parqueadero.
  */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Registro {

    // ── Iteración 1 ──────────────────────────────
    private int           id;
    private LocalDateTime horaEntrada;
    private LocalDateTime horaSalida;      // null = vehículo dentro
    private String        placaVehiculo;
    private int           idCelda;
    private int           idUsuario;

    /**
     * Calcula el tiempo de permanencia en minutos.

     * @return minutos entre entrada y salida,
     *         0 si el vehículo aún no ha salido
     */
    public long calcularMinutosPermanencia() {
        if (horaSalida == null) return 0;
        return Duration.between(horaEntrada, horaSalida).toMinutes();
    }
}