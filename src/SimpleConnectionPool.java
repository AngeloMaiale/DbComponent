import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SimpleConnectionPool {
    private final String url;
    private final String user;
    private final String password;
    private final List<Connection> connectionPool;
    private final List<Connection> usedConnections = new ArrayList<>();

    // El constructor ahora recibe TODO lo necesario para conectar
    public SimpleConnectionPool(String url, String user, String password, int initialSize) throws SQLException {
        this.url = url;
        this.user = user;
        this.password = password;
        this.connectionPool = new ArrayList<>(initialSize);

        for (int i = 0; i < initialSize; i++) {
            connectionPool.add(createNewConnection());
        }
        System.out.println("Pool inicializado con " + initialSize + " conexiones.");
    }

    private Connection createNewConnection() throws SQLException {
        // Usamos las variables locales configuradas en el constructor
        return DriverManager.getConnection(url, user, password);
    }

    public synchronized Connection getConnection() throws Exception {
        if (connectionPool.isEmpty()) {
            throw new Exception("No hay conexiones disponibles en el pool.");
        }
        Connection connection = connectionPool.remove(connectionPool.size() - 1);
        usedConnections.add(connection);
        return connection;
    }

    public synchronized void releaseConnection(Connection connection) throws SQLException {
        if (connection != null) {
            usedConnections.remove(connection);
            connectionPool.add(connection);
        }
    }

    public void shutdown() {
        for (Connection c : connectionPool) {
            try { c.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        for (Connection c : usedConnections) {
            try { c.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        connectionPool.clear();
        usedConnections.clear();
    }
}