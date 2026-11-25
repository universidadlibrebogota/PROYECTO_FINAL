package cafeteria.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Implementación en memoria de ventas y cierres. */
public class VentaRepositoryMemoria implements VentaRepository {

    private final List<RegistroVenta> ventas = new ArrayList<>();
    private LocalDateTime ultimoCierre;

    @Override
    public void guardar(RegistroVenta venta) {
        ventas.add(venta);
    }

    @Override
    public List<RegistroVenta> listar(LocalDateTime desde, LocalDateTime hasta) {
        List<RegistroVenta> resultado = new ArrayList<>();
        for (RegistroVenta v : ventas) {
            if (!v.fechaHora.isBefore(desde) && !v.fechaHora.isAfter(hasta)) {
                resultado.add(v);
            }
        }
        return resultado;
    }

    @Override
    public LocalDateTime ultimoCierre() {
        return ultimoCierre;
    }

    @Override
    public void registrarCierre(LocalDateTime fecha) {
        this.ultimoCierre = fecha;
    }
}
