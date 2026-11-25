package cafeteria.service;

import cafeteria.dominio.Combo;
import cafeteria.dominio.EstadoPedido;
import cafeteria.dominio.Pago;
import cafeteria.dominio.Pedido;
import cafeteria.dominio.Producto;
import cafeteria.dominio.builder.ComboBuilder;
import cafeteria.dominio.factory.ProductoFactory;
import cafeteria.dominio.observer.ObservadorPedido;
import cafeteria.dominio.strategy.EstrategiaPromocion;
import cafeteria.extension.ExtensionCafeteria;
import cafeteria.repository.ConfiguracionProducto;
import cafeteria.repository.PedidoRepository;
import cafeteria.repository.ProductoRepository;
import cafeteria.repository.RegistroPedido;
import cafeteria.repository.RegistroVenta;
import cafeteria.repository.VentaRepository;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Fachada del núcleo: punto de acceso único para la consola y la GUI.
 * Ambas interfaces usan exactamente esta misma lógica; ninguna conoce los subsistemas internos.
 */
public class CafeteriaFacade {

    private final MenuService menu;
    private final ComboService combos;
    private final PromocionService promociones;
    private final CajaService caja;
    private final PedidoService pedidos;
    private final Clock clock;
    private final String descripcionPersistencia;

    public CafeteriaFacade(ProductoRepository productos, PedidoRepository pedidosRepo, VentaRepository ventas,
                           Clock clock, String descripcionPersistencia, boolean datosIniciales) {
        this.clock = clock;
        this.descripcionPersistencia = descripcionPersistencia;
        this.menu = new MenuService(productos, datosIniciales);
        this.combos = new ComboService(menu);
        this.promociones = new PromocionService(clock);
        if (datosIniciales) {
            promociones.cargarPromocionesIniciales();
        }
        this.caja = new CajaService(ventas, clock);
        this.pedidos = new PedidoService(pedidosRepo, promociones, caja, clock);
    }

    public String descripcionPersistencia() {
        return descripcionPersistencia;
    }

    public LocalDateTime ahora() {
        return LocalDateTime.now(clock);
    }

    // ================================================================ menú

    public List<Producto> productos() {
        return menu.productos();
    }

    public List<Producto> productosDisponibles() {
        return menu.disponibles();
    }

    public Producto buscarProducto(String id) {
        return menu.buscar(id);
    }

    public ConfiguracionProducto configuracionProducto(String id) {
        return menu.configuracion(id);
    }

    public List<String> categorias() {
        return menu.categorias();
    }

    public String categoriaDe(String idProducto) {
        return menu.categoriaDe(idProducto);
    }

    public String tipoDe(String idProducto) {
        return menu.tipoDe(idProducto);
    }

    public List<Producto> productosPorCategoria(String categoria) {
        return menu.porCategoria(categoria);
    }

    public List<String> tiposProducto() {
        return menu.tipos();
    }

    public String ayudaExtra(String tipo) {
        return menu.ayudaExtra(tipo);
    }

    public Producto registrarProducto(String tipo, String categoria, String nombre, String descripcion,
                                      double precio, String extra) {
        return menu.registrarProducto(tipo, categoria, nombre, descripcion, precio, extra);
    }

    public Producto modificarProducto(String id, String categoria, String nombre, String descripcion,
                                      double precio, String extra) {
        return menu.modificarProducto(id, categoria, nombre, descripcion, precio, extra);
    }

    public void cambiarDisponibilidad(String idProducto, boolean disponible) {
        menu.cambiarDisponibilidad(idProducto, disponible);
    }

    public void renombrarCategoria(String vieja, String nueva) {
        menu.renombrarCategoria(vieja, nueva);
    }

    // ===================================================== personalización

    public Map<String, Double> adicionesDisponibles(String idProducto) {
        return menu.adiciones(menu.tipoDe(idProducto));
    }

    public Map<String, Double> adiciones(String tipo) {
        return menu.adiciones(tipo);
    }

    public void registrarAdicion(String tipo, String nombre, double costo) {
        menu.registrarAdicion(tipo, nombre, costo);
    }

    public void eliminarAdicion(String tipo, String nombre) {
        menu.eliminarAdicion(tipo, nombre);
    }

    public Producto personalizar(Producto base, List<String> adiciones, String nota) {
        return menu.personalizar(base, adiciones, nota);
    }

    // ================================================================ combos

    public List<Producto> opcionesCombo(PasoCombo paso) {
        return combos.opciones(paso);
    }

    public ComboBuilder nuevoConstructorCombo() {
        return combos.nuevoConstructor();
    }

    public Combo armarCombo(Producto plato, Producto bebida, Producto acompanamiento) {
        return combos.armar(plato, bebida, acompanamiento);
    }

    public double descuentoCombo() {
        return combos.getDescuentoPorDefecto();
    }

