package parqueadero.ui;

import parqueadero.model.Registro;
import parqueadero.model.Usuario;
import parqueadero.service.ParqueaderoService;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Panel para la edición de usuarios, desactivación y consulta de vehículos asociados.
 * Permite buscar usuarios, modificar sus datos y gestionar su estado de actividad.
 */
public class PanelSalidas extends JPanel {

    private final ParqueaderoService service;
    private final CardLayout cardLayout;
    private final JPanel panelContenedor;

    // ── Campos del formulario ────────────────────
    private JTextField txtNombre, txtDoc, txtEmail, txtTelefono;
    private JComboBox<String> cbEstado;
    private JLabel lblEstadoCirculo;

    // ── ID interno del usuario encontrado ────────
    // Necesario porque txtDoc guarda el documento (String),
    // no el ID (int) que necesitan los métodos de service
    private int idUsuarioActual = -1;

    // ── Panel confirmación desactivación ─────────
    private JPanel panelConfirmacion;
    private JLabel lblNombreConfirmar;
    private JButton btnConfirmarDesactivar, btnCancelarDesactivar;

    // ── Tabla vehículos ──────────────────────────
    private DefaultTableModel modeloVehiculos;

    /**
     * Inicializa el panel dividiéndolo en formulario de edición (izquierda)
     * y panel de confirmación/vehículos (derecha).
     */
    public PanelSalidas(ParqueaderoService service,
                        CardLayout cl, JPanel pc) {
        this.service        = service;
        this.cardLayout     = cl;
        this.panelContenedor = pc;

        setLayout(new BorderLayout(20, 0));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        add(construirPanelIzquierdo(), BorderLayout.CENTER);
        add(construirPanelDerecho(),   BorderLayout.EAST);
    }

    // ═══════════════════════════════════════════════
    // PANEL IZQUIERDO — Formulario edición
    // ═══════════════════════════════════════════════
    /**
     * Construye el formulario principal con los campos de datos del usuario.
     * @return JPanel con el diseño del formulario.
     */
    private JPanel construirPanelIzquierdo() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(20, 25, 20, 25)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        g.insets  = new Insets(4, 0, 4, 0);

        // ── Título ───────────────────────────────
        JLabel lblTitulo = new JLabel("Editar usuario");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 3;
        panel.add(lblTitulo, g);

        // ── Separador ────────────────────────────
        g.gridy = 1;
        panel.add(new JSeparator(), g);

        // ── Fila: label NOMBRES ──────────────────
        g.gridy = 2; g.gridwidth = 3;
        JLabel lblN = etiqueta("NOMBRES");
        panel.add(lblN, g);

        // ── Fila: campo nombre + lupa ─────────────
        g.gridy  = 3;
        g.gridwidth = 1;
        txtNombre = new JTextField();
        txtNombre.setPreferredSize(new Dimension(0, 35));
        g.gridx = 0; g.weightx = 1.0;
        panel.add(txtNombre, g);

        JButton btnBuscar = new JButton("🔍");
        btnBuscar.setPreferredSize(new Dimension(40, 35));
        btnBuscar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        g.gridx = 1; g.weightx = 0;
        panel.add(btnBuscar, g);

        // ── Fila: Doc + círculo estado ────────────
        g.gridy = 4; g.gridwidth = 3; g.gridx = 0;
        g.weightx = 1.0;
        JPanel filaDOc = new JPanel(new FlowLayout(
                FlowLayout.LEFT, 8, 0));
        filaDOc.setOpaque(false);
        filaDOc.add(etiqueta("Doc:"));
        txtDoc = new JTextField(12);
        txtDoc.setEditable(false);
        txtDoc.setBackground(new Color(245, 245, 245));
        filaDOc.add(txtDoc);
        lblEstadoCirculo = new JLabel("●");
        lblEstadoCirculo.setForeground(Color.GREEN);
        lblEstadoCirculo.setFont(new Font("Arial", Font.PLAIN, 18));
        filaDOc.add(lblEstadoCirculo);
        panel.add(filaDOc, g);

        // ── Separador ────────────────────────────
        g.gridy = 5;
        panel.add(new JSeparator(), g);

