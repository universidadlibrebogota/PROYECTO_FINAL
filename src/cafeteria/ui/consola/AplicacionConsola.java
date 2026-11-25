package cafeteria.ui.consola;

import cafeteria.dominio.EstadoPedido;
import cafeteria.dominio.Pedido;
import cafeteria.dominio.Producto;
import cafeteria.dominio.builder.ComboBuilder;
import cafeteria.dominio.observer.NotificadorCaja;
import cafeteria.dominio.observer.NotificadorCocina;
import cafeteria.dominio.observer.NotificadorMesero;
import cafeteria.dominio.strategy.EstrategiaPromocion;
import cafeteria.repository.RegistroPedido;
import cafeteria.service.CafeteriaFacade;
import cafeteria.service.PasoCombo;
import cafeteria.service.ReporteVentas;
import cafeteria.util.Formato;

import java.io.PrintStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

/**
 * Interfaz de consola. Solo presenta y lee datos: toda la lógica vive en CafeteriaFacade,
 * la misma que usa la interfaz gráfica.
 */
public class AplicacionConsola {

    private final CafeteriaFacade f;
    private final Scanner in;
    private final PrintStream out;

    public AplicacionConsola(CafeteriaFacade fachada, Scanner entrada, PrintStream salida) {
        this.f = fachada;
        this.in = entrada;
        this.out = salida;
    }

    public void ejecutar() {
        // Canal de notificaciones = consola. Para otro canal basta con otro Consumer<String>.
        f.registrarObservador(new NotificadorCocina(m -> out.println("   🔔 " + m)));
        f.registrarObservador(new NotificadorCaja(m -> out.println("   🔔 " + m)));
        f.registrarObservador(new NotificadorMesero(m -> out.println("   🔔 " + m)));
        out.println("\n☕  ROSETTA · Sistema de pedidos   [" + f.descripcionPersistencia() + "]");
        try {
            boolean seguir = true;
            while (seguir) {
                out.println("\n──────── MENÚ PRINCIPAL ────────");
                out.println(" 1) Ver menú");
                out.println(" 2) Nuevo pedido");
                out.println(" 3) Pedidos (avanzar estado, cancelar, cobrar)");
                out.println(" 4) Promociones");
                out.println(" 5) Caja");
                out.println(" 6) Reportes de ventas");
                out.println(" 7) Administrar menú");
                out.println(" 0) Salir");
                switch (leerEntero("Opción", 0, 7)) {
                    case 1: verMenu(); break;
                    case 2: nuevoPedido(); break;
                    case 3: pedidos(); break;
                    case 4: promociones(); break;
                    case 5: caja(); break;
                    case 6: reportes(); break;
                    case 7: administrar(); break;
                    default: seguir = false;
                }
            }
        } catch (NoSuchElementException finDeEntrada) {
            out.println();
        }
        out.println("¡Hasta pronto!");
    }

    // ------------------------------------------------------------------ menú

    private void verMenu() {
        for (String cat : f.categorias()) {
            out.println("\n● " + cat);
            for (Producto p : f.productosPorCategoria(cat)) {
                out.printf("   %-24s %9s  %s%s%n", p.getNombre(), Formato.moneda(p.getPrecio()), p.getDescripcion(),
                        p.isDisponible() ? "" : "  [AGOTADO]");
            }
        }
        out.println("\n● Combos: arma el tuyo desde \"Nuevo pedido\" (descuento " + Formato.porcentaje(f.descuentoCombo()) + ")");
    }

    // --------------------------------------------------------------- pedidos

    private void nuevoPedido() {
        Pedido pedido = f.nuevoPedido();
        out.println("\n=== Nuevo pedido #" + pedido.getIdPedido() + " ===");
        boolean abierto = true;
        while (abierto) {
            out.println("\n a) Agregar producto   b) Armar combo   c) Cambiar cantidad   d) Ver pedido   e) ENVIAR   x) Descartar");
            String op = leer("Opción").toLowerCase();
            try {
                switch (op) {
                    case "a": agregarProducto(pedido); break;
                    case "b": armarCombo(pedido); break;
                    case "c": cambiarCantidad(pedido); break;
                    case "d": mostrarPedido(pedido); break;
                    case "e":
                        f.confirmarPedido(pedido);
                        out.println("✔ Pedido #" + pedido.getIdPedido() + " enviado. Total: " + Formato.moneda(pedido.calcularTotal()));
                        abierto = false;
                        break;
                    case "x":
                        out.println("Pedido descartado.");
                        abierto = false;
                        break;
                    default: out.println("Opción no válida.");
                }
            } catch (RuntimeException e) {
                out.println("⚠ " + e.getMessage());
            }
        }
    }

