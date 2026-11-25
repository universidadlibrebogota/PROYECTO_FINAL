package cafeteria.dominio.observer;

import cafeteria.dominio.Pedido;

import java.util.function.Consumer;

/**
 * Avisa a cocina cuando un pedido llega, cambia o se cancela (RF-22).
 * El canal de salida es inyectable: consola, panel gráfico, sonido, etc.
 */
public class NotificadorCocina implements ObservadorPedido {

    private final Consumer<String> canal;

    public NotificadorCocina() {
        this(System.out::println);
    }

    public NotificadorCocina(Consumer<String> canal) {
        this.canal = canal;
    }

    @Override
    public void actualizar(Pedido pedido) {
        switch (pedido.getEstado()) {
            case PENDIENTE:
                canal.accept("[COCINA] " + pedido + " · pendiente de preparar");
                break;
            case EN_PREPARACION:
                canal.accept("[COCINA] Pedido #" + pedido.getIdPedido() + " en preparación");
                break;
            case CANCELADO:
                canal.accept("[COCINA] Pedido #" + pedido.getIdPedido() + " CANCELADO: detener preparación");
                break;
            default:
                break;
        }
    }
}
