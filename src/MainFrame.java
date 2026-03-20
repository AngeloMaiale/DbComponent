import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JTextArea logArea;
    private JButton btnTestConn, btnRaw, btnPooled, btnStop;
    private SimulationEngine engine;

    public MainFrame() {
        setTitle("Simulador de Conexiones a BD - Entrega Final");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        engine = new SimulationEngine(this, 4, 100, 5);

        Font fuenteConsola = new Font("Monospaced", Font.BOLD, 16);
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(fuenteConsola);
        logArea.setBackground(new Color(20, 20, 20));
        logArea.setForeground(new Color(0, 255, 100));

        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panelButtons.setBackground(new Color(240, 240, 240));

        btnTestConn = new JButton("Probar Conexión");
        btnRaw = new JButton("Simulación RAW");
        btnPooled = new JButton("Simulación POOLED");
        btnStop = new JButton("Detener");

        panelButtons.add(btnTestConn);
        panelButtons.add(btnRaw);
        panelButtons.add(btnPooled);
        panelButtons.add(btnStop);
        add(panelButtons, BorderLayout.SOUTH);

        configurarEventos();
    }

    private void configurarEventos() {
        btnTestConn.addActionListener(e -> {
            appendToGui("Verificando conexión con la Base de Datos...");
        });

        btnRaw.addActionListener(e -> {
            enableButtons(false);
            new Thread(() -> engine.runRawSimulation()).start();
        });

        btnPooled.addActionListener(e -> {
            enableButtons(false);
            new Thread(() -> engine.runPooledSimulation()).start();
        });

        btnStop.addActionListener(e -> engine.stopSimulation());
    }

    public void appendToGui(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void mostrarResultados(long tiempoMs, int exitosas, int fallidas) {
        SwingUtilities.invokeLater(() -> {
            appendToGui("\n" + "=".repeat(40));
            appendToGui("📊 RESUMEN DE EJECUCIÓN");
            appendToGui("=".repeat(40));
            appendToGui("⏱️ Tiempo Total: " + tiempoMs + " ms");
            appendToGui("✅ Tareas Exitosas: " + exitosas);
            appendToGui("❌ Tareas Fallidas: " + fallidas);
            appendToGui("=".repeat(40) + "\n");
            enableButtons(true);
        });
    }

    public void enableButtons(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            btnRaw.setEnabled(enabled);
            btnPooled.setEnabled(enabled);
            btnTestConn.setEnabled(enabled);
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
