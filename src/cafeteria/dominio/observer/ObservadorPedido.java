package cafeteria.dominio.observer;

import cafeteria.dominio.Pedido;

/** Patrón Observer: cualquier área interesada en los cambios de un pedido implementa esta interfaz. */
public interface ObservadorPedido {

    void actualizar(Pedido pedido);
}
