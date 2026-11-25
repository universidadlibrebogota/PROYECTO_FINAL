package cafeteria.dominio.builder;

import cafeteria.dominio.Combo;
import cafeteria.dominio.Producto;

/** Patrón Builder: construye un Combo paso a paso. */
public interface ComboBuilder {

    void reset();

    ComboBuilder setPlatoPrincipal(Producto plato);

    ComboBuilder setBebida(Producto bebida);

    ComboBuilder setAcompanamiento(Producto acomp);

    ComboBuilder setDescuento(double descuento);

    /** Verifica que estén todos los componentes obligatorios (RF-14) y construye el combo. */
    Combo build();
}
