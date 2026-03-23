package parqueadero.ui;

import parqueadero.model.Celda;
import parqueadero.service.ParqueaderoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.AbstractDocument;
import java.awt.*;

/**
 * Vista: PanelRegistro
 * Registra un nuevo usuario cliente y su primera entrada.
 * Campos obligatorios (*): nombre, documento, teléfono, placa
 * Tipo de usuario es obligatorio para continuar.
 */
public class PanelRegistro extends JPanel {

    private final Color AZUL_OSCURO = new Color(45, 55, 125);
    private final Color ROJO_ERROR  = new Color(200, 50, 50);

    private final ParqueaderoService service;
    private final CardLayout cardLayout;
    private final JPanel panelContenedor;

    // ── Campos del formulario ────────────────────
    private JTextField txtNombre, txtTelefono,
            txtPlaca, txtDocumento, txtCorreo;
    private JComboBox<String> cmbTipo;

    public PanelRegistro(ParqueaderoService service,
                         CardLayout cardLayout,
                         JPanel panelContenedor) {
        this.service         = service;
        this.cardLayout      = cardLayout;
        this.panelContenedor = panelContenedor;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(30, 40, 30, 40));

       // VALIDADOR DE CAMPO
        txtNombre = campo();
        txtTelefono = campo();
        txtPlaca = campo();
        txtDocumento = campo();
        txtCorreo = campo();

        //  APLICAR LÍMITES INDIVIDUALES (NUEVO)

        ((AbstractDocument) txtDocumento.getDocument())
                .setDocumentFilter(new ValidadorCampo(15, true, false, true, txtDocumento));

        ((AbstractDocument) txtTelefono.getDocument())
                .setDocumentFilter(new ValidadorCampo(10, true, false, true, txtTelefono));

        ((AbstractDocument) txtPlaca.getDocument())
                .setDocumentFilter(new ValidadorCampo(6, false, true, false, txtPlaca));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(25, 30, 25, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.insets  = new Insets(5, 10, 12, 10);
        gbc.weightx = 0.5;

        // ── Título ───────────────────────────────
        JLabel titulo = new JLabel("Registrar nuevo usuario");
        titulo.setFont(new Font("Arial", Font.BOLD, 22));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        form.add(titulo, gbc);

        // ── Columna izquierda ────────────────────
        gbc.gridwidth = 1;
        form.add(labelObligatorio("NOMBRE COMPLETO"),
                pos(0, 1, gbc));
        form.add(txtNombre,  pos(0, 2, gbc));

        form.add(labelObligatorio("TELÉFONO"),
                pos(0, 3, gbc));
        form.add(txtTelefono , pos(0, 4, gbc));

        form.add(labelObligatorio("PLACA DEL VEHÍCULO"),
                pos(0, 5, gbc));
        form.add(txtPlaca , pos(0, 6, gbc));

        // ── Columna derecha ──────────────────────
        form.add(labelObligatorio("NÚMERO DE DOCUMENTO"),
                pos(1, 1, gbc));
        form.add(txtDocumento , pos(1, 2, gbc));

        form.add(labelOpcional("CORREO ELECTRÓNICO"),
                pos(1, 3, gbc));
        form.add(txtCorreo , pos(1, 4, gbc));

        // ── Tipo de usuario ──────────────────────
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        form.add(labelObligatorio("TIPO DE USUARIO"), gbc);

        gbc.gridy = 8;
        cmbTipo = new JComboBox<>(
                new String[]{"Seleccionar", "Mensual", "Visitante"});
        cmbTipo.setPreferredSize(new Dimension(0, 35));
        form.add(cmbTipo, gbc);

        // ── Nota campos obligatorios ─────────────
        gbc.gridy = 9;
        JLabel lblNota = new JLabel(
                "  * Campos obligatorios");
        lblNota.setFont(new Font("Arial", Font.ITALIC, 11));
        lblNota.setForeground(new Color(150, 150, 150));
        form.add(lblNota, gbc);

        // ── Botones ──────────────────────────────
        JButton btnRegistrar = new JButton("Registrar usuario");
        btnRegistrar.setBackground(AZUL_OSCURO);
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFont(new Font("Arial", Font.BOLD, 13));
        btnRegistrar.setPreferredSize(new Dimension(200, 40));
        btnRegistrar.setCursor(
                new Cursor(Cursor.HAND_CURSOR));

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(140, 40));
        btnCancelar.setCursor(
                new Cursor(Cursor.HAND_CURSOR));

