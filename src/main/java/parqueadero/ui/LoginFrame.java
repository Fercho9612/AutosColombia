package parqueadero.ui;

import parqueadero.model.Usuario;
import parqueadero.service.ParqueaderoService;

import javax.swing.*;
import java.awt.*;

/**
 * Pantalla de inicio de sesión del sistema.
  * Funcionalidades:
 *   - Autenticación con usuario y contraseña
 *   - Restaurar contraseña mediante documento
 *
 * @author Ferney Rodrigo Marin Pai (y equipo)
 * @version 2.0
 */
public class LoginFrame extends JFrame {

    // ── Service de negocio ───────────────────────
    private final ParqueaderoService service = new ParqueaderoService();

    // ── Componentes del formulario ───────────────
    private JTextField     txtUsuario;
    private JPasswordField txtPassword;

    /**
     * Constructor — construye y configura la ventana de login.
     */
    public LoginFrame() {
        setTitle("Inicio de Sesión - Parqueadero Autos Colombia");

        // Tamaño relativo al monitor del usuario
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width / 2, screenSize.height / 2);
        setMinimumSize(new Dimension(600, 400));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel con imagen de fondo
        BackgroundPanel panel = new BackgroundPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // ── Título ───────────────────────────────
        gbc.gridx    = 0;
        gbc.gridy    = 0;
        gbc.gridwidth = 2;
        gbc.weighty  = 0.3;
        gbc.anchor   = GridBagConstraints.CENTER;

