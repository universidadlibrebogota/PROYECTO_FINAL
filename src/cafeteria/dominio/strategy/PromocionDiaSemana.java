package cafeteria.dominio.strategy;

import cafeteria.dominio.Pedido;
import cafeteria.dominio.Producto;
import cafeteria.util.Formato;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;

/** Descuento porcentual vigente en ciertos días de la semana (p. ej. "Fin de semana brunch"). */
public class PromocionDiaSemana implements EstrategiaPromocion {

    private final String nombre;
    private final Set<DayOfWeek> dias;
    private final double porcentaje;
    private Predicate<Producto> alcance = p -> true;
    private String etiquetaAlcance = "Todo el pedido";
    private LocalDate vigenteDesde;
    private LocalDate vigenteHasta;

    public PromocionDiaSemana(String nombre, Set<DayOfWeek> dias, double porcentaje) {
        if (dias == null || dias.isEmpty()) {
            throw new IllegalArgumentException("Indica al menos un día de la semana");
        }
        if (porcentaje <= 0 || porcentaje > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100");
        }
        this.nombre = nombre;
        this.dias = EnumSet.copyOf(dias);
        this.porcentaje = porcentaje;
    }

    /** Limita el descuento a los productos que cumplan el filtro (p. ej. solo combos). */
    public PromocionDiaSemana conAlcance(String etiqueta, Predicate<Producto> filtro) {
        this.etiquetaAlcance = etiqueta;
        this.alcance = filtro;
        return this;
    }

    /** Periodo de vigencia opcional (null = sin límite). */
    public PromocionDiaSemana conVigencia(LocalDate desde, LocalDate hasta) {
        this.vigenteDesde = desde;
        this.vigenteHasta = hasta;
        return this;
    }

    @Override
    public boolean esAplicable(Pedido pedido, LocalDateTime fecha) {
        LocalDate dia = fecha.toLocalDate();
        boolean vigente = (vigenteDesde == null || !dia.isBefore(vigenteDesde))
                && (vigenteHasta == null || !dia.isAfter(vigenteHasta));
        return vigente && dias.contains(fecha.getDayOfWeek()) && pedido.calcularSubtotal(alcance) > 0;
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
        return nombre + " (" + Formato.porcentaje(porcentaje) + " · " + Formato.dias(dias) + " · " + etiquetaAlcance + ")";
    }
}
