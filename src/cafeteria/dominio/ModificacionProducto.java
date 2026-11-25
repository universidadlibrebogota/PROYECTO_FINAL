package cafeteria.dominio;

/** Decorador concreto: modifica o retira algo del producto (p. ej. "Sin azúcar") sin costo. */
public class ModificacionProducto extends ProductoDecorador {

    private final String notaModificacion;

    public ModificacionProducto(Producto producto, String notaModificacion) {
        super(producto);
        if (notaModificacion == null || notaModificacion.trim().isEmpty()) {
            throw new IllegalArgumentException("La modificación necesita una nota");
        }
        this.notaModificacion = notaModificacion.trim();
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " | " + notaModificacion;
    }

    @Override
    public double getPrecio() {
        return super.getPrecio();
    }
}
