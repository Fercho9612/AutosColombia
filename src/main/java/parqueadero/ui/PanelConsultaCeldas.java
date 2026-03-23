package parqueadero.ui;

import parqueadero.model.Celda;
import parqueadero.model.Usuario;
import parqueadero.service.ParqueaderoService;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel para la gestión de celdas: permite registrar nuevas ubicaciones
 * y asignar celdas fijas a usuarios mensuales.
 */
public class PanelConsultaCeldas extends JPanel {

    // ── Labels del panel resultado izquierdo ─────
    private JLabel lblIdValor, lblTipoValor, lblEstadoValor;

    // ── Labels del panel resultado derecho ──────
    private JLabel lblUsuarioValor, lblCeldaValor,
            lblEstadoAsigValor, lblCeldaAsigValor;

    private final ParqueaderoService service;
    private final CardLayout cardLayout;
    private final JPanel panelContenedor;

    // ── Lado izquierdo ───────────────────────────
    private JTextField txtIdentificador;
    private JButton btnCarro, btnMoto;
    private JComboBox<String> cbEstadoInicial;
    private JPanel pnlMensajeExito;

    // Tipo seleccionado por botones Carro/Moto
    // "CARRO" por defecto
    private String tipoSeleccionado = "CARRO";

    // ── Lado derecho ─────────────────────────────
    private JComboBox<String> cbUsuariosMensual;
    private JComboBox<String> cbCeldasDisponibles;
    private JPanel pnlAsignacionExito;

    // ── Colores ──────────────────────────────────
    private final Color AZUL_BTN = new Color(45, 55, 125);
    private final Color VERDE_EXITO = new Color(40, 160, 80);
    private final Color AZUL_EXITO = new Color(45, 100, 200);
    /**
     * Inicializa el panel con dos secciones: registro (izquierda) y asignación (derecha).
     * @param service Servicio de lógica de negocio.
     * @param cardLayout Controlador de navegación.
     * @param panelContenedor Contenedor principal para cambios de pantalla.
     */
    public PanelConsultaCeldas(ParqueaderoService service,
                               CardLayout cardLayout,
                               JPanel panelContenedor) {
        this.service = service;
        this.cardLayout = cardLayout;
        this.panelContenedor = panelContenedor;

        setLayout(new GridLayout(1, 2, 20, 0));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(25, 35, 25, 35));

        add(construirPanelIzquierdo());
        add(construirPanelDerecho());
    }

    // ═══════════════════════════════════════════════
    // PANEL IZQUIERDO — Registrar nueva celda
    // ═══════════════════════════════════════════════

    /**
     * Crea la sección de formulario para dar de alta nuevas celdas en el sistema.
     * @return JPanel configurado para el registro de celdas.
     */
    private JPanel construirPanelIzquierdo() {
        JPanel panel = panelSeccion("Registrar nueva celda");

        // ── Identificador ────────────────────────
        panel.add(etiqueta("Identificador*"));
        txtIdentificador = new JTextField();
        txtIdentificador.setPreferredSize(new Dimension(0, 38));
        txtIdentificador.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 38));
        panel.add(txtIdentificador);
        panel.add(Box.createVerticalStrut(10));

        // ── Tipo de vehículo ─────────────────────
        panel.add(etiqueta("Tipo de vehículo*"));
        JPanel filaTipo = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaTipo.setOpaque(false);

        btnCarro = botonTipo("Carro", true);
        btnMoto = botonTipo("Moto", false);

        btnCarro.addActionListener(e -> seleccionarTipo("CARRO"));
        btnMoto.addActionListener(e -> seleccionarTipo("MOTO"));

        filaTipo.add(btnCarro);
        filaTipo.add(btnMoto);
        panel.add(filaTipo);

        panel.add(Box.createVerticalStrut(10));

        // ── Estado inicial ───────────────────────
        panel.add(etiqueta("Estado inicial"));
        cbEstadoInicial = new JComboBox<>(
                new String[]{"Disponible", "Ocupada"});
        cbEstadoInicial.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 38));
        panel.add(cbEstadoInicial);
        panel.add(Box.createVerticalStrut(20));

        // ── Botón guardar ────────────────────────
        JButton btnGuardar = new JButton("Guardar celda");
        btnGuardar.setBackground(AZUL_BTN);
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Arial", Font.BOLD, 13));
        btnGuardar.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 42));
        btnGuardar.setCursor(
                new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.addActionListener(
                e -> guardarNuevaCelda());
        panel.add(btnGuardar);
        panel.add(Box.createVerticalStrut(15));

        // ── Panel éxito verde ────────────────────
        // ── Panel resultado verde — visible desde el inicio ──
        pnlMensajeExito = new JPanel(new GridLayout(4, 2, 6, 6));
        pnlMensajeExito.setBackground(VERDE_EXITO);
        pnlMensajeExito.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(30, 130, 60), 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        pnlMensajeExito.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 120));

