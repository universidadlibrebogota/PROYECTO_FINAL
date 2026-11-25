package cafeteria.dominio;

/** Estados del ciclo de vida de un pedido. */
public enum EstadoPedido {
    PENDIENTE,
    EN_PREPARACION,
    LISTO_PARA_ENTREGAR,
    ENTREGADO,
    CANCELADO
}
