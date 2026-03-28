package parqueadero.ui;

import parqueadero.service.ParqueaderoService;

import javax.swing.*;
import java.awt.*;

public class PanelPagos extends JPanel {
    private final ParqueaderoService service;
    private final CardLayout cardLayout;
    private final JPanel panelContenedor;

    // Campos para editar tarifa
    private JTextField txtPrecioCarro = new JTextField(10);
    private JTextField txtPrecioMoto = new JTextField(10);
    private JButton btnGuardar = new JButton("Actualizar Tarifas");

    public PanelPagos(ParqueaderoService service, CardLayout cardLayout, JPanel panelContenedor) {
        this.service = service;
        this.cardLayout = cardLayout;
        this.panelContenedor = panelContenedor;

        setLayout(new GridLayout(3, 2, 10, 10));
        setBorder(BorderFactory.createTitledBorder("Configuración de Tarifas (Precio x Minuto)"));

        add(new JLabel("Valor Minuto Carro:"));
        add(txtPrecioCarro);
        add(new JLabel("Valor Minuto Moto:"));
        add(txtPrecioMoto);
        add(btnGuardar);

        btnGuardar.addActionListener(e -> {
            // Aquí llamarías a un SP nuevo o un UPDATE directo a la tabla tarifa
            // UPDATE tarifa SET precio_por_minuto = ? WHERE tipo_vehiculo = 'CARRO'
            JOptionPane.showMessageDialog(this, "Tarifas actualizadas en la Base de Datos");
        });
    }
}