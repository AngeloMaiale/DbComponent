import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JTextArea logArea;
    private JButton btnTestConn, btnRaw, btnPooled, btnStop;
    private JComboBox<String> comboMotores;
    private SimulationEngine engine;

    public MainFrame() {
        setTitle("Simulador de Persistencia - Postgres & SQLite");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.BOLD, 16));
        logArea.setBackground(new Color(15, 15, 15));
        logArea.setForeground(new Color(0, 255, 100));

        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panelButtons.setBackground(new Color(230, 230, 230));
        String[] opcionesBD = {"PostgreSQL (Servidor Remoto)", "SQLite (Archivo Local .db)"};
        comboMotores = new JComboBox<>(opcionesBD);

        btnTestConn = new JButton("Probar Conexión");
        btnRaw = new JButton("Simulación RAW");
        btnPooled = new JButton("Simulación POOLED");
        btnStop = new JButton("Detener");

        panelButtons.add(new JLabel("Seleccionar Motor:"));
        panelButtons.add(comboMotores);
        panelButtons.add(btnTestConn);
        panelButtons.add(btnRaw);
        panelButtons.add(btnPooled);
        panelButtons.add(btnStop);
        add(panelButtons, BorderLayout.SOUTH);

        configurarEventos();
    }

    private IAdapter obtenerAdaptadorSeleccionado() {
        String seleccion = (String) comboMotores.getSelectedItem();
        int poolSize = 5;

        try {
            if (seleccion.contains("PostgreSQL")) {
                return new PostgresAdapter("jdbc:postgresql://localhost:5432/Prueba", "postgres", "123456", poolSize);
            } else {
                return new SqliteAdapter("datos_simulacion.db", poolSize);
            }
        } catch (Exception e) {
            appendToGui("❌ ERROR de Configuración: " + e.getMessage());
            return null;
        }
    }

    private void configurarEventos() {
        btnTestConn.addActionListener(e -> {
            appendToGui("\n--- Verificando " + comboMotores.getSelectedItem() + " ---");
            IAdapter adapter = obtenerAdaptadorSeleccionado();
            if (adapter != null) {
                try {
                    DbComponent<IAdapter> db = new DbComponent<>(adapter, "queries.properties");
                    db.query("ver_usuarios");
                    appendToGui("✅ Comunicación establecida correctamente.");
                } catch (Exception ex) {
                    appendToGui("❌ Error de comunicación: " + ex.getMessage());
                } finally {
                    adapter.close();
                }
            }
        });

        btnRaw.addActionListener(e -> iniciarSimulacion(false));
        btnPooled.addActionListener(e -> iniciarSimulacion(true));

        btnStop.addActionListener(e -> {
            if (engine != null) {
                engine.stopSimulation();
                appendToGui("🛑 DETENIDO: Cancelando tareas pendientes...");
            }
        });
    }

    private void iniciarSimulacion(boolean isPooled) {
        IAdapter adapter = obtenerAdaptadorSeleccionado();
        if (adapter == null) return;

        enableButtons(false);
        appendToGui("\n🚀 LANZANDO SIMULACIÓN " + (isPooled ? "[CON POOL]" : "[SIN POOL]"));
        appendToGui("📍 Motor: " + comboMotores.getSelectedItem());

        engine = new SimulationEngine(this, adapter, 4, 100, 5);

        new Thread(() -> {
            if (isPooled) engine.runPooledSimulation();
            else engine.runRawSimulation();
        }).start();
    }

    public void appendToGui(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void mostrarResultados(long tiempoMs, int exitosas, int fallidas) {
        SwingUtilities.invokeLater(() -> {
            appendToGui("\n" + "=".repeat(45));
            appendToGui("📊 INFORME FINAL DE RENDIMIENTO");
            appendToGui("=".repeat(45));
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
            comboMotores.setEnabled(enabled);
        });
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}