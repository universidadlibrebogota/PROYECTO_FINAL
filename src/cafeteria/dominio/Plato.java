package cafeteria.dominio;

/** Plato. tipoPlato clasifica el plato (por ejemplo "Principal" o "Acompañamiento"). */
public class Plato extends Producto {

    private final String tipoPlato;

    public Plato(String id, String nombre, String descripcion, double precioBase, String tipoPlato) {
        super(id, nombre, descripcion, precioBase);
        this.tipoPlato = (tipoPlato == null || tipoPlato.trim().isEmpty()) ? "Principal" : tipoPlato.trim();
    }

    public String getTipoPlato() {
        return tipoPlato;
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " (" + tipoPlato + ")";
    }

    @Override
    public double getPrecio() {
        return getPrecioBase();
    }
}
