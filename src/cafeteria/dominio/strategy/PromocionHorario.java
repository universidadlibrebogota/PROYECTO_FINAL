package cafeteria.dominio.strategy;

import cafeteria.dominio.Pedido;
import cafeteria.dominio.Producto;
import cafeteria.util.Formato;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Descuento porcentual vigente en una franja horaria [desde, hasta), opcionalmente
 * restringida a ciertos días (p. ej. "Mañanas más dulces": Lun–Vie 7:00–11:30).
 */
public class PromocionHorario implements EstrategiaPromocion {

    private final String nombre;
    private final LocalTime desde;
    private final LocalTime hasta;
    private final double porcentaje;
    private Set<DayOfWeek> dias = EnumSet.noneOf(DayOfWeek.class); // vacío = todos los días
    private Predicate<Producto> alcance = p -> true;
    private String etiquetaAlcance = "Todo el pedido";
    private LocalDate vigenteDesde;
    private LocalDate vigenteHasta;

    public PromocionHorario(String nombre, LocalTime desde, LocalTime hasta, double porcentaje) {
        if (desde == null || hasta == null || !desde.isBefore(hasta)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la de fin");
        }
        if (porcentaje <= 0 || porcentaje > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100");
        }
        this.nombre = nombre;
        this.desde = desde;
        this.hasta = hasta;
        this.porcentaje = porcentaje;
    }

    public PromocionHorario enDias(Set<DayOfWeek> dias) {
        this.dias = (dias == null || dias.isEmpty()) ? EnumSet.noneOf(DayOfWeek.class) : EnumSet.copyOf(dias);
        return this;
    }

    public PromocionHorario conAlcance(String etiqueta, Predicate<Producto> filtro) {
        this.etiquetaAlcance = etiqueta;
        this.alcance = filtro;
        return this;
    }

    public PromocionHorario conVigencia(LocalDate desde, LocalDate hasta) {
        this.vigenteDesde = desde;
        this.vigenteHasta = hasta;
        return this;
    }

    @Override
    public boolean esAplicable(Pedido pedido, LocalDateTime fecha) {
        LocalDate dia = fecha.toLocalDate();
        LocalTime hora = fecha.toLocalTime();
        boolean vigente = (vigenteDesde == null || !dia.isBefore(vigenteDesde))
                && (vigenteHasta == null || !dia.isAfter(vigenteHasta));
        boolean diaOk = dias.isEmpty() || dias.contains(fecha.getDayOfWeek());
        boolean horaOk = !hora.isBefore(desde) && hora.isBefore(hasta);
        return vigente && diaOk && horaOk && pedido.calcularSubtotal(alcance) > 0;
    }

    @Override
    public double calcularDescuento(Pedido pedido) {
        return Math.round(pedido.calcularSubtotal(alcance) * porcentaje / 100.0);
    }

    public String getNombre() {
        return nombre;
    }

    public double getPorcentaje() {
        return porcentaje;
    }

    @Override
    public String toString() {
        return nombre + " (" + Formato.porcentaje(porcentaje) + " · " + Formato.dias(dias) + " "
                + Formato.hora(desde) + "–" + Formato.hora(hasta) + " · " + etiquetaAlcance + ")";
    }
}
