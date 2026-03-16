import java.sql.Connection;

public class PostgresAdapter implements IAdapter {
    private SimpleConnectionPool pool; // Tu pool creado anteriormente

    // El constructor recibe TODA la data de conexión directamente
    public PostgresAdapter(String url, String user, String password, int poolSize) {
        try {
            // Nota: Debes ajustar tu SimpleConnectionPool para que reciba 
            // estos parámetros en su constructor si no lo hace ya.
            this.pool = new SimpleConnectionPool(url, user, password, poolSize);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear el pool de Postgres", e);
        }
    }

    @Override
    public Connection getConnection() throws Exception {
        return pool.getConnection(); // Saca del pool
    }

    @Override
    public void returnConnection(Connection conn) {
        try {
            pool.releaseConnection(conn); // Devuelve al pool
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        pool.shutdown();
    }
}