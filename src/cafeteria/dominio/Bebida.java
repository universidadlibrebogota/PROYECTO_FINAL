package cafeteria.dominio;

/** Bebida: el tamaño especializa descripción y precio (Pequeño 0,9x · Mediano 1x · Grande 1,2x). */
public class Bebida extends Producto {

    private final String tamano;

    public Bebida(String id, String nombre, String descripcion, double precioBase, String tamano) {
        super(id, nombre, descripcion, precioBase);
        this.tamano = (tamano == null || tamano.trim().isEmpty()) ? "Mediano" : tamano.trim();
    }

    public String getTamano() {
        return tamano;
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " (" + tamano + ")";
    }

    @Override
    public double getPrecio() {
        return Math.round(getPrecioBase() * factorTamano(tamano));
    }

    private static double factorTamano(String t) {
        String s = t.toLowerCase();
        if (s.startsWith("peque")) {
            return 0.9;
        }
        if (s.startsWith("grand")) {
            return 1.2;
        }
        return 1.0;
    }
}