        JLabel lblTitulo = new JLabel("Inicio de sesión");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 32));
        lblTitulo.setForeground(Color.WHITE);
        panel.add(lblTitulo, gbc);

        // ── Espaciador izquierdo (arte del fondo) ─
        gbc.gridx    = 0;
        gbc.gridy    = 1;
        gbc.gridwidth = 1;
        gbc.weightx  = 0.6;
        gbc.weighty  = 0.7;
        panel.add(new Box.Filler(
                new Dimension(0,0),
                new Dimension(0,0),
                new Dimension(1000,1000)), gbc);

        // ── Formulario (derecha) ─────────────────
        // GridLayout: 7 filas para incluir botón olvidé contraseña
        JPanel formGroup = new JPanel(new GridLayout(7, 1, 0, 8));
        formGroup.setOpaque(false);

        // Etiqueta y campo usuario
        JLabel lblUsuario = new JLabel("Usuario:");
        lblUsuario.setForeground(Color.WHITE);
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 14));
        txtUsuario = new JTextField(15);

        // Etiqueta y campo contraseña
        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setForeground(Color.WHITE);
        lblPassword.setFont(new Font("Arial", Font.BOLD, 14));
        txtPassword = new JPasswordField(15);

        // Botón ingresar
        JButton btnLogin = new JButton("Ingresar");
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setBackground(new Color(30, 100, 200));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 13));

        // Botón olvidé contraseña
        JButton btnOlvide = new JButton("¿Olvidé mi contraseña?");
        btnOlvide.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOlvide.setOpaque(false);
        btnOlvide.setContentAreaFilled(false);
        btnOlvide.setBorderPainted(false);
        btnOlvide.setForeground(new Color(173, 216, 230)); // azul claro
        btnOlvide.setFont(new Font("Arial", Font.PLAIN, 12));

        // Agregar componentes al formulario
        formGroup.add(lblUsuario);
        formGroup.add(txtUsuario);
        formGroup.add(lblPassword);
        formGroup.add(txtPassword);
        formGroup.add(btnLogin);
        formGroup.add(new JLabel()); // separador visual
        formGroup.add(btnOlvide);

        // Posicionar formulario en el panel
        gbc.gridx    = 1;
        gbc.gridy    = 1;
        gbc.weightx  = 0.4;
        gbc.anchor   = GridBagConstraints.NORTHWEST;
        gbc.insets   = new Insets(20, 20, 20, 80);
        panel.add(formGroup, gbc);

        // ── Acción: Ingresar ─────────────────────
        btnLogin.addActionListener(e -> accionLogin());

        // Permite presionar Enter para ingresar
        txtPassword.addActionListener(e -> accionLogin());

        // ── Acción: Olvidé contraseña ────────────
        btnOlvide.addActionListener(e -> accionOlvidePassword());

        add(panel);
    }

    // ═══════════════════════════════════════════════
    // ACCIONES PRIVADAS
    // ═══════════════════════════════════════════════

    /**
     * Valida credenciales y abre el Dashboard si son correctas.
      */
    private void accionLogin() {
        String nombre   = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        // Validar campos vacíos
        if (nombre.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Por favor ingrese usuario y contraseña.",
                    "Campos vacíos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Usuario usuario = service.login(nombre, password);

            if (usuario != null) {
                // Login exitoso — abrir Dashboard
                dispose();
                new DashboardFrame(usuario, service).setVisible(true);
            } else {
                // Credenciales incorrectas
                JOptionPane.showMessageDialog(
                        this,
                        "Usuario o contraseña incorrectos.",
                        "Acceso denegado",
                        JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
                txtPassword.requestFocus();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error de conexión: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Flujo para restaurar contraseña.
     * Verifica identidad con nombre + documento
     * Pasos:
     *   1. Solicita nombre de usuario
     *   2. Solicita número de documento (segundo factor)
     *   3. Solicita nueva contraseña (con confirmación)
     *   4. Llama al service para actualizar
     */
    private void accionOlvidePassword() {

        // ── Paso 1: nombre de usuario ────────────
        String nombre = JOptionPane.showInputDialog(
                this,
                "Ingrese su nombre de usuario:",
                "Restaurar contraseña — Paso 1",
                JOptionPane.QUESTION_MESSAGE);

        if (nombre == null || nombre.trim().isEmpty()) return;

        // ── Paso 2: documento (verificación) ────
        String documento = JOptionPane.showInputDialog(
                this,
                "Ingrese su número de documento:",
                "Restaurar contraseña — Paso 2",
                JOptionPane.QUESTION_MESSAGE);

        if (documento == null || documento.trim().isEmpty()) return;

        // ── Paso 3: nueva contraseña ─────────────
        JPasswordField txtNueva    = new JPasswordField(15);
        JPasswordField txtConfirma = new JPasswordField(15);

        JPanel panelPass = new JPanel(new GridLayout(4, 1, 0, 6));
        panelPass.add(new JLabel("Nueva contraseña:"));
        panelPass.add(txtNueva);
        panelPass.add(new JLabel("Confirmar contraseña:"));
        panelPass.add(txtConfirma);

        int opcion = JOptionPane.showConfirmDialog(
                this,
                panelPass,
                "Restaurar contraseña — Paso 3",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (opcion != JOptionPane.OK_OPTION) return;

        String nuevaPass    = new String(txtNueva.getPassword()).trim();
        String confirmaPass = new String(txtConfirma.getPassword()).trim();

        // Validar que las contraseñas coinciden
        if (nuevaPass.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "La contraseña no puede estar vacía.",
                    "Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!nuevaPass.equals(confirmaPass)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Las contraseñas no coinciden.",
                    "Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Paso 4: actualizar en BD ─────────────
        try {
            service.restaurarPassword(
                    nombre.trim(),
                    documento.trim(),
                    nuevaPass);

            JOptionPane.showMessageDialog(
                    this,
                    "Contraseña actualizada correctamente.\n" +
                            "Ya puede iniciar sesión con su nueva contraseña.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "No se pudo restaurar",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ═══════════════════════════════════════════════
    // PANEL CON IMAGEN DE FONDO
    // ═══════════════════════════════════════════════

    /**
     * Panel personalizado que escala la imagen de fondo
     * dinámicamente al tamaño actual de la ventana.
     */
    private static class BackgroundPanel extends JPanel {

        private Image background;

        public BackgroundPanel() {
            try {
                background = new ImageIcon(
                        getClass().getResource("/images/background.jpg")
                ).getImage();
            } catch (Exception e) {
                // Sin imagen — fondo negro por defecto
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (background != null) {
                // Escala la imagen al tamaño actual del panel
                g.drawImage(background, 0, 0,
                        getWidth(), getHeight(), this);
            }
        }
    }
}