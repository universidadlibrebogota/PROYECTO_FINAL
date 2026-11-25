package cafeteria;

import cafeteria.app.Arranque;
import cafeteria.service.CafeteriaFacade;
import cafeteria.ui.consola.AplicacionConsola;
import cafeteria.ui.gui.VentanaPrincipal;

import java.time.Clock;
import java.util.Scanner;

/**
 * Punto de entrada. Uso:  java -jar cafeteria.jar [gui|consola] [--memoria]
 * La base SQLite es "cafeteria.db" (o -Dcafeteria.db=ruta). Con --memoria no se persiste nada.
 */
public final class Main {

    private Main() { }

    public static void main(String[] args) throws Exception {
        String modo = "gui";
        boolean memoria = false;
        for (String a : args) {
            if (a.equalsIgnoreCase("--memoria")) {
                memoria = true;
            } else {
                modo = a.toLowerCase();
            }
        }
        String ruta = memoria ? null : System.getProperty("cafeteria.db", "cafeteria.db");
        CafeteriaFacade fachada = Arranque.crear(ruta, Clock.systemDefaultZone());

        if (modo.equals("consola")) {
            // Se usa la codificación nativa del terminal (System.out/System.in) para que coincida con la consola.
            new AplicacionConsola(fachada, new Scanner(System.in), System.out).ejecutar();
        } else {
            VentanaPrincipal.mostrar(fachada);
        }
    }
}
