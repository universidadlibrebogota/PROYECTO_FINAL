package cafeteria.service;

import cafeteria.dominio.AdicionIngrediente;
import cafeteria.dominio.ModificacionProducto;
import cafeteria.dominio.Producto;
import cafeteria.dominio.factory.BebidaFactory;
import cafeteria.dominio.factory.PlatoFactory;
import cafeteria.dominio.factory.PostreFactory;
import cafeteria.dominio.factory.ProductoFactory;
import cafeteria.repository.ConfiguracionProducto;
import cafeteria.repository.ProductoRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Módulo "Productos y Personalización" (Factory Method + Decorator).
 * Los tipos de producto se registran con su fábrica: añadir un tipo nuevo no modifica este código.
 */
public class MenuService {

    private final ProductoRepository repo;
    private final Map<String, ProductoFactory> fabricas = new LinkedHashMap<>();
    private final Map<String, String> ayudaExtra = new LinkedHashMap<>();
    private final Map<String, ConfiguracionProducto> configs = new LinkedHashMap<>();
    private final Map<String, Producto> productos = new LinkedHashMap<>();
    private final Map<String, Map<String, Double>> adiciones = new LinkedHashMap<>();

    public MenuService(ProductoRepository repo) {
        this(repo, true);
    }

    /** @param sembrar si es true y el repositorio está vacío, carga el menú de ejemplo. */
    public MenuService(ProductoRepository repo, boolean sembrar) {
        this.repo = repo;
        registrarTipo("Bebida", new BebidaFactory(), "Tamaño (Pequeño, Mediano o Grande)");
        registrarTipo("Plato", new PlatoFactory(), "Tipo de plato (Principal o Acompañamiento)");
        registrarTipo("Postre", new PostreFactory(), "¿Es dulce? (true o false)");
        for (ConfiguracionProducto c : repo.listar()) {
            configs.put(c.id, c);
        }
        adiciones.putAll(repo.listarAdiciones());
        reconstruir();
        if (sembrar && configs.isEmpty() && adiciones.isEmpty()) {
            sembrarMenuInicial();
        }
    }

    // ------------------------------------------------------------- tipos

    /** Punto de extensión (OCP): registra un nuevo tipo de producto con su fábrica. */
    public void registrarTipo(String tipo, ProductoFactory fabrica, String ayudaDelExtra) {
        if (tipo == null || tipo.trim().isEmpty() || fabrica == null) {
            throw new IllegalArgumentException("Tipo y fábrica son obligatorios");
        }
        fabricas.put(tipo, fabrica);
        ayudaExtra.put(tipo, ayudaDelExtra == null ? "Dato adicional" : ayudaDelExtra);
        reconstruir();
    }

    public List<String> tipos() {
        return new ArrayList<>(fabricas.keySet());
    }

    public String ayudaExtra(String tipo) {
        return ayudaExtra.getOrDefault(tipo, "Dato adicional");
    }

    /** Crea (vía fábrica) los productos persistidos cuyo tipo ya esté registrado. */
    private void reconstruir() {
        for (ConfiguracionProducto c : new ArrayList<>(configs.values())) {
            ProductoFactory f = fabricas.get(c.tipo);
            if (f != null && !productos.containsKey(c.id)) {
                Producto p = f.crearProducto(c.nombre, c.descripcion, c.precio, c.extra);
                p.setDisponible(c.disponible);
                productos.put(p.getId(), p);
            }
        }
    }

    // --------------------------------------------------------- productos

    public Producto registrarProducto(String tipo, String categoria, String nombre, String descripcion,
                                      double precio, String extra) {
        ProductoFactory f = fabricas.get(tipo);
        if (f == null) {
            throw new IllegalArgumentException("Tipo de producto desconocido: " + tipo);
        }
        validar(nombre, precio);
        Producto p = f.crearProducto(nombre.trim(), descripcion, precio, extra);
        if (productos.containsKey(p.getId())) {
            throw new IllegalArgumentException("Ya existe un producto llamado \"" + nombre.trim() + "\"");
        }
        ConfiguracionProducto c = new ConfiguracionProducto(p.getId(), tipo, categoriaOPorDefecto(categoria, tipo),
                nombre.trim(), descripcion == null ? "" : descripcion, precio, extra == null ? "" : extra, true);
        configs.put(p.getId(), c);
        productos.put(p.getId(), p);
        repo.guardar(c);
        return p;
    }

