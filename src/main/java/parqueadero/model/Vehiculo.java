package parqueadero.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un vehículo que ha ingresado
 *  * El service aplica trim().toUpperCase() antes
 * de persistir — siempre en mayúsculas.
 */
@Data              // Genera: Getters, Setters, toString, equals y hashCode.
@NoArgsConstructor // Requerido para frameworks de persistencia (JDBC/JPA).
@AllArgsConstructor// Constructor para crear vehículos con todos sus datos.
@Builder           // Facilita la creación: Vehiculo.builder().placa("ABC123").build().
public class Vehiculo {

    public enum TipoVehiculo { CARRO, MOTO }

    private String placa;       // PK en la base de datos.
    private TipoVehiculo tipo;  // ENUM para restringir a CARRO o MOTO.
}