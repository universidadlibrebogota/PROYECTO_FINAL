package cafeteria.dominio;

import cafeteria.dominio.observer.ObservadorPedido;
import cafeteria.dominio.strategy.EstrategiaPromocion;
import cafeteria.dominio.strategy.SinPromocion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Pedido: es el "Sujeto" del patrón Observer (notifica a cocina, caja y meseros)
 * y el "Contexto" del patrón Strategy (delega el cálculo del descuento).
 * Trata igual a productos individuales, personalizados y combos (todos son Producto).
 */
public class Pedido {

    private final int idPedido;
    private final List<Producto> items = new ArrayList<>();
    private final List<Integer> cantidades = new ArrayList<>();
    private EstadoPedido estado = EstadoPedido.PENDIENTE;
    private final List<ObservadorPedido> observadores = new ArrayList<>();
    private EstrategiaPromocion estrategiaPromocion = new SinPromocion();

    public Pedido(int idPedido) {
        this.idPedido = idPedido;
    }

    // ---------------------------------------------------------------- items

    /** Agrega un producto (individual, personalizado o combo). Solo mientras el pedido esté PENDIENTE. */
    public void agregarProducto(Producto p, int cant) {
        exigirModificable();
        if (p == null) {
            throw new IllegalArgumentException("Producto nulo");
        }
        if (cant <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        if (!p.isDisponible()) {
            throw new IllegalArgumentException("El producto no está disponible: " + p.getNombre());
        }
        for (int i = 0; i < items.size(); i++) {
            if (mismaLinea(items.get(i), p)) {
                cantidades.set(i, cantidades.get(i) + cant);
                notificar();
                return;
            }
        }
        items.add(p);
        cantidades.add(cant);
        notificar();
    }

    /** Cambia la cantidad de la línea i. Con cant = 0 la línea se elimina. */
    public void modificarCantidad(int i, int cant) {
        exigirModificable();
        if (i < 0 || i >= items.size()) {
            throw new IndexOutOfBoundsException("No existe la línea " + i);
        }
        if (cant < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        if (cant == 0) {
            items.remove(i);
            cantidades.remove(i);
        } else {
            cantidades.set(i, cant);
        }
        notificar();
    }

    private void exigirModificable() {
        if (estado != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("El pedido #" + idPedido + " ya no se puede modificar (" + estado + ")");
        }
    }

    private static boolean mismaLinea(Producto a, Producto b) {
        return a.getId().equals(b.getId())
                && a.getDescripcion().equals(b.getDescripcion())
                && a.getPrecio() == b.getPrecio();
    }

    // --------------------------------------------------------------- estado

    /** Cambia el estado validando el flujo y notifica a los observadores. */
    public void cambiarEstado(EstadoPedido e) {
        if (e == null || !transicionValida(estado, e)) {
            throw new IllegalStateException("Transición no permitida: " + estado + " -> " + e);
        }
        this.estado = e;
        notificar();
    }

    private static boolean transicionValida(EstadoPedido de, EstadoPedido a) {
        switch (de) {
            case PENDIENTE:
                return a == EstadoPedido.EN_PREPARACION || a == EstadoPedido.CANCELADO;
            case EN_PREPARACION:
                return a == EstadoPedido.LISTO_PARA_ENTREGAR || a == EstadoPedido.CANCELADO;
            case LISTO_PARA_ENTREGAR:
                return a == EstadoPedido.ENTREGADO;
            default:
                return false;
        }
    }

    // ------------------------------------------------------------- observer

    public void suscribir(ObservadorPedido o) {
        if (o != null && !observadores.contains(o)) {
            observadores.add(o);
        }
    }

    public void desuscribir(ObservadorPedido o) {
        observadores.remove(o);
    }

    /** Avisa a todos los observadores; el fallo de uno no impide avisar a los demás. */
    public void notificar() {
        for (ObservadorPedido o : new ArrayList<>(observadores)) {
            try {
                o.actualizar(this);
            } catch (RuntimeException ex) {
                System.err.println("Observador con error (" + o.getClass().getSimpleName() + "): " + ex.getMessage());
            }
        }
    }

    // ------------------------------------------------------------- strategy

    public void setEstrategiaPromocion(EstrategiaPromocion est) {
        this.estrategiaPromocion = est == null ? new SinPromocion() : est;
    }

    /** Suma de precio x cantidad de todas las líneas, antes de promociones. */
    public double calcularSubtotal() {
        return calcularSubtotal(p -> true);
    }

    /** Subtotal restringido a las líneas que cumplan el filtro (lo usan las promociones por alcance). */
    public double calcularSubtotal(Predicate<Producto> filtro) {
        double suma = 0;
        for (int i = 0; i < items.size(); i++) {
            if (filtro.test(items.get(i))) {
                suma += items.get(i).getPrecio() * cantidades.get(i);
            }
        }
        return suma;
    }

    /** Total final = subtotal - descuento de la estrategia vigente (nunca negativo). */
    public double calcularTotal() {
        double subtotal = calcularSubtotal();
        double descuento = Math.min(subtotal, Math.max(0, estrategiaPromocion.calcularDescuento(this)));
        return subtotal - descuento;
    }

    // ------------------------------------------------------------ accesores

    public int getIdPedido() {
        return idPedido;
    }

    public List<Producto> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<Integer> getCantidades() {
        return Collections.unmodifiableList(cantidades);
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public EstrategiaPromocion getEstrategiaPromocion() {
        return estrategiaPromocion;
    }

    /** Ej.: "Pedido #3: 2× Latte Rosa, 1× Croissant clásico". */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Pedido #").append(idPedido).append(": ");
        if (items.isEmpty()) {
            return sb.append("(vacío)").toString();
        }
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(cantidades.get(i)).append("× ").append(items.get(i).getNombre());
        }
        return sb.toString();
    }
}
