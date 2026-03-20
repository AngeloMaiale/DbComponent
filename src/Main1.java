import java.util.List;
import java.util.Map;

public class Main1 {
    public static void main(String[] args) {
        System.out.println("=== SISTEMA DE PERSISTENCIA MULTIMOTOR ===\n");
        IAdapter adapter = new PostgresAdapter(
                DatabaseConfig.getURL(),
                DatabaseConfig.getUser(),
                DatabaseConfig.getPassword(),
                5
        );
        DbComponent<IAdapter> db = new DbComponent<>(adapter, "queries.properties");

        try {
            System.out.println("--- PASO 1: PREPARACIÓN (Solo para SQLite/Pruebas) ---");
            db.query("crear_tabla_usuarios");

            System.out.println("\n--- PASO 2: INSERCIÓN CON PARÁMETROS ---");
            db.query("insertar_usuario", "Angel_User_Test");
            System.out.println("✅ Datos enviados al componente.");

            System.out.println("\n--- PASO 3: CONSULTA DINÁMICA (SELECT) ---");
            List<Map<String, Object>> resultados = db.query("ver_usuarios");

            if (resultados.isEmpty()) {
                System.out.println("⚠️ No se encontraron registros en la tabla.");
            } else {
                System.out.println("📊 Registros recuperados (" + resultados.size() + "):");
                for (Map<String, Object> fila : resultados) {
                    System.out.println(" > ID: " + fila.get("id") + " | Nombre: " + fila.get("nombre"));
                }
            }

            System.out.println("\n--- PASO 4: PRUEBA DE TRANSACCIÓN ---");
            String[] acciones = {"insertar_usuario", "ver_usuarios"};
            db.transaction(acciones);

        } catch (Exception e) {
            System.err.println("❌ Error crítico en el flujo principal: " + e.getMessage());
        } finally {
            System.out.println("\n--- FINALIZANDO ---");
            adapter.close();
            System.out.println("🔌 Conexiones cerradas correctamente.");
        }
    }
}