        JPanel pnlBotones = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 20, 10));
        pnlBotones.setOpaque(false);
        pnlBotones.add(btnCancelar);
        pnlBotones.add(btnRegistrar);

        gbc.gridy = 10;
        form.add(pnlBotones, gbc);

        add(form, BorderLayout.CENTER);

        // ── Acción REGISTRAR ─────────────────────
        btnRegistrar.addActionListener(e -> registrar());

        // ── Acción CANCELAR ──────────────────────
        btnCancelar.addActionListener(e -> {
            int op = JOptionPane.showConfirmDialog(this,
                    "¿Desea cancelar y borrar los datos?",
                    "Confirmar cancelación",
                    JOptionPane.YES_NO_OPTION);
            if (op == JOptionPane.YES_OPTION)
                limpiarCampos();
        });
    }

    // ═══════════════════════════════════════════════
    // ACCIÓN REGISTRAR — lógica completa
    /**
     * Procesa el registro integral de un nuevo usuario en el sistema.
     * * Sigue este flujo lógico:
     * 1. Valida que los campos obligatorios no estén vacíos.
     * 2. Verifica la existencia de celdas físicas disponibles en la base de datos.
     * 3. Guarda la información del usuario, vehículo y asignación de celda.
     * 4. Notifica el resultado, limpia el formulario y actualiza la tabla principal
     * de entradas antes de redirigir al usuario.
     */
    private void registrar() {

        // 1. Validar campos obligatorios con mensaje
        //    específico indicando cuál falta
        String campoFaltante = validarCampos();
        if (campoFaltante != null) {
            JOptionPane.showMessageDialog(this,
                    "El campo \"" + campoFaltante
                            + "\" es obligatorio (*).",
                    "Campo requerido",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Buscar celda disponible real desde BD
        int idCelda = obtenerCeldaDisponible();
        if (idCelda <= 0) {
            JOptionPane.showMessageDialog(this,
                    "No hay celdas disponibles.\n"
                            + "Registre nuevas celdas en la sección Celdas.",
                    "Sin celdas libres",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Registrar en BD
        boolean exito = service.registrarNuevoUsuarioCompleto(
                txtNombre   .getText().trim(),
                txtDocumento.getText().trim(),
                txtTelefono .getText().trim(),
                txtCorreo   .getText().trim(),
                txtPlaca    .getText().trim().toUpperCase(),
                // tipo en minúscula para coincidir con ENUM BD
                cmbTipo.getSelectedItem().toString().toLowerCase(),
                idCelda,
                1  // ID del admin logueado
        );

        if (exito) {
            // 4. Mensaje de éxito
            JOptionPane.showMessageDialog(this,
                    "✓  Usuario registrado exitosamente.\n"
                            + "La entrada ha sido guardada.",
                    "Registro exitoso",
                    JOptionPane.INFORMATION_MESSAGE);

            limpiarCampos();

            // 5. Refrescar PanelEntradas
            for (java.awt.Component c :
                    panelContenedor.getComponents()) {
                if (c instanceof PanelEntradas) {
                    ((PanelEntradas) c).actualizarTabla();
                    break;
                }
            }

            // 6. Redirigir a ENTRADAS
            cardLayout.show(panelContenedor, "PANTALLA_ENTRADA");

        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo registrar el usuario.\n"
                            + "Verifique que el documento o la placa\n"
                            + "no estén ya registrados.",
                    "Error en registro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ═══════════════════════════════════════════════
    // VALIDACIÓN — retorna nombre del campo faltante
    // ═══════════════════════════════════════════════

    /**
     * Valida campos uno por uno.
     * Retorna el nombre del primer campo vacío,
     * o null si todo está completo.
     */
    private String validarCampos() {
        if (txtNombre.getText().trim().isEmpty())
            return "NOMBRE COMPLETO";
        if (txtDocumento.getText().trim().isEmpty())
            return "NÚMERO DE DOCUMENTO";
        if (txtTelefono.getText().trim().isEmpty())
            return "TELÉFONO";
        if (txtPlaca.getText().trim().isEmpty())
            return "PLACA DEL VEHÍCULO";
        if (cmbTipo.getSelectedIndex() == 0)
            return "TIPO DE USUARIO";
        return null; // todo OK
    }

    // ═══════════════════════════════════════════════
    // UTILIDADES
    // ═══════════════════════════════════════════════

    /**
     * Busca la primera celda disponible en BD.
     * @return ID de celda libre, -1 si no hay
     */
    private int obtenerCeldaDisponible() {
        try {
            return service.listarCeldas().stream()
                    .filter(Celda::isDisponible)
                    .map(Celda::getId)
                    .findFirst()
                    .orElse(-1);
        } catch (Exception e) {
            System.err.println(
                    "Error buscando celda: " + e.getMessage());
            return -1;
        }
    }
    /**
     * Limpia todos los campos del formulario y reinicia el selector de tipo.
     */
    private void limpiarCampos() {
        txtNombre   .setText("");
        txtDocumento.setText("");
        txtTelefono .setText("");
        txtPlaca    .setText("");
        txtCorreo   .setText("");
        cmbTipo.setSelectedIndex(0);
    }

    /** Etiqueta con asterisco rojo para campo obligatorio */
    private JLabel labelObligatorio(String texto) {
        JLabel lbl = new JLabel(
                "<html>" + texto
                        + " <font color='red'>*</font></html>");
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    /** Etiqueta sin asterisco para campo opcional */
    private JLabel labelOpcional(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }
    /**
     * Crea un campo de texto con un tamaño vertical estándar de 35px.
     * @return JTextField configurado.
     */
    private JTextField campo() {
        JTextField t = new JTextField();
        t.setPreferredSize(new Dimension(0, 35));
        return t;
    }

    /**
     * Asigna las coordenadas X e Y a un objeto GridBagConstraints.
     * @return El objeto de restricciones actualizado.
     */
    private GridBagConstraints pos(int x, int y,
                                   GridBagConstraints g) {
        g.gridx = x; g.gridy = y; return g;
    }
 }