package parqueadero.ui;

import parqueadero.model.Celda;
import parqueadero.model.Usuario;
import parqueadero.service.ParqueaderoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class PanelConsultaCeldas extends BasePanel {

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

    // Labels de resultados
    private JLabel lblIdValor, lblTipoValor, lblEstadoValor;
    private JLabel lblUsuarioValor, lblCeldaValor, lblEstadoAsigValor, lblCeldaAsigValor;

    // Colores
    private final Color AZUL_BTN = new Color(45, 55, 125);
    private final Color VERDE_EXITO = new Color(40, 160, 80);
    private final Color AZUL_EXITO = new Color(45, 100, 200);

    public PanelConsultaCeldas(ParqueaderoService service, CardLayout cardLayout, JPanel panelContenedor) {
        super(service, cardLayout, panelContenedor);

        setLayout(new GridLayout(1, 2, 25, 0));
        setBorder(new EmptyBorder(25, 35, 25, 35));

        add(construirPanelIzquierdo());
        add(construirPanelDerecho());

    }

    // ====================== PANEL IZQUIERDO ======================
    private JPanel construirPanelIzquierdo() {
        JPanel p = panelBase("Registrar nueva celda");

        p.add(new JLabel("Identificador (Numérico)*"));
        txtIdentificador = new JTextField();
        txtIdentificador.setPreferredSize(new Dimension(0, 38));
        p.add(txtIdentificador);

        p.add(Box.createVerticalStrut(15));
        p.add(new JLabel("Tipo de vehículo*"));

        JPanel fTipo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        fTipo.setOpaque(false);
        btnCarro = crearBotonTipo("Carro", true);
        btnMoto = crearBotonTipo("Moto", false);

        btnCarro.addActionListener(e -> alternarTipo("CARRO"));
        btnMoto.addActionListener(e -> alternarTipo("MOTO"));

        fTipo.add(btnCarro);
        fTipo.add(btnMoto);
        p.add(fTipo);

        p.add(Box.createVerticalStrut(15));
        p.add(new JLabel("Estado inicial"));
        cbEstadoInicial = new JComboBox<>(new String[]{"Disponible", "Ocupada"});
        p.add(cbEstadoInicial);

        p.add(Box.createVerticalStrut(25));

        JButton btnGuardar = botonPrimario("Guardar celda", 0); // ancho 0 = full
        btnGuardar.addActionListener(e -> ejecutarRegistro());
        p.add(btnGuardar);

        // Panel de éxito (verde)
        pnlMensajeExito = crearPanelExito(VERDE_EXITO);
        p.add(pnlMensajeExito);
        p.add(Box.createVerticalGlue());

        return p;
    }

    // ====================== PANEL DERECHO ======================
    private JPanel construirPanelDerecho() {
        JPanel p = panelBase("Asignar celda a usuario mensual");

        p.add(new JLabel("PASO 1. Seleccione Usuario"));
        cbUsuariosMensual = new JComboBox<>();
        p.add(cbUsuariosMensual);

        p.add(Box.createVerticalStrut(20));
        p.add(new JLabel("PASO 2. Celda Disponible"));
        cbCeldasDisponibles = new JComboBox<>();
        p.add(cbCeldasDisponibles);

        p.add(Box.createVerticalStrut(25));

        JButton btnAsignar = botonPrimario("Asignar celda", 0);
        btnAsignar.addActionListener(e -> ejecutarAsignacion());
        p.add(btnAsignar);

        // Panel de éxito (azul)
        pnlAsignacionExito = crearPanelExito(AZUL_EXITO);
        p.add(pnlAsignacionExito);
        p.add(Box.createVerticalGlue());

        return p;
    }

    // ====================== MÉTODOS AUXILIARES ======================

    private JPanel panelBase(String titulo) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(210, 210, 210), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("Arial", Font.BOLD, 18));
        p.add(lbl);
        p.add(new JSeparator());
        p.add(Box.createVerticalStrut(15));

        return p;
    }

    private JPanel crearPanelExito(Color colorFondo) {
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 6));
        p.setBackground(colorFondo);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(colorFondo.darker(), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        p.setVisible(false);
        return p;
    }

    private JButton crearBotonTipo(String texto, boolean seleccionado) {
        JButton btn = new JButton(texto);
        btn.setPreferredSize(new Dimension(110, 38));
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (seleccionado) {
            btn.setBackground(AZUL_BTN);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(new Color(210, 210, 210));
            btn.setForeground(Color.BLACK);
        }
        return btn;
    }

    private void alternarTipo(String tipo) {
        tipoSeleccionado = tipo;
        btnCarro.setBackground(tipo.equals("CARRO") ? AZUL_BTN : new Color(210, 210, 210));
        btnMoto.setBackground(tipo.equals("MOTO") ? AZUL_BTN : new Color(210, 210, 210));
        btnCarro.setForeground(tipo.equals("CARRO") ? Color.WHITE : Color.BLACK);
        btnMoto.setForeground(tipo.equals("MOTO") ? Color.WHITE : Color.BLACK);
    }

    // ====================== LÓGICA ======================

    private void ejecutarRegistro() {
        try {
            int id = Integer.parseInt(txtIdentificador.getText().trim());
            boolean disponible = "Disponible".equals(cbEstadoInicial.getSelectedItem());

            service.registrarCelda(id, tipoSeleccionado);

            mostrarExitoRegistro(id, tipoSeleccionado, disponible ? "Disponible" : "Ocupada");

            actualizarCombos();
            Timer timer = new Timer(2000, e -> cardLayout.show(panelContenedor, "PANTALLA_CELDAS"));
            timer.setRepeats(false);
            timer.start();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El identificador debe ser numérico.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar celda: " + ex.getMessage());
        }
    }

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

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en la asignación: " + ex.getMessage());
        }
    }

    private void mostrarExitoRegistro(int id, String tipo, String estado) {
        pnlMensajeExito.removeAll();
        pnlMensajeExito.setLayout(new GridLayout(4, 2, 8, 6));

        pnlMensajeExito.add(new JLabel("✓ Celda registrada", SwingConstants.CENTER) {{
            setFont(new Font("Arial", Font.BOLD, 14)); setForeground(Color.WHITE);
        }});
        pnlMensajeExito.add(new JLabel(""));

        pnlMensajeExito.add(new JLabel("Identificador:"));
        pnlMensajeExito.add(new JLabel("C" + id) {{ setFont(new Font("Arial", Font.BOLD, 12)); setForeground(Color.WHITE); }});

        pnlMensajeExito.add(new JLabel("Tipo:"));
        pnlMensajeExito.add(new JLabel(tipo) {{ setFont(new Font("Arial", Font.BOLD, 12)); setForeground(Color.WHITE); }});

        pnlMensajeExito.add(new JLabel("Estado:"));
        pnlMensajeExito.add(new JLabel(estado) {{ setFont(new Font("Arial", Font.BOLD, 12)); setForeground(Color.WHITE); }});

        pnlMensajeExito.setVisible(true);
        pnlMensajeExito.revalidate();
        pnlMensajeExito.repaint();
    }

    private void mostrarExitoAsignacion(int idUsuario, int idCelda) {
        pnlAsignacionExito.removeAll();
        pnlAsignacionExito.setLayout(new GridLayout(4, 2, 8, 6));

        pnlAsignacionExito.add(new JLabel("✓ Celda asignada", SwingConstants.CENTER) {{
            setFont(new Font("Arial", Font.BOLD, 14)); setForeground(Color.WHITE);
        }});
        pnlAsignacionExito.add(new JLabel(""));

        pnlAsignacionExito.add(new JLabel("Usuario:"));
        pnlAsignacionExito.add(new JLabel(String.valueOf(idUsuario)) {{ setFont(new Font("Arial", Font.BOLD, 12)); setForeground(Color.WHITE); }});

        pnlAsignacionExito.add(new JLabel("Celda:"));
        pnlAsignacionExito.add(new JLabel("C" + idCelda) {{ setFont(new Font("Arial", Font.BOLD, 12)); setForeground(Color.WHITE); }});

        pnlAsignacionExito.add(new JLabel("Tipo:"));
        pnlAsignacionExito.add(new JLabel("Mensual") {{ setFont(new Font("Arial", Font.BOLD, 12)); setForeground(Color.WHITE); }});

        pnlAsignacionExito.setVisible(true);
        pnlAsignacionExito.revalidate();
        pnlAsignacionExito.repaint();
    }

    public void actualizarCombos() {
        cargarUsuariosMensualesActivos();
        cargarCeldasDisponibles();
    }

    private void cargarUsuariosMensualesActivos() {
        cbUsuariosMensual.removeAllItems();
        cbUsuariosMensual.addItem("Seleccione usuario...");

        try {
            List<Usuario> usuarios = service.buscarUsuarios("");
            for (Usuario u : usuarios) {
                if (u.isActivo() && "Mensual".equalsIgnoreCase(String.valueOf(u.getTipoCliente()))) {
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
}