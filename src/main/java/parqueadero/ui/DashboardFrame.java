package parqueadero.ui;

import parqueadero.model.Usuario;
import parqueadero.service.ParqueaderoService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Ventana Principal del Sistema - Dashboard
 * Gestiona la navegación entre todos los paneles mediante CardLayout.
 */
public class DashboardFrame extends JFrame {

    private final ParqueaderoService service;
    private final Usuario usuario;

    private CardLayout cardLayout;
    private JPanel panelContenedor;
    private JButton[] botonesMenu;

    // Colores (coherentes con BasePanel)
    private final Color AZUL_PRINCIPAL = new Color(30, 110, 220);
    private final Color GRIS_MENU_FONDO = new Color(225, 225, 225);
    private final Color GRIS_BOTON_TEXTO = new Color(50, 50, 50);
    private final Color AZUL_SELECCIONADO = new Color(180, 210, 250);
    private final Color AZUL_BOTON = new Color(45, 55, 125);

    public DashboardFrame(Usuario usuario, ParqueaderoService service) {
        this.usuario = usuario;
        this.service = service;

        configurarVentana();
        inicializarComponentes();
        registrarPantallas();
        configurarEventos();

        // Vista inicial
        cardLayout.show(panelContenedor, "PANTALLA_ENTRADA");
        actualizarEstadoMenu(botonesMenu[1]); // Entradas seleccionado por defecto
    }

