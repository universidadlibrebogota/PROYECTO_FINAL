package cafeteria.dominio;

/**
 * Componente base del patrón Decorator. Representa un producto del menú.
 * Bebida, Plato, Postre, Combo y ProductoDecorador extienden esta clase.
 */
public class Producto {

    private final String id;
    private String nombre;
    private String descripcion;
    private double precioBase;
    private boolean disponible;

    public Producto(String id, String nombre, String descripcion, double precioBase) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El producto necesita un id");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El producto necesita un nombre");
        }
        if (precioBase < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion == null ? "" : descripcion;
        this.precioBase = precioBase;
        this.disponible = true;
    }

    /** Accesor implícito del atributo id (el diagrama lo declara privado). */
    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPrecio() {
        return precioBase;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    /** Accesor implícito para que las subclases (Bebida) puedan especializar getPrecio(). */
    protected double getPrecioBase() {
        return precioBase;
    }
}
