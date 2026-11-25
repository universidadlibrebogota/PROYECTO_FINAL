package cafeteria.service;

import cafeteria.dominio.Combo;
import cafeteria.dominio.Producto;
import cafeteria.dominio.builder.ComboBuilder;
import cafeteria.dominio.builder.ComboPersonalizadoBuilder;
import cafeteria.repository.ConfiguracionProducto;
import cafeteria.util.Formato;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/** Módulo "Combos" (Builder): qué productos pueden elegirse en cada paso y cómo se arma el combo. */
public class ComboService {

    private final MenuService menu;
    private final Map<PasoCombo, List<Predicate<ConfiguracionProducto>>> condiciones = new EnumMap<>(PasoCombo.class);
    private double descuentoPorDefecto = 15;

    public ComboService(MenuService menu) {
        this.menu = menu;
        for (PasoCombo p : PasoCombo.values()) {
            condiciones.put(p, new ArrayList<>());
        }
        permitirEnPaso(PasoCombo.PLATO_PRINCIPAL, c -> "Plato".equals(c.tipo) && esTipoPlato(c, "Principal"));
        permitirEnPaso(PasoCombo.BEBIDA, c -> "Bebida".equals(c.tipo));
        permitirEnPaso(PasoCombo.ACOMPANAMIENTO, c -> "Plato".equals(c.tipo) && esTipoPlato(c, "Acompañamiento"));
        permitirEnPaso(PasoCombo.ACOMPANAMIENTO, c -> "Postre".equals(c.tipo));
    }

    private static boolean esTipoPlato(ConfiguracionProducto c, String tipoPlato) {
        return Formato.slug(c.extra).equals(Formato.slug(tipoPlato));
    }

    /** Punto de extensión (OCP): qué productos pueden elegirse en un paso del combo (RF-11/RF-13). */
    public void permitirEnPaso(PasoCombo paso, Predicate<ConfiguracionProducto> condicion) {
        condiciones.get(paso).add(condicion);
    }

    /** Alternativas disponibles para un paso. */
    public List<Producto> opciones(PasoCombo paso) {
        List<Producto> r = new ArrayList<>();
        for (Producto p : menu.disponibles()) {
            ConfiguracionProducto c = menu.configuracion(p.getId());
            for (Predicate<ConfiguracionProducto> cond : condiciones.get(paso)) {
                if (c != null && cond.test(c)) {
                    r.add(p);
                    break;
                }
            }
        }
        return r;
    }

    public double getDescuentoPorDefecto() {
        return descuentoPorDefecto;
    }

    public void setDescuentoPorDefecto(double porcentaje) {
        if (porcentaje < 0 || porcentaje > 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
        }
        this.descuentoPorDefecto = porcentaje;
    }

    /** Builder nuevo con el descuento configurado. */
    public ComboBuilder nuevoConstructor() {
        ComboBuilder b = new ComboPersonalizadoBuilder();
        b.setDescuento(descuentoPorDefecto);
        return b;
    }

    public Combo armar(Producto plato, Producto bebida, Producto acompanamiento) {
        return nuevoConstructor().setPlatoPrincipal(plato).setBebida(bebida).setAcompanamiento(acompanamiento).build();
    }
}