    private void configurarVentana() {
        setTitle("Autos Colombia - Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);   // Adaptable a cualquier pantalla
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void inicializarComponentes() {
        add(crearHeader(), BorderLayout.NORTH);
        add(crearMenuLateral(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        panelContenedor = new JPanel(cardLayout);
        add(panelContenedor, BorderLayout.CENTER);
    }

    /** Barra superior (Header) */
    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AZUL_PRINCIPAL);
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));

        // Izquierda
        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        izq.setOpaque(false);
        JLabel icono = new JLabel("≡");
        icono.setForeground(Color.WHITE);
        icono.setFont(new Font("Arial", Font.BOLD, 24));
        JLabel titulo = new JLabel("Autos Colombia");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));

        izq.add(icono);
        izq.add(titulo);

        // Derecha
        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        der.setOpaque(false);
        JLabel userIcon = new JLabel("👤");
        userIcon.setForeground(Color.WHITE);
        userIcon.setFont(new Font("Arial", Font.BOLD, 20));
        JLabel rol = new JLabel("Administrador");
        rol.setForeground(Color.WHITE);
        rol.setFont(new Font("Arial", Font.PLAIN, 16));

        der.add(userIcon);
        der.add(rol);

        header.add(izq, BorderLayout.WEST);
        header.add(der, BorderLayout.EAST);
        return header;
    }

    /** Menú lateral */
    private JPanel crearMenuLateral() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(GRIS_MENU_FONDO);
        menu.setPreferredSize(new Dimension(220, 0));
        menu.setBorder(new EmptyBorder(20, 15, 20, 15));

        JLabel lblMenu = new JLabel("MENÚ");
        lblMenu.setFont(new Font("Arial", Font.BOLD, 18));
        lblMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
        menu.add(lblMenu);
        menu.add(Box.createRigidArea(new Dimension(0, 25)));

        // Botones del menú
        JButton btnUsuarios = crearBotonMenu("👤 Usuarios", false);
        JButton btnEntradas = crearBotonMenu("≡ Entradas", true);     // seleccionado por defecto
        JButton btnSalidas  = crearBotonMenu("⟃ Salidas", false);
        JButton btnCeldas   = crearBotonMenu("⊡ Celdas", false);
        JButton btnGestion  = crearBotonMenu("⚙ Gestión Celda", false);  // ← Mantenido
        JButton btnPagos    = crearBotonMenu("🤝 Pagos", false);

        botonesMenu = new JButton[]{btnUsuarios, btnEntradas, btnSalidas, btnCeldas, btnGestion, btnPagos};

        // Agregar botones
        for (JButton btn : botonesMenu) {
            menu.add(btn);
            menu.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        menu.add(Box.createVerticalGlue());

        // Botón Cerrar Sesión
        JButton btnCerrar = new JButton("Cerrar Sesión");
        btnCerrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCerrar.setForeground(Color.RED);
        btnCerrar.addActionListener(e -> cerrarSesion());
        menu.add(btnCerrar);

        return menu;
    }

    private JButton crearBotonMenu(String texto, boolean seleccionado) {
        JButton btn = new JButton(texto);
        btn.setMaximumSize(new Dimension(200, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));

        if (seleccionado) {
            btn.setBackground(AZUL_SELECCIONADO);
            btn.setForeground(AZUL_PRINCIPAL);
            btn.setFont(new Font("Arial", Font.BOLD, 15));
        } else {
            btn.setBackground(GRIS_MENU_FONDO);
            btn.setForeground(GRIS_BOTON_TEXTO);
            btn.setFont(new Font("Arial", Font.PLAIN, 15));
        }
        return btn;
    }

    /** Registra todas las pantallas */
    private void registrarPantallas() {
        panelContenedor.add(new PanelRegistro(service, cardLayout, panelContenedor), "PANTALLA_REGISTRO");
        panelContenedor.add(new PanelEntradas(service, cardLayout, panelContenedor), "PANTALLA_ENTRADA");
        panelContenedor.add(new PanelSalidas(service, cardLayout, panelContenedor),  "PANTALLA_SALIDA");
        panelContenedor.add(new PanelCeldas(service, cardLayout, panelContenedor),   "PANTALLA_CELDAS");
        panelContenedor.add(new PanelConsultaCeldas(service, cardLayout, panelContenedor), "PANTALLA_GESTION_CELDAS");
        panelContenedor.add(new PanelPagos(service, cardLayout, panelContenedor), "PANTALLA_PAGOS");

       }

    /** Configura eventos de navegación */
    private void configurarEventos() {
        JButton btnUsuarios = botonesMenu[0];
        JButton btnEntradas = botonesMenu[1];
        JButton btnSalidas  = botonesMenu[2];
        JButton btnCeldas   = botonesMenu[3];
        JButton btnGestion  = botonesMenu[4];
        JButton btnPagos    = botonesMenu[5];

        btnUsuarios.addActionListener(e -> navegar("PANTALLA_REGISTRO", btnUsuarios));
        btnEntradas.addActionListener(e -> navegar("PANTALLA_ENTRADA", btnEntradas));
        btnSalidas.addActionListener(e -> navegar("PANTALLA_SALIDA", btnSalidas));
        btnCeldas.addActionListener(e -> navegar("PANTALLA_CELDAS", btnCeldas));
        btnGestion.addActionListener(e -> navegar("PANTALLA_GESTION_CELDAS", btnGestion));
        btnPagos.addActionListener(e -> navegar("PANTALLA_PAGOS", btnPagos));
    }

    private void navegar(String pantalla, JButton botonActivo) {
        actualizarEstadoMenu(botonActivo);
        cardLayout.show(panelContenedor, pantalla);
    }

    public void actualizarEstadoMenu(JButton botonActivo) {
        for (JButton btn : botonesMenu) {
            if (btn == botonActivo) {
                btn.setBackground(AZUL_SELECCIONADO);
                btn.setForeground(AZUL_PRINCIPAL);
                btn.setFont(new Font("Arial", Font.BOLD, 15));
                btn.setOpaque(true);
            } else {
                btn.setBackground(GRIS_MENU_FONDO);
                btn.setForeground(GRIS_BOTON_TEXTO);
                btn.setFont(new Font("Arial", Font.PLAIN, 15));
                btn.setOpaque(false);
            }
        }
        repaint();
    }

    private void cerrarSesion() {
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Desea cerrar sesión?", "Confirmar",
                JOptionPane.YES_NO_OPTION);

        if (opcion == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
    /**
     * Permite a los paneles internos (hijos) cambiar el resaltado del menú
     * cuando ocurre una redirección automática.
     */
    public void marcarBotonPorPantalla(String nombrePantalla) {
        JButton botonADestacar = null;

        switch (nombrePantalla) {
            case "PANTALLA_REGISTRO":        botonADestacar = botonesMenu[0]; break;
            case "PANTALLA_ENTRADA":         botonADestacar = botonesMenu[1]; break;
            case "PANTALLA_SALIDA":          botonADestacar = botonesMenu[2]; break;
            case "PANTALLA_CELDAS":          botonADestacar = botonesMenu[3]; break;
            case "PANTALLA_GESTION_CELDAS":  botonADestacar = botonesMenu[4]; break;
            case "PANTALLA_PAGOS":           botonADestacar = botonesMenu[5]; break;
        }

        if (botonADestacar != null) {
            actualizarEstadoMenu(botonADestacar);
        }
    }
}