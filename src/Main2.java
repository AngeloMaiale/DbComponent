public class Main2 {
    public static void main(String[] args) {
        // 1. Instanciamos el adaptador pasándole TODOS los datos de conexión
        PostgresAdapter pgAdapter = new PostgresAdapter(
                "jdbc:postgresql://localhost:5432/Prueba", "postgres", "123456", 5
        );

        // 2. Inyectamos el adaptador y la ruta del archivo de queries
        DbComponent<PostgresAdapter> db = new DbComponent<>(pgAdapter, "queries.properties");

        System.out.println("--- PRUEBA QUERY SIMPLE ---");
        // No pasamos SQL, pasamos la CLAVE del archivo .properties
        db.query("ver_usuarios");

        System.out.println("\n--- PRUEBA TRANSACCIÓN ---");
        String[] transaccionBancaria = {"restar_saldo", "sumar_saldo"};
        db.transaction(transaccionBancaria);

        // Apagamos
        pgAdapter.close();
    }
}