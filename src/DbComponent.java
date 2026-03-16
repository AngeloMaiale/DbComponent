import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;

// Componente genérico acoplado a la interfaz, no a una BD específica
public class DbComponent<T extends IAdapter> {

    private T adapter;
    private Properties queryMap;

    public DbComponent(T adapter, String queriesFilePath) {
        this.adapter = adapter;
        this.queryMap = new Properties();
        cargarQueries(queriesFilePath);
    }

    // Carga las consultas desde el archivo .properties
    private void cargarQueries(String path) {
        try (FileInputStream fis = new FileInputStream(path)) {
            queryMap.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar el archivo de queries: " + path);
        }
    }

    /**
     * Cumple Observación 1 y 3: Solicita conexión, ejecuta UNA query sin usar
     * SQL crudo, y devuelve la conexión al pool en el finally.
     */
    public void query(String queryName) {
        String sql = queryMap.getProperty(queryName);
        if (sql == null) {
            System.err.println("La consulta '" + queryName + "' no existe en el archivo.");
            return;
        }

        Connection conn = null;
        try {
            conn = adapter.getConnection(); // Solicita al Pool
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                System.out.println("✅ Query ejecutado: [" + queryName + "]");
            }
        } catch (Exception e) {
            System.err.println("❌ Error en query: " + e.getMessage());
        } finally {
            if (conn != null) {
                adapter.returnConnection(conn); // Devuelve al Pool obligatoriamente
            }
        }
    }

    /**
     * Cumple el esquema base: transaction().
     * Recibe una lista de nombres de queries para ejecutarlas como bloque.
     */
    public void transaction(String[] queryNames) {
        Connection conn = null;
        try {
            conn = adapter.getConnection(); // Solicita al Pool
            conn.setAutoCommit(false); // Inicia la transacción

            try (Statement stmt = conn.createStatement()) {
                for (String qName : queryNames) {
                    String sql = queryMap.getProperty(qName);
                    if (sql != null) {
                        stmt.executeUpdate(sql);
                        System.out.println("-> Ejecutando en TX: [" + qName + "]");
                    }
                }
            }

            conn.commit(); // Si todo sale bien, guarda los cambios
            System.out.println("✅ Transacción confirmada (COMMIT).");

        } catch (Exception e) {
            System.err.println("❌ Error. Revirtiendo cambios (ROLLBACK)...");
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (Exception ex) { ex.printStackTrace(); }
                adapter.returnConnection(conn); // Devuelve al Pool
            }
        }
    }
}
