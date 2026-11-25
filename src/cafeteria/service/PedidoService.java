package cafeteria.service;

import cafeteria.dominio.EstadoPedido;
import cafeteria.dominio.Pago;
import cafeteria.dominio.Pedido;
import cafeteria.dominio.Producto;
import cafeteria.dominio.observer.ObservadorPedido;
import cafeteria.repository.LineaVenta;
import cafeteria.repository.PedidoRepository;
import cafeteria.repository.RegistroPedido;
import cafeteria.repository.RegistroVenta;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Módulo "Pedidos y notificaciones" (Observer): ciclo de vida del pedido, promociones y cobro simulado. */
public class PedidoService {

    private final PedidoRepository repo;
    private final PromocionService promociones;
    private final CajaService caja;
    private final Clock clock;
    private int ultimoId;

    private final Map<Integer, Pedido> pedidos = new LinkedHashMap<>();
    private final Map<Integer, LocalDateTime> fechas = new HashMap<>();
    private final Set<Integer> pagados = new HashSet<>();
    private final List<ObservadorPedido> observadores = new ArrayList<>();
    private final List<String> metodosPago = new ArrayList<>(Arrays.asList("Efectivo", "Tarjeta"));

    public PedidoService(PedidoRepository repo, PromocionService promociones, CajaService caja, Clock clock) {
        this.repo = repo;
        this.promociones = promociones;
        this.caja = caja;
        this.clock = clock;
        this.ultimoId = repo.ultimoId();
    }

    // ----------------------------------------------------- construcción

    /** Crea un pedido en borrador (aún no confirmado: nadie es notificado). */
    public Pedido crear() {
        return new Pedido(++ultimoId);
    }

    public void agregar(Pedido pedido, Producto producto, int cantidad) {
        pedido.agregarProducto(producto, cantidad);
        refrescar(pedido);
    }

    public void cambiarCantidad(Pedido pedido, int linea, int cantidad) {
        pedido.modificarCantidad(linea, cantidad);
        refrescar(pedido);
    }

    private void refrescar(Pedido pedido) {
        if (pedido.getEstado() == EstadoPedido.PENDIENTE) {
            promociones.aplicar(pedido);
        }
        if (pedidos.containsKey(pedido.getIdPedido())) {
            persistir(pedido);
        }
    }

    // ------------------------------------------------------- ciclo de vida

    /** Envía el pedido: se suscriben los observadores, se fija la promoción y se avisa a cocina (RF-16, RF-22). */
    public void confirmar(Pedido pedido) {
        if (pedido.getItems().isEmpty()) {
            throw new IllegalStateException("El pedido está vacío");
        }
        if (pedidos.containsKey(pedido.getIdPedido())) {
            throw new IllegalStateException("El pedido #" + pedido.getIdPedido() + " ya fue enviado");
        }
        pedidos.put(pedido.getIdPedido(), pedido);
        fechas.put(pedido.getIdPedido(), LocalDateTime.now(clock));
        for (ObservadorPedido o : observadores) {
            pedido.suscribir(o);
        }
        promociones.aplicar(pedido);
        persistir(pedido);
        pedido.notificar();
    }

    public void cambiarEstado(Pedido pedido, EstadoPedido nuevo) {
        exigirConfirmado(pedido);
        pedido.cambiarEstado(nuevo);
        persistir(pedido);
    }

    /** Cancela si el estado lo permite (PENDIENTE o EN_PREPARACION) y registra la cancelación (RF-20). */
    public void cancelar(Pedido pedido) {
        cambiarEstado(pedido, EstadoPedido.CANCELADO);
    }

    private void exigirConfirmado(Pedido pedido) {
        if (!pedidos.containsKey(pedido.getIdPedido())) {
            throw new IllegalStateException("El pedido #" + pedido.getIdPedido() + " aún no ha sido enviado");
        }
    }

    // ----------------------------------------------------------------- pago

    public List<String> metodosPago() {
        return Collections.unmodifiableList(metodosPago);
    }

    /** Punto de extensión (OCP): nuevo método de pago simulado (p. ej. "Transferencia"). */
    public void registrarMetodoPago(String metodo) {
        if (metodo != null && !metodo.trim().isEmpty() && !metodosPago.contains(metodo.trim())) {
            metodosPago.add(metodo.trim());
        }
    }

    public boolean estaPagado(Pedido pedido) {
        return pagados.contains(pedido.getIdPedido());
    }

    /** Cobra un pedido listo o entregado con un método simulado (RF-28). */
    public Pago pagar(Pedido pedido, String metodo) {
        exigirConfirmado(pedido);
        if (estaPagado(pedido)) {
            throw new IllegalStateException("El pedido #" + pedido.getIdPedido() + " ya fue pagado");
        }
        if (pedido.getEstado() != EstadoPedido.LISTO_PARA_ENTREGAR && pedido.getEstado() != EstadoPedido.ENTREGADO) {
            throw new IllegalStateException("Solo se cobran pedidos listos o entregados (estado actual: " + pedido.getEstado() + ")");
        }
        if (metodo == null || !metodosPago.contains(metodo)) {
            throw new IllegalArgumentException("Método de pago no configurado: " + metodo);
        }
        LocalDateTime ahora = LocalDateTime.now(clock);
        double total = pedido.calcularTotal();
        Pago pago = new Pago("PAG-" + pedido.getIdPedido(), pedido.getIdPedido(), total, metodo, ahora);
        if (!pago.procesarPago()) {
            throw new IllegalStateException("El pago fue rechazado");
        }
        double subtotal = pedido.calcularSubtotal();
        List<LineaVenta> lineas = new ArrayList<>();
        for (int i = 0; i < pedido.getItems().size(); i++) {
            Producto p = pedido.getItems().get(i);
            lineas.add(new LineaVenta(p.getNombre(), pedido.getCantidades().get(i), p.getPrecio()));
        }
        caja.registrarVenta(new RegistroVenta(pago.getIdPago(), pedido.getIdPedido(), ahora, subtotal, subtotal - total,
                total, metodo, pedido.getEstrategiaPromocion().toString(), lineas));
        pagados.add(pedido.getIdPedido());
        persistir(pedido);
        return pago;
    }

    // ------------------------------------------------------------ consultas

    /** Punto de extensión (OCP): nuevo destinatario de notificaciones para todos los pedidos. */
    public void registrarObservador(ObservadorPedido observador) {
        observadores.add(observador);
        for (Pedido p : pedidos.values()) {
            p.suscribir(observador);
        }
    }

    public Pedido obtener(int id) {
        return pedidos.get(id);
    }

    /** Pedidos enviados en esta sesión, el más reciente primero. */
    public List<Pedido> listar() {
        List<Pedido> lista = new ArrayList<>(pedidos.values());
        Collections.reverse(lista);
        return lista;
    }

    public LocalDateTime fechaDe(Pedido pedido) {
        return fechas.getOrDefault(pedido.getIdPedido(), LocalDateTime.now(clock));
    }

    /** Historial persistido (incluye sesiones anteriores). */
    public List<RegistroPedido> historial() {
        return repo.listar();
    }

    private void persistir(Pedido p) {
        String texto = p.toString();
        String detalle = texto.substring(texto.indexOf(": ") + 2);
        repo.guardar(new RegistroPedido(p.getIdPedido(), p.getEstado().name(), p.calcularTotal(), fechaDe(p), detalle,
                estaPagado(p)));
    }
}
