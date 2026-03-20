import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConfig {
    private static final String URL = "jdbc:postgresql://localhost:5432/Prueba";
    private static final String USER = "postgres";
    private static final String PASSWORD = "123456";
    private static final int POOL_SIZE = 5;
    public static String getURL() {
        return URL;
    }
    public static String getUser() {
        return USER;
    }
    public static String getPassword() {
        return PASSWORD;
    }
    public static int getPoolSize() {
        return POOL_SIZE;
    }
}