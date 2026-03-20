import java.sql.Connection;

public class PostgresAdapter implements IAdapter {
    private SimpleConnectionPool pool;

    public PostgresAdapter(String url, String user, String password, int poolSize) {
        try {
            this.pool = new SimpleConnectionPool(url, user, password, poolSize);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear el pool de Postgres", e);
        }
    }

    @Override
    public Connection getConnection() throws Exception {
        return pool.getConnection();
    }

    @Override
    public void returnConnection(Connection conn) {
        try {
            pool.releaseConnection(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        pool.shutdown();
    }
}