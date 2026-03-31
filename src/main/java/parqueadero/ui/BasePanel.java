package parqueadero.ui;

import parqueadero.service.ParqueaderoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Clase base para todos los paneles principales de la aplicación.
 * Contiene configuración común y métodos de utilidad.
 */
public class BasePanel extends JPanel {

    protected final ParqueaderoService service;
    protected final CardLayout cardLayout;
    protected final JPanel panelContenedor;

    // --- CONFIGURACIÓN DE FUENTES (Centralizada) ---
    protected final String FONT_NAME = "Segoe UI"; // Fuente moderna
    protected final Font FUENTE_TITULO = new Font(FONT_NAME, Font.BOLD, 24);
    protected final Font FUENTE_NORMAL = new Font(FONT_NAME, Font.PLAIN, 14);
    protected final Font FUENTE_BOLD   = new Font(FONT_NAME, Font.BOLD, 13);
    protected final Font FUENTE_PEQUENA = new Font(FONT_NAME, Font.PLAIN, 12);
    protected final Color GRIS_FONDO = new Color(245, 245, 245);

    // --- COLORES ---
    protected final Color AZUL_BTN = new Color(45, 55, 125);
    protected final Color GRIS_BORDE = new Color(210, 210, 210);
    protected final Color GRIS_TEXTO = new Color(70, 70, 70);
    protected final Color VERDE_DISPONIBLE = new Color(60, 180, 80);
    protected final Color ROJO_OCUPADO = new Color(220, 60, 60);
    protected final Color FONDO_TABLA_HEAD = new Color(240, 242, 245);

    public BasePanel(ParqueaderoService service, CardLayout cardLayout, JPanel panelContenedor) {
        this.service = service;
        this.cardLayout = cardLayout;
        this.panelContenedor = panelContenedor;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(25, 35, 25, 35));
    }

    /**
     * Crea una tabla con diseño moderno y estandarizado.
     */
    protected JTable crearTablaEstilizada(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setFont(FUENTE_NORMAL);
        tabla.setRowHeight(35);
        tabla.setSelectionBackground(new Color(230, 240, 255));
        tabla.setSelectionForeground(Color.BLACK);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(240, 240, 240));

        // Estilo del encabezado
        JTableHeader header = tabla.getTableHeader();
        header.setFont(FUENTE_BOLD);
        header.setBackground(FONDO_TABLA_HEAD);
        header.setForeground(AZUL_BTN);
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, AZUL_BTN));

        // Alineación centrada por defecto
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tabla.setDefaultRenderer(Object.class, centerRenderer);

        return tabla;
    }

    /**
     * Crea un contenedor para la barra de búsqueda (Label + TextField + Botón)
     */
    protected JPanel crearBarraBusqueda(JTextField txtBusqueda, JButton btnAccion) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setBackground(Color.WHITE);

        txtBusqueda.setPreferredSize(new Dimension(250, 35));

        panel.add(etiqueta("Buscar:"));
        panel.add(txtBusqueda);
        panel.add(btnAccion);

        return panel;
    }

    /**
     * Título principal para cada panel
     */
    protected JLabel crearTitulo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FUENTE_TITULO);
        lbl.setForeground(AZUL_BTN);
        lbl.setBorder(new EmptyBorder(0, 0, 20, 0));
        return lbl;
    }

    // ====================== MÉTODOS DE UTILIDAD ======================

    protected JTextField campo() {
        JTextField txt = new JTextField();
        txt.setPreferredSize(new Dimension(200, 35));
        txt.setFont(FUENTE_NORMAL);
        txt.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return txt;
    }

    protected JLabel labelObligatorio(String texto) {
        JLabel lbl = new JLabel("<html>" + texto + " <font color='red'>*</font></html>");
        lbl.setFont(FUENTE_BOLD);
        lbl.setForeground(new Color(60, 60, 60));
        return lbl;
    }

    protected JLabel labelOpcional(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FUENTE_BOLD);
        lbl.setForeground(new Color(100, 100, 100));
        return lbl;
    }

    protected JLabel etiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FUENTE_BOLD);
        lbl.setForeground(GRIS_TEXTO);
        return lbl;
    }

    protected GridBagConstraints pos(int x, int y, GridBagConstraints gbc) {
        gbc.gridx = x;
        gbc.gridy = y;
        return gbc;
    }

    protected JButton botonPrimario(String texto, int ancho) {
        JButton btn = new JButton(texto);

        btn.setBackground(AZUL_BTN);
        btn.setForeground(Color.WHITE);
        btn.setFont(FUENTE_BOLD);
        btn.setPreferredSize(new Dimension(ancho, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        if (ancho > 0) btn.setPreferredSize(new Dimension(ancho, 40));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(AZUL_BTN.brighter()); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(AZUL_BTN); }
        });
        return btn;
    }

    protected JButton botonSecundario(String texto, int ancho) {
        JButton btn = new JButton(texto);
        btn.setFont(FUENTE_BOLD);
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
     * Método genérico privado para no repetir el código del for en cada refresco.
     */
    protected   <T> T buscarComponente(Container contenedor, Class<T> clase) {
        for (Component c : contenedor.getComponents()) {
            if (clase.isInstance(c)) {
                return clase.cast(c);
            }
            if (c instanceof Container) {
                T encontrado = buscarComponente((Container) c, clase);
                if (encontrado != null) return encontrado;
            }
        }
        return null;
    }
    /**
     * Refresca PanelEntradas si existe
     */
    protected void refrescarPanelEntradas() {
        // Agregamos 'panelContenedor' como primer argumento para que coincida con la firma del método
        PanelEntradas p = buscarComponente(panelContenedor, PanelEntradas.class);
        if (p != null) {
            p.actualizarTabla();
        }
    }

    /**
     * Refresca PanelCeldas si existe
     */
    protected void refrescarPanelCeldas() {
        PanelCeldas p = buscarComponente(panelContenedor, PanelCeldas.class);
        if (p != null) {
            p.actualizarMapa();
        }
    }

    /**
     * Refresca TODOS los paneles importantes del sistema.
     * Úsalo después de registrar, cobrar o asignar celdas.
     */
    protected void refrescarOtrosPaneles() {
        // 1. Refresca la tabla de Entradas
        PanelEntradas ent = buscarComponente(panelContenedor, PanelEntradas.class);
        if (ent != null) ent.actualizarTabla();

        // 2. Refresca el mapa visual de celdas
        PanelCeldas cel = buscarComponente(panelContenedor, PanelCeldas.class);
        if (cel != null) cel.actualizarMapa();

        // 3. 🔥 NUEVO: Refresca el combo de Pagos (Placas activas)
        PanelPagos pag = buscarComponente(panelContenedor, PanelPagos.class);
        if (pag != null) pag.actualizarPlacasActivas();

        // 4. 🔥 NUEVO: Refresca el combo de Gestión de Celdas (Usuarios para asignar)
        PanelConsultaCeldas con = buscarComponente(panelContenedor, PanelConsultaCeldas.class);
        if (con != null) con.actualizarCombos();
    }

}