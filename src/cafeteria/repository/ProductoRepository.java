package cafeteria.repository;

import java.util.List;
import java.util.Map;

/** Persistencia del menú: productos y adiciones configurables. */
public interface ProductoRepository {

    /** Productos en el orden en que fueron registrados. */
    List<ConfiguracionProducto> listar();

    /** Inserta o actualiza (por id) conservando la posición. */
    void guardar(ConfiguracionProducto configuracion);

    void eliminar(String id);

    /** tipo de producto -> (nombre de adición -> costo), en orden de registro. */
    Map<String, Map<String, Double>> listarAdiciones();

    void guardarAdicion(String tipo, String nombre, double costo);

    void eliminarAdicion(String tipo, String nombre);
}
