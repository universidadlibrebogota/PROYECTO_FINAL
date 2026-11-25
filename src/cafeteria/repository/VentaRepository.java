package cafeteria.repository;

import java.time.LocalDateTime;
import java.util.List;

/** Persistencia de ventas y cierres de caja (base de los reportes). */
public interface VentaRepository {

    void guardar(RegistroVenta venta);

    /** Ventas con fechaHora en [desde, hasta], de la más antigua a la más reciente. */
    List<RegistroVenta> listar(LocalDateTime desde, LocalDateTime hasta);

    /** Fecha del último cierre de caja, o null si nunca se ha cerrado. */
    LocalDateTime ultimoCierre();

    void registrarCierre(LocalDateTime fecha);
}
