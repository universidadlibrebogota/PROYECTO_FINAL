package cafeteria.dominio.observer;

import cafeteria.dominio.Pedido;

import java.util.function.Consumer;

/** Avisa a caja cuando un pedido está listo para cobrar o tiene cambios relevantes (RF-23). */
public class NotificadorCaja implements ObservadorPedido {

    private final Consumer<String> canal;

    public NotificadorCaja() {
        this(System.out::println);
    }

    public NotificadorCaja(Consumer<String> canal) {
        this.canal = canal;
    }

    @Override
    public void actualizar(Pedido pedido) {
        String total = cafeteria.util.Formato.moneda(pedido.calcularTotal());
        switch (pedido.getEstado()) {
            case LISTO_PARA_ENTREGAR:
                canal.accept("[CAJA] Pedido #" + pedido.getIdPedido() + " listo para cobrar · " + total);
                break;
            case ENTREGADO:
                canal.accept("[CAJA] Pedido #" + pedido.getIdPedido() + " entregado · " + total);
                break;
            case CANCELADO:
                canal.accept("[CAJA] Pedido #" + pedido.getIdPedido() + " cancelado: no se cobra");
                break;
            default:
                break;
        }
    }
}
