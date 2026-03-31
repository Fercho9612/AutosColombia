package parqueadero.ui;

import parqueadero.model.Usuario;
import parqueadero.service.ParqueaderoService;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Pantalla principal para consultar, filtrar y editar usuarios.
 */
public class PanelEntradas extends BasePanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField txtBuscar;
    private JComboBox<String> cbTipo, cbEstado;
    private JLabel lblActivosValor, lblTotalValor;

    private final Color AZUL_BOTON = new Color(45, 55, 125);
    private final Color VERDE_ACTIVO = new Color(220, 255, 220);

    public PanelEntradas(ParqueaderoService service, CardLayout cardLayout, JPanel panelContenedor) {
        super(service, cardLayout, panelContenedor);   // ← Hereda configuración común

        // Estructura superior: Título + Contadores
        add(crearPanelNorte(), BorderLayout.NORTH);

        // Centro: Filtros + Tabla
        add(construirCuerpoCentral(), BorderLayout.CENTER);

        // Eventos de filtrado en tiempo real
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                actualizarTabla();
            }
        });
        cbTipo.addActionListener(e -> actualizarTabla());
        cbEstado.addActionListener(e -> actualizarTabla());

        // Carga inicial de datos
        actualizarTabla();
    }

    /** Panel superior con título y contadores */
    private JPanel crearPanelNorte() {
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setOpaque(false);

        JLabel lblRuta = new JLabel("CONSULTAR / EDITAR USUARIOS");
        lblRuta.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel panelConteos = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 0));
        panelConteos.setOpaque(false);
        panelConteos.add(crearIndicador("ACTIVOS", "0", true));
        panelConteos.add(crearIndicador("TOTAL USUARIOS", "0", false));

        panelNorte.add(lblRuta, BorderLayout.WEST);
        panelNorte.add(panelConteos, BorderLayout.EAST);

        return panelNorte;
    }

    /** Construye la sección central: filtros + tabla */
    private JPanel construirCuerpoCentral() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);

        // ==================== PANEL DE FILTROS ====================
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        panelFiltros.setOpaque(false);

        // Buscador con ícono
        JPanel pBusqueda = new JPanel(new BorderLayout());
        pBusqueda.setPreferredSize(new Dimension(220, 35));
        pBusqueda.setBackground(Color.WHITE);
        pBusqueda.setBorder(new LineBorder(new Color(180, 180, 180)));

        txtBuscar = new JTextField();
        txtBuscar.setBorder(null);
        pBusqueda.add(new JLabel("  🔍  "), BorderLayout.WEST);
        pBusqueda.add(txtBuscar, BorderLayout.CENTER);

        // Filtros
        cbTipo = new JComboBox<>(new String[]{"Tipo", "Mensual", "Visitante"});
        cbEstado = new JComboBox<>(new String[]{"Estado", "Activo", "Inactivo"});

        cbTipo.setPreferredSize(new Dimension(120, 35));
        cbEstado.setPreferredSize(new Dimension(120, 35));

        // Botón Nuevo (usando método de BasePanel)
        JButton btnNuevo = botonPrimario("Nuevo usuario", 140);
        btnNuevo.addActionListener(e -> cardLayout.show(panelContenedor, "PANTALLA_REGISTRO"));

        panelFiltros.add(pBusqueda);
        panelFiltros.add(cbTipo);
        panelFiltros.add(cbEstado);
        panelFiltros.add(btnNuevo);

        // ==================== TABLA ====================
        String[] columnas = {"USUARIO", "DOCUMENTO", "TIPO", "TELÉFONO", "PLACA", "ESTADO", "ACCION"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 6; // Solo columna de acción
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(38);
        tabla.setShowGrid(true);
        tabla.setGridColor(new Color(220, 220, 220));
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Renderizador de estado (verde / rojo)
        tabla.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setForeground("Activo".equals(v) ? new Color(0, 150, 0) : Color.RED);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                return lbl;
            }
        });

        // Botón Editar en la tabla
        tabla.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        tabla.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(new LineBorder(new Color(200, 200, 200)));

        wrapper.add(panelFiltros, BorderLayout.NORTH);
        wrapper.add(scroll, BorderLayout.CENTER);

        return wrapper;
    }

    /**
     * Actualiza la tabla según los filtros de búsqueda, tipo y estado.
     */
    public void actualizarTabla() {
        modelo.setRowCount(0);
        String busqueda = txtBuscar.getText().trim().toLowerCase();
        String tipoSel = cbTipo.getSelectedItem().toString(); // "Tipo", "Mensual", "Visitante"
        String estadoSel = cbEstado.getSelectedItem().toString(); // "Estado", "Activo", "Inactivo"

        try {
            // Traemos la lista desde el service (que ya trae a todos del DAO)
            List<Usuario> lista = service.buscarUsuarios("");
            int activos = 0;

            for (Usuario u : lista) {
                // Lógica de coincidencia de Tipo
                boolean matchTipo = tipoSel.equals("Tipo") ||
                        tipoSel.equalsIgnoreCase(String.valueOf(u.getTipoCliente()));

                // Lógica de coincidencia de Estado (ACTUALIZADA)
                boolean matchEstado = estadoSel.equals("Estado") ||
                        estadoSel.equalsIgnoreCase(u.isActivo() ? "Activo" : "Inactivo");
                boolean coincide = busqueda.isEmpty() ||
                        u.getNombre().toLowerCase().contains(busqueda) ||
                        u.getDocumento().contains(busqueda);

                if (matchTipo && matchEstado && coincide) {
                    if (u.isActivo()) activos++;

                    String placa = service.obtenerPlacaPorUsuario(u.getId());

                    modelo.addRow(new Object[]{
                            u.getNombre(),
                            u.getDocumento(),
                            u.getTipoCliente(),
                            u.getTelefono() != null ? u.getTelefono() : "---",
                            placa != null ? placa : "---",
                            u.isActivo() ? "Activo" : "Inactivo", // Mostrará el texto según el booleano
                            "Editar"
                    });
                }
            }

            // Actualizar los labels de conteo en la parte superior
            lblActivosValor.setText(String.valueOf(activos));
            lblTotalValor.setText(String.valueOf(lista.size()));
            tabla.revalidate();
            tabla.repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Crea un indicador visual de estadísticas (Activos / Total)
     */
    private JPanel crearIndicador(String titulo, String valorInicial, boolean esActivo) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);

        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.PLAIN, 11));

        JLabel valor = new JLabel(valorInicial, SwingConstants.CENTER);
        valor.setFont(new Font("Arial", Font.BOLD, 22));
        valor.setBorder(new LineBorder(new Color(200, 200, 200)));
        valor.setPreferredSize(new Dimension(85, 45));
        valor.setOpaque(true);
        valor.setBackground(esActivo ? VERDE_ACTIVO : new Color(245, 245, 245));

        if (esActivo) lblActivosValor = valor;
        else lblTotalValor = valor;

        p.add(lblTitulo, BorderLayout.NORTH);
        p.add(valor, BorderLayout.CENTER);
        return p;
    }

    // ====================== CLASES INTERNAS PARA BOTÓN EN TABLA ======================

    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setText("Editar");
            setBackground(AZUL_BOTON);
            setForeground(Color.WHITE);
            setFocusPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private String docId;

        public ButtonEditor(JCheckBox cb) {
            super(cb);
            JButton btn = new JButton("Editar");
            btn.setBackground(AZUL_BOTON);
            btn.setForeground(Color.WHITE);
            btn.addActionListener(e -> {
                fireEditingStopped();
                abrirEdicion();
            });
            this.editorComponent = btn;
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            docId = (String) t.getValueAt(r, 1); // Documento en columna 1
            return editorComponent;
        }

        private void abrirEdicion() {
            if (docId == null) return;
            try {
                List<Usuario> resultados = service.buscarUsuarios(docId);
                if (!resultados.isEmpty()) {
                    Usuario seleccionado = resultados.get(0);

                    for (Component c : panelContenedor.getComponents()) {
                        if (c instanceof PanelSalidas) {
                            PanelSalidas panelDestino = (PanelSalidas) c;
                            // 3. Pasamos el objeto al panel de edición
                            panelDestino.cargarUsuario(seleccionado);
                            // 4. Cambiamos la vista (Usa el nombre exacto que definiste en tu Main/Router)
                            cardLayout.show(panelContenedor, "PANTALLA_SALIDA");
                            Window w = SwingUtilities.getWindowAncestor(panelContenedor);
                            if (w instanceof DashboardFrame) {
                                // Usamos el método que creamos en el paso anterior
                                ((DashboardFrame) w).marcarBotonPorPantalla("PANTALLA_SALIDA");
                            }
                            return;

                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}