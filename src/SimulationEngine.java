import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulationEngine {
    private SimpleConnectionPool pool;
    private MainFrame frame; // Referencia al GUI
    private final int numThreads;
    private final int numTasks;
    private final AtomicInteger successfulTasks = new AtomicInteger(0);
    private final AtomicInteger failedTasks = new AtomicInteger(0);
    private boolean isRunning = false;

    // Constructor que recibe el Frame y los parámetros de configuración
    public SimulationEngine(MainFrame frame, int numThreads, int numTasks, int poolSize) {
        this.frame = frame;
        this.numThreads = numThreads;
        this.numTasks = numTasks;
        try {
            this.pool = new SimpleConnectionPool(
                    DatabaseConfig.getURL(),
                    DatabaseConfig.getUser(),
                    DatabaseConfig.getPassword(),
                    poolSize
            );
        } catch (SQLException e) {
            System.err.println("Error inicializando pool: " + e.getMessage());
        }
    }

    public void runPooledSimulation() {
        this.isRunning = true;
        frame.appendToGui("Iniciando simulación POOLED...");
        // Aquí iría tu lógica de ejecución
    }

    public void runRawSimulation() {
        this.isRunning = true;
        frame.appendToGui("Iniciando simulación RAW...");
    }

    public void stopSimulation() {
        this.isRunning = false;
        if (pool != null) pool.shutdown();
        frame.appendToGui("Simulación detenida.");
    }

    public int getSuccessfulTasks() { return successfulTasks.get(); }
    public int getFailedTasks() { return failedTasks.get(); }
}