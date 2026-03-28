package parqueadero.ui;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Filtro avanzado para JTextField.
 * Controla longitud, tipo de dato (numérico/decimal) y formato (MAYÚSCULAS).
 */
public class ValidadorCampo extends DocumentFilter {
    private final int limite;
    private final boolean soloNumeros;
    private final boolean aMayusculas;
    private final boolean permitirPunto;
    private final JTextField campoAsociado;

    public ValidadorCampo(int limite, boolean soloNumeros, boolean aMayusculas, boolean permitirPunto, JTextField campoAsociado) {
        this.limite = limite;
        this.soloNumeros = soloNumeros;
        this.aMayusculas = aMayusculas;
        this.permitirPunto = permitirPunto;
        this.campoAsociado = campoAsociado;
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {

        if (text == null) return;

        // 1. Transformación a Mayúsculas
        if (aMayusculas) {
            text = text.toUpperCase();
        }

        // 2. Filtrado de Caracteres Prohibidos
        String filtrado = text;
        if (soloNumeros) {
            if (permitirPunto) {
                filtrado = text.replaceAll("[^0-9.]", "");
            } else {
                filtrado = text.replaceAll("[^0-9]", "");
            }

            // Si el texto cambió tras el filtro, avisamos al usuario
            if (!filtrado.equals(text)) {
                mostrarAviso("Solo se permiten números" + (permitirPunto ? " y puntos." : "."));
            }
        }

        // 3. Verificación de Límite de Longitud
        int longitudActual = fb.getDocument().getLength();
        int longitudNueva = longitudActual - length + filtrado.length();

        if (longitudNueva <= limite) {
            super.replace(fb, offset, length, filtrado, attrs);
        } else {
            // Si el texto es muy largo (ej. al pegar), cortamos lo que quepa
            int espacioDisponible = limite - (longitudActual - length);
            if (espacioDisponible > 0) {
                String textoCortado = filtrado.substring(0, espacioDisponible);
                super.replace(fb, offset, length, textoCortado, attrs);
            }
            mostrarAvisoLimite();
        }
    }

    private void mostrarAviso(String mensaje) {
        if (campoAsociado != null) {
            campoAsociado.setToolTipText(mensaje);
            // Truco para forzar la aparición del ToolTip
            MouseEvent me = new MouseEvent(campoAsociado, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, 0, 0, 0, false);
            ToolTipManager.sharedInstance().mouseMoved(me);
        }
    }

    private void mostrarAvisoLimite() {
        if (campoAsociado != null) {
            Toolkit.getDefaultToolkit().beep();
            campoAsociado.setToolTipText("Máximo " + limite + " caracteres.");
        }
    }

    /**
     * Método de utilidad para aplicar este validador a un campo fácilmente.
     */
    public static void aplicar(JTextField campo, int limite, boolean soloNum, boolean mayus, boolean punto) {
        ((javax.swing.text.AbstractDocument) campo.getDocument())
                .setDocumentFilter(new ValidadorCampo(limite, soloNum, mayus, punto, campo));
    }
}