    private void agregarProducto(Pedido pedido) {
        List<Producto> disponibles = f.productosDisponibles();
        for (int i = 0; i < disponibles.size(); i++) {
            Producto p = disponibles.get(i);
            out.printf(" %2d) %-24s %9s%n", i + 1, p.getNombre(), Formato.moneda(p.getPrecio()));
        }
        int n = leerEntero("Producto (0 = volver)", 0, disponibles.size());
        if (n == 0) {
            return;
        }
        Producto elegido = disponibles.get(n - 1);
        int cantidad = leerEntero("Cantidad", 1, 99);
        List<String> adiciones = new ArrayList<>();
        String nota = "";
        Map<String, Double> opciones = f.adicionesDisponibles(elegido.getId());
        if (!opciones.isEmpty() && leer("¿Personalizar? (s/n)").equalsIgnoreCase("s")) {
            List<String> nombres = new ArrayList<>(opciones.keySet());
            for (int i = 0; i < nombres.size(); i++) {
                out.printf("   %d) %-26s +%s%n", i + 1, nombres.get(i), Formato.moneda(opciones.get(nombres.get(i))));
            }
            String sel = leer("Adiciones (números separados por coma, vacío = ninguna)");
            for (String t : sel.split(",")) {
                if (!t.trim().isEmpty()) {
                    adiciones.add(nombres.get(Integer.parseInt(t.trim()) - 1));
                }
            }
            nota = leer("Modificación/retiro (ej. Sin azúcar; vacío = ninguna)");
        }
        Producto listo = f.personalizar(elegido, adiciones, nota);
        f.agregarAlPedido(pedido, listo, cantidad);
        out.println("✔ " + cantidad + "× " + listo.getNombre() + " · " + listo.getDescripcion() + " · " + Formato.moneda(listo.getPrecio()) + " c/u");
    }

    private void armarCombo(Pedido pedido) {
        ComboBuilder builder = f.nuevoConstructorCombo();
        for (PasoCombo paso : PasoCombo.values()) {
            out.println("\nPaso " + paso.numero() + " de 3 · " + paso.titulo());
            List<Producto> opciones = f.opcionesCombo(paso);
            for (int i = 0; i < opciones.size(); i++) {
                out.printf(" %d) %-24s %9s%n", i + 1, opciones.get(i).getNombre(), Formato.moneda(opciones.get(i).getPrecio()));
            }
            int n = leerEntero("Elige (0 = cancelar combo)", 0, opciones.size());
            if (n == 0) {
                return;
            }
            Producto p = opciones.get(n - 1);
            if (paso == PasoCombo.PLATO_PRINCIPAL) {
                builder.setPlatoPrincipal(p);
            } else if (paso == PasoCombo.BEBIDA) {
                builder.setBebida(p);
            } else {
                builder.setAcompanamiento(p);
            }
        }
        Producto combo = builder.build();
        f.agregarAlPedido(pedido, combo, leerEntero("Cantidad de combos", 1, 99));
        out.println("✔ " + combo.getNombre() + ": " + combo.getDescripcion() + " · " + Formato.moneda(combo.getPrecio()));
    }

    private void cambiarCantidad(Pedido pedido) {
        mostrarPedido(pedido);
        if (pedido.getItems().isEmpty()) {
            return;
        }
        int linea = leerEntero("Línea", 1, pedido.getItems().size());
        f.cambiarCantidad(pedido, linea - 1, leerEntero("Nueva cantidad (0 = quitar)", 0, 99));
    }

