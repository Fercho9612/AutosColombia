package parqueadero.ui;

import parqueadero.model.Celda;
import parqueadero.model.Usuario;
import parqueadero.service.ParqueaderoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel para la gestión de celdas refactorizado mediante herencia de BasePanel.
 */
public class PanelConsultaCeldas extends BasePanel {

    // Labels de resultados
    private JLabel lblIdValor, lblTipoValor, lblEstadoValor;
    private JLabel lblUsuarioValor, lblCeldaValor,
            lblEstadoAsigValor, lblCeldaAsigValor;

    private JTable tablaCeldas;
    private DefaultTableModel modeloTabla;
    // Componente izquierda (Registrar Celda)
    private JTextField txtIdentificador;
    private JButton btnCarro, btnMoto;
    private JComboBox<String> cbEstadoInicial;
    private JPanel pnlMensajeExito;
    private String tipoSeleccionado = "CARRO";

    // Componentes derecha (Asignar Celda)
    private JComboBox<String> cbUsuariosMensual;
    private JComboBox<String> cbCeldasDisponibles;
    private JPanel pnlAsignacionExito;

    // Colores específicos (Complementarios a BasePanel)
    private final Color VERDE_EXITO = new Color(40, 160, 80);
    private final Color AZUL_EXITO = new Color(45, 100, 200);

