package cafeteria.dominio;

/** Postre. esDulce distingue postres dulces de salados. */
public class Postre extends Producto {

    private final boolean esDulce;

    public Postre(String id, String nombre, String descripcion, double precioBase, boolean esDulce) {
        super(id, nombre, descripcion, precioBase);
        this.esDulce = esDulce;
    }

    public boolean isEsDulce() {
        return esDulce;
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + (esDulce ? " (dulce)" : " (salado)");
    }

    @Override
    public double getPrecio() {
        return getPrecioBase();
    }
}
