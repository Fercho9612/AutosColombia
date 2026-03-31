package parqueadero.ui;
import parqueadero.service.ParqueaderoService;

import parqueadero.model.Celda;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/**
 * Panel que visualiza el mapa de celdas del parqueadero en tiempo real.
 * Permite filtrar por tipo y estado, y ver el estado visual de cada celda.
 */
public class PanelCeldas extends BasePanel {

    private JLabel lblTotal, lblDisponibles, lblOcupadas;
    private JPanel gridCeldas;
    private JComboBox<String> cbTipos, cbEstados;

    // Colores específicos de este panel
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
        super(service, cardLayout, panelContenedor);

        // Configuración específica
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // Sección Norte: Título + Indicadores
        add(crearPanelNorte(), BorderLayout.NORTH);

        // Sección Central: Filtros + Grid de celdas
        add(crearPanelCentral(), BorderLayout.CENTER);

        // Sección Sur: Leyenda
        add(crearPanelLeyenda(), BorderLayout.SOUTH);

        // Carga inicial del mapa
        actualizarMapa();
    }

    /** Panel superior con título y contadores */
    private JPanel crearPanelNorte() {
        JPanel panelNorte = new JPanel(new BorderLayout(0, 10));
        panelNorte.setOpaque(false);

        JLabel lblTitulo = new JLabel("MAPA DE CELDAS");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        panelNorte.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelStats = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelStats.setOpaque(false);

        lblTotal = crearIndicador("Total Celdas", "0");
        lblDisponibles = crearIndicador("Disponibles", "0");
        lblOcupadas = crearIndicador("Ocupadas", "0");

        panelStats.add(lblTotal);
        panelStats.add(lblDisponibles);
        panelStats.add(lblOcupadas);

        panelNorte.add(panelStats, BorderLayout.CENTER);
        add(panelNorte, BorderLayout.NORTH);
        return panelNorte;
    }

    /** Panel central: Filtros + Mapa de celdas */
    private JPanel crearPanelCentral() {
        JPanel contenedor = new JPanel(new BorderLayout(0, 15));
        contenedor.setOpaque(false);

        // Barra de filtros y acciones - BOTÓN NUEVA CELDA
        JPanel barraHerramientas = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        barraHerramientas.setOpaque(false);

        cbTipos = new JComboBox<>(new String[]{"Todos los tipos", "Carro", "Moto"});
        cbTipos.setPreferredSize(new Dimension(160, 35));

        cbEstados = new JComboBox<>(new String[]{"Todos los estados", "Disponible", "Ocupado"});
        cbEstados.setPreferredSize(new Dimension(160, 35));

        JButton btnActualizar = new JButton("Actualizar ⟳");
        btnActualizar.setPreferredSize(new Dimension(120, 35));
        btnActualizar.addActionListener(e -> actualizarMapa());

        // Botón Gestionar usando método de BasePanel
        JButton btnGestion = botonPrimario("+ Gestionar Celdas", 160);
        btnGestion.setBackground(AZUL_BTN);
        btnGestion.setForeground(Color.WHITE);
        btnGestion.setPreferredSize(new Dimension(160, 35));
        btnGestion.addActionListener(e -> cardLayout.show(panelContenedor, "PANTALLA_GESTION_CELDAS"));

        Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof DashboardFrame) {
            ((DashboardFrame) w).marcarBotonPorPantalla("PANTALLA_GESTION_CELDAS");
        }

        barraHerramientas.add(new JLabel("Filtrar:"));
        barraHerramientas.add(cbTipos);
        barraHerramientas.add(cbEstados);
        barraHerramientas.add(btnActualizar);
        barraHerramientas.add(Box.createHorizontalStrut(30));
        barraHerramientas.add(btnGestion);

        // Grid de celdas
        gridCeldas = new JPanel(new GridLayout(0, 6, 15, 15));
        gridCeldas.setOpaque(false);

        JScrollPane scroll = new JScrollPane(gridCeldas);
        scroll.setBorder(new LineBorder(new Color(200, 200, 200)));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(null); // Sin bordes para verse limpio como el mockup
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        JPanel wrapperMapa = new JPanel(new BorderLayout());
        wrapperMapa.setBackground(GRIS_FONDO);
        wrapperMapa.setBorder(new EmptyBorder(15, 15, 15, 15));
        wrapperMapa.add(scroll, BorderLayout.CENTER);

        contenedor.add(barraHerramientas, BorderLayout.NORTH);
        contenedor.add(wrapperMapa, BorderLayout.CENTER);

        return contenedor;
    }

    /** Panel inferior con leyenda de colores */
    private JPanel crearPanelLeyenda() {
        JPanel panelLeyenda = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10));
        panelLeyenda.setOpaque(false);

        JLabel lblDisp = new JLabel("● Disponible");
        lblDisp.setForeground(VERDE_DISPONIBLE);
        lblDisp.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel lblOcup = new JLabel("● Ocupado");
        lblOcup.setForeground(ROJO_OCUPADO);
        lblOcup.setFont(new Font("Arial", Font.BOLD, 14));

        panelLeyenda.add(lblDisp);
        panelLeyenda.add(lblOcup);

        //contenedorMapa.add(panelLeyenda, BorderLayout.SOUTH);
        //add(contenedorMapa, BorderLayout.CENTER);

        // Carga inicial
        actualizarMapa();

        return panelLeyenda;
    }

    /**
     * Actualiza el mapa de celdas según los filtros seleccionados
     */

    public void actualizarMapa() {
        gridCeldas.removeAll(); // Limpiamos para redibujar con datos nuevos de la BD

        try {
            // Consultamos la BD a través del servicio
            List<Celda> listaCeldas = service.listarCeldas();

            int ocupadas = 0;
            int disponibles = 0;

            for (Celda c : listaCeldas) {
                // Contamos según el estado real en BD
                if (c.isDisponible()) disponibles++;
                else ocupadas++;

                // Aplicamos filtros visuales sin alterar la BD
                String tipoFiltro = cbTipos.getSelectedItem().toString();
                String estadoFiltro = cbEstados.getSelectedItem().toString();

                boolean matchTipo = tipoFiltro.equals("Todos los tipos") ||
                        tipoFiltro.equalsIgnoreCase(c.getTipo().name());

                boolean matchEstado = estadoFiltro.equals("Todos los estados") ||
                        (estadoFiltro.equals("Disponible") && c.isDisponible()) ||
                        (estadoFiltro.equals("Ocupado") && !c.isDisponible());

                if (matchTipo && matchEstado) {
                    gridCeldas.add(crearBotonCelda(c)); // El botón recibirá el color correcto
                }
            }

            // Actualizamos los indicadores con los valores de la BD
            lblTotal.setText("Total Celdas: " + listaCeldas.size());
            lblDisponibles.setText("Disponibles: " + disponibles);
            lblOcupadas.setText("Ocupadas: " + ocupadas);

            gridCeldas.revalidate();
            gridCeldas.repaint();

        } catch (Exception e) {
            System.err.println("Error al sincronizar con BD: " + e.getMessage());
        }
    }


    /**
     * Crea un botón estilizado que representa visualmente una celda individual.
     * El color del botón cambia según la disponibilidad (Verde: Libre, Rojo: Ocupado).
     * * @param celda El objeto Celda con la información a mostrar.
     * @return Un JButton configurado con el ID y tipo de la celda.
     */
    /**
     * Crea un botón estilizado que representa visualmente una celda individual.
     * Al hacer clic, envía el ID al panel de gestión y cambia la vista.
     */
    private JButton crearBotonCelda(Celda celda) {
        // Formato visual: C01, C02, etc.
        String idVisual = "C" + String.format("%02d", celda.getId());
        String html = "<html><center><b>" + idVisual + "</b><br>" +
                "<font size='2'>" + celda.getTipo() + "</font></center></html>";

        JButton btn = new JButton(html);
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cursor de mano para indicar que es cliqueable
        btn.setBorder(new LineBorder(Color.WHITE, 2, true));
        btn.setPreferredSize(new Dimension(100, 80));

        // Colores según disponibilidad
        if (celda.isDisponible()) {
            btn.setBackground(VERDE_DISPONIBLE);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(ROJO_OCUPADO);
            btn.setForeground(Color.WHITE);
        }

        // ACCIÓN AL HACER CLIC: Llenar el identificador y saltar a la pantalla de gestión
        // Dentro de crearBotonCelda en PanelCeldas.java
        btn.addActionListener(e -> {
            // Buscamos el panel de consulta entre los hijos del contenedor
            for (Component comp : panelContenedor.getComponents()) {
                if (comp instanceof PanelConsultaCeldas) {
                    PanelConsultaCeldas pConsulta = (PanelConsultaCeldas) comp;

                    // Le pasamos el ID de la celda cliqueada
                    pConsulta.cargarCeldaDesdeMapa(celda.getId());

                    // Cambiamos a la pantalla de gestión/consulta
                    cardLayout.show(panelContenedor, "PANTALLA_GESTION_CELDAS");
                    // 2. Sincronizas el Menú Lateral
                    Window w = SwingUtilities.getWindowAncestor(this);
                    if (w instanceof DashboardFrame) {
                        ((DashboardFrame) w).marcarBotonPorPantalla("PANTALLA_GESTION_CELDAS");
                    }
                    break;
                }
            }
        });

        return btn;
    }

    /**
     * Crea una etiqueta (JLabel) diseñada para mostrar indicadores estadísticos.
     * @param titulo El nombre del indicador (ej: "Total").
     * @param valor El valor numérico inicial a mostrar.
     * @return Un JLabel configurado con borde y fuente específica.
     */
    private JLabel crearIndicador (String titulo, String valor) {
        JLabel lbl = new JLabel(titulo + ": " + valor, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        lbl.setOpaque(true);
        lbl.setBackground(new Color(240, 240, 240));
        lbl.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
        lbl.setPreferredSize(new Dimension(150, 45));
        return lbl;
    }


    @Override
    public void addNotify() {
        super.addNotify();
        // Se ejecuta automáticamente cada vez que el panel se muestra
        actualizarMapa();
    }
}