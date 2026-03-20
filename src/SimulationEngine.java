import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulationEngine {
    private MainFrame frame;
    private DbComponent<PostgresAdapter> dbComponent;
    private PostgresAdapter adapter;

    private final int numThreads;
    private final int numTasks;
    private final AtomicInteger successfulTasks = new AtomicInteger(0);
    private final AtomicInteger failedTasks = new AtomicInteger(0);
    private boolean isRunning = false;
    private long totalExecutionTime = 0;

    public SimulationEngine(MainFrame frame, int numThreads, int numTasks, int poolSize) {
        this.frame = frame;
        this.numThreads = numThreads;
        this.numTasks = numTasks;

        try {
            this.adapter = new PostgresAdapter(
                    DatabaseConfig.getURL(),
                    DatabaseConfig.getUser(),
                    DatabaseConfig.getPassword(),
                    poolSize
            );
            this.dbComponent = new DbComponent<>(adapter, "queries.properties");
        } catch (Exception e) {
            frame.appendToGui("❌ Error inicializando BD: " + e.getMessage());
        }
    }

    public void runPooledSimulation() {
        this.isRunning = true;
        successfulTasks.set(0);
        failedTasks.set(0);

        frame.appendToGui("\n🚀 Iniciando simulación POOLED...");
        frame.appendToGui("Hilos: " + numThreads + " | Tareas: " + numTasks);

        long startTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numTasks; i++) {
            if (!isRunning) break;

            final int taskId = i;
            executor.execute(() -> {
                try {
                    dbComponent.query("ver_usuarios");
                    successfulTasks.incrementAndGet();
                } catch (Exception e) {
                    failedTasks.incrementAndGet();
                }
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            frame.appendToGui("⚠️ Simulación interrumpida.");
        }
        this.totalExecutionTime = System.currentTimeMillis() - startTime;
        this.isRunning = false;
        frame.mostrarResultados(totalExecutionTime, successfulTasks.get(), failedTasks.get());
    }

    public void runRawSimulation() {
        frame.appendToGui("🚧 Simulación RAW en construcción para la entrega final.");
        frame.enableButtons(true);
    }

    public void stopSimulation() {
        this.isRunning = false;
        frame.appendToGui("🛑 Deteniendo simulación...");
        if (adapter != null) adapter.close();
    }
}