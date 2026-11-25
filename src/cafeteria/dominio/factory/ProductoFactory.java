package cafeteria.dominio.factory;

import cafeteria.dominio.Producto;

/**
 * Patrón Factory Method: cada categoría de producto decide cómo construirse.
 * "extra" es el dato específico de la categoría (tamaño, tipo de plato, esDulce...).
 */
public interface ProductoFactory {

    Producto crearProducto(String nombre, String desc, double precio, String extra);
}
