package cafeteria.ui.gui;

import cafeteria.dominio.Pedido;
import cafeteria.service.CafeteriaFacade;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Estado compartido de la sesión gráfica: fachada, usuario, pedido en construcción y avisos. */
public class Contexto {

    public final CafeteriaFacade f;
    public String usuario = "";
    public String rol = "";
    /** Interruptor de "Notificaciones operativas" (Configuración). */
    public boolean avisosActivos = true;
    public Pedido borrador;
    /** Cada aviso: {área, mensaje, hora}. El más reciente primero. */
    public final List<String[]> avisos = new ArrayList<>();

    Consumer<String> toast = t -> { };
    Consumer<String> irA = v -> { };
    Runnable refrescarTodo = () -> { };
    Runnable refrescarPedido = () -> { };
    Runnable refrescarAvisos = () -> { };

    public Contexto(CafeteriaFacade fachada) {
        this.f = fachada;
        this.borrador = fachada.nuevoPedido();
    }

    public void toast(String texto) {
        toast.accept(texto);
    }

    public void irA(String vista) {
        irA.accept(vista);
    }

    /** Canal de notificaciones de la GUI: lo usan los Observer (cocina, caja, mesero). */
    public void aviso(String area, String mensaje) {
        String limpio = mensaje.replaceFirst("^\\[[A-ZÁÉÍÓÚ]+\\]\\s*", "");
        avisos.add(0, new String[]{area, limpio, cafeteria.util.Formato.hora(f.ahora().toLocalTime())});
        refrescarAvisos.run();
        if (avisosActivos) {
            toast(area + ": " + limpio);
        }
        refrescarTodo.run();
    }

    public void nuevoBorrador() {
        borrador = f.nuevoPedido();
        refrescarPedido.run();
    }

    public String saludo() {
        int h = LocalTime.now().getHour();
        return h < 12 ? "Buenos días" : h < 19 ? "Buenas tardes" : "Buenas noches";
    }

    /** Ejecuta una acción de negocio mostrando los errores de validación como aviso, no como excepción. */
    public boolean intentar(Runnable accion) {
        try {
            accion.run();
            return true;
        } catch (RuntimeException e) {
            toast("⚠ " + e.getMessage());
            return false;
        }
    }
}
