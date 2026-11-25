package cafeteria.dominio;

/**
 * Decorador base (patrón Decorator). Envuelve un Producto y puede alterar
 * descripción y precio sin crear una clase por cada combinación.
 * Hereda id, nombre y disponibilidad del producto envuelto al momento de decorar.
 */
public class ProductoDecorador extends Producto {

    protected Producto producto;

    public ProductoDecorador(Producto producto) {
        super(requerir(producto).getId(), producto.getNombre(), producto.getDescripcion(), producto.getPrecio());
        this.producto = producto;
        setDisponible(producto.isDisponible());
    }

    private static Producto requerir(Producto p) {
        if (p == null) {
            throw new IllegalArgumentException("El decorador necesita un producto a envolver");
        }
        return p;
    }

    @Override
    public String getDescripcion() {
        return producto.getDescripcion();
    }

    @Override
    public double getPrecio() {
        return producto.getPrecio();
    }
}
