package cafeteria.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Conexión SQLite compartida por los tres repositorios. Solo usa java.sql: el driver
 * (sqlite-jdbc) se coloca en lib/ y se detecta en tiempo de ejecución.
 */
public class ConexionSqlite implements AutoCloseable {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private final Connection conexion;

    /** @param ruta archivo (p. ej. "cafeteria.db") o ":memory:" */
    public ConexionSqlite(String ruta) {
        try {
            conexion = DriverManager.getConnection("jdbc:sqlite:" + ruta);
            crearEsquema();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo abrir la base SQLite: " + e.getMessage(), e);
        }
    }

    /** ¿Está el driver sqlite-jdbc en el classpath? */
    public static boolean controladorDisponible() {
        try {
            Class.forName("org.sqlite.JDBC");
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

    /** Fecha en formato de ancho fijo: el orden alfabético coincide con el cronológico. */
    public static String texto(LocalDateTime fecha) {
        return fecha.format(FMT);
    }

    public static LocalDateTime fecha(String texto) {
        return LocalDateTime.parse(texto, FMT);
    }

    public Connection get() {
        return conexion;
    }

    private void crearEsquema() throws SQLException {
        try (Statement st = conexion.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS producto(id TEXT PRIMARY KEY, tipo TEXT NOT NULL, categoria TEXT, "
                    + "nombre TEXT NOT NULL, descripcion TEXT, precio REAL NOT NULL, extra TEXT, disponible INTEGER NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS adicion(tipo TEXT NOT NULL, nombre TEXT NOT NULL, costo REAL NOT NULL, "
                    + "PRIMARY KEY(tipo, nombre))");
            st.execute("CREATE TABLE IF NOT EXISTS pedido(id INTEGER PRIMARY KEY, estado TEXT NOT NULL, total REAL NOT NULL, "
                    + "fecha TEXT NOT NULL, detalle TEXT, pagado INTEGER NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS venta(id_pago TEXT PRIMARY KEY, id_pedido INTEGER NOT NULL, fecha TEXT NOT NULL, "
                    + "subtotal REAL NOT NULL, descuento REAL NOT NULL, total REAL NOT NULL, metodo TEXT NOT NULL, promocion TEXT)");
            st.execute("CREATE TABLE IF NOT EXISTS venta_linea(id_pago TEXT NOT NULL, producto TEXT NOT NULL, "
                    + "cantidad INTEGER NOT NULL, precio REAL NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS cierre(id INTEGER PRIMARY KEY AUTOINCREMENT, fecha TEXT NOT NULL)");
        }
    }

    @Override
    public void close() {
        try {
            conexion.close();
        } catch (SQLException ignorada) {
            // nada que hacer al cerrar
        }
    }
}
