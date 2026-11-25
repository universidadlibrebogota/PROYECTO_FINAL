package cafeteria.dominio.strategy;

import cafeteria.dominio.Pedido;

import java.time.LocalDateTime;

/** Estrategia nula: no descuenta nada. Es la estrategia por defecto de todo pedido. */
public class SinPromocion implements EstrategiaPromocion {

    @Override
    public boolean esAplicable(Pedido pedido, LocalDateTime fecha) {
        return true;
    }

    @Override
    public double calcularDescuento(Pedido pedido) {
        return 0;
    }

    @Override
    public String toString() {
        return "Sin promoción";
    }
}
