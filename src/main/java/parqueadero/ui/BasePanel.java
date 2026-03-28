package parqueadero.ui;

import parqueadero.service.ParqueaderoService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Clase base para todos los paneles principales de la aplicación.
 * Contiene configuración común y métodos de utilidad.
 */
public class BasePanel extends JPanel {

    protected final ParqueaderoService service;
    protected final CardLayout cardLayout;
    protected final JPanel panelContenedor;

    // Colores comunes
    protected final Color AZUL_OSCURO = new Color(45, 55, 125);
    protected final Color AZUL_BOTON = new Color(45, 55, 125);
    protected final Color GRIS_BORDE = new Color(220, 220, 220);
    protected final Color GRIS_FONDO = new Color(245, 245, 245);
    protected final Color VERDE_DISPONIBLE = new Color(60, 180, 80);
    protected final Color ROJO_OCUPADO = new Color(220, 60, 60);

    public BasePanel(ParqueaderoService service, CardLayout cardLayout, JPanel panelContenedor) {
        this.service = service;
        this.cardLayout = cardLayout;
        this.panelContenedor = panelContenedor;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(25, 35, 25, 35));
    }

    // ====================== MÉTODOS DE UTILIDAD ======================

    protected JTextField campo() {
        JTextField txt = new JTextField();
        txt.setPreferredSize(new Dimension(0, 35));
        txt.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return txt;
    }

    protected JLabel labelObligatorio(String texto) {
        JLabel lbl = new JLabel("<html>" + texto + " <font color='red'>*</font></html>");
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(new Color(60, 60, 60));
        return lbl;
    }

    protected JLabel labelOpcional(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 100, 100));
        return lbl;
    }

    protected JLabel etiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    protected GridBagConstraints pos(int x, int y, GridBagConstraints gbc) {
        gbc.gridx = x;
        gbc.gridy = y;
        return gbc;
    }

    protected JButton botonPrimario(String texto, int ancho) {
        JButton btn = new JButton(texto);
        btn.setBackground(AZUL_OSCURO);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(ancho, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    protected JButton botonSecundario(String texto, int ancho) {
        JButton btn = new JButton(texto);
        btn.setPreferredSize(new Dimension(ancho, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    protected JPanel crearPanelFormulario() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(GRIS_BORDE, 1, true),
                new EmptyBorder(15, 20, 15, 20)
        ));
        return form;
    }

    /**
     * Método opcional para construir UI (útil en PanelRegistro)
     */
    protected void inicializarUI() {
        // Por defecto no hace nada
    }

    // ====================== MÉTODOS DE REFRESCO ======================

    /**
     * Refresca PanelEntradas si existe
     */
    protected void refrescarPanelEntradas() {
        for (Component c : panelContenedor.getComponents()) {
            if (c instanceof PanelEntradas) {
                ((PanelEntradas) c).actualizarTabla();
                break;
            }
        }
    }

    /**
     * Refresca PanelCeldas si existe
     */
    protected void refrescarPanelCeldas() {
        for (Component c : panelContenedor.getComponents()) {
            if (c instanceof PanelCeldas) {
                ((PanelCeldas) c).actualizarMapa();
                break;
            }
        }
    }

    /**
     * Refresca tanto Entradas como Celdas (útil en PanelSalidas)
     */
    protected void refrescarOtrosPaneles() {
        refrescarPanelEntradas();
        refrescarPanelCeldas();
    }

}