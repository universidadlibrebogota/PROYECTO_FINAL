package cafeteria.service;

import cafeteria.repository.RegistroVenta;
import cafeteria.repository.VentaRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Módulo "Reportes y Cierre de Caja": abre/cierra la caja y consolida los pagos del turno. */
public class CajaService {

    private static final LocalDateTime INICIO_DE_LOS_TIEMPOS = LocalDateTime.of(2000, 1, 1, 0, 0);

    private final VentaRepository ventas;
    private final Clock clock;
    private boolean abierta = true;

    public CajaService(VentaRepository ventas, Clock clock) {
        this.ventas = ventas;
        this.clock = clock;
    }

    public boolean estaAbierta() {
        return abierta;
    }

    public void abrirCaja() {
        abierta = true;
    }

    /** Registra el cobro (solo con la caja abierta). */
    public void registrarVenta(RegistroVenta venta) {
        if (!abierta) {
            throw new IllegalStateException("La caja está cerrada: ábrela para registrar pagos");
        }
        ventas.guardar(venta);
    }

    public LocalDateTime inicioTurno() {
        LocalDateTime ultimo = ventas.ultimoCierre();
        return ultimo == null ? INICIO_DE_LOS_TIEMPOS : ultimo.plusNanos(1_000_000);
    }

    public List<RegistroVenta> ventasDelTurno() {
        return ventas.listar(inicioTurno(), LocalDateTime.now(clock));
    }

    /** Vista previa del cierre, sin cerrar. */
    public ReporteVentas reporteTurnoActual() {
        LocalDateTime ahora = LocalDateTime.now(clock);
        return ReporteVentas.de(ventas.listar(inicioTurno(), ahora), inicioTurno(), ahora);
    }

    /** Cierre de caja (RF-29): consolida los pagos desde el último cierre y deja la caja cerrada. */
    public ReporteVentas cerrarCaja() {
        if (!abierta) {
            throw new IllegalStateException("La caja ya está cerrada");
        }
        LocalDateTime ahora = LocalDateTime.now(clock);
        ReporteVentas reporte = ReporteVentas.de(ventas.listar(inicioTurno(), ahora), inicioTurno(), ahora);
        ventas.registrarCierre(ahora);
        abierta = false;
        return reporte;
    }

    /** Reporte de un periodo arbitrario (RF-30). */
    public ReporteVentas reporte(LocalDateTime desde, LocalDateTime hasta) {
        return ReporteVentas.de(ventas.listar(desde, hasta), desde, hasta);
    }

    /** Las últimas n ventas del turno, la más reciente primero. */
    public List<RegistroVenta> movimientosRecientes(int n) {
        List<RegistroVenta> lista = new ArrayList<>(ventasDelTurno());
        java.util.Collections.reverse(lista);
        return lista.size() > n ? new ArrayList<>(lista.subList(0, n)) : lista;
    }
}
