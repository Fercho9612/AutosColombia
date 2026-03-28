package parqueadero.ui;

import parqueadero.model.Usuario;
import parqueadero.service.ParqueaderoService;

import javax.swing.*;
import java.awt.*;

/**
 * Pantalla de inicio de sesión del sistema.
 */
public class LoginFrame extends JFrame {

    private final ParqueaderoService service = new ParqueaderoService();
    private JTextField txtUsuario;
    private JPasswordField txtPassword;

    public LoginFrame() {
        setTitle("Inicio de Sesión - Parqueadero Autos Colombia");

        // Configuración de tamaño y posición
        setSize(800, 500);
        setMinimumSize(new Dimension(700, 450));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel con imagen de fondo
        BackgroundPanel panelPrincipal = new BackgroundPanel();
        panelPrincipal.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // --- TÍTULO ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 0.2;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel lblTitulo = new JLabel("SISTEMA DE PARQUEADERO");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);
        panelPrincipal.add(lblTitulo, gbc);

        // --- FORMULARIO (Lado Derecho) ---
        JPanel formGroup = new JPanel(new GridLayout(0, 1, 0, 10));
        formGroup.setOpaque(false);
        formGroup.setPreferredSize(new Dimension(250, 300));

        JLabel lblUser = new JLabel("Usuario:");
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new Font("Arial", Font.BOLD, 14));
        txtUsuario = new JTextField();
        txtUsuario.setPreferredSize(new Dimension(200, 35));

        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setForeground(Color.WHITE);
        lblPass.setFont(new Font("Arial", Font.BOLD, 14));
        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(200, 35));

        JButton btnLogin = new JButton("INGRESAR");
        btnLogin.setBackground(new Color(30, 100, 200));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 13));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnOlvide = new JButton("¿Olvidó su contraseña?");
        btnOlvide.setForeground(new Color(173, 216, 230));
        btnOlvide.setContentAreaFilled(false);
        btnOlvide.setBorderPainted(false);
        btnOlvide.setCursor(new Cursor(Cursor.HAND_CURSOR));

        formGroup.add(lblUser);
        formGroup.add(txtUsuario);
        formGroup.add(lblPass);
        formGroup.add(txtPassword);
        formGroup.add(Box.createVerticalStrut(10));
        formGroup.add(btnLogin);
        formGroup.add(btnOlvide);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 50);
        panelPrincipal.add(formGroup, gbc);

        // --- ACCIONES ---
        btnLogin.addActionListener(e -> accionLogin());
        txtPassword.addActionListener(e -> accionLogin()); // Enter en password
        btnOlvide.addActionListener(e -> accionOlvidePassword());

        add(panelPrincipal);

        // Foco inicial en usuario
        SwingUtilities.invokeLater(() -> txtUsuario.requestFocusInWindow());
    }

    private void accionLogin() {
        String user = txtUsuario.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Usuario u = service.login(user, pass);
            if (u != null) {
                this.dispose();
                // Asegúrate que DashboardFrame reciba (Usuario, ParqueaderoService)
                new DashboardFrame(u, service).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Credenciales incorrectas.", "Error", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage());
        }
    }

    private void accionOlvidePassword() {
        String nombre = JOptionPane.showInputDialog(this, "Ingrese su nombre de usuario:");
        if (nombre == null || nombre.isEmpty()) return;

        String documento = JOptionPane.showInputDialog(this, "Ingrese su número de documento:");
        if (documento == null || documento.isEmpty()) return;

        // Panel de cambio de clave
        JPasswordField p1 = new JPasswordField();
        JPasswordField p2 = new JPasswordField();
        Object[] message = { "Nueva Contraseña:", p1, "Confirme Contraseña:", p2 };

        int option = JOptionPane.showConfirmDialog(this, message, "Restablecer Clave", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String pass1 = new String(p1.getPassword());
            String pass2 = new String(p2.getPassword());

            if (pass1.equals(pass2) && !pass1.isEmpty()) {
                try {
                    service.restaurarPassword(nombre, documento, pass1);
                    JOptionPane.showMessageDialog(this, "Contraseña actualizada con éxito.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden o están vacías.");
            }
        }
    }

    // PANEL PERSONALIZADO PARA EL FONDO
    private static class BackgroundPanel extends JPanel {
        private Image img;

        public BackgroundPanel() {
            try {
                // Intenta cargar la imagen
                java.net.URL imgURL = getClass().getResource("/images/background.jpg");
                if (imgURL != null) {
                    img = new ImageIcon(imgURL).getImage();
                }
            } catch (Exception e) {
                img = null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            } else {
                // Si no hay imagen, un degradado o color oscuro profesional
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, new Color(20, 30, 48), 0, getHeight(), new Color(36, 59, 85)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }
}