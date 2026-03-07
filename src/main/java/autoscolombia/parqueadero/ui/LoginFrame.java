package autoscolombia.parqueadero.ui;

import autoscolombia.parqueadero.model.Usuario;
import autoscolombia.parqueadero.service.ParqueaderoService;

import javax.swing.*;
import java.awt.*;


public class LoginFrame extends JFrame {
    private final ParqueaderoService service = new ParqueaderoService();

    public LoginFrame() {
        setTitle("Inicio de Sesión - Parqueadero Autos Colombia");

        // Iniciamos con un tamaño relativo al monitor del usuario (ej. 50% de la pantalla)
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width / 2, screenSize.height / 2);
        setMinimumSize(new Dimension(600, 400)); // Evita que se rompa si se hace muy pequeña
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        BackgroundPanel panel = new BackgroundPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // --- 1. TÍTULO (Ocupa la parte superior) ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 0.3; // Toma el 30% del espacio vertical
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel lblTitulo = new JLabel("Inicio de sesión");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 32));
        lblTitulo.setForeground(Color.WHITE);
        panel.add(lblTitulo, gbc);

        // --- 2. ESPACIADOR IZQUIERDO (Dinámico) ---
        // Este componente "empuja" el formulario a la derecha proporcionalmente
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.6; // Ocupa el 60% del ancho (donde está el arte del fondo)
        gbc.weighty = 0.7; // Ocupa el resto del alto
        panel.add(new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(1000, 1000)), gbc);

        // --- 3. CONTENEDOR DEL FORMULARIO (A la derecha) ---
        // Usamos un sub-panel invisible para agrupar los inputs y que no se desparramen
        JPanel formGroup = new JPanel(new GridLayout(5, 1, 0, 10));
        formGroup.setOpaque(false);

        JLabel lblUsuario = new JLabel("Usuario:");
        lblUsuario.setForeground(Color.WHITE);
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField txtUsuario = new JTextField(15);
        //txtUsuario.setPreferredSize(new Dimension(0, 30));

        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setForeground(Color.WHITE);
        lblPassword.setFont(new Font("Arial", Font.BOLD, 14));
        JPasswordField txtPassword = new JPasswordField(15);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(50, 20, 0, 80);
        panel.add(formGroup, gbc);

        JButton btnLogin = new JButton("Ingresar");
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        //btnLogin.setPreferredSize(new Dimension(0, 40));

        formGroup.add(lblUsuario);
        formGroup.add(txtUsuario);
        formGroup.add(lblPassword);
        formGroup.add(txtPassword);
        formGroup.add(btnLogin);

        // Posicionamos el grupo del formulario
        gbc.gridx = 1;
        gbc.weightx = 0.1; // Ocupa el 40% del ancho restante
        gbc.anchor = GridBagConstraints.NORTHWEST; // Se pega arriba a la izquierda de su celda
        gbc.insets = new Insets(20, 20, 20, 80); // Margen para que no toque los bordes
        panel.add(formGroup, gbc);

        // Lógica (Simplificada para el ejemplo)
        btnLogin.addActionListener(e -> {
            // ... tu lógica de login actual ...
            JOptionPane.showMessageDialog(this, "Intentando ingresar...");
        });

        add(panel);
    }

    private static class BackgroundPanel extends JPanel {
        private Image background;

        public BackgroundPanel() {
            try {
                background = new ImageIcon(getClass().getResource("/images/background.jpg")).getImage();
            } catch (Exception e) { /* Manejo de error */ }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (background != null) {
                // Aquí está la magia: la imagen se redibuja siempre al tamaño actual del panel
                g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}