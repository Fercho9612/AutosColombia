package parqueadero.ui;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

/**
 * Filtro personalizado para restringir la entrada de datos en campos de texto.
 * Permite controlar el límite de caracteres, forzar mayúsculas y filtrar
 * números o puntos en tiempo real.
 */
public class ValidadorCampo extends DocumentFilter {
    private final int limite;
    private final boolean soloNumeros;
    private final boolean aMayusculas;
    private final boolean permitirPunto; // <--- NUEVO
    private final JTextField campoAsociado; // Para mostrar el mensaje de error

    /**
     * Configura las reglas de validación para un campo específico.
     * @param limite Máximo de caracteres permitidos.
     * @param soloNumeros Si es true, solo acepta dígitos.
     * @param aMayusculas Si es true, convierte el texto a mayúsculas automáticamente.
     * @param permitirPunto Si es true, permite el carácter '.' junto a los números.
     * @param campoAsociado El JTextField al que se aplica para mostrar alertas visuales.
     */
    public ValidadorCampo(int limite, boolean soloNumeros, boolean aMayusculas, boolean permitirPunto, JTextField campoAsociado) {
        this.limite = limite;
        this.soloNumeros = soloNumeros;
        this.aMayusculas = aMayusculas;
        this.permitirPunto = permitirPunto;
        this.campoAsociado = campoAsociado;
    }

    /**
     * Procesa y filtra el texto antes de que se inserte en el documento.
     * Aplica las reglas de formato y muestra avisos si se intenta ingresar caracteres inválidos.
     */
    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {

        if (text == null) return;

        String originalText = text;

        // 1. Manejo de Mayúsculas
        if (aMayusculas) text = text.toUpperCase();

        // 2. Manejo de Números y Puntos
        if (soloNumeros) {
            if (permitirPunto) {
                // Permite números (0-9) y el punto (.)
                text = text.replaceAll("[^0-9.]", "");
            } else {
                // Solo números
                text = text.replaceAll("[^0-9]", "");
            }

            // SI EL TEXTO CAMBIÓ, significa que el usuario intentó meter algo prohibido
            if (!text.equals(originalText)) {
                mostrarAviso();
            }
        }

        // 3. Manejo de Límite
        int total = fb.getDocument().getLength() + text.length() - length;
        if (total <= limite) {
            super.replace(fb, offset, length, text, attrs);
        } else {
            mostrarAvisoLimite();
        }
    }
    /**
     * Muestra un ToolTip informativo cuando el usuario ingresa caracteres no permitidos.
     */
    private void mostrarAviso() {
        if (campoAsociado != null) {
            campoAsociado.setToolTipText("Solo se permiten números" + (permitirPunto ? " y puntos." : "."));
            // Mostrar el tooltip inmediatamente
            ToolTipManager.sharedInstance().mouseMoved(
                    new java.awt.event.MouseEvent(campoAsociado, 0, 0, 0, 0, 0, 0, false)
            );
        }
    }
    /**
     * Notifica mediante un sonido y ToolTip cuando se alcanza el máximo de caracteres.
     */
    private void mostrarAvisoLimite() {
        if (campoAsociado != null) {
            campoAsociado.setToolTipText("Límite de " + limite + " caracteres alcanzado.");
            Toolkit.getDefaultToolkit().beep();
        }
    }
}