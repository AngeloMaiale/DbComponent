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

    public SimpleConnectionPool(String url, String user, String password, int initialSize) throws SQLException {
        this.url = url;
        this.user = user;
        this.password = password;
        this.connectionPool = new ArrayList<>(initialSize);

        try {
            if (url.contains("sqlite")) {
                Class.forName("org.sqlite.JDBC");
            } else if (url.contains("postgresql")) {
                Class.forName("org.postgresql.Driver");
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("⚠️ No se encontró el archivo .jar del Driver: " + e.getMessage());
        }

        for (int i = 0; i < initialSize; i++) {
            connectionPool.add(createNewConnection());
        }
        System.out.println("✅ Pool inicializado con " + initialSize + " conexiones.");
    }


    private Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public synchronized Connection getConnection() throws Exception {
        if (connectionPool.isEmpty()) {
            throw new Exception("❌ No hay conexiones disponibles en el pool (Capacidad máxima alcanzada).");
        }
        Connection connection = connectionPool.remove(connectionPool.size() - 1);

        if (connection.isClosed()) {
            connection = createNewConnection();
        }

        usedConnections.add(connection);
        return connection;
    }

    public synchronized void releaseConnection(Connection connection) throws SQLException {
        if (connection != null) {
            usedConnections.remove(connection);
            if (!connection.isClosed()) {
                connectionPool.add(connection);
            }
        }
    }

    public void shutdown() {

        for (Connection c : connectionPool) {
            try { if (!c.isClosed()) c.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        for (Connection c : usedConnections) {
            try { if (!c.isClosed()) c.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        connectionPool.clear();
        usedConnections.clear();
        System.out.println("🔌 Pool de conexiones destruido correctamente.");
    }
}