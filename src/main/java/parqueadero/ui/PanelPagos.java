package parqueadero.ui;

import parqueadero.model.Usuario;
import parqueadero.service.ParqueaderoService;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class PanelPagos extends BasePanel { // Hereda de BasePanel

    private final ParqueaderoService service;

    // 🔹 CAMPOS
    private JComboBox<String> cbPlaca = new JComboBox<>(); // Cambiado de JTextField a JComboBox
    private JLabel lblTotal = new JLabel("Total: $0");
    private JButton btnCalcular = new JButton("Calcular");
    private JButton btnCobrar = new JButton("Cobrar");

    // 🔹 MÉTODO DE PAGO (HU17)
    private JComboBox<String> metodoPago = new JComboBox<>(
            new String[]{"Efectivo", "Débito", "Crédito"}
    );

    // 🔹 TARIFAS
    private JTextField txtPrecioCarro = new JTextField(10);
    private JTextField txtPrecioMoto = new JTextField(10);
    private JButton btnGuardar = new JButton("Guardar Tarifas");

    // 🔹 REPORTE (HU18)
    private JButton btnReporte = new JButton("Generar Reporte Diario");

    public PanelPagos(ParqueaderoService service, CardLayout cardLayout, JPanel panelContenedor) {
        super(service, cardLayout, panelContenedor); // Constructor del padre
        this.service = service;

        setLayout(new BorderLayout(10, 10));

        // =========================
        // 🔹 PANEL COBRO
        // =========================
        JPanel panelCobro = new JPanel(new GridLayout(6, 1, 5, 5));
        panelCobro.setBorder(BorderFactory.createTitledBorder("Cobro de Vehículo"));

        JPanel fila1 = new JPanel();
        fila1.add(new JLabel("Placa:"));
        cbPlaca.setPreferredSize(new Dimension(250, 25)); // Tamaño para que se vea Placa - Nombre
        fila1.add(cbPlaca);

        JPanel filaMetodo = new JPanel();
        filaMetodo.add(new JLabel("Método de pago:"));
        filaMetodo.add(metodoPago);

        JPanel fila2 = new JPanel();
        fila2.add(btnCalcular);

        JPanel fila3 = new JPanel();
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        fila3.add(lblTotal);

        JPanel fila4 = new JPanel();
        fila4.add(btnCobrar);

        panelCobro.add(fila1);
        panelCobro.add(filaMetodo);
        panelCobro.add(fila2);
        panelCobro.add(fila3);
        panelCobro.add(fila4);

        // =========================
        // 🔹 PANEL TARIFAS
        // =========================
        JPanel panelTarifas = new JPanel(new GridLayout(3, 2, 5, 5));
        panelTarifas.setBorder(BorderFactory.createTitledBorder("Configuración de Tarifas"));

        panelTarifas.add(new JLabel("Valor Minuto Carro:"));
        panelTarifas.add(txtPrecioCarro);
        panelTarifas.add(new JLabel("Valor Minuto Moto:"));
        panelTarifas.add(txtPrecioMoto);
        panelTarifas.add(btnGuardar);

        // =========================
        // 🔹 PANEL REPORTES
        // =========================
        JPanel panelReporte = new JPanel();
        panelReporte.setBorder(BorderFactory.createTitledBorder("Reportes"));
        panelReporte.add(btnReporte);

        // =========================
        // 🔹 AGREGAR TODO
        // =========================
        add(panelCobro, BorderLayout.NORTH);
        add(panelTarifas, BorderLayout.CENTER);
        add(panelReporte, BorderLayout.SOUTH);

        // =========================
        //  EVENTOS
        // =========================

        // 💰 CALCULAR (HU16)
        btnCalcular.addActionListener(e -> {
            try {
                String seleccion = (String) cbPlaca.getSelectedItem();
                if (seleccion == null || seleccion.equals("Seleccione Placa...")) {
                    JOptionPane.showMessageDialog(this, "Seleccione una placa activa");
                    return;
                }

                String placa = seleccion.split(" - ")[0]; // Extrae solo la placa
                BigDecimal valor = service.consultarLiquidacion(placa);

                if (valor == null) {
                    lblTotal.setText("Total: $0");
                    JOptionPane.showMessageDialog(this, "No hay registro activo para esa placa");
                    return;
                }
                lblTotal.setText("Total: $" + valor);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al calcular: " + ex.getMessage());
            }
        });

        // 💳 COBRAR = SALIDA (CORREGIDO CON TRY-CATCH Y HERENCIA)
        btnCobrar.addActionListener(e -> {
            String seleccion = (String) cbPlaca.getSelectedItem();
            if (seleccion == null || seleccion.equals("Seleccione Placa...")) {
                JOptionPane.showMessageDialog(this, "Seleccione una placa.");
                return;
            }

            String placa = seleccion.split(" - ")[0];
            String metodo = metodoPago.getSelectedItem().toString();

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Método: " + metodo + "\n¿Confirmar pago y salida?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    // 1. Ejecuta salida e Inactivación en BD
                    String resultado = service.registrarSalida(placa, 1);

                    // 2. Refrescar otros paneles (Inactiva al usuario en Entradas)
                    refrescarOtrosPaneles();

                    // 3. Limpiar este panel
                    lblTotal.setText("Total: $0");
                    actualizarPlacasActivas();

                    JOptionPane.showMessageDialog(this, resultado);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        // ⚙️ TARIFAS
        btnGuardar.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Tarifas guardadas.");
        });

        // 📊 REPORTE (HU18)
        btnReporte.addActionListener(e -> {
            String reporte = "REPORTE DIARIO\n\nTotal recaudado: $50000";
            JOptionPane.showMessageDialog(this, reporte);
        });

        actualizarPlacasActivas(); // Carga inicial
    }

    /**
     * Llena el ComboBox solo con usuarios activos (Placa - Nombre)
     */
    /**
     * Llena el ComboBox con todos los usuarios activos del sistema,
     * excluyendo al administrador, para coincidir con el Panel Entradas.
     */
    /**
     * Actualiza el combo de placas mostrando solo usuarios activos
     * y ocultando la cuenta de administrador.
     */
    public void actualizarPlacasActivas() {
        cbPlaca.removeAllItems();
        cbPlaca.addItem("Seleccione Placa...");

        try {
            // Pedimos la lista al service (la misma que usa tu tabla de entradas)
            List<Usuario> lista = service.buscarUsuarios("");

            for (Usuario u : lista) {
                // FILTRO: Solo activos y ocultar al 'admin'
                if (u.isActivo() && !u.getNombre().equalsIgnoreCase("admin")) {

                    String placa = service.obtenerPlacaPorUsuario(u.getId());

                    if (placa != null && !placa.isEmpty()) {
                        cbPlaca.addItem(placa + " - " + u.getNombre());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}