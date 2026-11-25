package cafeteria.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Venta consolidada (un pago aprobado). Es la fuente única para caja y reportes. */
public class RegistroVenta {

    public final String idPago;
    public final int idPedido;
    public final LocalDateTime fechaHora;
    public final double subtotal;
    public final double descuento;
    public final double total;
    public final String metodoPago;
    public final String promocion;
    public final List<LineaVenta> lineas;

    public RegistroVenta(String idPago, int idPedido, LocalDateTime fechaHora, double subtotal, double descuento,
                         double total, String metodoPago, String promocion, List<LineaVenta> lineas) {
        this.idPago = idPago;
        this.idPedido = idPedido;
        this.fechaHora = fechaHora;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.total = total;
        this.metodoPago = metodoPago;
        this.promocion = promocion;
        this.lineas = Collections.unmodifiableList(new ArrayList<>(lineas));
    }
}
