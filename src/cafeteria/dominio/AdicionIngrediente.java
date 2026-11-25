package cafeteria.dominio;

/** Decorador concreto: añade un ingrediente opcional con costo adicional. */
public class AdicionIngrediente extends ProductoDecorador {

    private final String nombreAdicion;
    private final double costoAdicional;

    public AdicionIngrediente(Producto producto, String nombreAdicion, double costoAdicional) {
        super(producto);
        if (nombreAdicion == null || nombreAdicion.trim().isEmpty()) {
            throw new IllegalArgumentException("La adición necesita un nombre");
        }
        if (costoAdicional < 0) {
            throw new IllegalArgumentException("El costo adicional no puede ser negativo");
        }
        this.nombreAdicion = nombreAdicion;
        this.costoAdicional = costoAdicional;
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " + " + nombreAdicion;
    }

    @Override
    public double getPrecio() {
        return super.getPrecio() + costoAdicional;
    }
}