    private void mostrarPedido(Pedido p) {
        out.println("\n── Pedido #" + p.getIdPedido() + " [" + p.getEstado() + "]" + (f.estaPagado(p) ? " · PAGADO" : "") + " ──");
        for (int i = 0; i < p.getItems().size(); i++) {
            Producto it = p.getItems().get(i);
            int c = p.getCantidades().get(i);
            out.printf(" %d) %d× %s  %s%n      %s%n", i + 1, c, it.getNombre(), Formato.moneda(it.getPrecio() * c), it.getDescripcion());
        }
        out.println(" Subtotal : " + Formato.moneda(p.calcularSubtotal()));
        out.println(" Promoción: " + p.getEstrategiaPromocion());
        out.println(" TOTAL    : " + Formato.moneda(p.calcularTotal()));
    }

    private void pedidos() {
        List<Pedido> lista = f.pedidosEnviados();
        if (lista.isEmpty()) {
            out.println("No hay pedidos enviados en esta sesión.");
        }
        for (Pedido p : lista) {
            out.printf(" #%d  %-20s %-10s %s%s%n", p.getIdPedido(), p.getEstado(), Formato.moneda(p.calcularTotal()), p,
                    f.estaPagado(p) ? "  ✔pagado" : "");
        }
        List<RegistroPedido> hist = f.historialPedidos();
        if (hist.size() > lista.size()) {
            out.println(" (historial de sesiones anteriores: " + (hist.size() - lista.size()) + " pedidos)");
        }
        if (lista.isEmpty()) {
            return;
        }
        int id = leerEntero("Número de pedido para gestionar (0 = volver)", 0, Integer.MAX_VALUE);
        Pedido p = id == 0 ? null : f.buscarPedido(id);
        if (p == null) {
            return;
        }
        mostrarPedido(p);
        out.println(" 1) Avanzar estado   2) Cancelar   3) Cobrar   0) Volver");
        try {
            switch (leerEntero("Opción", 0, 3)) {
                case 1: f.cambiarEstado(p, siguiente(p.getEstado())); break;
                case 2: f.cancelarPedido(p); break;
                case 3: cobrar(p); break;
                default: break;
            }
        } catch (RuntimeException e) {
            out.println("⚠ " + e.getMessage());
        }
    }

    private static EstadoPedido siguiente(EstadoPedido e) {
        switch (e) {
            case PENDIENTE: return EstadoPedido.EN_PREPARACION;
            case EN_PREPARACION: return EstadoPedido.LISTO_PARA_ENTREGAR;
            case LISTO_PARA_ENTREGAR: return EstadoPedido.ENTREGADO;
            default: throw new IllegalStateException("El pedido ya está en un estado final (" + e + ")");
        }
    }

    private void cobrar(Pedido p) {
        List<String> metodos = f.metodosPago();
        for (int i = 0; i < metodos.size(); i++) {
            out.println(" " + (i + 1) + ") " + metodos.get(i));
        }
        String metodo = metodos.get(leerEntero("Método de pago", 1, metodos.size()) - 1);
        out.println("✔ Pago aprobado (simulado): " + Formato.moneda(f.pagarPedido(p, metodo).getMontoTotal()) + " · " + metodo);
    }

    // ----------------------------------------------------------- promociones

    private void promociones() {
        out.println("\n── Promociones ──");
        List<EstrategiaPromocion> lista = f.promociones();
        for (int i = 0; i < lista.size(); i++) {
            out.println(" " + (i + 1) + ") [" + (f.promocionActiva(lista.get(i)) ? "ACTIVA" : "inactiva") + "] " + lista.get(i));
        }
        out.println(" a) Crear por día   b) Crear por horario   t) Activar/desactivar   e) Eliminar   x) Volver");
        try {
            switch (leer("Opción").toLowerCase()) {
                case "a": crearPromocion(false); break;
                case "b": crearPromocion(true); break;
                case "t": {
                    EstrategiaPromocion e = elegirPromocion(lista);
                    if (e != null) {
                        f.activarPromocion(e, !f.promocionActiva(e));
                    }
                    break;
                }
                case "e": {
                    EstrategiaPromocion e = elegirPromocion(lista);
                    if (e != null) {
                        f.eliminarPromocion(e);
                    }
                    break;
                }
                default: break;
            }
        } catch (RuntimeException e) {
            out.println("⚠ " + e.getMessage());
        }
    }

