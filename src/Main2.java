import java.util.List;
import java.util.Map;

public class Main2 {
    public static void main(String[] args) {
        PostgresAdapter pgAdapter = new PostgresAdapter(
                DatabaseConfig.getURL(),
                DatabaseConfig.getUser(),
                DatabaseConfig.getPassword(),
                DatabaseConfig.getPoolSize()
        );
        DbComponent<PostgresAdapter> db = new DbComponent<>(pgAdapter, "queries.properties");
        System.out.println("--- PRUEBA 1: INSERTAR CON PARÁMETROS DINÁMICOS ---");
        db.query("insertar_usuario", "Usuario_Prueba_G");
        System.out.println("✅ Usuario insertado con éxito.\n");
        System.out.println("--- PRUEBA 2: CONSULTA Y LECTURA DE DATOS ---");
        List<Map<String, Object>> resultados = db.query("ver_usuarios");
        if (resultados.isEmpty()) {
            System.out.println("No se encontraron registros.");
        } else {
            System.out.println("Registros encontrados: " + resultados.size());
            for (Map<String, Object> fila : resultados) {
                System.out.println("ID: " + fila.get("id") + " | Nombre: " + fila.get("nombre"));
            }
        }
        System.out.println("\n--- PRUEBA 3: TRANSACCIÓN POR BLOQUE ---");
        String[] tx = {"restar_saldo", "sumar_saldo"};
        db.transaction(tx);
        pgAdapter.close();
        System.out.println("\n--- PRUEBAS FINALIZADAS ---");
    }
}