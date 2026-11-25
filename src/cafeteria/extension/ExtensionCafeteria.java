package cafeteria.extension;

import cafeteria.service.CafeteriaFacade;

/**
 * Punto de extensión (Principio Abierto/Cerrado). Para agregar una variante nueva
 * (adición, regla de descuento, tipo de producto, observador, método de pago...):
 *   1. Crea una clase que implemente esta interfaz y registre lo nuevo en la fachada.
 *   2. Escribe su nombre completo en el archivo src/META-INF/services/cafeteria.extension.ExtensionCafeteria.
 * No se modifica ninguna clase existente. Ver docs/GUIA_CAMBIO_EN_VIVO.md.
 */
public interface ExtensionCafeteria {

    void registrar(CafeteriaFacade fachada);
}