// Fila checkmark
        JLabel lblCheck = new JLabel("✓  Celda registrada");
        lblCheck.setFont(new Font("Arial", Font.BOLD, 12));
        lblCheck.setForeground(Color.WHITE);
        pnlMensajeExito.add(lblCheck);
        pnlMensajeExito.add(new JLabel(""));

// Fila Identificador
        pnlMensajeExito.add(etiquetaResultado("Identificador"));
        lblIdValor = valorResultado("");
        pnlMensajeExito.add(lblIdValor);

// Fila Tipo
        pnlMensajeExito.add(etiquetaResultado("Tipo"));
        lblTipoValor = valorResultado("");
        pnlMensajeExito.add(lblTipoValor);

// Fila Estado
        pnlMensajeExito.add(etiquetaResultado("Estado"));
        lblEstadoValor = valorResultado("");
        pnlMensajeExito.add(lblEstadoValor);

        panel.add(pnlMensajeExito);
        panel.add(Box.createVerticalGlue());

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    // ═══════════════════════════════════════════════
    // PANEL DERECHO — Asignar celda

    /**
     * Crea la sección para vincular usuarios de tipo 'Mensual' con celdas disponibles.
     * @return JPanel configurado para la asignación.
     */
    private JPanel construirPanelDerecho() {
        JPanel panel = panelSeccion("Asignar celda");

        // ── Paso 1 ───────────────────────────────
        JLabel lbl1 = new JLabel("PASO 1. SELECCIONAR USUARIO");
        lbl1.setFont(new Font("Arial", Font.BOLD, 11));
        lbl1.setForeground(new Color(100, 100, 100));
        panel.add(lbl1);
        panel.add(Box.createVerticalStrut(4));

        panel.add(etiqueta("Buscar usuario*"));
        cbUsuariosMensual = new JComboBox<>();
        cbUsuariosMensual.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 38));
        panel.add(cbUsuariosMensual);
        panel.add(Box.createVerticalStrut(18));

        // ── Paso 2 ───────────────────────────────
        JLabel lbl2 = new JLabel("PASO 2. SELECCIONAR CELDA");
        lbl2.setFont(new Font("Arial", Font.BOLD, 11));
        lbl2.setForeground(new Color(100, 100, 100));
        panel.add(lbl2);
        panel.add(Box.createVerticalStrut(4));

        panel.add(etiqueta("Celda disponible*"));
        cbCeldasDisponibles = new JComboBox<>();
        cbCeldasDisponibles.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 38));
        panel.add(cbCeldasDisponibles);
        panel.add(Box.createVerticalStrut(20));

        // ── Botón asignar ────────────────────────
        JButton btnAsignar = new JButton("Asignar celda");
        btnAsignar.setBackground(AZUL_BTN);
        btnAsignar.setForeground(Color.WHITE);
        btnAsignar.setFont(new Font("Arial", Font.BOLD, 13));
        btnAsignar.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 42));
        btnAsignar.setCursor(
                new Cursor(Cursor.HAND_CURSOR));
        btnAsignar.addActionListener(e -> asignarCelda());
        panel.add(btnAsignar);
        panel.add(Box.createVerticalStrut(15));

        // ── Panel éxito azul ─────────────────────
        // ── Panel resultado azul — visible desde el inicio ──
        pnlAsignacionExito = new JPanel(new GridLayout(5, 2, 6, 6));
        pnlAsignacionExito.setBackground(AZUL_EXITO);
        pnlAsignacionExito.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(30, 80, 170), 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        pnlAsignacionExito.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 140));

