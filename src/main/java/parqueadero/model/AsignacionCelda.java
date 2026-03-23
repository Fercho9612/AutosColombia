package parqueadero.model;

import java.time.LocalDate;

/**
 * Modelo: AsignacionCelda
  * Representa la asignación de una celda fija
  * Regla de negocio:
 *   - activa=true  → celda reservada para el usuario
 *   - activa=false → celda liberada (historial conservado)


 * Se implementan constructor manual + getters/setters.
 */
public class AsignacionCelda {

    // ── Iteración 2 ──────────────────────────────
    private int       id;
    private int       idUsuario;        // FK → usuario.id
    private int       idCelda;          // FK → celda.id
    private LocalDate fechaAsignacion;
    private boolean   activa;           // false = liberada

    // ── Constructor vacío — requerido por el mapper ──
    public AsignacionCelda() {}

    // ── Constructor completo ─────────────────────
    @SuppressWarnings("unused")
    public AsignacionCelda(int id, int idUsuario, int idCelda,
                           LocalDate fechaAsignacion, boolean activa) {
        this.id              = id;
        this.idUsuario       = idUsuario;
        this.idCelda         = idCelda;
        this.fechaAsignacion = fechaAsignacion;
        this.activa          = activa;
    }

    // ── Getters ──────────────────────────────────
    public int       getId()               { return id; }
    public int       getIdUsuario()        { return idUsuario; }
    public int       getIdCelda()          { return idCelda; }
    public LocalDate getFechaAsignacion()  { return fechaAsignacion; }
    public boolean   isActiva()            { return activa; }

    // ── Setters ──────────────────────────────────
    public void setId              (int id)              { this.id = id; }
    public void setIdUsuario       (int idUsuario)       { this.idUsuario = idUsuario; }
    public void setIdCelda         (int idCelda)         { this.idCelda = idCelda; }
    public void setFechaAsignacion (LocalDate f)         { this.fechaAsignacion = f; }
    public void setActiva          (boolean activa)      { this.activa = activa; }

    @Override
    public String toString() {
        return "AsignacionCelda{" +
                "id=" + id +
                ", idUsuario=" + idUsuario +
                ", idCelda=" + idCelda +
                ", fechaAsignacion=" + fechaAsignacion +
                ", activa=" + activa + '}';
    }
}