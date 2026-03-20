import java.sql.Connection;
import java.sql.SQLException;

public class SqliteAdapter implements IAdapter {
    private SimpleConnectionPool pool;

    public SqliteAdapter(String dbPath, int poolSize) {
        try {
            String url = "jdbc:sqlite:" + dbPath;

            this.pool = new SimpleConnectionPool(url, null, null, poolSize);
        } catch (SQLException e) {
            e.printStackTrace();
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