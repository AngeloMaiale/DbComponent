import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    private JTextArea logArea;
    private JComboBox<String> comboMotores;
    private JButton btnTestConn, btnRaw, btnPooled, btnStop;
    private JTextField txtNombre, txtEmail, txtId;
    private JButton btnInsertar, btnActualizar, btnEliminar, btnTxPrueba;
    private AppController controller;

    public MainFrame() {
        setTitle("Simulador de Persistencia - MVC con Entrada de Datos");
        setSize(1100, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        controller = new AppController(this);
        inicializarConsola();
        JPanel panelControlTotal = new JPanel();
        panelControlTotal.setLayout(new BoxLayout(panelControlTotal, BoxLayout.Y_AXIS));
        panelControlTotal.setBackground(new Color(230, 230, 230));
        panelControlTotal.add(crearPanelMotorSimulacion());
        panelControlTotal.add(crearPanelEntradaDatos());

        add(panelControlTotal, BorderLayout.SOUTH);
        configurarEventos();
    }

    private void inicializarConsola() {
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.BOLD, 16));
        logArea.setBackground(new Color(15, 15, 15));
        logArea.setForeground(new Color(0, 255, 100));
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel crearPanelMotorSimulacion() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setOpaque(false);
        panel.setBorder(new TitledBorder("Configuración y Simulación de Carga"));

        String[] opcionesBD = {"PostgreSQL (Servidor Remoto)", "SQLite (Archivo Local .db)"};
        comboMotores = new JComboBox<>(opcionesBD);
        btnTestConn = new JButton("Verificar Estado DB");
        btnRaw = new JButton("Simulación RAW");
        btnPooled = new JButton("Simulación POOLED");
        btnStop = new JButton("Detener");
        btnStop.setForeground(Color.RED);

        panel.add(new JLabel("Motor:"));
        panel.add(comboMotores);
        panel.add(btnTestConn);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(btnRaw);
        panel.add(btnPooled);
        panel.add(btnStop);
        return panel;
    }

    private JPanel crearPanelEntradaDatos() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setOpaque(false);
        panelPrincipal.setBorder(new TitledBorder("Operaciones Manuales con Argumentos"));

        JPanel panelInputs = new JPanel(new GridLayout(2, 3, 10, 5));
        panelInputs.setOpaque(false);

        txtNombre = new JTextField();
        txtEmail = new JTextField();
        txtId = new JTextField(); // Para Updates y Deletes

        panelInputs.add(new JLabel("Nombre:"));
        panelInputs.add(new JLabel("Email:"));
        panelInputs.add(new JLabel("ID (para Modificar/Eliminar):"));
        panelInputs.add(txtNombre);
        panelInputs.add(txtEmail);
        panelInputs.add(txtId);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelAcciones.setOpaque(false);

        btnInsertar = new JButton("Insertar (Nombre, Email)");
        btnActualizar = new JButton("Actualizar Email por ID");
        btnEliminar = new JButton("Eliminar por ID");
        btnTxPrueba = new JButton("Ejecutar Transacción Compleja (Test)");

        btnInsertar.setBackground(new Color(200, 255, 200));
        btnEliminar.setBackground(new Color(255, 200, 200));

        panelAcciones.add(btnInsertar);
        panelAcciones.add(btnActualizar);
        panelAcciones.add(btnEliminar);
        panelAcciones.add(new JSeparator(SwingConstants.VERTICAL));
        panelAcciones.add(btnTxPrueba);

        panelPrincipal.add(panelInputs, BorderLayout.CENTER);
        panelPrincipal.add(panelAcciones, BorderLayout.SOUTH);

        return panelPrincipal;
    }

    private void configurarEventos() {
        btnTestConn.addActionListener(e -> controller.probarConexion(getMotorSeleccionado()));
        btnRaw.addActionListener(e -> controller.iniciarSimulacion(getMotorSeleccionado(), false));
        btnPooled.addActionListener(e -> controller.iniciarSimulacion(getMotorSeleccionado(), true));
        btnStop.addActionListener(e -> controller.detenerSimulacion());
        btnInsertar.addActionListener(e -> controller.ejecutarInsertarManual(getMotorSeleccionado()));
        btnActualizar.addActionListener(e -> controller.ejecutarActualizarManual(getMotorSeleccionado()));
        btnEliminar.addActionListener(e -> controller.ejecutarEliminarManual(getMotorSeleccionado()));
        btnTxPrueba.addActionListener(e -> controller.ejecutarTransaccionPrueba(getMotorSeleccionado()));
    }
    public String getMotorSeleccionado() { return (String) comboMotores.getSelectedItem(); }
    public String getNombreInput() { return txtNombre.getText().trim(); }
    public String getEmailInput() { return txtEmail.getText().trim(); }
    public String getIdInput() { return txtId.getText().trim(); }
    public void appendToGui(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void limpiarInputs() {
        txtNombre.setText("");
        txtEmail.setText("");
        txtId.setText("");
    }

    public void mostrarResultados(long tiempoMs, int exitosas, int fallidas) {
        SwingUtilities.invokeLater(() -> {
            appendToGui("\n" + "=".repeat(45));
            appendToGui("📊 INFORME FINAL DE RENDIMIENTO");
            appendToGui("⏱️ Tiempo de ejecución: " + tiempoMs + " ms");
            appendToGui("✅ Operaciones exitosas: " + exitosas);
            appendToGui("❌ Operaciones fallidas: " + fallidas);
            appendToGui("=".repeat(45) + "\n");
            enableButtons(true);
        });
    }

    public void enableButtons(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            btnRaw.setEnabled(enabled);
            btnPooled.setEnabled(enabled);
            btnTestConn.setEnabled(enabled);
            btnInsertar.setEnabled(enabled);
            btnActualizar.setEnabled(enabled);
            btnEliminar.setEnabled(enabled);
            btnTxPrueba.setEnabled(enabled);
            comboMotores.setEnabled(enabled);
        });
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}