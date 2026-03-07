package autoscolombia.parqueadero.ui;

import autoscolombia.parqueadero.model.Usuario;
import autoscolombia.parqueadero.model.Registro;
import autoscolombia.parqueadero.service.ParqueaderoService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class DashboardFrame extends JFrame {
    private final ParqueaderoService service;
    private final Usuario usuario;

    public DashboardFrame(Usuario usuario, ParqueaderoService service) {
        this.usuario = usuario;
        this.service = service;
        setTitle("Parqueadero Autos Colombia - Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Panel superior con bienvenida
        JLabel lblBienvenida = new JLabel("Bienvenido, " + usuario.getNombre() + " (" + usuario.getRol() + ")", SwingConstants.CENTER);
        lblBienvenida.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblBienvenida, BorderLayout.NORTH);

        // Panel central con botones grandes
        JPanel panelBotones = new JPanel(new GridLayout(2, 2, 20, 20));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JButton btnEntrada = new JButton("Registrar Entrada");
        JButton btnSalida = new JButton("Registrar Salida");
        JButton btnActivos = new JButton("Vehículos Activos");
        JButton btnSalir = new JButton("Cerrar Sesión");

        btnEntrada.setFont(new Font("Arial", Font.PLAIN, 18));
        btnSalida.setFont(new Font("Arial", Font.PLAIN, 18));
        btnActivos.setFont(new Font("Arial", Font.PLAIN, 18));
        btnSalir.setFont(new Font("Arial", Font.PLAIN, 18));

        panelBotones.add(btnEntrada);
        panelBotones.add(btnSalida);
        panelBotones.add(btnActivos);
        panelBotones.add(btnSalir);

        add(panelBotones, BorderLayout.CENTER);

        // Acciones
        btnEntrada.addActionListener(e -> registrarEntrada());
        btnSalida.addActionListener(e -> registrarSalida());
        btnActivos.addActionListener(e -> mostrarVehiculosActivos());
        btnSalir.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    private void registrarEntrada() {
        JTextField txtPlaca = new JTextField(10);
        String[] tipos = {"CARRO", "MOTO"};
        JComboBox<String> cmbTipo = new JComboBox<>(tipos);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Placa:"));
        panel.add(txtPlaca);
        panel.add(new JLabel("Tipo:"));
        panel.add(cmbTipo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Registrar Entrada", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                service.registrarEntrada(txtPlaca.getText().trim(), (String) cmbTipo.getSelectedItem(), usuario.getId());
                JOptionPane.showMessageDialog(this, "Entrada registrada exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void registrarSalida() {
        String placa = JOptionPane.showInputDialog(this, "Ingrese la placa:", "Registrar Salida", JOptionPane.QUESTION_MESSAGE);
        if (placa != null && !placa.trim().isEmpty()) {
            try {
                long minutos = service.registrarSalida(placa.trim());
                long horas = minutos / 60;
                long minsRest = minutos % 60;
                JOptionPane.showMessageDialog(this,
                        "Salida registrada.\nTiempo de permanencia: " + horas + " horas y " + minsRest + " minutos",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void mostrarVehiculosActivos() {
        try {
            List<Registro> activos = service.getVehiculosActivos();
            if (activos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay vehículos activos en este momento.", "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] columnas = {"Placa", "Celda", "Hora Entrada"};
            DefaultTableModel model = new DefaultTableModel(columnas, 0);

            for (Registro r : activos) {
                model.addRow(new Object[]{
                        r.getPlacaVehiculo(),
                        r.getIdCelda(),
                        r.getHoraEntrada()
                });
            }

            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(600, 300));

            JOptionPane.showMessageDialog(this, scrollPane, "Vehículos Activos", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
