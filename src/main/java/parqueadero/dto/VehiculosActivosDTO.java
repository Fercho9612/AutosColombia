package parqueadero.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Mapea la vista SQL 'v_vehiculos_activos'.
 * Muestra información combinada de Registro, Celda y Usuario.
 */
@Data
public class VehiculosActivosDTO {
    private int idRegistro;
    private String placa;
    private int idCelda;
    private String tipoCelda;
    private LocalDateTime horaEntrada;
    private int minutosPermanencia; // Calculado por la base de datos
    private String nombreVigilante; // El que registró la entrada
}