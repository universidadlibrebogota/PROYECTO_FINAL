package cafeteria.dominio.strategy;

import cafeteria.dominio.Pedido;

import java.time.LocalDateTime;

/** Patrón Strategy: cada regla de descuento es una implementación intercambiable. */
public interface EstrategiaPromocion {

    /** ¿La promoción aplica a este pedido en la fecha/hora indicada? */
    boolean esAplicable(Pedido pedido, LocalDateTime fecha);

    /** Valor (en pesos) a descontar del subtotal del pedido. */
    double calcularDescuento(Pedido pedido);
}
