package parqueadero.ui;

import parqueadero.model.Usuario;
import parqueadero.service.ParqueaderoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class DashboardFrame extends JFrame {

    private final ParqueaderoService service;
    private final Usuario usuario;

    private CardLayout cardLayout;
    private JPanel panelContenedor;

    private final Color AZUL_PRINCIPAL = new Color(30, 110, 220);
    private final Color GRIS_MENU_FONDO = new Color(225, 225, 225);
    private final Color GRIS_BOTON_TEXTO = new Color(50, 50, 50);
    private final Color NEGRO_TEXTO = Color.BLACK;
    private final Color AZUL_BOTON = new Color(45, 55, 125);

    private JButton[] botonesMenu;
    private final Color AZUL_SELECCIONADO = new Color(180, 210, 250);

//PANEL PRINCIPAL
    public DashboardFrame(Usuario usuario, ParqueaderoService service) {
        this.usuario = usuario;
        this.service = service;

        setTitle("Autos Colombia - Dashboard");
        setSize(1100, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- 1. HEADER ---
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(AZUL_PRINCIPAL);
        panelHeader.setPreferredSize(new Dimension(0, 60));
        panelHeader.setBorder(new EmptyBorder(0, 20, 0, 20));

        JPanel panelHeaderIzquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        panelHeaderIzquierda.setOpaque(false);
        JLabel lblIconoMenu = new JLabel("≡");
        lblIconoMenu.setForeground(Color.WHITE);
        lblIconoMenu.setFont(new Font("Arial", Font.BOLD, 24));
        JLabel lblTitulo = new JLabel("Autos Colombia");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        panelHeaderIzquierda.add(lblIconoMenu);
        panelHeaderIzquierda.add(lblTitulo);

        JPanel panelHeaderDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panelHeaderDerecha.setOpaque(false);
        JLabel lblIconoUser = new JLabel("👤");
        lblIconoUser.setForeground(Color.WHITE);
        lblIconoUser.setFont(new Font("Arial", Font.BOLD, 20));
        JLabel lblRol = new JLabel("Administrador");
        lblRol.setForeground(Color.WHITE);
        lblRol.setFont(new Font("Arial", Font.PLAIN, 16));
        panelHeaderDerecha.add(lblIconoUser);
        panelHeaderDerecha.add(lblRol);

        panelHeader.add(panelHeaderIzquierda, BorderLayout.WEST);
        panelHeader.add(panelHeaderDerecha, BorderLayout.EAST);
        add(panelHeader, BorderLayout.NORTH);

        // --- 2. PANEL OESTE (MENÚ) ---
        JPanel panelOeste = new JPanel();
        panelOeste.setLayout(new BoxLayout(panelOeste, BoxLayout.Y_AXIS));
        panelOeste.setBackground(GRIS_MENU_FONDO);
        panelOeste.setPreferredSize(new Dimension(220, 0));
        panelOeste.setBorder(new EmptyBorder(20, 15, 20, 15));

        JLabel lblMenuTitulo = new JLabel("Menú");
        lblMenuTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblMenuTitulo.setForeground(NEGRO_TEXTO);
        lblMenuTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelOeste.add(lblMenuTitulo);
        panelOeste.add(Box.createRigidArea(new Dimension(0, 25)));

        JButton btnUsuarios = crearBotonMenu("👤 Usuarios", true);
        JButton btnEntradas = crearBotonMenu("≡ Entradas", false);
        JButton btnSalidas = crearBotonMenu("⟃ Salidas", false);
        JButton btnCeldas = crearBotonMenu("⊡ Celdas", false);
        JButton btnPagos = crearBotonMenu("🤝 Pagos", false);

        botonesMenu = new JButton[]{btnUsuarios, btnEntradas, btnSalidas, btnCeldas, btnPagos};

        panelOeste.add(btnUsuarios); panelOeste.add(Box.createRigidArea(new Dimension(0, 10)));
        panelOeste.add(btnEntradas); panelOeste.add(Box.createRigidArea(new Dimension(0, 10)));
        panelOeste.add(btnSalidas);  panelOeste.add(Box.createRigidArea(new Dimension(0, 10)));
        panelOeste.add(btnCeldas);   panelOeste.add(Box.createRigidArea(new Dimension(0, 10)));
        panelOeste.add(btnPagos);
        panelOeste.add(Box.createVerticalGlue());
        add(panelOeste, BorderLayout.WEST);

        // --- 3. CONFIGURACIÓN DEL CONTENEDOR (CardLayout) ---
        cardLayout = new CardLayout();
        panelContenedor = new JPanel(cardLayout);
        add(panelContenedor, BorderLayout.CENTER);

        // --- 4. DISEÑO DE REGISTRO DE USUARIOS ---
        JPanel panelCentralRegistro = new JPanel(new BorderLayout());

        panelCentralRegistro.setBackground(Color.WHITE);
        panelCentralRegistro.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel lblRuta = new JLabel("<html><font color='gray'>👤</font> Usuarios / <font color='black'>Nuevo usuario</font></html>");
        lblRuta.setFont(new Font("Arial", Font.PLAIN, 14));
        panelCentralRegistro.add(lblRuta, BorderLayout.NORTH);

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBackground(Color.WHITE);
        panelFormulario.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 15, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel lblTituloForm = new JLabel("Registrar nuevo usuario");
        lblTituloForm.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panelFormulario.add(lblTituloForm, gbc);

        gbc.gridwidth = 1;
        panelFormulario.add(crearLabelForm("NOMBRE COMPLETO *"), pos(0, 1, gbc));
        panelFormulario.add(crearFieldForm(), pos(0, 2, gbc));
        panelFormulario.add(crearLabelForm("TELÉFONO *"), pos(0, 3, gbc));
        panelFormulario.add(crearFieldForm(), pos(0, 4, gbc));
        panelFormulario.add(crearLabelForm("PLACA DEL VEHÍCULO *"), pos(0, 5, gbc));
        panelFormulario.add(crearFieldForm(), pos(0, 6, gbc));
        panelFormulario.add(crearLabelForm("TIPO DE USUARIO"), pos(0, 7, gbc));

        panelFormulario.add(crearLabelForm("NÚMERO DE DOCUMENTO *"), pos(1, 1, gbc));
        panelFormulario.add(crearFieldForm(), pos(1, 2, gbc));
        panelFormulario.add(crearLabelForm("CORREO ELECTRÓNICO"), pos(1, 3, gbc));
        panelFormulario.add(crearFieldForm(), pos(1, 4, gbc));

        JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"Seleccionar", "Mensual", "Visitante"});
        cmbTipo.setPreferredSize(new Dimension(0, 35));
        cmbTipo.setBackground(Color.WHITE);
        panelFormulario.add(cmbTipo, pos(0, 8, gbc));

        JPanel panelBotonesAccion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelBotonesAccion.setOpaque(false);
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(100, 40));
        btnCancelar.setBackground(Color.WHITE);
        btnCancelar.setBorder(new LineBorder(Color.GRAY, 1, true));

        JButton btnRegistrar = new JButton("Registrar usuario");
        btnRegistrar.setPreferredSize(new Dimension(180, 40));
        btnRegistrar.setBackground(AZUL_BOTON);
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setBorder(BorderFactory.createCompoundBorder(new LineBorder(AZUL_BOTON, 1, true), new EmptyBorder(0, 15, 0, 15)));

        panelBotonesAccion.add(btnCancelar);
        panelBotonesAccion.add(btnRegistrar);

        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2;
        panelFormulario.add(panelBotonesAccion, gbc);
        panelCentralRegistro.add(panelFormulario, BorderLayout.CENTER);

        // --- 5. REGISTRO DE PANTALLAS ---
        panelContenedor.add(new PanelRegistro(service, cardLayout, panelContenedor), "PANTALLA_REGISTRO");
        panelContenedor.add(new PanelEntradas(service, cardLayout, panelContenedor), "PANTALLA_ENTRADA");
        panelContenedor.add(new PanelSalidas(service, cardLayout, panelContenedor), "PANTALLA_SALIDA");
        panelContenedor.add(new PanelCeldas(service, cardLayout, panelContenedor), "PANTALLA_CELDAS");
        panelContenedor.add(new PanelConsultaCeldas(service, cardLayout, panelContenedor), "PANTALLA_NUEVA_CELDA");
        // --- 6. EVENTOS DE REDIRECCIÓN ---
        // Inicializa el arreglo antes de los eventos
        botonesMenu = new JButton[]{btnUsuarios, btnEntradas, btnSalidas, btnCeldas, btnPagos};

        btnUsuarios.addActionListener(e -> {
            actualizarEstadoMenu(btnUsuarios); // <--- ANEXO
            cardLayout.show(panelContenedor, "PANTALLA_REGISTRO");
        });

        btnEntradas.addActionListener(e -> {
            actualizarEstadoMenu(btnEntradas); // <--- ANEXO
            cardLayout.show(panelContenedor, "PANTALLA_ENTRADA");
        });

        btnSalidas.addActionListener(e -> {
            actualizarEstadoMenu(btnSalidas);
            cardLayout.show(panelContenedor, "PANTALLA_SALIDA");
        });

        btnCeldas.addActionListener(e -> {
            actualizarEstadoMenu(btnCeldas);
            cardLayout.show(panelContenedor, "PANTALLA_CELDA");
        });

        btnPagos.addActionListener(e -> {
            actualizarEstadoMenu(btnPagos);
            cardLayout.show(panelContenedor, "PANTALLA_PAGOS");
        });

        btnUsuarios.addActionListener(e -> cardLayout.show(panelContenedor, "PANTALLA_REGISTRO"));
        btnEntradas.addActionListener(e -> cardLayout.show(panelContenedor, "PANTALLA_ENTRADA"));
        btnSalidas.addActionListener(e -> cardLayout.show(panelContenedor, "PANTALLA_SALIDA"));
        btnCeldas.addActionListener(e -> cardLayout.show(panelContenedor, "PANTALLA_CELDAS"));
        btnCeldas.addActionListener(e -> cardLayout.show(panelContenedor, "PANTALLA_PAGOS"));

        // Botón cancelar del formulario de registro
        btnCancelar.addActionListener(e -> cardLayout.show(panelContenedor, "PANTALLA_ENTRADA"));

        // Vista inicial
        cardLayout.show(panelContenedor, "PANTALLA_ENTRADA");
    }

    private JButton crearBotonMenu(String texto, boolean seleccionado) {
        JButton btn = new JButton(texto);
        btn.setMaximumSize(new Dimension(200, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(new Font("Arial", seleccionado ? Font.BOLD : Font.PLAIN, 15));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (seleccionado) {
            btn.setBackground(new Color(180, 210, 250));
            btn.setForeground(AZUL_PRINCIPAL);
        } else {
            btn.setBackground(GRIS_MENU_FONDO);
            btn.setForeground(GRIS_BOTON_TEXTO);
        }
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private JLabel crearLabelForm(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 100, 100));
        return lbl;
    }

    private JTextField crearFieldForm() {
        JTextField txt = new JTextField();
        txt.setPreferredSize(new Dimension(0, 35));
        txt.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 200, 200), 1, true), new EmptyBorder(5, 10, 5, 10)));
        return txt;
    }

    private GridBagConstraints pos(int x, int y, GridBagConstraints gbc) {
        gbc.gridx = x; gbc.gridy = y; return gbc;
    }

    public void actualizarEstadoMenu(JButton botonActivo) {
        for (JButton btn : botonesMenu) {
            if (btn == botonActivo) {
                btn.setBackground(AZUL_SELECCIONADO);
                btn.setFont(new Font("Arial", Font.BOLD, 15));
                btn.setForeground(AZUL_PRINCIPAL);
                btn.setOpaque(true);
            } else {
                btn.setBackground(GRIS_MENU_FONDO);
                btn.setFont(new Font("Arial", Font.PLAIN, 15));
                btn.setForeground(GRIS_BOTON_TEXTO);
                btn.setOpaque(false);
            }
        }
        // Forzar a la interfaz a repintarse
        this.repaint();
    }
}