    private EstrategiaPromocion elegirPromocion(List<EstrategiaPromocion> lista) {
        if (lista.isEmpty()) {
            return null;
        }
        int n = leerEntero("Número (0 = volver)", 0, lista.size());
        return n == 0 ? null : lista.get(n - 1);
    }

    private void crearPromocion(boolean porHorario) {
        String nombre = leer("Nombre");
        double pct = leerDecimal("Porcentaje de descuento (1-100)");
        out.println("Días: 1=Lun 2=Mar 3=Mié 4=Jue 5=Vie 6=Sáb 7=Dom");
        Set<DayOfWeek> dias = EnumSet.noneOf(DayOfWeek.class);
        for (String t : leer("Días separados por coma" + (porHorario ? " (vacío = todos)" : "")).split(",")) {
            if (!t.trim().isEmpty()) {
                dias.add(DayOfWeek.of(Integer.parseInt(t.trim())));
            }
        }
        List<String> alcances = f.alcancesPromocion();
        for (int i = 0; i < alcances.size(); i++) {
            out.println(" " + (i + 1) + ") " + alcances.get(i));
        }
        String alcance = alcances.get(leerEntero("Alcance", 1, alcances.size()) - 1);
        LocalDate desde = leerFechaOpcional("Vigente desde (AAAA-MM-DD, vacío = sin límite)");
        LocalDate hasta = leerFechaOpcional("Vigente hasta (AAAA-MM-DD, vacío = sin límite)");
        if (porHorario) {
            LocalTime h1 = LocalTime.parse(leer("Hora inicio (HH:mm)"));
            LocalTime h2 = LocalTime.parse(leer("Hora fin (HH:mm)"));
            f.crearPromocionPorHorario(nombre, dias, h1, h2, pct, alcance, desde, hasta);
        } else {
            f.crearPromocionPorDia(nombre, dias, pct, alcance, desde, hasta);
        }
        out.println("✔ Promoción creada.");
    }

    // ------------------------------------------------------------------ caja

    private void caja() {
        out.println("\n── Caja: " + (f.cajaAbierta() ? "ABIERTA" : "CERRADA") + " ──");
        out.println(f.reporteTurnoActual());
        out.println(" 1) Realizar cierre de caja   2) Abrir caja   0) Volver");
        try {
            int op = leerEntero("Opción", 0, 2);
            if (op == 1) {
                out.println("\n══ CIERRE DE CAJA ══\n" + f.cerrarCaja());
            } else if (op == 2) {
                f.abrirCaja();
                out.println("✔ Caja abierta.");
            }
        } catch (RuntimeException e) {
            out.println("⚠ " + e.getMessage());
        }
    }

    private void reportes() {
        out.println(" 1) Turno actual   2) Hoy   3) Últimos 7 días   0) Volver");
        LocalDateTime ahora = f.ahora();
        switch (leerEntero("Opción", 0, 3)) {
            case 1: out.println(f.reporteTurnoActual()); break;
            case 2: out.println(f.reporte(ahora.toLocalDate().atStartOfDay(), ahora)); break;
            case 3: out.println(f.reporte(ahora.minusDays(7), ahora)); break;
            default: break;
        }
    }

    // --------------------------------------------------------- administración

