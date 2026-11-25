package cafeteria.dominio.factory;

import cafeteria.dominio.Postre;
import cafeteria.dominio.Producto;
import cafeteria.util.Formato;

/** Crea postres. extra = "true"/"false" (esDulce); por defecto dulce. */
public class PostreFactory implements ProductoFactory {

    public static final String PREFIJO = "POS";

    @Override
    public Producto crearProducto(String nombre, String desc, double precio, String extra) {
        boolean dulce = extra == null || extra.trim().isEmpty() || Boolean.parseBoolean(extra.trim());
        return new Postre(PREFIJO + "-" + Formato.slug(nombre), nombre, desc, precio, dulce);
    }
}
