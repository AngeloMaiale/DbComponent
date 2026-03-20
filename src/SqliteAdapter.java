import java.sql.Connection;
import java.sql.SQLException;

public class SqliteAdapter implements IAdapter {
    private SimpleConnectionPool pool;

    public SqliteAdapter(String dbPath, int poolSize) {
        try {
            // CORRECCIÓN: Usamos la variable dbPath que viene del Main
            String url = "jdbc:sqlite:" + dbPath;

            // Le pasamos null, pero ahora el Pool ya sabe cómo manejarlo
            this.pool = new SimpleConnectionPool(url, null, null, poolSize);
        } catch (SQLException e) {
            e.printStackTrace(); // Esto nos dirá el error exacto en la consola si falla
            throw new RuntimeException("Error al crear el pool de SQLite", e);
        }
    }

    @Override
    public Connection getConnection() throws Exception {
        return pool.getConnection();
    }

    @Override
    public void returnConnection(Connection conn) {
        try {
            if (conn != null) pool.releaseConnection(conn);
        } catch (SQLException e) {
            System.err.println("Error al liberar conexión SQLite: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        if (pool != null) pool.shutdown();
    }
}