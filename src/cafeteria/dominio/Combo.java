package cafeteria.dominio;

/**
 * Combo: agrupa plato principal + bebida + acompañamiento bajo la misma estructura
 * Producto, por lo que un Pedido lo trata igual que a un producto individual.
 * descuentoCombo es un porcentaje (0–100) aplicado a la suma de los componentes.
 */
public class Combo extends Producto {

    private final Producto platoPrincipal;
    private final Producto bebida;
    private final Producto acompanamiento;
    private final double descuentoCombo;

    public Combo(String id, String nombre, Producto platoPrincipal, Producto bebida,
                 Producto acompanamiento, double descuentoCombo) {
        super(id, nombre, "Combo", 0);
        if (platoPrincipal == null || bebida == null || acompanamiento == null) {
            throw new IllegalArgumentException("Un combo requiere plato principal, bebida y acompañamiento");
        }
        if (descuentoCombo < 0 || descuentoCombo > 100) {
            throw new IllegalArgumentException("El descuento del combo debe estar entre 0 y 100");
        }
        this.platoPrincipal = platoPrincipal;
        this.bebida = bebida;
        this.acompanamiento = acompanamiento;
        this.descuentoCombo = descuentoCombo;
        setDisponible(platoPrincipal.isDisponible() && bebida.isDisponible() && acompanamiento.isDisponible());
    }

    public Producto getPlatoPrincipal() {
        return platoPrincipal;
    }

    public Producto getBebida() {
        return bebida;
    }

    public Producto getAcompanamiento() {
        return acompanamiento;
    }

    public double getDescuentoCombo() {
        return descuentoCombo;
    }

    @Override
    public String getDescripcion() {
        String base = platoPrincipal.getNombre() + " + " + bebida.getNombre() + " + " + acompanamiento.getNombre();
        return descuentoCombo > 0 ? base + " (-" + cafeteria.util.Formato.porcentaje(descuentoCombo) + ")" : base;
    }

    @Override
    public double getPrecio() {
        double suma = platoPrincipal.getPrecio() + bebida.getPrecio() + acompanamiento.getPrecio();
        return Math.round(suma * (1 - descuentoCombo / 100.0));
    }
}
