package cafeteria.repository;

/** Línea de una venta: producto, cantidad y precio unitario al momento del cobro. */
public class LineaVenta {

    public final String producto;
    public final int cantidad;
    public final double precioUnitario;

    public LineaVenta(String producto, int cantidad, double precioUnitario) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }
}
