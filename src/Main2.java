import java.util.List;
import java.util.Map;

public class Main2 {
    public static void main(String[] args) {
        PostgresAdapter pgAdapter = new PostgresAdapter(
                DatabaseConfig.getURL(), DatabaseConfig.getUser(), DatabaseConfig.getPassword(), 5
        );
        DbComponent<PostgresAdapter> db = new DbComponent<>(pgAdapter, "queries.properties");
        System.out.println("--- PRUEBA 1: INSERTAR CON PARÁMETROS ---");
        db.query("insertar_usuario", "Carlos Estudiante");
        System.out.println("\n--- PRUEBA 2: SELECT DINÁMICO ---");
        List<Map<String, Object>> usuarios = db.query("ver_usuarios");
        for (Map<String, Object> fila : usuarios) {
            System.out.println("Usuario: " + fila);
        }
        pgAdapter.close();
    }
}