    private void administrar() {
        out.println("\n 1) Registrar producto   2) Modificar producto   3) Disponibilidad   4) Nueva adición   5) Descuento de combos   6) Renombrar categoría   0) Volver");
        try {
            switch (leerEntero("Opción", 0, 6)) {
                case 1: registrarProducto(); break;
                case 2: modificarProducto(); break;
                case 3: {
                    Producto p = elegirProducto();
                    if (p != null) {
                        f.cambiarDisponibilidad(p.getId(), !p.isDisponible());
                        out.println(p.getNombre() + (p.isDisponible() ? " ahora está AGOTADO" : " ahora está DISPONIBLE"));
                    }
                    break;
                }
                case 4: {
                    List<String> tipos = f.tiposProducto();
                    for (int i = 0; i < tipos.size(); i++) {
                        out.println(" " + (i + 1) + ") " + tipos.get(i));
                    }
                    String tipo = tipos.get(leerEntero("Tipo de producto", 1, tipos.size()) - 1);
                    f.registrarAdicion(tipo, leer("Nombre de la adición"), leerDecimal("Costo adicional"));
                    out.println("✔ Adición registrada.");
                    break;
                }
                case 5:
                    f.setDescuentoCombo(leerDecimal("Descuento de combos (%)"));
                    out.println("✔ Descuento actualizado.");
                    break;
                case 6: {
                    List<String> cats = f.categorias();
                    for (int i = 0; i < cats.size(); i++) {
                        out.println(" " + (i + 1) + ") " + cats.get(i));
                    }
                    String vieja = cats.get(leerEntero("Categoría a renombrar", 1, cats.size()) - 1);
                    f.renombrarCategoria(vieja, leer("Nuevo nombre"));
                    out.println("✔ Categoría renombrada.");
                    break;
                }
                default: break;
            }
        } catch (RuntimeException e) {
            out.println("⚠ " + e.getMessage());
        }
    }

    private void registrarProducto() {
        List<String> tipos = f.tiposProducto();
        for (int i = 0; i < tipos.size(); i++) {
            out.println(" " + (i + 1) + ") " + tipos.get(i));
        }
        String tipo = tipos.get(leerEntero("Tipo", 1, tipos.size()) - 1);
        String categoria = leer("Categoría (ej. Cafés)");
        String nombre = leer("Nombre");
        String desc = leer("Descripción");
        double precio = leerDecimal("Precio");
        String extra = leer(f.ayudaExtra(tipo));
        f.registrarProducto(tipo, categoria, nombre, desc, precio, extra);
        out.println("✔ Producto registrado.");
    }

    private void modificarProducto() {
        Producto p = elegirProducto();
        if (p == null) {
            return;
        }
        cafeteria.repository.ConfiguracionProducto c = f.configuracionProducto(p.getId());
        String categoria = leerOpcional("Categoría", c.categoria);
        String nombre = leerOpcional("Nombre", c.nombre);
        String desc = leerOpcional("Descripción", c.descripcion);
        double precio = Double.parseDouble(leerOpcional("Precio", String.valueOf((long) c.precio)));
        String extra = leerOpcional(f.ayudaExtra(c.tipo), c.extra);
        f.modificarProducto(p.getId(), categoria, nombre, desc, precio, extra);
        out.println("✔ Producto actualizado.");
    }

    private Producto elegirProducto() {
        List<Producto> todos = f.productos();
        for (int i = 0; i < todos.size(); i++) {
            out.printf(" %2d) %-24s %9s%s%n", i + 1, todos.get(i).getNombre(), Formato.moneda(todos.get(i).getPrecio()),
                    todos.get(i).isDisponible() ? "" : "  [AGOTADO]");
        }
        int n = leerEntero("Producto (0 = volver)", 0, todos.size());
        return n == 0 ? null : todos.get(n - 1);
    }

    // ---------------------------------------------------------------- entrada

    private String leer(String prompt) {
        out.print(prompt + ": ");
        out.flush();
        return in.nextLine().trim();
    }

    private String leerOpcional(String prompt, String actual) {
        String v = leer(prompt + " [" + actual + "]");
        return v.isEmpty() ? actual : v;
    }

    private int leerEntero(String prompt, int min, int max) {
        while (true) {
            try {
                int v = Integer.parseInt(leer(prompt));
                if (v >= min && v <= max) {
                    return v;
                }
            } catch (NumberFormatException ignorada) {
                // se vuelve a pedir
            }
            out.println("   Ingresa un número entre " + min + " y " + (max == Integer.MAX_VALUE ? "…" : String.valueOf(max)) + ".");
        }
    }

    private double leerDecimal(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(leer(prompt).replace(',', '.'));
            } catch (NumberFormatException ignorada) {
                out.println("   Ingresa un número válido.");
            }
        }
    }

    private LocalDate leerFechaOpcional(String prompt) {
        String v = leer(prompt);
        return v.isEmpty() ? null : LocalDate.parse(v);
    }
}