    /** Modifica nombre, descripción, precio, categoría y dato extra (RF-03). Conserva tipo y disponibilidad. */
    public Producto modificarProducto(String id, String categoria, String nombre, String descripcion,
                                      double precio, String extra) {
        ConfiguracionProducto actual = configs.get(id);
        if (actual == null) {
            throw new IllegalArgumentException("No existe el producto " + id);
        }
        validar(nombre, precio);
        Producto nuevo = fabricas.get(actual.tipo).crearProducto(nombre.trim(), descripcion, precio, extra);
        if (!nuevo.getId().equals(id) && productos.containsKey(nuevo.getId())) {
            throw new IllegalArgumentException("Ya existe un producto llamado \"" + nombre.trim() + "\"");
        }
        nuevo.setDisponible(actual.disponible);
        ConfiguracionProducto c = new ConfiguracionProducto(nuevo.getId(), actual.tipo,
                categoriaOPorDefecto(categoria, actual.tipo), nombre.trim(), descripcion == null ? "" : descripcion,
                precio, extra == null ? "" : extra, actual.disponible);
        if (!nuevo.getId().equals(id)) {
            configs.remove(id);
            productos.remove(id);
            repo.eliminar(id);
        }
        configs.put(c.id, c);
        productos.put(c.id, nuevo);
        repo.guardar(c);
        return nuevo;
    }

    public void cambiarDisponibilidad(String id, boolean disponible) {
        Producto p = productos.get(id);
        if (p == null) {
            throw new IllegalArgumentException("No existe el producto " + id);
        }
        p.setDisponible(disponible);
        ConfiguracionProducto c = configs.get(id).conDisponible(disponible);
        configs.put(id, c);
        repo.guardar(c);
    }

    public void renombrarCategoria(String vieja, String nueva) {
        if (nueva == null || nueva.trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría necesita un nombre");
        }
        for (ConfiguracionProducto c : new ArrayList<>(configs.values())) {
            if (c.categoria.equalsIgnoreCase(vieja)) {
                ConfiguracionProducto n = c.conCategoria(nueva.trim());
                configs.put(n.id, n);
                repo.guardar(n);
            }
        }
    }

