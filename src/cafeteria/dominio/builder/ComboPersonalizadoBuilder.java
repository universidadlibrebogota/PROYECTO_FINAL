package cafeteria.dominio.builder;

import cafeteria.dominio.Combo;
import cafeteria.dominio.Producto;
import cafeteria.util.Formato;

import java.util.ArrayList;
import java.util.List;

/** Builder concreto de combos personalizados (plato + bebida + acompañamiento + descuento). */
public class ComboPersonalizadoBuilder implements ComboBuilder {

    /** Prefijo de los ids de combo (permite a las promociones identificar combos). */
    public static final String PREFIJO = "COMBO";

    private Producto platoPrincipal;
    private Producto bebida;
    private Producto acompanamiento;
    private double descuento;

    public ComboPersonalizadoBuilder() {
        reset();
    }

    @Override
    public void reset() {
        platoPrincipal = null;
        bebida = null;
        acompanamiento = null;
        descuento = 0;
    }

    @Override
    public ComboBuilder setPlatoPrincipal(Producto plato) {
        this.platoPrincipal = plato;
        return this;
    }

    @Override
    public ComboBuilder setBebida(Producto bebida) {
        this.bebida = bebida;
        return this;
    }

    @Override
    public ComboBuilder setAcompanamiento(Producto acomp) {
        this.acompanamiento = acomp;
        return this;
    }

    @Override
    public ComboBuilder setDescuento(double descuento) {
        if (descuento < 0 || descuento > 100) {
            throw new IllegalArgumentException("El descuento del combo debe estar entre 0 y 100");
        }
        this.descuento = descuento;
        return this;
    }

    @Override
    public Combo build() {
        List<String> faltantes = new ArrayList<>();
        if (platoPrincipal == null) {
            faltantes.add("plato principal");
        }
        if (bebida == null) {
            faltantes.add("bebida");
        }
        if (acompanamiento == null) {
            faltantes.add("acompañamiento");
        }
        if (!faltantes.isEmpty()) {
            throw new IllegalStateException("Faltan componentes obligatorios del combo: " + String.join(", ", faltantes));
        }
        String id = PREFIJO + "-" + Formato.slug(platoPrincipal.getNombre()) + "-" + Formato.slug(bebida.getNombre())
                + "-" + Formato.slug(acompanamiento.getNombre());
        return new Combo(id, "Combo " + platoPrincipal.getNombre(), platoPrincipal, bebida, acompanamiento, descuento);
    }
}
