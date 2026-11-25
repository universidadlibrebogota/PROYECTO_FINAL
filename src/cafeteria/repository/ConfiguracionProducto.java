package cafeteria.repository;

/**
 * Registro de configuración de un producto del menú. Es lo que se persiste:
 * al arrancar, las fábricas (Factory Method) reconstruyen los Producto a partir de él.
 */
public class ConfiguracionProducto {

    public final String id;
    public final String tipo;
    public final String categoria;
    public final String nombre;
    public final String descripcion;
    public final double precio;
    public final String extra;
    public final boolean disponible;

    public ConfiguracionProducto(String id, String tipo, String categoria, String nombre,
                                 String descripcion, double precio, String extra, boolean disponible) {
        this.id = id;
        this.tipo = tipo;
        this.categoria = categoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.extra = extra;
        this.disponible = disponible;
    }

    public ConfiguracionProducto conDisponible(boolean d) {
        return new ConfiguracionProducto(id, tipo, categoria, nombre, descripcion, precio, extra, d);
    }

    public ConfiguracionProducto conCategoria(String c) {
        return new ConfiguracionProducto(id, tipo, c, nombre, descripcion, precio, extra, disponible);
    }
}