        // ── Correo ───────────────────────────────
        g.gridy = 6; panel.add(etiqueta("CORREO ELECTRÓNICO"), g);
        g.gridy = 7;
        txtEmail = new JTextField();
        txtEmail.setPreferredSize(new Dimension(0, 35));
        panel.add(txtEmail, g);

        // ── Teléfono ─────────────────────────────
        g.gridy = 8; panel.add(etiqueta("TELÉFONO"), g);
        g.gridy = 9;
        txtTelefono = new JTextField();
        txtTelefono.setPreferredSize(new Dimension(0, 35));
        panel.add(txtTelefono, g);

        // ── Estado ───────────────────────────────
        g.gridy = 10; panel.add(etiqueta("ESTADO"), g);
        g.gridy = 11;
        cbEstado = new JComboBox<>(
                new String[]{"Activo", "Inactivo"});
        cbEstado.setPreferredSize(new Dimension(0, 35));
        panel.add(cbEstado, g);

        // ── Separador ────────────────────────────
        g.gridy = 12;
        panel.add(new JSeparator(), g);

        // ── Botones acción ────────────────────────
        g.gridy   = 13;
        g.insets  = new Insets(12, 0, 4, 0);
        JPanel filaBotones = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filaBotones.setOpaque(false);

        JButton btnCancelar   = new JButton("Cancelar");
        JButton btnDesactivar = new JButton("Desactivar");
        JButton btnGuardar    = new JButton("Guardar cambios");

        btnDesactivar.setForeground(Color.RED);
        btnDesactivar.setBackground(Color.WHITE);
        btnDesactivar.setBorder(
                BorderFactory.createLineBorder(Color.RED));

        btnGuardar.setBackground(new Color(45, 55, 125));
        btnGuardar.setForeground(Color.WHITE);

        filaBotones.add(btnCancelar);
        filaBotones.add(btnDesactivar);
        filaBotones.add(btnGuardar);
        panel.add(filaBotones, g);

