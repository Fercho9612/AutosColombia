package parqueadero.ui;

import parqueadero.model.Celda;
import parqueadero.service.ParqueaderoService;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * Panel de interfaz de usuario que visualiza el mapa de celdas del parqueadero.
 * Proporciona una representación gráfica (Grid) de las celdas, permite filtrar por
 * tipo y estado, y muestra estadísticas en tiempo real sobre la ocupación.

 * @version 1.0
 */
public class PanelCeldas extends JPanel {

    private final ParqueaderoService service;
    private final CardLayout cardLayout;
    private final JPanel panelContenedor;

    private JLabel lblTotal, lblDisponibles, lblOcupadas;
    private JPanel gridCeldas;
    private JComboBox<String> cbTipos, cbEstados;

    private final Color AZUL_BOTON = new Color(45, 55, 125);
    private final Color VERDE_DISPONIBLE = new Color(60, 180, 80);
    private final Color ROJO_OCUPADO = new Color(220, 60, 60);
    private final Color GRIS_FONDO = new Color(245, 245, 245);

    /**
     * Constructor que inicializa los componentes del mapa de celdas.
     * * @param service Servicio de lógica de negocio para gestionar los datos de las celdas.
     * @param cardLayout Layout del contenedor principal para la navegación entre pantallas.
     * @param panelContenedor Panel principal que contiene las diferentes vistas de la aplicación.
     */
    public PanelCeldas(ParqueaderoService service, CardLayout cardLayout, JPanel panelContenedor) {
        this.service = service;
        this.cardLayout = cardLayout;
        this.panelContenedor = panelContenedor;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // =======================================
        //          NORTE: Título + Estadísticas
        // =======================================
        JPanel panelNorte = new JPanel(new BorderLayout(0, 10));
        panelNorte.setOpaque(false);

        JLabel lblTitulo = new JLabel("MAPA DE CELDAS");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        panelNorte.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelStats = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        panelStats.setOpaque(false);

        lblTotal = crearIndicador("Total", "0");
        lblDisponibles = crearIndicador("Disponibles", "0");
        lblOcupadas = crearIndicador("Ocupadas", "0");

        panelStats.add(lblTotal);
        panelStats.add(lblDisponibles);
        panelStats.add(lblOcupadas);

        panelNorte.add(panelStats, BorderLayout.CENTER);
        add(panelNorte, BorderLayout.NORTH);

        // =======================================
        //       FILTROS + BOTÓN NUEVA CELDA
        // =======================================
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        panelFiltros.setOpaque(false);

        cbTipos = new JComboBox<>(new String[]{"Todos los tipos", "Carro", "Moto", "Mensual", "Semanal"});
        cbTipos.setPreferredSize(new Dimension(160, 35));
        panelFiltros.add(cbTipos);

        cbEstados = new JComboBox<>(new String[]{"Todos los estados", "Disponible", "Ocupado"});
        cbEstados.setPreferredSize(new Dimension(160, 35));
        panelFiltros.add(cbEstados);

        JButton btnActualizar = new JButton("Actualizar ⟳");
        btnActualizar.setPreferredSize(new Dimension(120, 35));
        btnActualizar.addActionListener(e -> actualizarMapa());
        panelFiltros.add(btnActualizar);

        JButton btnNuevaCelda = new JButton("+ Nueva celda");
        btnNuevaCelda.setBackground(AZUL_BOTON);
        btnNuevaCelda.setForeground(Color.WHITE);
        btnNuevaCelda.setPreferredSize(new Dimension(160, 35));
        btnNuevaCelda.addActionListener(e ->
                cardLayout.show(panelContenedor, "PANTALLA_NUEVA_CELDA")  // ← redirige al formulario
        );
        panelFiltros.add(Box.createHorizontalStrut(20));
        panelFiltros.add(btnNuevaCelda);

        add(panelFiltros, BorderLayout.CENTER); // temporal

        // =======================================
        //               MAPA (GRID)
        // =======================================
        gridCeldas = new JPanel(new GridLayout(0, 6, 12, 12)); // 6 columnas, filas automáticas
        gridCeldas.setOpaque(false);

        JScrollPane scroll = new JScrollPane(gridCeldas);
        scroll.setBorder(new LineBorder(new Color(200, 200, 200)));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrapperMapa = new JPanel(new BorderLayout());
        wrapperMapa.setBackground(GRIS_FONDO);
        wrapperMapa.setBorder(new EmptyBorder(15, 15, 15, 15));
        wrapperMapa.add(scroll, BorderLayout.CENTER);

        JPanel contenedorMapa = new JPanel(new BorderLayout(0, 15));
        contenedorMapa.add(panelFiltros, BorderLayout.NORTH);
        contenedorMapa.add(wrapperMapa, BorderLayout.CENTER);

        // Leyenda inferior
        JPanel panelLeyenda = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        panelLeyenda.setOpaque(false);

        JLabel lblDisp = new JLabel("● Disponible");
        lblDisp.setForeground(VERDE_DISPONIBLE);
        lblDisp.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel lblOcup = new JLabel("● Ocupado");
        lblOcup.setForeground(ROJO_OCUPADO);
        lblOcup.setFont(new Font("Arial", Font.BOLD, 14));

        panelLeyenda.add(lblDisp);
        panelLeyenda.add(lblOcup);

        contenedorMapa.add(panelLeyenda, BorderLayout.SOUTH);

        add(contenedorMapa, BorderLayout.CENTER);

        // Carga inicial
        actualizarMapa();
    }

