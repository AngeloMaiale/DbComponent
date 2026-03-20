import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class DbComponent<T extends IAdapter> {
    private T adapter;
    private Properties queryMap;
    public DbComponent(T adapter, String queriesFilePath) {
        this.adapter = adapter;
        this.queryMap = new Properties();
        cargarQueries(queriesFilePath);
    }
    private void cargarQueries(String path) {
        try (FileInputStream fis = new FileInputStream(path)) {
            queryMap.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar el archivo de queries: " + path);
        }
    }
    public List<Map<String, Object>> query(String queryName, Object... params) {
        String sql = queryMap.getProperty(queryName);
        if (sql == null) {
            System.err.println("La consulta '" + queryName + "' no existe.");
            return new ArrayList<>();
        }
        List<Map<String, Object>> resultados = new ArrayList<>();
        Connection conn = null;
        try {
            conn = adapter.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }
                if (sql.trim().toUpperCase().startsWith("SELECT")) {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();
                        while (rs.next()) {
                            Map<String, Object> fila = new HashMap<>();
                            for (int i = 1; i <= columnCount; i++) {
                                fila.put(metaData.getColumnName(i), rs.getObject(i));
                            }
                            resultados.add(fila);
                        }
                    }
                } else {
                    int filasAfectadas = pstmt.executeUpdate();
                    System.out.println("✅ [" + queryName + "] Filas afectadas: " + filasAfectadas);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error en query '" + queryName + "': " + e.getMessage());
        } finally {
            if (conn != null) adapter.returnConnection(conn);
        }

        return resultados;
    }
    public void transaction(String[] queryNames) {
        Connection conn = null;
        try {
            conn = adapter.getConnection();
            conn.setAutoCommit(false);

            for (String qName : queryNames) {
                String sql = queryMap.getProperty(qName);
                if (sql != null) {
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.executeUpdate();
                        System.out.println("-> Ejecutando en TX: [" + qName + "]");
                    }
                }
            }

            conn.commit();
            System.out.println("✅ Transacción confirmada (COMMIT).");

        } catch (Exception e) {
            System.err.println("❌ Error en transacción. Revirtiendo (ROLLBACK): " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
                adapter.returnConnection(conn);
            }
        }
    }
}