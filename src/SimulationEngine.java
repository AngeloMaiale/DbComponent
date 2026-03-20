import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulationEngine {
    private final MainFrame gui;
    private final IAdapter adapter;
    private final int numHilos;
    private final int numTareas;
    private volatile boolean running = false;

    public SimulationEngine(MainFrame gui, IAdapter adapter, int numHilos, int numTareas, int poolSize) {
        this.gui = gui;
        this.adapter = adapter;
        this.numHilos = numHilos;
        this.numTareas = numTareas;
    }

    public void runPooledSimulation() {
        running = true;
        long startTime = System.currentTimeMillis();
        AtomicInteger exitosas = new AtomicInteger(0);
        AtomicInteger fallidas = new AtomicInteger(0);

        DbComponent<IAdapter> db = new DbComponent<>(adapter, "queries.properties");
        ExecutorService executor = Executors.newFixedThreadPool(numHilos);

        for (int i = 0; i < numTareas && running; i++) {
            int tareaId = i + 1;
            executor.execute(() -> {
                try {
                    // Simulamos una consulta real
                    db.query("ver_usuarios");
                    exitosas.incrementAndGet();
                    gui.appendToGui("🧵 Hilo " + Thread.currentThread().threadId() + " - T...");
                } catch (Exception e) {
                    fallidas.incrementAndGet();
                    gui.appendToGui("❌ Error en Tarea " + tareaId + ": " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            gui.appendToGui("⚠️ Simulación interrumpida.");
        }

        long endTime = System.currentTimeMillis();
        gui.mostrarResultados(endTime - startTime, exitosas.get(), fallidas.get());
        adapter.close();
    }

    public void runRawSimulation() {
        running = true;
        long startTime = System.currentTimeMillis();
        AtomicInteger exitosas = new AtomicInteger(0);
        AtomicInteger fallidas = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(numHilos);

        for (int i = 0; i < numTareas && running; i++) {
            int tareaId = i + 1;
            executor.execute(() -> {
                try (java.sql.Connection conn = adapter.getConnection()) {
                    var stmt = conn.prepareStatement("SELECT 1");
                    stmt.executeQuery();
                    exitosas.incrementAndGet();
                    gui.appendToGui("🐌 RAW - Tarea " + tareaId + " ✅");
                } catch (Exception e) {
                    fallidas.incrementAndGet();
                }
            });
        }

        executor.shutdown();
        try { executor.awaitTermination(1, TimeUnit.MINUTES); } catch (InterruptedException e) {}

        long endTime = System.currentTimeMillis();
        gui.mostrarResultados(endTime - startTime, exitosas.get(), fallidas.get());
        adapter.close();
    }

    public void stopSimulation() {
        this.running = false;
    }
}