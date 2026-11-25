package cafeteria.dominio;

import java.time.LocalDateTime;

/** Pago simulado de un pedido (sin pasarelas externas reales). */
public class Pago {

    private final String idPago;
    private final int idPedido;
    private final double montoTotal;
    private final String metodoPago;
    private final LocalDateTime fechaHora;

    public Pago(String idPago, int idPedido, double montoTotal, String metodoPago, LocalDateTime fechaHora) {
        this.idPago = idPago;
        this.idPedido = idPedido;
        this.montoTotal = montoTotal;
        this.metodoPago = metodoPago;
        this.fechaHora = fechaHora;
    }

    public String getIdPago() {
        return idPago;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    /** Simulación estandarizada: se aprueba si hay monto positivo y un método de pago indicado. */
    public boolean procesarPago() {
        return montoTotal > 0 && metodoPago != null && !metodoPago.trim().isEmpty();
    }
}
