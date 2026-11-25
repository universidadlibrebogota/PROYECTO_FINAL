package cafeteria.dominio.factory;

import cafeteria.dominio.Plato;
import cafeteria.dominio.Producto;
import cafeteria.util.Formato;

/** Crea platos. extra = tipo de plato ("Principal", "Acompañamiento"...). */
public class PlatoFactory implements ProductoFactory {

    public static final String PREFIJO = "PLA";

    @Override
    public Producto crearProducto(String nombre, String desc, double precio, String extra) {
        return new Plato(PREFIJO + "-" + Formato.slug(nombre), nombre, desc, precio, extra);
    }
}