// Fila checkmark
        JLabel lblCheckA = new JLabel("✓  Celda asignada");
        lblCheckA.setFont(new Font("Arial", Font.BOLD, 12));
        lblCheckA.setForeground(Color.WHITE);
        pnlAsignacionExito.add(lblCheckA);
        pnlAsignacionExito.add(new JLabel(""));

// Fila Usuario
        pnlAsignacionExito.add(etiquetaResultado("Usuario"));
        lblUsuarioValor = valorResultado("");
        pnlAsignacionExito.add(lblUsuarioValor);

// Fila Celda asignada
        pnlAsignacionExito.add(etiquetaResultado("Celda asignada"));
        lblCeldaValor = valorResultado("");
        pnlAsignacionExito.add(lblCeldaValor);

// Fila Estado
        pnlAsignacionExito.add(etiquetaResultado("Estado"));
        lblEstadoAsigValor = valorResultado("");
        pnlAsignacionExito.add(lblEstadoAsigValor);

// Fila Celda
        pnlAsignacionExito.add(etiquetaResultado("Celda"));
        lblCeldaAsigValor = valorResultado("");
        pnlAsignacionExito.add(lblCeldaAsigValor);

        panel.add(pnlAsignacionExito);
        panel.add(Box.createVerticalGlue());

        panel.add(Box.createVerticalGlue());

        // Cargar datos iniciales
        cargarUsuariosMensualesActivos();
        cargarCeldasDisponibles();

        return panel;
    }


    // ═══════════════════════════════════════════════
    // LÓGICA — sin cambios funcionales
    // ═══════════════════════════════════════════════
    /**
     * Alterna visualmente la selección entre los tipos de vehículo "CARRO" y "MOTO".
     * @param tipo El tipo de vehículo seleccionado.
     */
    private void seleccionarTipo(String tipo) {
        tipoSeleccionado = tipo;
        if (tipo.equals("CARRO")) {
            btnCarro.setBackground(AZUL_BTN);
            btnCarro.setForeground(Color.WHITE);
            btnMoto.setBackground(new Color(210, 210, 210));
            btnMoto.setForeground(Color.BLACK);
        } else {
            btnMoto.setBackground(AZUL_BTN);
            btnMoto.setForeground(Color.WHITE);
            btnCarro.setBackground(new Color(210, 210, 210));
            btnCarro.setForeground(Color.BLACK);
        }
    }
    /**
     * Valida los datos e inserta una nueva celda en la base de datos.
     */
    private void guardarNuevaCelda() {
        String idStr = txtIdentificador.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El identificador es obligatorio.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idCelda;
        try {
            idCelda = Integer.parseInt(
                    idStr.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Identificador inválido (use números).",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean disponible = cbEstadoInicial
                .getSelectedItem().toString()
                .equals("Disponible");

        try {
            // tipoSeleccionado viene de los botones Carro/Moto
            service.registrarCelda(idCelda, tipoSeleccionado);

            mostrarExitoRegistro(idCelda, tipoSeleccionado,
                    disponible ? "Disponible" : "Disponible");

// Actualizar combo de celdas disponibles INMEDIATAMENTE
// para que la sección Asignar celda refleje la nueva celda
            actualizarCombosAsignacion();

// Redirigir al mapa después de 2s
            Timer t = new Timer(2000,
                    ev -> cardLayout.show(panelContenedor, "PANTALLA_CELDAS"));
            t.setRepeats(false);
            t.start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al registrar celda:\n"
                            + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * Procesa la asignación mensual de una celda a un usuario seleccionado.
     */
    private void asignarCelda() {
        if (cbUsuariosMensual.getSelectedIndex() <= 0
                || cbCeldasDisponibles.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione usuario y celda.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String usuarioStr = (String)
                cbUsuariosMensual.getSelectedItem();
        int idUsuario = Integer.parseInt(
                usuarioStr.split(" - ")[0].trim());

        String celdaStr = (String)
                cbCeldasDisponibles.getSelectedItem();
        int idCelda = Integer.parseInt(
                celdaStr.split(" - ")[0]
                        .replace("C", "").trim());

        try {
            service.asignarCeldaMensual(idUsuario, idCelda);

            // Mostrar panel éxito AZUL como en el mockup
            mostrarExitoAsignacion(idUsuario, idCelda);

            cargarCeldasDisponibles();

            Timer t = new Timer(2000,
                    ev -> cardLayout.show(
                            panelContenedor, "PANTALLA_CELDAS"));
            t.setRepeats(false);
            t.start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo asignar:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ═══════════════════════════════════════════════
    // PANELES DE ÉXITO — Verde y Azul como el mockup

    /**
     * Muestra el resumen visual en color verde tras registrar exitosamente una celda.
     * @param id Identificador de la celda.
     * @param tipo Tipo de vehículo permitido.
     * @param estado Estado inicial asignado.
     */
    private void mostrarExitoRegistro(int id, String tipo, String estado) {
        // 1. Limpiar el panel para evitar el desorden de la imagen 031b28
        pnlMensajeExito.removeAll();
        pnlMensajeExito.setLayout(new GridLayout(4, 2, 6, 6));

        // 2. Encabezado
        JLabel lblCheck = new JLabel("✓  Celda registrada");
        lblCheck.setFont(new Font("Arial", Font.BOLD, 12));
        lblCheck.setForeground(Color.WHITE);
        pnlMensajeExito.add(lblCheck);
        pnlMensajeExito.add(new JLabel("")); // Espacio vacío para el grid

        // 3. ASIGNACIÓN REAL DE VALORES (Para que no aparezca vacío)
        pnlMensajeExito.add(etiquetaResultado("Identificacion"));
        pnlMensajeExito.add(valorResultado("C.C" + id));

        pnlMensajeExito.add(etiquetaResultado("Tipo"));
        pnlMensajeExito.add(valorResultado(tipo));

        pnlMensajeExito.add(etiquetaResultado("Estado"));
        pnlMensajeExito.add(valorResultado(estado));

        // 4. Forzar a Swing a redibujar el panel
        pnlMensajeExito.revalidate();
        pnlMensajeExito.repaint();
        pnlMensajeExito.setVisible(true);
    }

    /**
     * Muestra el resumen visual en color azul tras asignar una celda a un usuario.
     * @param idUsuario ID del cliente mensual.
     * @param idCelda ID de la celda vinculada.
     */
    private void mostrarExitoAsignacion(int idUsuario, int idCelda) {
        pnlAsignacionExito.removeAll(); // LIMPIA EL PANEL ANTES DE PINTAR
        pnlAsignacionExito.setLayout(new BorderLayout(10, 10));

        // Cabecera con Check
        JLabel lblCheck = new JLabel("✓  Celda asignada correctamente");
        lblCheck.setFont(new Font("Arial", Font.BOLD, 14));
        lblCheck.setForeground(Color.WHITE);
        pnlAsignacionExito.add(lblCheck, BorderLayout.NORTH);

        // Grid para los datos (2 columnas)
        JPanel grid = new JPanel(new GridLayout(4, 2, 5, 2));
        grid.setOpaque(false);

        // Filas de datos
        agregarFila(grid, "Usuario:", String.valueOf(idUsuario));
        agregarFila(grid, "Celda:", "C" + idCelda);
        agregarFila(grid, "Tipo:", "Mensual");
        agregarFila(grid, "Estado:", "OCUPADA");

        pnlAsignacionExito.add(grid, BorderLayout.CENTER);
        pnlAsignacionExito.setVisible(true);

        // Refrescar la interfaz
        pnlAsignacionExito.revalidate();
        pnlAsignacionExito.repaint();
    }

    // Método auxiliar para que las etiquetas se vean bien
    private void agregarFila(JPanel p, String titulo, String valor) {
        JLabel t = new JLabel(titulo);
        JLabel v = new JLabel(valor);
        t.setForeground(new Color(200, 220, 255));
        v.setForeground(Color.WHITE);
        v.setFont(new Font("Arial", Font.BOLD, 12));
        p.add(t);
        p.add(v);
    }

    // ═══════════════════════════════════════════════
    // CARGA DE DATOS
    /**
     * Filtra y carga en el combo box solo los usuarios activos de tipo Mensual.
     */

    private void cargarUsuariosMensualesActivos() {
        cbUsuariosMensual.removeAllItems();
        cbUsuariosMensual.addItem("Seleccione usuario...");
        try {
            List<Usuario> usuarios =
                    service.buscarUsuarios("");
            List<Usuario> filtrados = usuarios.stream()
                    .filter(u -> u.isActivo()
                            && "Mensual".equalsIgnoreCase(
                            u.getTipoCliente()))
                    .collect(Collectors.toList());

            for (Usuario u : filtrados) {
                cbUsuariosMensual.addItem(
                        u.getId() + " - "
                                + u.getNombre()
                                + " (" + u.getDocumento() + ")");
            }
        } catch (Exception e) {
            System.err.println(
                    "Error usuarios mensuales: "
                            + e.getMessage());
        }
    }
    /**
     * Consulta y despliega en el combo box todas las celdas con estado disponible.
     */
    private void cargarCeldasDisponibles() {
        cbCeldasDisponibles.removeAllItems();
        cbCeldasDisponibles.addItem("Seleccione celda...");
        try {
            List<Celda> celdas = service.listarCeldas();
            for (Celda c : celdas) {
                if (c.isDisponible()) {
                    cbCeldasDisponibles.addItem(
                            "C" + c.getId()
                                    + " - " + c.getTipo()
                                    + "_Disponible");
                }
            }
        } catch (Exception e) {
            System.err.println(
                    "Error celdas disponibles: "
                            + e.getMessage());
        }
    }

    private void actualizarCombosAsignacion() {
        cargarCeldasDisponibles();
    }

    // ═══════════════════════════════════════════════
    // UTILIDADES
    // ═══════════════════════════════════════════════

    /**
     * Crea un panel sección con título y separador.
     * BoxLayout vertical para apilar componentes.
     */
    private JPanel panelSeccion(String titulo) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(210, 210, 210), 1, true),
                new EmptyBorder(22, 22, 22, 22)
        ));

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("Arial", Font.BOLD, 20));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 1));
        p.add(sep);
        p.add(Box.createVerticalStrut(16));

        return p;
    }

    /**
     * Genera un botón estilizado para la selección de tipo de vehículo.
     * @param texto Nombre del botón.
     * @param seleccionado Estado inicial del botón.
     * @return JButton configurado.
     */
    private JButton botonTipo(String texto,
                              boolean seleccionado) {
        JButton btn = new JButton(texto);
        btn.setPreferredSize(new Dimension(100, 36));
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        if (seleccionado) {
            btn.setBackground(AZUL_BTN);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(new Color(210, 210, 210));
            btn.setForeground(Color.BLACK);
        }
        return btn;
    }

    /** Etiqueta de campo con estilo estándar */
    private JLabel etiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.PLAIN, 13));
        lbl.setForeground(new Color(60, 60, 60));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /** Etiqueta clara para el panel de resultado */
    private JLabel etiquetaResultado(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));
        lbl.setForeground(new Color(210, 235, 210));
        return lbl;
    }

    /** Valor en blanco/negrilla para el panel de resultado */
    private JLabel valorResultado(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }


}