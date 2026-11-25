package cafeteria.repository;

import java.util.List;

/** Persistencia del historial de pedidos. */
public interface PedidoRepository {

    /** Mayor id usado hasta ahora (1000 si no hay pedidos). */
    int ultimoId();

    /** Inserta o actualiza la instantánea del pedido. */
    void guardar(RegistroPedido registro);

    /** Todos los pedidos, el más reciente primero. */
    List<RegistroPedido> listar();
}
