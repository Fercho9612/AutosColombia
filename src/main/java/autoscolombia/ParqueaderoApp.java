
/**
 * Clase principal (entry point) de la aplicación Parqueadero Autos Colombia.
 * Cumple con RF01 (autenticación inicial) y lanza la interfaz gráfica de forma segura en el EDT.
 *
 * @author Ferney Rodrigo Marin Pai (y equipo)
 * @version 1.0 - Iteración 1
*/
package autoscolombia;

import autoscolombia.parqueadero.ui.LoginFrame;

import javax.swing.*;

public class ParqueaderoApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}