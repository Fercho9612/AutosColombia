package parqueadero;

import parqueadero.ui.LoginFrame;

import javax.swing.*;

/**
 * Punto de entrada principal de la aplicación Parqueadero Autos Colombia.
 * Inicia la interfaz gráfica
 * mostrando la pantalla de acceso (Login).
 * * @author Ferney Rodrigo Marin Pai (y equipo)
 */ 

public class ParqueaderoApp {
    /**
     * Lanza la aplicación de forma segura dentro del hilo de eventos de Swing (EDT).
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