    private static void validar(String nombre, double precio) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
    }

    private static String categoriaOPorDefecto(String categoria, String tipo) {
        return (categoria == null || categoria.trim().isEmpty()) ? tipo + "s" : categoria.trim();
    }

    // ---------------------------------------------------------- consultas

    public List<Producto> productos() {
        return new ArrayList<>(productos.values());
    }

    public List<Producto> disponibles() {
        List<Producto> r = new ArrayList<>();
        for (Producto p : productos.values()) {
            if (p.isDisponible()) {
                r.add(p);
            }
        }
        return r;
    }

    public Producto buscar(String id) {
        return productos.get(id);
    }

    public ConfiguracionProducto configuracion(String id) {
        return configs.get(id);
    }

    public String categoriaDe(String id) {
        ConfiguracionProducto c = configs.get(id);
        return c == null ? null : c.categoria;
    }

    public String tipoDe(String id) {
        ConfiguracionProducto c = configs.get(id);
        return c == null ? null : c.tipo;
    }

    public List<String> categorias() {
        Set<String> r = new LinkedHashSet<>();
        for (ConfiguracionProducto c : configs.values()) {
            if (productos.containsKey(c.id)) {
                r.add(c.categoria);
            }
        }
        return new ArrayList<>(r);
    }

    public List<Producto> porCategoria(String categoria) {
        List<Producto> r = new ArrayList<>();
        for (Producto p : productos.values()) {
            if (categoria.equalsIgnoreCase(categoriaDe(p.getId()))) {
                r.add(p);
            }
        }
        return r;
    }

    // -------------------------------------------------------- adiciones

    /** Punto de extensión (OCP): nueva adición disponible para los productos de un tipo. */
    public void registrarAdicion(String tipo, String nombre, double costo) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("La adición necesita un nombre");
        }
        if (costo < 0) {
            throw new IllegalArgumentException("El costo no puede ser negativo");
        }
        adiciones.computeIfAbsent(tipo, t -> new LinkedHashMap<>()).put(nombre.trim(), costo);
        repo.guardarAdicion(tipo, nombre.trim(), costo);
    }

    public void eliminarAdicion(String tipo, String nombre) {
        Map<String, Double> m = adiciones.get(tipo);
        if (m != null) {
            m.remove(nombre);
        }
        repo.eliminarAdicion(tipo, nombre);
    }

    public Map<String, Double> adiciones(String tipo) {
        Map<String, Double> m = adiciones.get(tipo);
        return m == null ? Collections.<String, Double>emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(m));
    }

    /**
     * Personaliza un producto aplicando decoradores: una AdicionIngrediente por cada adición
     * elegida y una ModificacionProducto si hay nota (p. ej. "Sin azúcar").
     */
    public Producto personalizar(Producto base, List<String> nombresAdiciones, String nota) {
        Producto resultado = base;
        Map<String, Double> disponibles = adiciones(tipoDe(base.getId()));
        if (nombresAdiciones != null) {
            for (String nombre : nombresAdiciones) {
                Double costo = disponibles.get(nombre);
                if (costo == null) {
                    throw new IllegalArgumentException("La adición \"" + nombre + "\" no está configurada para este producto");
                }
                resultado = new AdicionIngrediente(resultado, nombre, costo);
            }
        }
        if (nota != null && !nota.trim().isEmpty()) {
            resultado = new ModificacionProducto(resultado, nota);
        }
        return resultado;
    }

    // ------------------------------------------------------------- datos

    private void sembrarMenuInicial() {
        registrarProducto("Bebida", "Cafés", "Latte Rosa", "Espresso, leche y vainilla", 9500, "Mediano");
        registrarProducto("Bebida", "Cafés", "Capuccino canela", "Espresso doble y espuma", 8500, "Mediano");
        registrarProducto("Bebida", "Cafés", "Americano", "Espresso largo", 5500, "Mediano");
        registrarProducto("Bebida", "Bebidas", "Fresa helada", "Fresas, limón y soda", 8900, "Mediano");
        registrarProducto("Bebida", "Bebidas", "Jugo de naranja", "Naranja natural", 7000, "Mediano");
        registrarProducto("Bebida", "Bebidas", "Agua mineral", "Con o sin gas", 4500, "Mediano");
        registrarProducto("Plato", "Panadería", "Croissant clásico", "Mantequilla, recién horneado", 6500, "Acompañamiento");
        registrarProducto("Plato", "Panadería", "Pan de bono", "Queso y almidón de yuca", 3500, "Acompañamiento");
        registrarProducto("Plato", "Platos", "Tostada brunch", "Aguacate, huevo y semillas", 16500, "Principal");
        registrarProducto("Plato", "Platos", "Sándwich de pollo", "Pollo, queso y vegetales", 15500, "Principal");
        registrarProducto("Plato", "Platos", "Bowl de frutas", "Frutas de temporada, yogur y granola", 12500, "Principal");
        registrarProducto("Postre", "Postres", "Tarta del día", "Porción de tarta artesanal", 9000, "true");
        registrarProducto("Postre", "Postres", "Brownie", "Chocolate y nueces", 7500, "true");

        registrarAdicion("Bebida", "Leche de almendras", 3000);
        registrarAdicion("Bebida", "Shot de espresso extra", 3500);
        registrarAdicion("Bebida", "Crema batida", 2000);
        registrarAdicion("Bebida", "Sirope de vainilla", 1500);
        registrarAdicion("Bebida", "Leche deslactosada", 1500);
        registrarAdicion("Plato", "Aguacate extra", 3500);
        registrarAdicion("Plato", "Huevo extra", 2500);
        registrarAdicion("Plato", "Queso extra", 2500);
        registrarAdicion("Plato", "Tocineta", 3000);
        registrarAdicion("Postre", "Helado de vainilla", 3500);
        registrarAdicion("Postre", "Salsa de caramelo", 1500);
    }
}
