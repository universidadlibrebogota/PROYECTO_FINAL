package cafeteria.dominio.factory;

import cafeteria.dominio.Bebida;
import cafeteria.dominio.Producto;
import cafeteria.util.Formato;

/** Crea bebidas. extra = tamaño ("Pequeño", "Mediano", "Grande"). */
public class BebidaFactory implements ProductoFactory {

    public static final String PREFIJO = "BEB";

    @Override
    public Producto crearProducto(String nombre, String desc, double precio, String extra) {
        return new Bebida(PREFIJO + "-" + Formato.slug(nombre), nombre, desc, precio, extra);
    }
}
