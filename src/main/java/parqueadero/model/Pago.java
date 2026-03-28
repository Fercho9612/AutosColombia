package parqueadero.model;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {
    // Coincide con el ENUM('CARRO','MOTO') de tu SQL
    public enum TipoVehiculo { CARRO, MOTO }

    private int id;
    private TipoVehiculo tipoVehiculo;
    private BigDecimal precioPorMinuto; // Mapea a 'precio_por_minuto' en SQL
    private String descripcion;
    private boolean activo;
}