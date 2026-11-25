package cafeteria.repository;

import java.time.LocalDateTime;

/** Instantánea de un pedido para el historial (no reconstruye decoradores). */
public class RegistroPedido {

    public final int id;
    public final String estado;
    public final double total;
    public final LocalDateTime fecha;
    public final String detalle;
    public final boolean pagado;

    public RegistroPedido(int id, String estado, double total, LocalDateTime fecha, String detalle, boolean pagado) {
        this.id = id;
        this.estado = estado;
        this.total = total;
        this.fecha = fecha;
        this.detalle = detalle;
        this.pagado = pagado;
    }
}
