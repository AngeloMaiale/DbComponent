import java.sql.Connection;

public interface IAdapter {
    Connection getConnection() throws Exception;
    void returnConnection(Connection conn);
    void close();
}