    public void setDescuentoCombo(double porcentaje) {
        combos.setDescuentoPorDefecto(porcentaje);
    }

    // =========================================================== promociones

    public List<EstrategiaPromocion> promociones() {
        return promociones.listar();
    }

    public boolean promocionActiva(EstrategiaPromocion e) {
        return promociones.estaActiva(e);
    }

    public void activarPromocion(EstrategiaPromocion e, boolean activa) {
        promociones.activar(e, activa);
    }

    public void eliminarPromocion(EstrategiaPromocion e) {
        promociones.eliminarPromocion(e);
    }

    public List<String> alcancesPromocion() {
        return promociones.alcances();
    }

    public EstrategiaPromocion crearPromocionPorDia(String nombre, Set<DayOfWeek> dias, double porcentaje,
                                                    String alcance, LocalDate desde, LocalDate hasta) {
        return promociones.crearPorDia(nombre, dias, porcentaje, alcance, desde, hasta);
    }

    public EstrategiaPromocion crearPromocionPorHorario(String nombre, Set<DayOfWeek> dias, LocalTime horaDesde,
                                                        LocalTime horaHasta, double porcentaje, String alcance,
                                                        LocalDate desde, LocalDate hasta) {
        return promociones.crearPorHorario(nombre, dias, horaDesde, horaHasta, porcentaje, alcance, desde, hasta);
    }

    // ================================================================ pedidos

    public Pedido nuevoPedido() {
        return pedidos.crear();
    }

    public void agregarAlPedido(Pedido pedido, Producto producto, int cantidad) {
        pedidos.agregar(pedido, producto, cantidad);
    }

    public void cambiarCantidad(Pedido pedido, int linea, int cantidad) {
        pedidos.cambiarCantidad(pedido, linea, cantidad);
    }

    public void confirmarPedido(Pedido pedido) {
        pedidos.confirmar(pedido);
    }

    public void cambiarEstado(Pedido pedido, EstadoPedido estado) {
        pedidos.cambiarEstado(pedido, estado);
    }

    public void cancelarPedido(Pedido pedido) {
        pedidos.cancelar(pedido);
    }

    public Pago pagarPedido(Pedido pedido, String metodo) {
        return pedidos.pagar(pedido, metodo);
    }

    public boolean estaPagado(Pedido pedido) {
        return pedidos.estaPagado(pedido);
    }

    public List<Pedido> pedidosEnviados() {
        return pedidos.listar();
    }

    public Pedido buscarPedido(int id) {
        return pedidos.obtener(id);
    }

    public LocalDateTime fechaDelPedido(Pedido pedido) {
        return pedidos.fechaDe(pedido);
    }

    public List<RegistroPedido> historialPedidos() {
        return pedidos.historial();
    }

    public List<String> metodosPago() {
        return pedidos.metodosPago();
    }

    // ============================================================ caja y reportes

    public boolean cajaAbierta() {
        return caja.estaAbierta();
    }

    public void abrirCaja() {
        caja.abrirCaja();
    }

    public ReporteVentas cerrarCaja() {
        return caja.cerrarCaja();
    }

    public ReporteVentas reporteTurnoActual() {
        return caja.reporteTurnoActual();
    }

    public ReporteVentas reporte(LocalDateTime desde, LocalDateTime hasta) {
        return caja.reporte(desde, hasta);
    }

    public List<RegistroVenta> movimientosRecientes(int n) {
        return caja.movimientosRecientes(n);
    }

    // ================================================== puntos de extensión (OCP)

    public void registrarTipoProducto(String tipo, ProductoFactory fabrica, String ayudaDelExtra) {
        menu.registrarTipo(tipo, fabrica, ayudaDelExtra);
    }

    public void registrarObservador(ObservadorPedido observador) {
        pedidos.registrarObservador(observador);
    }

    public void registrarPromocion(EstrategiaPromocion estrategia) {
        promociones.registrarPromocion(estrategia);
    }

    public void registrarAlcancePromocion(String nombre, Predicate<Producto> filtro) {
        promociones.registrarAlcance(nombre, filtro);
    }

    public void registrarMetodoPago(String metodo) {
        pedidos.registrarMetodoPago(metodo);
    }

    public void permitirEnPasoCombo(PasoCombo paso, Predicate<ConfiguracionProducto> condicion) {
        combos.permitirEnPaso(paso, condicion);
    }

    /** Carga todas las ExtensionCafeteria declaradas en META-INF/services. Devuelve sus nombres. */
    public List<String> cargarExtensiones() {
        List<String> cargadas = new ArrayList<>();
        for (ExtensionCafeteria e : ServiceLoader.load(ExtensionCafeteria.class)) {
            e.registrar(this);
            cargadas.add(e.getClass().getSimpleName());
        }
        return cargadas;
    }
}
