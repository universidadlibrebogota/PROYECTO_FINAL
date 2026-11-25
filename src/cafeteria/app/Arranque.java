package cafeteria.app;

import cafeteria.repository.ConexionSqlite;
import cafeteria.repository.PedidoRepositoryMemoria;
import cafeteria.repository.PedidoRepositorySqlite;
import cafeteria.repository.ProductoRepositoryMemoria;
import cafeteria.repository.ProductoRepositorySqlite;
import cafeteria.repository.VentaRepositoryMemoria;
import cafeteria.repository.VentaRepositorySqlite;
import cafeteria.service.CafeteriaFacade;

import java.time.Clock;

/** Raíz de composición: decide qué repositorios usar y ensambla la fachada. */
public final class Arranque {

    private Arranque() { }

    /**
     * @param rutaDb archivo SQLite; null o "memoria" = sin persistencia.
     *               Si el driver sqlite-jdbc no está en lib/, se usa memoria y se avisa.
     */
    public static CafeteriaFacade crear(String rutaDb, Clock reloj) {
        if (rutaDb != null && !rutaDb.equalsIgnoreCase("memoria")) {
            if (ConexionSqlite.controladorDisponible()) {
                ConexionSqlite cx = new ConexionSqlite(rutaDb);
                Runtime.getRuntime().addShutdownHook(new Thread(cx::close));
                return configurar(new CafeteriaFacade(new ProductoRepositorySqlite(cx), new PedidoRepositorySqlite(cx),
                        new VentaRepositorySqlite(cx), reloj, "SQLite · " + rutaDb, true));
            }
            System.err.println("AVISO: no se encontró el driver sqlite-jdbc en lib/. Se usa memoria (los datos no se guardan).");
        }
        return enMemoria(reloj);
    }

    public static CafeteriaFacade enMemoria(Clock reloj) {
        return configurar(new CafeteriaFacade(new ProductoRepositoryMemoria(), new PedidoRepositoryMemoria(),
                new VentaRepositoryMemoria(), reloj, "Memoria (sin persistencia)", true));
    }

    private static CafeteriaFacade configurar(CafeteriaFacade fachada) {
        fachada.cargarExtensiones();
        return fachada;
    }
}