    public PanelConsultaCeldas(ParqueaderoService service, CardLayout cardLayout, JPanel panelContenedor) {
        super(service, cardLayout, panelContenedor);

        pnlMensajeExito = new JPanel();
        pnlAsignacionExito = new JPanel();

        setLayout(new GridBagLayout());
        setBackground(new Color(245, 247, 250));
        // El borde se hereda de BasePanel o se puede sobrescribir:
        setBorder(new EmptyBorder(30, 50, 30, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 15, 0, 15);

        gbc.gridx = 0;
        add(construirPanelIzquierdo(), gbc);

        gbc.gridx = 1;
        add(construirPanelDerecho(), gbc);
    }

    // ====================== PANEL IZQUIERDO ======================
    private JPanel construirPanelIzquierdo() {
        JPanel p = panelBase("Registrar nueva celda");
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        Dimension dimCampo = new Dimension(Integer.MAX_VALUE, 38);

        // ── Identificador
        JLabel lblId = etiqueta("Identificador (Numérico)*");
        lblId.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lblId);
        p.add(Box.createVerticalStrut(5));

        txtIdentificador = campo();

        txtIdentificador.setMaximumSize(dimCampo);
        txtIdentificador.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(txtIdentificador);
        p.add(Box.createVerticalStrut(15));

        // Botones Tipo
        JLabel lblTipo = etiqueta("Tipo de vehículo*");
        lblTipo.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(Box.createVerticalStrut(8));

        JPanel fTipo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fTipo.setOpaque(false);
        fTipo.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnCarro = crearBotonTipo("Carro", true);
        btnMoto = crearBotonTipo("Moto", false);

        btnCarro.addActionListener(e -> alternarTipo("CARRO"));
        btnMoto.addActionListener(e -> alternarTipo("MOTO"));

        fTipo.add(btnCarro);
        fTipo.add(btnMoto);
        p.add(fTipo);
        p.add(Box.createVerticalStrut(15));

        // ── Estado inicial
        p.add(etiqueta("Estado inicial"));
        p.add(Box.createVerticalStrut(5));

        cbEstadoInicial = new JComboBox<>(new String[]{"Disponible", "Ocupada"});
        cbEstadoInicial.setFont(FUENTE_NORMAL);
        cbEstadoInicial.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cbEstadoInicial.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(cbEstadoInicial);
        p.add(Box.createVerticalStrut(25));

        // ── Botón guardar
        JButton btnGuardar = botonPrimario("Guardar celda", 0); // Heredado
        btnGuardar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnGuardar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGuardar.addActionListener(e -> ejecutarRegistro());
        p.add(btnGuardar);
        p.add(Box.createVerticalStrut(15));

        // Panel de éxito
        pnlMensajeExito = new JPanel(new GridLayout(4, 2, 6, 6));
        pnlMensajeExito.setBackground(VERDE_EXITO);
        pnlMensajeExito.setBorder(BorderFactory.createCompoundBorder(new LineBorder(VERDE_EXITO.darker(), 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        pnlMensajeExito.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel lblCheck = new JLabel("✓  Celda registrada");
        lblCheck.setFont(FUENTE_BOLD);
        lblCheck.setForeground(Color.WHITE);
        pnlMensajeExito.add(lblCheck);
        pnlMensajeExito.add(new JLabel(""));

        pnlMensajeExito.add(etiquetaResultado("Identificador"));
        lblIdValor = valorResultado("");
        pnlMensajeExito.add(lblIdValor);

        pnlMensajeExito.add(etiquetaResultado("Tipo"));
        lblTipoValor = valorResultado("");
        pnlMensajeExito.add(lblTipoValor);

        pnlMensajeExito.add(etiquetaResultado("Estado"));
        lblEstadoValor = valorResultado("");
        pnlMensajeExito.add(lblEstadoValor);

        pnlMensajeExito.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(pnlMensajeExito);

        p.add(Box.createVerticalGlue());

        return p;
    }

    // ====================== PANEL DERECHO ======================
    private JPanel construirPanelDerecho() {
        JPanel p = panelBase("Asignar celda a usuario");
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        Dimension dimCampo = new Dimension(Integer.MAX_VALUE, 38);

        JLabel lbl1 = new JLabel("PASO 1. SELECCIONAR USUARIO");
        lbl1.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        lbl1.setForeground(new Color(100, 100, 100));
        p.add(lbl1);
        p.add(Box.createVerticalStrut(10));

        p.add(etiqueta("Buscar usuario*"));
        p.add(Box.createVerticalStrut(5));

        cbUsuariosMensual = new JComboBox<>();
        cbUsuariosMensual.setFont(FUENTE_NORMAL);
        cbUsuariosMensual.setMaximumSize(dimCampo);
        cbUsuariosMensual.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(cbUsuariosMensual);
        p.add(Box.createVerticalStrut(20));

        JLabel lbl2 = new JLabel("PASO 2. SELECCIONAR CELDA");
        lbl2.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        lbl2.setForeground(new Color(100, 100, 100));
        p.add(lbl2);
        p.add(Box.createVerticalStrut(10));

        p.add(etiqueta("Celda disponible*"));
        p.add(Box.createVerticalStrut(5));

        cbCeldasDisponibles = new JComboBox<>();
        cbCeldasDisponibles.setFont(FUENTE_NORMAL);
        cbCeldasDisponibles.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbCeldasDisponibles.setMaximumSize(dimCampo);
        p.add(cbCeldasDisponibles);
        p.add(Box.createVerticalStrut(25));

        JButton btnAsignar = botonPrimario("Asignar celda", 0);
        btnAsignar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnAsignar.addActionListener(e -> ejecutarAsignacion());
        p.add(btnAsignar);
        p.add(Box.createVerticalStrut(20));

        // Panel de éxito (azul)
        pnlAsignacionExito = new JPanel(new GridLayout(5, 2, 6, 6));
        pnlAsignacionExito.setBackground(AZUL_EXITO);
        pnlAsignacionExito.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AZUL_EXITO.darker(), 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        pnlAsignacionExito.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        pnlAsignacionExito.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCheckA = new JLabel("✓  Celda asignada");
        lblCheckA.setFont(FUENTE_BOLD);
        lblCheckA.setForeground(Color.WHITE);
        pnlAsignacionExito.add(lblCheckA);
        pnlAsignacionExito.add(new JLabel(""));

        pnlAsignacionExito.add(etiquetaResultado("Usuario"));
        lblUsuarioValor = valorResultado("");
        pnlAsignacionExito.add(lblUsuarioValor);

        pnlAsignacionExito.add(etiquetaResultado("Celda asignada"));
        lblCeldaValor = valorResultado("");
        pnlAsignacionExito.add(lblCeldaValor);

        pnlAsignacionExito.add(etiquetaResultado("Estado"));
        lblEstadoAsigValor = valorResultado("");
        pnlAsignacionExito.add(lblEstadoAsigValor);

        pnlAsignacionExito.add(etiquetaResultado("Celda"));
        lblCeldaAsigValor = valorResultado("");
        pnlAsignacionExito.add(lblCeldaAsigValor);

        p.add(pnlAsignacionExito);
        p.add(Box.createVerticalGlue());

        cargarUsuariosMensualesActivos();
        cargarCeldasDisponibles();

        return p;
    }

    // ====================== MÉTODOS DE APOYO REFACTORIZADOS ======================

    private JPanel panelBase(String titulo) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(GRIS_BORDE, 1, true),
                new EmptyBorder(25, 30, 25, 30)
        ));

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(FUENTE_TITULO); // Uso de constante heredada
        lbl.setForeground(AZUL_BTN);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(lbl);
        p.add(Box.createVerticalStrut(15));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(230, 230, 230));
        p.add(sep);

