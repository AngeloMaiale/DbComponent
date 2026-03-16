import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConfig {
    // Estas son las credenciales. Ajústalas a tu base de datos local.
    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "admin"; // Cambia esto por tu contraseña real
    private static final int POOL_SIZE = 5;

    // Métodos Getter (Símbolos que el compilador no encontraba)

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