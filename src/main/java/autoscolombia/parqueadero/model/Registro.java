package autoscolombia.parqueadero.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.Duration;

@Data
public class Registro {
    private int id;
    private LocalDateTime horaEntrada;
    private LocalDateTime horaSalida;
    private String placaVehiculo;
    private int idCelda;
    private int idUsuario;

    public long calcularMinutosPermanencia() {
        if (horaSalida == null) return 0;
        return Duration.between(horaEntrada, horaSalida).toMinutes();
    }
}