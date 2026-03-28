package parqueadero.model;

import lombok.*;

/**
 * Entidad Celda: Representa un espacio físico en el parqueadero.
 */
@Data              // Automatiza el código repetitivo (Boilerplate).
@NoArgsConstructor // Constructor vacío esencial.
@AllArgsConstructor// Constructor total.
@Builder           // Permite construir celdas rápidamente en pruebas.
public class Celda {

    public enum TipoCelda { CARRO, MOTO }

    private int id;             // Identificador autoincremental.
    private TipoCelda tipo;     // Define si la celda es para carro o moto.
    private boolean disponible;


    public Celda(int id, String tipo, boolean disponible) {
        this.id = id;
        this.tipo = TipoCelda.valueOf(tipo.toUpperCase());
        this.disponible = disponible;
    }

    public int getId() { return id; }
    public TipoCelda getTipo() {
        return tipo;
    }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

}