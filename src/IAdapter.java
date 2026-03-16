import java.sql.Connection;

public interface IAdapter {
    // Obliga al adaptador a entregar una conexión de su pool interno
    Connection getConnection() throws Exception;

    // Obliga al adaptador a recibir la conexión de vuelta al pool
    void returnConnection(Connection conn);

    // Para apagar el pool al terminar
    void close();
}