package parqueadero.ui;

import parqueadero.model.Usuario;
import parqueadero.service.ParqueaderoService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.List;

/**
 * Pantalla principal para la gestión de usuarios del parqueadero.
 * * Permite visualizar una lista de clientes, realizar búsquedas en tiempo real,
 * filtrar por tipo o estado, y acceder a la edición de cada registro.
 * * @author TuNombre
 */
public class PanelEntradas extends JPanel {

    private final ParqueaderoService service;
    private final CardLayout cardLayout;
    private final JPanel panelContenedor;

    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField txtBuscar;
    private JComboBox<String> cbTipo, cbEstado;
    private JLabel lblActivosValor, lblTotalValor;
    private final Color AZUL_BOTON = new Color(45, 55, 125);

    /**
     * Crea el panel de administración de usuarios.
     * Configura la tabla, los filtros de búsqueda y los contadores estadísticos.
     * * @param service Servicio que conecta con la base de datos.
     * @param cardLayout Gestor para cambiar entre pantallas.
     * @param panelContenedor Panel principal que contiene todas las vistas.
     */
    public PanelEntradas(ParqueaderoService service, CardLayout cardLayout, JPanel panelContenedor) {
        this.service = service;
        this.cardLayout = cardLayout;
        this.panelContenedor = panelContenedor;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // =======================================
        //          NORTE: Título + Contadores
        // =======================================
        JPanel panelNorte = new JPanel(new BorderLayout(0, 10));
        panelNorte.setOpaque(false);

        JLabel lblRuta = new JLabel("CONSULTAR / EDITAR USUARIOS");
        lblRuta.setFont(new Font("Arial", Font.BOLD, 14));
        panelNorte.add(lblRuta, BorderLayout.WEST);

        JPanel panelConteos = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 0));
        panelConteos.setOpaque(false);
        panelConteos.add(crearIndicador("ACTIVOS", "0", true));
        panelConteos.add(crearIndicador("TOTAL USUARIOS", "0", false));
        panelNorte.add(panelConteos, BorderLayout.EAST);

        add(panelNorte, BorderLayout.NORTH);

        // =======================================
        //       FILTROS + BOTÓN NUEVO
        // =======================================
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        panelFiltros.setOpaque(false);

        JLabel lblUsuariosActivos = new JLabel("Usuarios activos");
        lblUsuariosActivos.setFont(new Font("Arial", Font.BOLD, 16));
        panelFiltros.add(lblUsuariosActivos);

        // Búsqueda
        JPanel pBusqueda = new JPanel(new BorderLayout(0, 0));
        pBusqueda.setPreferredSize(new Dimension(220, 35));
        pBusqueda.setBorder(new LineBorder(new Color(180, 180, 180)));
        pBusqueda.setBackground(Color.WHITE);

        txtBuscar = new JTextField();
        txtBuscar.setBorder(null);
        pBusqueda.add(new JLabel("  🔍  "), BorderLayout.WEST);
        pBusqueda.add(txtBuscar, BorderLayout.CENTER);

        panelFiltros.add(pBusqueda);

        // Filtro Tipo
        cbTipo = new JComboBox<>(new String[]{"Tipo", "Mensual", "Visitante"});
        cbTipo.setPreferredSize(new Dimension(120, 35));
        panelFiltros.add(cbTipo);

        // Filtro Estado
        cbEstado = new JComboBox<>(new String[]{"Estado", "Activo", "Inactivo"});
        cbEstado.setPreferredSize(new Dimension(120, 35));
        panelFiltros.add(cbEstado);

        // Botón Nuevo
        JButton btnNuevo = new JButton("Nuevo usuario");
        btnNuevo.setBackground(AZUL_BOTON);
        btnNuevo.setForeground(Color.WHITE);
        btnNuevo.setPreferredSize(new Dimension(140, 35));
        btnNuevo.addActionListener(e -> cardLayout.show(panelContenedor, "PANTALLA_REGISTRO"));
        panelFiltros.add(btnNuevo);

        add(panelFiltros, BorderLayout.CENTER);  // temporal, luego lo movemos

        // =======================================
        //               TABLA
        // =======================================
        String[] columnas = {"USUARIO", "IDENTIFICACIÓN", "TIPO", "TELÉFONO", "PLACA / VEHÍCULO", "ESTADO", "EDITAR"};
        modelo = new DefaultTableModel(columnas, 0);
        tabla = new JTable(modelo);
        tabla.setRowHeight(38);
        tabla.setShowGrid(true);
        tabla.setGridColor(new Color(220, 220, 220));

        // Renderizado de estado (verde / rojo)
        tabla.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String estado = (String) value;
                lbl.setForeground("Activo".equals(estado) ? new Color(0, 140, 0) : new Color(200, 0, 0));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                return lbl;
            }
        });

        // Columna EDITAR → botón funcional
        tabla.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        tabla.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(new LineBorder(new Color(200, 200, 200)));

        JPanel contenedorTabla = new JPanel(new BorderLayout());
        contenedorTabla.setBackground(new Color(245, 245, 245));
        contenedorTabla.add(scroll, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout(0, 15));
        wrapper.add(panelFiltros, BorderLayout.NORTH);
        wrapper.add(contenedorTabla, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);

        // =======================================
        //          EVENTOS DE ACTUALIZACIÓN
        // =======================================
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                actualizarTabla();
            }
        });

        cbTipo.addActionListener(e -> actualizarTabla());
        cbEstado.addActionListener(e -> actualizarTabla());

        // Carga inicial
        actualizarTabla();
        actualizarContadores();
    }
    /**
     * Recarga la tabla de usuarios aplicando los filtros de búsqueda, tipo y estado.
     * Se activa automáticamente al escribir en el buscador o cambiar un filtro.
     */
    protected void actualizarTabla() {
        modelo.setRowCount(0);

        String busqueda = txtBuscar.getText().trim().toLowerCase();
        String tipoSel = cbTipo.getSelectedItem().toString();
        String estadoSel = cbEstado.getSelectedItem().toString();

        boolean filtroTipo = !tipoSel.equals("Tipo");
        boolean filtroEstado = !estadoSel.equals("Estado");

        try {
            List<Usuario> usuarios = service.buscarUsuarios(busqueda.isEmpty() ? "" : busqueda);

            for (Usuario u : usuarios) {
                boolean pasaTipo = !filtroTipo || tipoSel.equalsIgnoreCase(u.getTipoCliente());
                boolean pasaEstado = !filtroEstado || estadoSel.equalsIgnoreCase(u.isActivo() ? "Activo" : "Inactivo");

                if (pasaTipo && pasaEstado) {
                    String placa = obtenerPlacaPrincipal(u); // Implementar lógica real
                    modelo.addRow(new Object[]{
                            u.getNombre(),
                            u.getDocumento(),
                            u.getTipoCliente(),
                            u.getTelefono() != null ? u.getTelefono() : "-",
                            placa != null ? placa : "Sin vehículo",
                            u.isActivo() ? "Activo" : "Inactivo",
                            "Editar"
                    });
                }
            }

            actualizarContadores();
            tabla.repaint();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar usuarios");
        }
    }
    /**
     * Consulta el total de usuarios y cuántos están activos para actualizar
     * los cuadros de estadísticas en la parte superior.
     */
    private void actualizarContadores() {
        try {
            List<Usuario> todos = service.buscarUsuarios("");
            int total = todos.size();
            int activos = (int) todos.stream().filter(Usuario::isActivo).count();

            lblActivosValor.setText(String.valueOf(activos));
            lblTotalValor.setText(String.valueOf(total));
        } catch (Exception e) {
            lblActivosValor.setText("?");
            lblTotalValor.setText("?");
        }
    }
    /**
     * Obtiene la placa del vehículo asociado a un usuario específico.
     * @param u El usuario a consultar.
     * @return La placa del vehículo o "Sin vehículo" si no tiene.
     */
    private String obtenerPlacaPrincipal(Usuario u) {
        try {
            // En lugar de crear un new UsuarioDAO(), usamos el service
            return service.obtenerPlacaPorUsuario(u.getId());
        } catch (Exception e) {
            System.err.println("Error placa: " + e.getMessage());
            return "Sin vehículo";
        }
    }

    /**
     * Crea un componente visual para mostrar estadísticas (contadores).
     * @param titulo Nombre del indicador (ej: "ACTIVOS").
     * @param valorInicial Número a mostrar.
     * @param esActivo Si es true, aplica un estilo verde; si es false, gris.
     * @return Un panel con el diseño del contador.
     */
    private JPanel crearIndicador(String titulo, String valorInicial, boolean esActivo) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);

        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.PLAIN, 12));

        JLabel lblValor = new JLabel(valorInicial, SwingConstants.CENTER);
        lblValor.setFont(new Font("Arial", Font.BOLD, 22));
        lblValor.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        lblValor.setPreferredSize(new Dimension(80, 45));
        lblValor.setOpaque(true);
        lblValor.setBackground(esActivo ? new Color(220, 255, 220) : new Color(240, 240, 240));

        p.add(lblTitulo, BorderLayout.NORTH);
        p.add(lblValor, BorderLayout.CENTER);

        if (esActivo) lblActivosValor = lblValor;
        else lblTotalValor = lblValor;

        return p;
    }

    // =======================================
    //        BOTÓN EDITAR POR FILA
    /**
     * Clase interna para dibujar el botón "Editar" dentro de las celdas de la tabla.
     */
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setText("Editar");
            setBackground(AZUL_BOTON);
            setForeground(Color.WHITE);
            setFocusPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }
    /**
     * Clase interna que gestiona el clic en el botón "Editar" de la tabla.
     * Al pulsar, busca al usuario seleccionado y lo carga en la pantalla de edición.
     */
    class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private String docSeleccionado;
        private String nombreSeleccionado;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener((ActionEvent e) -> {
                fireEditingStopped();
                editarUsuarioSeleccionado();
            });
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value,
                boolean isSelected, int row, int column) {

            // Guardar datos ANTES de fireEditingStopped()
            // porque después getEditingRow() retorna -1
            docSeleccionado = (String) table.getValueAt(row, 1);
            nombreSeleccionado = (String) table.getValueAt(row, 0);

            button.setText("Editar");
            button.setBackground(AZUL_BOTON);
            button.setForeground(Color.WHITE);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            isPushed = false;
            return "Editar";
        }

        /**
         * Realiza la búsqueda del usuario por documento o nombre y cambia
         * la vista hacia la pantalla de edición (PanelSalidas).
         */
        private void editarUsuarioSeleccionado() {
            // Usar documento si existe, sino usar nombre
            String criterio =
                    (docSeleccionado != null &&
                            !docSeleccionado.trim().isEmpty())
                            ? docSeleccionado.trim()
                            : (nombreSeleccionado != null
                            ? nombreSeleccionado.trim() : "");

            if (criterio.isEmpty()) return;

            try {
                List<Usuario> resultados =
                        service.buscarUsuarios(criterio);

                if (!resultados.isEmpty()) {
                    Usuario u = resultados.get(0);

                    // Cargar usuario en PanelSalidas
                    for (java.awt.Component c :
                            panelContenedor.getComponents()) {
                        if (c instanceof PanelSalidas) {
                            ((PanelSalidas) c).cargarUsuario(u);
                            break;
                        }
                    }
                    cardLayout.show(
                            panelContenedor, "PANTALLA_SALIDA");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        PanelEntradas.this,
                        "Error al cargar usuario: "
                                + ex.getMessage());
            }
        }
    }


}
