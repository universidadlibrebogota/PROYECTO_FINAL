package cafeteria.service;

import cafeteria.repository.LineaVenta;
import cafeteria.repository.RegistroVenta;
import cafeteria.util.Formato;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Métricas de ventas de un periodo. Una sola fuente de cálculo para caja, reportes, consola y GUI. */
public class ReporteVentas {

    public final LocalDateTime desde;
    public final LocalDateTime hasta;
    public final int numeroPedidos;
    public final double subtotal;
    public final double descuentos;
    public final double ingresos;
    public final Map<String, Double> ingresosPorMetodo;
    public final Map<String, Integer> unidadesPorProducto;
    public final Map<String, Double> ventasPorProducto;
    public final Map<String, Integer> promocionesAplicadas;

    private ReporteVentas(LocalDateTime desde, LocalDateTime hasta, int numeroPedidos, double subtotal, double descuentos,
                          double ingresos, Map<String, Double> metodos, Map<String, Integer> unidades,
                          Map<String, Double> ventas, Map<String, Integer> promos) {
        this.desde = desde;
        this.hasta = hasta;
        this.numeroPedidos = numeroPedidos;
        this.subtotal = subtotal;
        this.descuentos = descuentos;
        this.ingresos = ingresos;
        this.ingresosPorMetodo = Collections.unmodifiableMap(metodos);
        this.unidadesPorProducto = Collections.unmodifiableMap(unidades);
        this.ventasPorProducto = Collections.unmodifiableMap(ventas);
        this.promocionesAplicadas = Collections.unmodifiableMap(promos);
    }

    public static ReporteVentas de(List<RegistroVenta> ventas, LocalDateTime desde, LocalDateTime hasta) {
        double subtotal = 0;
        double descuentos = 0;
        double ingresos = 0;
        Map<String, Double> metodos = new LinkedHashMap<>();
        Map<String, Integer> unidades = new LinkedHashMap<>();
        Map<String, Double> porProducto = new LinkedHashMap<>();
        Map<String, Integer> promos = new LinkedHashMap<>();
        for (RegistroVenta v : ventas) {
            subtotal += v.subtotal;
            descuentos += v.descuento;
            ingresos += v.total;
            metodos.merge(v.metodoPago, v.total, Double::sum);
            if (v.descuento > 0 && v.promocion != null) {
                promos.merge(v.promocion, 1, Integer::sum);
            }
            for (LineaVenta l : v.lineas) {
                unidades.merge(l.producto, l.cantidad, Integer::sum);
                porProducto.merge(l.producto, l.precioUnitario * l.cantidad, Double::sum);
            }
        }
        return new ReporteVentas(desde, hasta, ventas.size(), subtotal, descuentos, ingresos, metodos,
                ordenarDesc(unidades), porProducto, promos);
    }

    private static Map<String, Integer> ordenarDesc(Map<String, Integer> mapa) {
        List<Map.Entry<String, Integer>> entradas = new ArrayList<>(mapa.entrySet());
        entradas.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        Map<String, Integer> r = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : entradas) {
            r.put(e.getKey(), e.getValue());
        }
        return r;
    }

    public double ticketPromedio() {
        return numeroPedidos == 0 ? 0 : ingresos / numeroPedidos;
    }

    /** Nombre del producto más vendido o null si no hubo ventas. */
    public String productoFavorito() {
        return unidadesPorProducto.isEmpty() ? null : unidadesPorProducto.keySet().iterator().next();
    }

    public double ingresosEn(String metodo) {
        return ingresosPorMetodo.getOrDefault(metodo, 0.0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Periodo: ").append(desde.format(Formato.FECHA_HORA)).append(" → ").append(hasta.format(Formato.FECHA_HORA)).append('\n');
        sb.append("Pedidos cobrados : ").append(numeroPedidos).append('\n');
        sb.append("Ventas brutas    : ").append(Formato.moneda(subtotal)).append('\n');
        sb.append("Descuentos       : ").append(Formato.moneda(descuentos)).append('\n');
        sb.append("INGRESOS NETOS   : ").append(Formato.moneda(ingresos)).append('\n');
        sb.append("Ticket promedio  : ").append(Formato.moneda(ticketPromedio())).append('\n');
        sb.append("Por método de pago:\n");
        if (ingresosPorMetodo.isEmpty()) {
            sb.append("  (sin pagos)\n");
        }
        ingresosPorMetodo.forEach((m, v) -> sb.append("  - ").append(m).append(": ").append(Formato.moneda(v)).append('\n'));
        sb.append("Productos vendidos:\n");
        if (unidadesPorProducto.isEmpty()) {
            sb.append("  (sin ventas)\n");
        }
        unidadesPorProducto.forEach((p, u) -> sb.append("  - ").append(p).append(": ").append(u).append(" und · ")
                .append(Formato.moneda(ventasPorProducto.get(p))).append('\n'));
        sb.append("Promociones aplicadas:\n");
        if (promocionesAplicadas.isEmpty()) {
            sb.append("  (ninguna)\n");
        }
        promocionesAplicadas.forEach((p, n) -> sb.append("  - ").append(p).append(": ").append(n).append(" vez/veces\n"));
        return sb.toString();
    }
}
