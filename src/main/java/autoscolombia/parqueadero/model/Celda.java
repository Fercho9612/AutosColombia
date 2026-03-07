package autoscolombia.parqueadero.model;

import lombok.Data;

@Data
public class Celda {
    private int id;
    private String tipo;
    private boolean disponible = true;
}