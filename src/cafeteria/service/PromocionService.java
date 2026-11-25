package cafeteria.service;

import cafeteria.dominio.Pedido;
import cafeteria.dominio.Producto;
import cafeteria.dominio.builder.ComboPersonalizadoBuilder;
import cafeteria.dominio.factory.BebidaFactory;
import cafeteria.dominio.factory.PlatoFactory;
import cafeteria.dominio.factory.PostreFactory;
import cafeteria.dominio.strategy.EstrategiaPromocion;
import cafeteria.dominio.strategy.PromocionDiaSemana;
import cafeteria.dominio.strategy.PromocionHorario;
import cafeteria.dominio.strategy.SinPromocion;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Módulo "Promociones" (Strategy). Mantiene el catálogo de estrategias y elige,
 * para cada pedido, la de mayor descuento vigente en ese momento (una por pedido).
 */
public class PromocionService {

    private final Clock clock;
    private final Map<EstrategiaPromocion, Boolean> promociones = new LinkedHashMap<>();
    private final Map<String, Predicate<Producto>> alcances = new LinkedHashMap<>();
    private final EstrategiaPromocion ninguna = new SinPromocion();

    public PromocionService(Clock clock) {
        this.clock = clock;
        alcances.put("Todo el pedido", p -> true);
        alcances.put("Combos", p -> p.getId().startsWith(ComboPersonalizadoBuilder.PREFIJO + "-"));
        alcances.put("Bebidas", p -> p.getId().startsWith(BebidaFactory.PREFIJO + "-"));
        alcances.put("Platos", p -> p.getId().startsWith(PlatoFactory.PREFIJO + "-"));
        alcances.put("Postres", p -> p.getId().startsWith(PostreFactory.PREFIJO + "-"));
    }

    public LocalDateTime ahora() {
        return LocalDateTime.now(clock);
    }

    // ----------------------------------------------------- catálogo

    /** Punto de extensión (OCP): registra cualquier estrategia nueva. */
    public void registrarPromocion(EstrategiaPromocion estrategia) {
        promociones.put(estrategia, Boolean.TRUE);
    }

    public void eliminarPromocion(EstrategiaPromocion estrategia) {
        promociones.remove(estrategia);
    }

    public void activar(EstrategiaPromocion estrategia, boolean activa) {
        if (promociones.containsKey(estrategia)) {
            promociones.put(estrategia, activa);
        }
    }

    public boolean estaActiva(EstrategiaPromocion estrategia) {
        return Boolean.TRUE.equals(promociones.get(estrategia));
    }

    public List<EstrategiaPromocion> listar() {
        return new ArrayList<>(promociones.keySet());
    }

    /** Punto de extensión: nuevo alcance de promoción (qué productos cubre). */
    public void registrarAlcance(String nombre, Predicate<Producto> filtro) {
        alcances.put(nombre, filtro);
    }

    public List<String> alcances() {
        return new ArrayList<>(alcances.keySet());
    }

    public EstrategiaPromocion crearPorDia(String nombre, Set<DayOfWeek> dias, double porcentaje, String alcance,
                                           LocalDate vigenteDesde, LocalDate vigenteHasta) {
        PromocionDiaSemana p = new PromocionDiaSemana(nombre, dias, porcentaje)
                .conAlcance(alcance, filtro(alcance)).conVigencia(vigenteDesde, vigenteHasta);
        registrarPromocion(p);
        return p;
    }

    public EstrategiaPromocion crearPorHorario(String nombre, Set<DayOfWeek> dias, LocalTime desde, LocalTime hasta,
                                               double porcentaje, String alcance, LocalDate vigenteDesde,
                                               LocalDate vigenteHasta) {
        PromocionHorario p = new PromocionHorario(nombre, desde, hasta, porcentaje).enDias(dias)
                .conAlcance(alcance, filtro(alcance)).conVigencia(vigenteDesde, vigenteHasta);
        registrarPromocion(p);
        return p;
    }

    private Predicate<Producto> filtro(String alcance) {
        Predicate<Producto> f = alcances.get(alcance);
        if (f == null) {
            throw new IllegalArgumentException("Alcance desconocido: " + alcance);
        }
        return f;
    }

    // --------------------------------------------------- selección

    /** La promoción activa y aplicable con mayor descuento; SinPromocion si ninguna aplica. */
    public EstrategiaPromocion mejorPara(Pedido pedido, LocalDateTime fecha) {
        EstrategiaPromocion mejor = ninguna;
        double mayor = 0;
        for (Map.Entry<EstrategiaPromocion, Boolean> e : promociones.entrySet()) {
            if (Boolean.TRUE.equals(e.getValue()) && e.getKey().esAplicable(pedido, fecha)) {
                double d = e.getKey().calcularDescuento(pedido);
                if (d > mayor) {
                    mayor = d;
                    mejor = e.getKey();
                }
            }
        }
        return mejor;
    }

    /** Asigna al pedido la mejor promoción vigente ahora (RF-27). */
    public void aplicar(Pedido pedido) {
        pedido.setEstrategiaPromocion(mejorPara(pedido, ahora()));
    }

    /** Promociones de ejemplo (las de la interfaz de referencia). */
    public void cargarPromocionesIniciales() {
        crearPorHorario("Mañanas más dulces", EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                LocalTime.of(7, 0), LocalTime.of(11, 30), 15, "Combos", null, null);
        crearPorHorario("Tarde de amigas", EnumSet.of(DayOfWeek.WEDNESDAY),
                LocalTime.of(15, 0), LocalTime.of(18, 0), 20, "Bebidas", null, null);
        crearPorDia("Fin de semana brunch", EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY), 10, "Platos", null, null);
    }
}