        p.add(Box.createVerticalStrut(25));
        return p;
    }

    private JButton crearBotonTipo(String texto, boolean seleccionado) {
        JButton btn = new JButton(texto);
        btn.setPreferredSize(new Dimension(110, 38));
        btn.setFont(FUENTE_BOLD);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (seleccionado) {
            btn.setBackground(AZUL_BTN);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(GRIS_BORDE);
            btn.setForeground(Color.BLACK);
        }
        return btn;
    }

    private void alternarTipo(String tipo) {
        tipoSeleccionado = tipo;
        btnCarro.setBackground(tipo.equals("CARRO") ? AZUL_BTN : GRIS_BORDE);
        btnMoto.setBackground(tipo.equals("MOTO") ? AZUL_BTN : GRIS_BORDE);
        btnCarro.setForeground(tipo.equals("CARRO") ? Color.WHITE : Color.BLACK);
        btnMoto.setForeground(tipo.equals("MOTO") ? Color.WHITE : Color.BLACK);
    }

    private void ejecutarRegistro() {
        String idOriginal = txtIdentificador.getText().trim();
        // Extrae solo números: "C01" -> "1"
        String soloNumeros = idOriginal.replaceAll("[^0-9]", "");

        if (soloNumeros.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un número de celda válido (ej: C01).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int idNumerico = Integer.parseInt(soloNumeros);

            // --- CAMBIO DE COHERENCIA AQUÍ ---
            // Obtenemos si el usuario seleccionó "Disponible" en el combo
            boolean esDisponible = cbEstadoInicial.getSelectedItem().toString().equalsIgnoreCase("Disponible");

            // Llamamos a tu nuevo método del Service con los 3 parámetros
            service.registrarCelda(idNumerico, tipoSeleccionado, esDisponible);

            // Actualizamos la tabla y los combos de asignación
            actualizarTablaCeldas("");
            actualizarCombos();

            // IMPORTANTE: Refrescar el mapa visual para que el color cambie de inmediato
            for (Component comp : panelContenedor.getComponents()) {
                if (comp instanceof PanelCeldas) {
                    ((PanelCeldas) comp).actualizarMapa();
                    break;
                }
            }

            // Mostramos el éxito con el estado real
            mostrarExitoRegistro(idNumerico, tipoSeleccionado, esDisponible ? "Disponible" : "Ocupada");

            // Timer para volver al mapa
            Timer timer = new Timer(2000, e -> cardLayout.show(panelContenedor, "PANTALLA_CELDAS"));
            timer.setRepeats(false);
            timer.start();
// 2. Sincronizas el Menú Lateral
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof DashboardFrame) {
                ((DashboardFrame) w).marcarBotonPorPantalla("PANTALLA_CELDAS");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al procesar celda: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Procesa la asignación mensual de una celda a un usuario seleccionado.
     */
    private void ejecutarAsignacion() {
        if (cbUsuariosMensual.getSelectedIndex() <= 0 || cbCeldasDisponibles.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar usuario y celda.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String usuarioStr = (String) cbUsuariosMensual.getSelectedItem();
            int idUsuario = Integer.parseInt(usuarioStr.split(" - ")[0]);

            String celdaStr = (String) cbCeldasDisponibles.getSelectedItem();
            int idCelda = Integer.parseInt(celdaStr.split(" - ")[0].replace("C", ""));

            service.asignarCeldaMensual(idUsuario, idCelda);

            mostrarExitoAsignacion(idUsuario, idCelda);
            actualizarCombos();

            Timer timer = new Timer(2500, e -> cardLayout.show(panelContenedor, "PANTALLA_CELDAS"));
            timer.setRepeats(false);
            timer.start();
            // 2. Sincronizas el Menú Lateral
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof DashboardFrame) {
                ((DashboardFrame) w).marcarBotonPorPantalla("PANTALLA_CELDAS");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en la asignación: " + ex.getMessage());
        }
    }

    /**
     * Muestra el resumen visual en color verde tras registrar exitosamente una celda.
     *
     * @param id     Identificador de la celda.
     * @param tipo   Tipo de vehículo permitido.
     * @param estado Estado inicial asignado.
     */
    private void mostrarExitoRegistro(int id, String tipo, String estado) {
        pnlMensajeExito.removeAll();
        pnlMensajeExito.setLayout(new GridLayout(4, 2, 8, 6));

        // 2. Encabezado
        pnlMensajeExito.add(new JLabel("✓ Celda registrada", SwingConstants.CENTER) {{
            setFont(new Font("Arial", Font.BOLD, 14));
            setForeground(Color.WHITE);
        }});
        pnlMensajeExito.add(new JLabel(""));

        pnlMensajeExito.add(new JLabel("Identificador:"));
        pnlMensajeExito.add(new JLabel("C.C" + id) {{
            setFont(new Font("Arial", Font.BOLD, 12));
            setForeground(Color.WHITE);
        }});

        pnlMensajeExito.add(new JLabel("Tipo:"));
        pnlMensajeExito.add(new JLabel(tipo) {{
            setFont(new Font("Arial", Font.BOLD, 12));
            setForeground(Color.WHITE);
        }});

        pnlMensajeExito.add(new JLabel("Estado:"));
        pnlMensajeExito.add(new JLabel(estado) {{
            setFont(new Font("Arial", Font.BOLD, 12));
            setForeground(Color.WHITE);
        }});

        pnlMensajeExito.setVisible(true);
        pnlMensajeExito.revalidate();
        pnlMensajeExito.repaint();
    }

    /**
     * Muestra el resumen visual en color azul tras asignar una celda a un usuario.
     *
     * @param idUsuario ID del cliente mensual.
     * @param idCelda   ID de la celda vinculada.
     */
    private void mostrarExitoAsignacion(int idUsuario, int idCelda) {
        pnlAsignacionExito.removeAll();
        pnlAsignacionExito.setLayout(new GridLayout(4, 2, 8, 6));

        pnlAsignacionExito.add(new JLabel("✓ Celda asignada", SwingConstants.CENTER) {{
            setFont(new Font("Arial", Font.BOLD, 14));
            setForeground(Color.WHITE);
        }});
        pnlAsignacionExito.add(new JLabel(""));

        pnlAsignacionExito.add(new JLabel("Usuario:"));
        pnlAsignacionExito.add(new JLabel(String.valueOf(idUsuario)) {{
            setFont(new Font("Arial", Font.BOLD, 12));
            setForeground(Color.WHITE);
        }});

        pnlAsignacionExito.add(new JLabel("Celda:"));
        pnlAsignacionExito.add(new JLabel("C" + idCelda) {{
            setFont(new Font("Arial", Font.BOLD, 12));
            setForeground(Color.WHITE);
        }});

        pnlAsignacionExito.add(new JLabel("Tipo:"));
        pnlAsignacionExito.add(new JLabel("Mensual") {{
            setFont(new Font("Arial", Font.BOLD, 12));
            setForeground(Color.WHITE);
        }});

        pnlAsignacionExito.setVisible(true);
        // Refrescar la interfaz
        pnlAsignacionExito.revalidate();
        pnlAsignacionExito.repaint();
    }

    public void actualizarCombos() {
        cbUsuariosMensual.removeAllItems();
        cbUsuariosMensual.addItem("Seleccione usuario...");

        try {
            // Traemos la lista fresca de la BD
            List<Usuario> usuarios = service.buscarUsuarios("");

            for (Usuario u : usuarios) {
                // REGLA DE ORO: Solo activos y NO el administrador
                if (u.isActivo() && !u.getNombre().equalsIgnoreCase("admin")) {
                    cbUsuariosMensual.addItem(u.getId() + " - " + u.getNombre());
                }
            }
            cargarCeldasDisponibles(); // Actualiza también el combo de celdas
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarUsuariosMensualesActivos() {
        cbUsuariosMensual.removeAllItems();
        cbUsuariosMensual.addItem("Seleccione usuario...");

        try {
            // Obtenemos la lista actualizada desde el service
            List<Usuario> usuarios = service.buscarUsuarios("");

            for (Usuario u : usuarios) {
                // FILTRO: Solo usuarios activos y que NO sean el administrador
                if (u.isActivo() && !u.getNombre().equalsIgnoreCase("admin")) {
                    cbUsuariosMensual.addItem(u.getId() + " - " + u.getNombre());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarCeldasDisponibles() {
        cbCeldasDisponibles.removeAllItems();
        cbCeldasDisponibles.addItem("Seleccione celda...");

        try {
            List<Celda> celdas = service.listarCeldas();
            for (Celda c : celdas) {
                if (c.isDisponible()) {
                    cbCeldasDisponibles.addItem("C" + c.getId() + " - " + c.getTipo());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Etiqueta de campo con estilo estándar
     */
    @Override
    protected JLabel etiqueta(String texto) {
        JLabel lbl = super.etiqueta(texto);
        lbl.setFont(new Font("Arial", Font.PLAIN, 13));
        lbl.setForeground(new Color(60, 60, 60));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /**
     * Etiqueta clara para el panel de resultado
     */
    private JLabel etiquetaResultado(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));
        lbl.setForeground(new Color(210, 235, 210));
        return lbl;
    }

    /**
     * Valor en blanco/negrilla para el panel de resultado
     */
    private JLabel valorResultado(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }


    private void actualizarTablaCeldas(String filtro) {
        if (tablaCeldas == null) return;

        DefaultTableModel modelo = (DefaultTableModel) tablaCeldas.getModel();
        modelo.setRowCount(0); // Limpia la tabla actual

        try {
            List<Celda> lista = service.listarCeldas();
            for (Celda c : lista) {
                String idVisual = "C" + String.format("%02d", c.getId());
                String estado = c.isDisponible() ? "Disponible" : "Ocupada";

                // Filtro "Letreando": Si coincide con lo escrito o está vacío
                if (filtro.isEmpty() || idVisual.toLowerCase().contains(filtro.toLowerCase())) {
                    modelo.addRow(new Object[]{
                            idVisual,
                            c.getTipo(),
                            estado // Aquí se muestra la condición que pediste
                    });
                }
            }
        } catch (Exception ex) {
            System.err.println("Error al sincronizar tabla: " + ex.getMessage());
        }
    }
    /**
     * Recibe el ID desde el mapa y llena el campo automáticamente.
     */
    public void cargarCeldaDesdeMapa(int id) {
        // Formatea el número (1) a "C01"
        String idVisual = "C" + String.format("%02d", id);

        // Llena tu JTextField original
        txtIdentificador.setText(idVisual);

        // Opcional: Solicita el foco para que el usuario sepa que ya se cargó
        txtIdentificador.requestFocus();
    }
}