        // ── Eventos ──────────────────────────────
        KeyAdapter buscarEnter = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    buscarUsuario();
            }
        };
        txtNombre.addKeyListener(buscarEnter);
        btnBuscar.addActionListener(e -> buscarUsuario());

        btnCancelar.addActionListener(e -> limpiarCampos());

        btnDesactivar.addActionListener(e -> {
            if (idUsuarioActual == -1) {
                JOptionPane.showMessageDialog(this,
                        "Primero busque un usuario.");
                return;
            }
            // Mostrar nombre en confirmación y revelar panel
            lblNombreConfirmar.setText(
                    "Usuario: " + txtNombre.getText().trim());
            panelConfirmacion.setVisible(true);
        });

        btnGuardar.addActionListener(e -> guardarCambios());

        return panel;
    }

    // ═══════════════════════════════════════════════
    // PANEL DERECHO — Confirmación + Vehículos

    /**
     * Construye la sección de confirmación de desactivación y la tabla de vehículos.
     * @return JPanel con las herramientas de confirmación y vista de flota.
     */
    private JPanel construirPanelDerecho() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(420, 0));

        // ── Panel confirmación desactivación ─────
        panelConfirmacion = new JPanel(new GridBagLayout());
        panelConfirmacion.setBackground(new Color(248, 248, 248));
        panelConfirmacion.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        panelConfirmacion.setVisible(false); // oculto hasta buscar usuario

        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        g.insets  = new Insets(6, 0, 6, 0);

        JLabel lblTituloConf = new JLabel(
                "¿Confirmar desactivación?", SwingConstants.CENTER);
        lblTituloConf.setFont(new Font("Arial", Font.BOLD, 15));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        panelConfirmacion.add(lblTituloConf, g);

        lblNombreConfirmar = new JLabel("", SwingConstants.LEFT);
        lblNombreConfirmar.setFont(
                new Font("Arial", Font.BOLD, 13));
        g.gridy = 1;
        panelConfirmacion.add(lblNombreConfirmar, g);

        // Sub-panel gris con mensaje
        JPanel subPanel = new JPanel(new BorderLayout(0, 8));
        subPanel.setBackground(new Color(238, 238, 238));
        subPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel lblTitSub = new JLabel("Desactivar usuario");
        lblTitSub.setFont(new Font("Arial", Font.BOLD, 13));
        JLabel lblMsg = new JLabel(
                "<html>Esta acción puede revertirse<br>" +
                        "desde el panel de usuario.</html>");
        lblMsg.setFont(new Font("Arial", Font.PLAIN, 12));

        JPanel filaBotConf = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 15, 8));
        filaBotConf.setOpaque(false);

        btnCancelarDesactivar  = new JButton("Cancelar");
        btnConfirmarDesactivar = new JButton("Sí, desactivar");
        btnConfirmarDesactivar.setForeground(Color.RED);
        btnConfirmarDesactivar.setBackground(Color.WHITE);
        btnConfirmarDesactivar.setBorder(
                BorderFactory.createLineBorder(Color.RED));

        filaBotConf.add(btnCancelarDesactivar);
        filaBotConf.add(btnConfirmarDesactivar);

        subPanel.add(lblTitSub,   BorderLayout.NORTH);
        subPanel.add(lblMsg,      BorderLayout.CENTER);
        subPanel.add(filaBotConf, BorderLayout.SOUTH);

        g.gridy = 2;
        panelConfirmacion.add(subPanel, g);

        panel.add(panelConfirmacion, BorderLayout.NORTH);

        // ── Tabla vehículos asociados ─────────────
        JPanel pVehiculos = new JPanel(new BorderLayout());
        pVehiculos.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        pVehiculos.setBackground(Color.WHITE);

        JLabel lblVeh = new JLabel("Vehículos asociados");
        lblVeh.setFont(new Font("Arial", Font.BOLD, 14));
        pVehiculos.add(lblVeh, BorderLayout.NORTH);

        String[] cols = {"PLACA", "TIPO", "ESTADO"};
        modeloVehiculos = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable tablaVeh = new JTable(modeloVehiculos);
        tablaVeh.setRowHeight(32);
        tablaVeh.setShowGrid(true);
        tablaVeh.setGridColor(new Color(220, 220, 220));
        pVehiculos.add(new JScrollPane(tablaVeh),
                BorderLayout.CENTER);

        panel.add(pVehiculos, BorderLayout.CENTER);

        // ── Eventos confirmación ──────────────────
        btnCancelarDesactivar.addActionListener(
                e -> panelConfirmacion.setVisible(false));

        btnConfirmarDesactivar.addActionListener(
                e -> confirmarDesactivacion());

        return panel;
    }

    // ═══════════════════════════════════════════════
    // LÓGICA PRIVADA
    // ═══════════════════════════════════════════════

    /**
     * Busca un usuario en la base de datos según el texto ingresado en el campo de nombre.
     */
    private void buscarUsuario() {
        String criterio = txtNombre.getText().trim();
        if (criterio.isEmpty()) return;

        try {
            List<Usuario> resultados =
                    service.buscarUsuarios(criterio);

            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No se encontró usuario con: " + criterio);
                return;
            }

            Usuario u = resultados.get(0);
            cargarUsuario(u);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al buscar: " + ex.getMessage());
        }
    }

    /**
     * Llena todos los campos de la interfaz con la información del usuario seleccionado.
     * @param u Objeto Usuario con los datos a mostrar.
     */
    public void cargarUsuario(Usuario u) {
        if (u == null) return;

        // Guardar ID interno para operaciones posteriores
        idUsuarioActual = u.getId();

        txtNombre.setText(u.getNombre() != null
                ? u.getNombre() : "");
        txtDoc.setText(u.getDocumento() != null
                ? u.getDocumento() : "");
        txtEmail.setText(u.getCorreo() != null
                ? u.getCorreo() : "");
        txtTelefono.setText(u.getTelefono() != null
                ? u.getTelefono() : "");
        cbEstado.setSelectedItem(
                u.isActivo() ? "Activo" : "Inactivo");

        // Actualizar círculo de estado
        lblEstadoCirculo.setForeground(
                u.isActivo() ? Color.GREEN : Color.RED);

        // Mostrar panel confirmación listo (oculto hasta clic)
        panelConfirmacion.setVisible(false);

        // Cargar vehículos reales de BD
        cargarVehiculosAsociados(u.getId());
    }

    /**
     * Envía las modificaciones del usuario al servicio y actualiza el estado en la base de datos.
     */
    private void guardarCambios() {
        if (idUsuarioActual == -1) {
            JOptionPane.showMessageDialog(this,
                    "Primero busque un usuario.");
            return;
        }

        try {
            String tel    = txtTelefono.getText().trim();
            String email  = txtEmail.getText().trim();
            // tipo_cliente se toma del estado del combo
            String estado = cbEstado.getSelectedItem()
                    .toString();

            // Actualizar datos en BD (RF12)
            service.actualizarUsuario(
                    idUsuarioActual, tel, email, estado);

            // Cerrar entrada activa si existe (RF04 · RF06)
            boolean salidaHecha = false;
            try {
                salidaHecha = service
                        .finalizarSalidaPorPlacaParaUsuario(
                                idUsuarioActual);
            } catch (Exception ex) {
                // No bloquear guardado si falla la salida
                System.err.println("Advertencia salida: "
                        + ex.getMessage());
            }

            // Refrescar tabla de vehículos
            cargarVehiculosAsociados(idUsuarioActual);

            // Refrescar PanelEntradas y PanelCeldas
            refrescarOtrosPaneles();

            JOptionPane.showMessageDialog(this,
                    "Cambios guardados correctamente." +
                            (salidaHecha
                                    ? "\nEntrada activa cerrada."
                                    : ""));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar: " + ex.getMessage());
        }
    }

    /**
     * Confirma y ejecuta la desactivación del usuario.
      */
    private void confirmarDesactivacion() {
        if (idUsuarioActual == -1) return;

        try {
            // desactivarUsuario ya libera celda asignada
            // y cambia activo=FALSE (RF13)
            service.desactivarUsuario(idUsuarioActual);

            JOptionPane.showMessageDialog(this,
                    "Usuario desactivado correctamente.\n" +
                            "La acción puede revertirse desde " +
                            "el panel de usuarios.");

            panelConfirmacion.setVisible(false);
            limpiarCampos();

            // Refrescar otros paneles
            refrescarOtrosPaneles();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al desactivar: " + ex.getMessage());
        }
    }

    /**
     * Consulta y muestra en la tabla todos los vehículos vinculados al ID del usuario.
     * @param idUsuario Identificador único del dueño de los vehículos.
     */

    private void cargarVehiculosAsociados(int idUsuario) {
        modeloVehiculos.setRowCount(0);
        try {
            List<Registro> registros =
                    service.obtenerVehiculosPorUsuario(idUsuario);

            for (Registro r : registros) {
                String estado = r.getHoraSalida() == null
                        ? "Activo" : "Histórico";
                modeloVehiculos.addRow(new Object[]{
                        r.getPlacaVehiculo(),
                        "Carro",   // tipo viene del vehiculo — simplificado
                        estado
                });
            }
        } catch (Exception e) {
            System.err.println(
                    "Error al cargar vehículos: " + e.getMessage());
        }
    }

    /**
     * Refresca PanelEntradas y PanelCeldas después de
     * guardar o desactivar — para que los cambios se
     * reflejen en toda la aplicación.
     */
    private void refrescarOtrosPaneles() {
        for (java.awt.Component c :
                panelContenedor.getComponents()) {
            if (c instanceof PanelEntradas) {
                // Forzar recarga de la tabla de usuarios
                ((PanelEntradas) c).actualizarTabla();
            }
            if (c instanceof PanelCeldas) {
                // Forzar recarga del mapa de celdas
                ((PanelCeldas) c).actualizarMapa();
            }
        }
    }

    /**
     * Limpia todos los campos del formulario.
     */
    private void limpiarCampos() {
        idUsuarioActual = -1;
        txtNombre.setText("");
        txtDoc.setText("");
        txtEmail.setText("");
        txtTelefono.setText("");
        cbEstado.setSelectedIndex(0);
        lblEstadoCirculo.setForeground(Color.GREEN);
        modeloVehiculos.setRowCount(0);
        panelConfirmacion.setVisible(false);
    }

    /** Crea una etiqueta de formulario con estilo estándar. */
    private JLabel etiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }
}