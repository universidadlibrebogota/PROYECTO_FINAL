package cafeteria.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Implementación en memoria del historial de pedidos. */
public class PedidoRepositoryMemoria implements PedidoRepository {

    private final Map<Integer, RegistroPedido> pedidos = new LinkedHashMap<>();

    @Override
    public int ultimoId() {
        return pedidos.keySet().stream().mapToInt(Integer::intValue).max().orElse(1000);
    }

    @Override
    public void guardar(RegistroPedido r) {
        pedidos.put(r.id, r);
    }

    @Override
    public List<RegistroPedido> listar() {
        List<RegistroPedido> lista = new ArrayList<>(pedidos.values());
        Collections.reverse(lista);
        return lista;
    }
}
