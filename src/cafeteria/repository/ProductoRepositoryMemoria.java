package cafeteria.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Implementación en memoria (pruebas y modo sin SQLite). */
public class ProductoRepositoryMemoria implements ProductoRepository {

    private final Map<String, ConfiguracionProducto> productos = new LinkedHashMap<>();
    private final Map<String, Map<String, Double>> adiciones = new LinkedHashMap<>();

    @Override
    public List<ConfiguracionProducto> listar() {
        return new ArrayList<>(productos.values());
    }

    @Override
    public void guardar(ConfiguracionProducto c) {
        productos.put(c.id, c);
    }

    @Override
    public void eliminar(String id) {
        productos.remove(id);
    }

    @Override
    public Map<String, Map<String, Double>> listarAdiciones() {
        Map<String, Map<String, Double>> copia = new LinkedHashMap<>();
        adiciones.forEach((tipo, m) -> copia.put(tipo, new LinkedHashMap<>(m)));
        return copia;
    }

    @Override
    public void guardarAdicion(String tipo, String nombre, double costo) {
        adiciones.computeIfAbsent(tipo, t -> new LinkedHashMap<>()).put(nombre, costo);
    }

    @Override
    public void eliminarAdicion(String tipo, String nombre) {
        Map<String, Double> m = adiciones.get(tipo);
        if (m != null) {
            m.remove(nombre);
        }
    }
}
