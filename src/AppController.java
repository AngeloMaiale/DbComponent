import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AppController {
    private MainFrame gui;
    private SimulationEngine engine;

    public AppController(MainFrame gui) {
        this.gui = gui;
    }

    private IAdapter obtenerAdaptador(String seleccion) {
        if (seleccion.contains("PostgreSQL")) {
            return new PostgresAdapter(DatabaseConfig.getURL(), DatabaseConfig.getUser(), DatabaseConfig.getPassword(), DatabaseConfig.getPoolSize());
        } else if (seleccion.contains("SQLite")) {
            return new SqliteAdapter("datos_simulacion.db", DatabaseConfig.getPoolSize());
        }
        return null;
    }

    public void ejecutarInsertarManual(String motorSeleccionado) {
        String nombre = gui.getNombreInput();
        String email = gui.getEmailInput();
        if (nombre.isEmpty() || email.isEmpty()) {
            gui.appendToGui("⚠️ Error: Nombre y Email son obligatorios para insertar.");
            return;
        }
        gui.appendToGui("\n👤 Insertando usuario manualmente...");
        ejecutarOperacionDML(motorSeleccionado, "insertar_usuario", nombre, email);
        gui.limpiarInputs(); // Limpiar campos tras éxito
    }

    public void ejecutarActualizarManual(String motorSeleccionado) {
        String idStr = gui.getIdInput();
        String nuevoEmail = gui.getEmailInput();

        if (idStr.isEmpty() || nuevoEmail.isEmpty()) {
            gui.appendToGui("⚠️ Error: Se requiere ID y Nuevo Email para actualizar.");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            gui.appendToGui("\n📝 Actualizando email del usuario ID " + id + "...");
            ejecutarOperacionDML(motorSeleccionado, "actualizar_email_por_id", nuevoEmail, id);
            gui.limpiarInputs();
        } catch (NumberFormatException e) {
            gui.appendToGui("❌ Error: El ID debe ser un número válido.");
        }
    }

    public void ejecutarEliminarManual(String motorSeleccionado) {
        String idStr = gui.getIdInput();

        if (idStr.isEmpty()) {
            gui.appendToGui("⚠️ Error: Se requiere ID para eliminar.");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            gui.appendToGui("\n🗑️ Eliminando usuario ID " + id + "...");
            ejecutarOperacionDML(motorSeleccionado, "eliminar_usuario_por_id", id);
            gui.limpiarInputs();
        } catch (NumberFormatException e) {
            gui.appendToGui("❌ Error: El ID debe ser un número válido.");
        }
    }
    private void ejecutarOperacionDML(String motor, String queryName, Object... params) {
        IAdapter adapter = obtenerAdaptador(motor);
        if (adapter == null) return;

        try {
            DbComponent<IAdapter> db = new DbComponent<>(adapter, "queries.properties");
            DbComponent.DbResult resultado = db.query(queryName, params);

            gui.appendToGui("✅ Operación exitosa. Filas afectadas: " + resultado.filasAfectadas);
            mostrarTablaActualizada(db);

        } catch (Exception ex) {
            gui.appendToGui("❌ Error en la operación: " + ex.getMessage());
        } finally {
            adapter.close();
        }
    }

    private void mostrarTablaActualizada(DbComponent<IAdapter> db) {
        DbComponent.DbResult res = db.query("ver_usuarios_resumido");
        gui.appendToGui("--- Estado actual (Top 5) ---");
        for (Map<String, Object> fila : res.data) {
            gui.appendToGui("ID: " + fila.get("id") + " | " + fila.get("nombre") + " (" + fila.get("email") + ")");
        }
    }

    public void ejecutarTransaccionPrueba(String motorSeleccionado) {
        gui.appendToGui("\n📦 Ejecutando Transacción Compleja de Prueba (Atomicidad)...");
        IAdapter adapter = obtenerAdaptador(motorSeleccionado);
        if (adapter == null) return;

        try {
            DbComponent<IAdapter> db = new DbComponent<>(adapter, "queries.properties");
            db.query("crear_tabla_usuarios");
            List<DbComponent.QueryTask> tareas = Arrays.asList(
                    new DbComponent.QueryTask("insertar_usuario", "Tx_User_A", "a@mail.com"),
                    new DbComponent.QueryTask("insertar_usuario", "Tx_User_B", "b@mail.com"),
                    new DbComponent.QueryTask("actualizar_email_por_id", "error@mail.com", -1)
            );

            int afectadas = db.transaction(tareas);

            if (afectadas != -1) {
                gui.appendToGui("✅ Transacción confirmada. Total filas afectadas: " + afectadas);
            } else {
                gui.appendToGui("🛑 Transacción fallida. Se aplicó ROLLBACK (Los datos no cambiaron).");
            }

        } catch (Exception ex) {
            gui.appendToGui("❌ Error en TX: " + ex.getMessage());
        } finally {
            adapter.close();
        }
    }

    public void probarConexion(String motorSeleccionado) {
        gui.appendToGui("\n--- Verificando Estado de " + motorSeleccionado + " ---");
        IAdapter adapter = obtenerAdaptador(motorSeleccionado);
        if (adapter != null) {
            try {
                DbComponent<IAdapter> db = new DbComponent<>(adapter, "queries.properties");
                db.query("crear_tabla_usuarios");
                DbComponent.DbResult res = db.query("contar_usuarios");
                if (!res.data.isEmpty()) {
                    gui.appendToGui("✅ Conexión OK. Total registros en tabla 'usuarios': " + res.data.get(0).get("total"));
                }
            } catch (Exception ex) {
                gui.appendToGui("❌ Error: " + ex.getMessage());
            } finally {
                adapter.close();
            }
        }
    }

    public void iniciarSimulacion(String motorSeleccionado, boolean isPooled) {
        IAdapter adapter = obtenerAdaptador(motorSeleccionado);
        if (adapter == null) return;
        try {
            DbComponent<IAdapter> setup = new DbComponent<>(adapter, "queries.properties");
            setup.query("crear_tabla_usuarios");
        } catch (Exception e) {}
        gui.enableButtons(false);
        gui.appendToGui("\n🚀 LANZANDO SIMULACIÓN " + (isPooled ? "[CON POOL]" : "[SIN POOL]"));
        engine = new SimulationEngine(gui, adapter, 4, 100, 5);
        new Thread(() -> {
            if (isPooled) engine.runPooledSimulation();
            else engine.runRawSimulation();
        }).start();
    }

    public void detenerSimulacion() {
        if (engine != null) {
            engine.stopSimulation();
            gui.appendToGui("🛑 Señal de detención enviada...");
        }
    }
}