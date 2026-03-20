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
    public static class DbResult {
        public List<Map<String, Object>> data = new ArrayList<>();
        public int filasAfectadas = 0;
    }

    public static class QueryTask {
        public String queryName;
        public Object[] params;
        public QueryTask(String queryName, Object... params) {
            this.queryName = queryName;
            this.params = params;
        }
    }

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
    public DbResult query(String queryName, Object... params) {
        DbResult resultado = new DbResult();
        String sql = queryMap.getProperty(queryName);
        if (sql == null) {
            System.err.println("La consulta '" + queryName + "' no existe.");
            return resultado;
        }

        Connection conn = null;
        try {
            conn = adapter.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }
                boolean isResultSet = pstmt.execute();

                if (isResultSet) {
                    try (ResultSet rs = pstmt.getResultSet()) {
                        ResultSetMetaData md = rs.getMetaData();
                        int columns = md.getColumnCount();
                        while (rs.next()) {
                            Map<String, Object> row = new HashMap<>();
                            for (int i = 1; i <= columns; ++i) {
                                row.put(md.getColumnName(i), rs.getObject(i));
                            }
                            resultado.data.add(row);
                        }
                    }
                } else {
                    resultado.filasAfectadas = pstmt.getUpdateCount();
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error en query '" + queryName + "': " + e.getMessage());
        } finally {
            if (conn != null) adapter.returnConnection(conn);
        }

        return resultado;
    }

    public int transaction(List<QueryTask> tareas) {
        Connection conn = null;
        int totalFilasAfectadas = 0;

        try {
            conn = adapter.getConnection();
            conn.setAutoCommit(false);
            for (QueryTask tarea : tareas) {
                String sql = queryMap.getProperty(tarea.queryName);
                if (sql != null) {
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        if (tarea.params != null) {
                            for (int i = 0; i < tarea.params.length; i++) {
                                pstmt.setObject(i + 1, tarea.params[i]);
                            }
                        }
                        pstmt.executeUpdate();
                        totalFilasAfectadas += pstmt.getUpdateCount();
                        System.out.println("-> Ejecutando en TX: [" + tarea.queryName + "] | Filas afectadas: " + pstmt.getUpdateCount());
                    }
                }
            }

            conn.commit();
            System.out.println("✅ Transacción confirmada. Total afectadas: " + totalFilasAfectadas);
            return totalFilasAfectadas;

        } catch (Exception e) {
            System.err.println("❌ Error en transacción. Revirtiendo (ROLLBACK): " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return -1; // Indica error
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
                adapter.returnConnection(conn);
            }
        }
    }
}