    /**
     * Consulta el servicio para obtener la lista actualizada de celdas,
     * aplica los filtros seleccionados en la UI y reconstruye el grid gráfico.
     * También actualiza las etiquetas de estadísticas (Total, Disponibles, Ocupadas).
     */
    protected void actualizarMapa() {
        gridCeldas.removeAll();

        String filtroTipo = cbTipos.getSelectedItem().toString();
        String filtroEstado = cbEstados.getSelectedItem().toString();

        boolean todosTipos = filtroTipo.equals("Todos los tipos");
        boolean todosEstados = filtroEstado.equals("Todos los estados");

        try {
            List<Celda> celdas = service.listarCeldas();

            int total = celdas.size();
            int disponibles = 0;
            int ocupadas = 0;

            for (Celda c : celdas) {
                boolean pasaTipo = todosTipos || c.getTipo().equalsIgnoreCase(filtroTipo);
                boolean pasaEstado = todosEstados ||
                        (filtroEstado.equals("Disponible") && c.isDisponible()) ||
                        (filtroEstado.equals("Ocupado") && !c.isDisponible());

                if (pasaTipo && pasaEstado) {
                    JButton btn = crearBotonCelda(c);
                    gridCeldas.add(btn);

                    if (c.isDisponible()) disponibles++;
                    else ocupadas++;
                }
            }

            lblTotal.setText(String.valueOf(total));
            lblDisponibles.setText(String.valueOf(disponibles));
            lblOcupadas.setText(String.valueOf(ocupadas));

            gridCeldas.revalidate();
            gridCeldas.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar mapa de celdas");
        }
    }

    /**
     * Crea un botón estilizado que representa visualmente una celda individual.
     * El color del botón cambia según la disponibilidad (Verde: Libre, Rojo: Ocupado).
     * * @param celda El objeto Celda con la información a mostrar.
     * @return Un JButton configurado con el ID y tipo de la celda.
     */
    private JButton crearBotonCelda(Celda celda) {
        String texto = "<html><center><b>C" + String.format("%02d", celda.getId()) + "</b><br>" +
                "<font size='3'>" + celda.getTipo() + "</font></center></html>";

        JButton btn = new JButton(texto);
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(new Color(180, 180, 180), 1, true));
        btn.setPreferredSize(new Dimension(90, 70));

        if (celda.isDisponible()) {
            btn.setBackground(VERDE_DISPONIBLE);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(ROJO_OCUPADO);
            btn.setForeground(Color.WHITE);
        }

        // Acción al clic (opcional: mostrar info o liberar)
        btn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Celda: C" + celda.getId() + "\n" +
                            "Tipo: " + celda.getTipo() + "\n" +
                            "Estado: " + (celda.isDisponible() ? "Disponible" : "Ocupada"));
        });

        return btn;
    }

    /**
     * Crea una etiqueta (JLabel) diseñada para mostrar indicadores estadísticos.
     * * @param titulo El nombre del indicador (ej: "Total").
     * @param valorInicial El valor numérico inicial a mostrar.
     * @return Un JLabel configurado con borde y fuente específica.
     */

    private JLabel crearIndicador(String titulo, String valorInicial) {
        JLabel lbl = new JLabel(titulo + ": " + valorInicial, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 16));
        lbl.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        lbl.setPreferredSize(new Dimension(140, 50));
        lbl.setOpaque(true);
        lbl.setBackground(new Color(250, 250, 250));
        return lbl;
    }
}