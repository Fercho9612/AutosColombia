package parqueadero.ui;

import parqueadero.model.Registro;
import parqueadero.model.Usuario;
import parqueadero.service.ParqueaderoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class PanelSalidas extends BasePanel {

    private JTextField txtNombre, txtDoc, txtEmail, txtTelefono;
    private JComboBox<String> cbEstado;
    private JLabel lblEstadoCirculo;

    private int idUsuarioActual = -1;

    private JPanel panelConfirmacion;
    private JLabel lblNombreConfirmar;
    private JButton btnConfirmarDesactivar, btnCancelarDesactivar;

    private DefaultTableModel modeloVehiculos;

    public PanelSalidas(ParqueaderoService service, CardLayout cardLayout, JPanel panelContenedor) {
        super(service, cardLayout, panelContenedor);

        setLayout(new BorderLayout(20, 0));
        setBorder(new EmptyBorder(20, 30, 20, 30));

        add(construirPanelIzquierdo(), BorderLayout.CENTER);
        add(construirPanelDerecho(), BorderLayout.EAST);
    }

    private JPanel construirPanelIzquierdo() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(25, 30, 25, 30)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        g.insets = new Insets(6, 0, 8, 0);

        // Título
        JLabel lblTitulo = new JLabel("Editar usuario");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 3;
        panel.add(lblTitulo, g);

        g.gridy = 1;
        panel.add(new JSeparator(), g);

        // NOMBRES + Lupa
        g.gridy = 2; g.gridwidth = 3;
        panel.add(etiqueta("NOMBRES"), g);

        g.gridy = 3;
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

        // Doc + Círculo
        g.gridy = 4; g.gridwidth = 3; g.gridx = 0;
        JPanel filaDoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaDoc.setOpaque(false);
        filaDoc.add(new JLabel("Doc:"));
        txtDoc = new JTextField(12);
        txtDoc.setEditable(false);
        txtDoc.setBackground(new Color(245, 245, 245));
        filaDoc.add(txtDoc);

        lblEstadoCirculo = new JLabel("●");
        lblEstadoCirculo.setFont(new Font("Arial", Font.PLAIN, 18));
        filaDoc.add(lblEstadoCirculo);
        panel.add(filaDoc, g);

        g.gridy = 5;
        panel.add(new JSeparator(), g);

        // Correo
        g.gridy = 6;
        panel.add(etiqueta("CORREO ELECTRÓNICO"), g);
        g.gridy = 7;
        txtEmail = new JTextField();
        txtEmail.setPreferredSize(new Dimension(0, 35));
        panel.add(txtEmail, g);

        // Teléfono
        g.gridy = 8;
        panel.add(etiqueta("TELÉFONO"), g);
        g.gridy = 9;
        txtTelefono = new JTextField();
        txtTelefono.setPreferredSize(new Dimension(0, 35));
        panel.add(txtTelefono, g);

        // Estado
        g.gridy = 10;
        panel.add(etiqueta("ESTADO"), g);
        g.gridy = 11;
        cbEstado = new JComboBox<>(new String[]{"Activo", "Inactivo"});
        cbEstado.setPreferredSize(new Dimension(0, 35));
        panel.add(cbEstado, g);

        g.gridy = 12;
        panel.add(new JSeparator(), g);

        // Botones (Cancelar a la izquierda, Guardar a la derecha - como en mockup)
        g.gridy = 13;
        g.insets = new Insets(20, 0, 10, 0);
        JPanel filaBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        filaBotones.setOpaque(false);

        JButton btnCancelar = botonSecundario("Cancelar", 110);
        JButton btnGuardar = botonPrimario("Guardar cambios", 160);

        filaBotones.add(btnCancelar);
        filaBotones.add(btnGuardar);

        panel.add(filaBotones, g);

        // Eventos
        KeyAdapter buscarEnter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) buscarUsuario();
            }
        };
        btnBuscar.addActionListener(e -> buscarUsuario());
        btnGuardar.addActionListener(e -> guardarCambios());

        btnCancelar.addActionListener(e -> limpiarCampos());

        return panel;
    }
    /** Panel derecho: Confirmación + Vehículos  */
    private JPanel construirPanelDerecho() {
        JPanel panelDerecho = new JPanel(new GridBagLayout());
        panelDerecho.setOpaque(false); // Para que tome el fondo del padre

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10); // Margen entre paneles
        gbc.weightx = 1.0;

        // 1. Panel de Confirmación (Arriba)
        gbc.gridy = 0;
        gbc.weighty = 0.4; // Ocupa menos espacio
        panelDerecho.add(crearPanelConfirmacion(), gbc);

        // 2. Panel de Vehículos (Abajo)
        gbc.gridy = 1;
        gbc.weighty = 0.6; // Ocupa más espacio hacia abajo
        panelDerecho.add(crearPanelVehiculos(), gbc);

        return panelDerecho;
    }

    // === AJUSTE PANEL CONFIRMACIÓN ===
    private JPanel crearPanelConfirmacion() {
        panelConfirmacion = new JPanel(new GridBagLayout());
        panelConfirmacion.setBackground(Color.WHITE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE); // Fondo blanco según mockup
        // Borde redondeado suave
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        g.gridx = 0;

        // Titulo central
        JLabel titulo = new JLabel("¿CONFIRMAR ELIMINACIÓN?", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 14));
        g.gridy = 0; g.insets = new Insets(0, 0, 10, 0);
        panel.add(titulo, g);

        // Etiqueta "Usuario:" con fondo gris claro
        lblNombreConfirmar = new JLabel(" Usuario: ", SwingConstants.LEFT);
        lblNombreConfirmar.setOpaque(true);
        lblNombreConfirmar.setBackground(new Color(225, 225, 225)); // Gris del mockup
        lblNombreConfirmar.setPreferredSize(new Dimension(0, 35));
        lblNombreConfirmar.setFont(new Font("Arial", Font.BOLD, 13));
        g.gridy = 1;
        panel.add(lblNombreConfirmar, g);

        // Subpanel Gris de advertencia (el cuadro interno)
        JPanel subPanel = new JPanel(new GridBagLayout());
        subPanel.setBackground(new Color(215, 215, 215)); // Gris más oscuro
        subPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(180, 180, 180), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Cuadro blanco interno del mensaje
        JPanel burbujaBlanca = new JPanel(new BorderLayout(10, 10));
        burbujaBlanca.setBackground(Color.WHITE);
        burbujaBlanca.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel txt1 = new JLabel("Desactivar usuario");
        txt1.setFont(new Font("Arial", Font.BOLD, 13));
        JLabel txt2 = new JLabel("<html>Esta acción puede revertirse desde el panel de usuario.</html>");
        txt2.setFont(new Font("Arial", Font.PLAIN, 12));

        burbujaBlanca.add(txt1, BorderLayout.NORTH);
        burbujaBlanca.add(txt2, BorderLayout.CENTER);

        // Botones
        JPanel pBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pBotones.setOpaque(false);

        JButton btnCan = botonSecundario("Cancelar", 100);
        btnCan.addActionListener(e -> panelConfirmacion.setVisible(false));

        JButton btnDes = new JButton("Si, ELIMINAR");
        btnDes.setForeground(new Color(180, 0, 0)); // Rojo oscuro
        btnDes.setBackground(Color.WHITE);
        btnDes.setFont(new Font("Arial", Font.BOLD, 12));
        btnDes.addActionListener(e -> confirmarDesactivacion());

        pBotones.add(btnCan);
        pBotones.add(btnDes);
        burbujaBlanca.add(pBotones, BorderLayout.SOUTH);

        subPanel.add(burbujaBlanca);

        g.gridy = 2; g.insets = new Insets(10, 0, 0, 0);
        panel.add(subPanel, g);

        return panel;
    }
    // === TABLA DE VEHÍCULOS ===
        private JPanel crearPanelVehiculos() {
        Color colorGrisFondo = new Color(215, 215, 215);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));
        //p.setBackground(Color.WHITE);

        JLabel lblVeh = new JLabel("Vehículos asociados");
        lblVeh.setFont(new Font("Arial", Font.BOLD, 14));
        p.add(lblVeh, BorderLayout.NORTH);

        String[] cols = {"PLACA", "TIPO", "ESTADO"};
        modeloVehiculos = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

            // Configuración de la Tabla
            JTable tabla = new JTable(modeloVehiculos);
            tabla.setBackground(colorGrisFondo);
            tabla.setRowHeight(40);
            tabla.setShowGrid(false); // Quita las líneas de la tabla
            tabla.setIntercellSpacing(new Dimension(0, 0));
            tabla.setFillsViewportHeight(true);
            tabla.setFont(new Font("Arial", Font.PLAIN, 13));

            // Estilo del encabezado (Header)
            tabla.getTableHeader().setBackground(colorGrisFondo);
            tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
            tabla.getTableHeader().setForeground(new Color(80, 80, 80));
            tabla.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

            // Quitar bordes al ScrollPane
            JScrollPane scroll = new JScrollPane(tabla);
            scroll.setBorder(null);
            scroll.getViewport().setBackground(colorGrisFondo);
            scroll.setOpaque(false);

            p.add(scroll, BorderLayout.CENTER);

            return p;

    }

    private void buscarUsuario() {
        /*String criterio = txtNombre.getText().trim();
        if (criterio.isEmpty()) return;*/

        try {
            List<Usuario> res = service.buscarUsuarios(txtNombre.getText().trim());
            if (!res.isEmpty()) {
                cargarUsuario(res.get(0));
            } else {
                JOptionPane.showMessageDialog(this, "Usuario no encontrado.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Llena todos los campos de la interfaz con la información del usuario seleccionado.
     * @param u Objeto Usuario con los datos a mostrar.
     */
    public void cargarUsuario(Usuario u) {
        if (u == null) return;
        idUsuarioActual = u.getId();
        txtNombre.setText(u.getNombre());
        txtDoc.setText(u.getDocumento());
        txtEmail.setText(u.getCorreo() != null ? u.getCorreo() : "");
        txtTelefono.setText(u.getTelefono() != null ? u.getTelefono() : "");
        cbEstado.setSelectedItem(u.isActivo() ? "Activo" : "Inactivo");
        lblEstadoCirculo.setForeground(u.isActivo() ? Color.GREEN : Color.RED);

        lblNombreConfirmar.setText(" Usuario: " + u.getNombre());
        panelConfirmacion.setVisible(u.isActivo());

        cargarVehiculosAsociados(u.getId());
        //panelConfirmacion.setVisible(false);
    }
    /**
     * Envía las modificaciones del usuario al servicio y actualiza el estado en la base de datos.
     */
    private void guardarCambios() {
        if (idUsuarioActual == -1) {
            JOptionPane.showMessageDialog(this, "Primero busque un usuario.");
            return;
        }

        try {
            // 1. Guardar en la Base de Datos (Tu código actual)
            service.actualizarUsuario(idUsuarioActual,
                    txtTelefono.getText().trim(),
                    txtEmail.getText().trim(),
                    cbEstado.getSelectedItem().toString());

            // 2. REFRESCAR: Invoca este método que ya está en tu BasePanel
            // Este método buscará el PanelEntradas y ejecutará su actualización.
            refrescarOtrosPaneles();

            // 3. Feedback y Navegación (Tu código actual)
            cardLayout.show(panelContenedor, "PANTALLA_ENTRADA");
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof DashboardFrame) {
                // Le decimos que marque "PANTALLA_ENTRADA" para que el botón de Entradas se ponga azul
                ((DashboardFrame) w).marcarBotonPorPantalla("PANTALLA_ENTRADA");
            }
            JOptionPane.showMessageDialog(this, "Cambios guardados correctamente.");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Confirma y ejecuta la desactivación del usuario.
     */
    private void confirmarDesactivacion() {
        if (idUsuarioActual == -1) return;

        try {
            // Usa el método que ya dispara el mensaje de éxito que viste en la imagen
            service.desactivarUsuario(idUsuarioActual);

            JOptionPane.showMessageDialog(this,
                    "Usuario desactivado correctamente.");

            // Importante: No olvides refrescar para que el usuario "desaparezca" de la tabla actual
            refrescarOtrosPaneles();
            limpiarCampos();
            cardLayout.show(panelContenedor, "PANTALLA_ENTRADA");
            // 2. Sincronizas el Menú Lateral
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof DashboardFrame) {
                ((DashboardFrame) w).marcarBotonPorPantalla("PANTALLA_ENTRADA");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al desactivar usuario: " + e.getMessage());
        }
    }

    /**
     * Consulta y muestra en la tabla todos los vehículos vinculados al ID del usuario.
     * @param idUsuario Identificador único del dueño de los vehículos
     */
    private void cargarVehiculosAsociados(int idUsuario) {
        modeloVehiculos.setRowCount(0);
        try {
            List<Registro> historial = service.obtenerVehiculosPorUsuario(idUsuario);
            for (Registro r : historial) {
                modeloVehiculos.addRow(new Object[]{
                        r.getPlacaVehiculo(),
                        (r.getHoraSalida() == null ? "DENTRO" : "SALIDA"),
                        r.getHoraEntrada()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Se encarga de notificar a los demás paneles que un usuario ya no está activo.
     */
    @Override
    protected void refrescarOtrosPaneles() {
        // 1. Refresca la tabla de Entradas (Heredado de BasePanel)
        refrescarPanelEntradas();

        // 2. Refresca el mapa de celdas (Heredado de BasePanel)
        refrescarPanelCeldas();

        // 3. Notificar a Pagos para que quite la placa del combo
        PanelPagos pPagos = buscarComponente(panelContenedor, PanelPagos.class);
        if (pPagos != null) {
            pPagos.actualizarPlacasActivas();
        }

        // 4. Notificar a Gestión de Celdas para que quite al usuario de la lista de asignación
        PanelConsultaCeldas pCeldas = buscarComponente(panelContenedor, PanelConsultaCeldas.class);
        if (pCeldas != null) {
            pCeldas.actualizarCombos();
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
}