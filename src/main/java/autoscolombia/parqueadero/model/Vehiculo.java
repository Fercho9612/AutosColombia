package autoscolombia.parqueadero.model;

import lombok.Data;

@Data
public class Vehiculo {
    private String placa;
    private String tipo; // "CARRO" o "MOTO"
}
