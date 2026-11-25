package cafeteria.dominio.observer;

import cafeteria.dominio.Pedido;

import java.util.function.Consumer;

/** Avisa al personal de servicio de los cambios de estado, sobre todo cuando está listo para entregar (RF-24). */
public class NotificadorMesero implements ObservadorPedido {

    private final Consumer<String> canal;

    public NotificadorMesero() {
        this(System.out::println);
    }

    public NotificadorMesero(Consumer<String> canal) {
        this.canal = canal;
    }

    @Override
    public void actualizar(Pedido pedido) {
        switch (pedido.getEstado()) {
            case EN_PREPARACION:
                canal.accept("[MESERO] Pedido #" + pedido.getIdPedido() + " en preparación");
                break;
            case LISTO_PARA_ENTREGAR:
                canal.accept("[MESERO] ¡Pedido #" + pedido.getIdPedido() + " LISTO! Llévalo al cliente");
                break;
            case ENTREGADO:
                canal.accept("[MESERO] Pedido #" + pedido.getIdPedido() + " entregado");
                break;
            case CANCELADO:
                canal.accept("[MESERO] Pedido #" + pedido.getIdPedido() + " cancelado");
                break;
            default:
                break;
        }
    }
}
