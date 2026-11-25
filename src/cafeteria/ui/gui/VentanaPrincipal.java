package cafeteria.ui.gui;

import cafeteria.dominio.EstadoPedido;
import cafeteria.dominio.Pedido;
import cafeteria.dominio.observer.NotificadorCaja;
import cafeteria.dominio.observer.NotificadorCocina;
import cafeteria.dominio.observer.NotificadorMesero;
import cafeteria.service.CafeteriaFacade;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Ventana principal de la GUI Swing (réplica de la interfaz Rosetta) sobre CafeteriaFacade. */
public class VentanaPrincipal extends JFrame {

    private final Contexto ctx;
    private final CardLayout raizCards = new CardLayout();
    private final JPanel raiz = new JPanel(raizCards);
    private final Toast toast = new Toast();
    private boolean observadoresRegistrados;

    private final Map<String, VistaBase> vistas = new LinkedHashMap<>();
    private final CardLayout vistasCards = new CardLayout();
    private final JPanel zonaVistas = new JPanel(vistasCards);
    private PanelPedido panelPedido;
    private JPanel sidebarNav;
    private JPanel cabecera;
    private String actual = "menu";
    private final Map<String, Tema.Boton> botonesNav = new HashMap<>();

    private static final String[] IDS = {"menu", "orders", "cash", "promos", "reports", "settings"};

    public static void mostrar(CafeteriaFacade fachada) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignorada) {
                // se usa el aspecto por defecto
            }
            new VentanaPrincipal(fachada).setVisible(true);
        });
    }

    public VentanaPrincipal(CafeteriaFacade fachada) {
        super("Rosetta · Sistema de pedidos");
        this.ctx = new Contexto(fachada);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1380, 830);
        setMinimumSize(new Dimension(1120, 680));
        setLocationRelativeTo(null);
        setGlassPane(toast);
        toast.setVisible(true);

        ctx.toast = toast::mostrar;
        ctx.irA = this::ir;
        ctx.refrescarTodo = this::refrescarTodo;
        ctx.refrescarPedido = () -> {
            if (panelPedido != null) {
                panelPedido.refrescar();
            }
        };
        ctx.refrescarAvisos = () -> {
            if (cabecera != null) {
                construirCabecera();
            }
        };

        raiz.add(new LoginPanel(this::entrar), "login");
        setContentPane(raiz);
        raizCards.show(raiz, "login");
    }

    // ------------------------------------------------------------- sesión

    private void entrar(String usuario, String rol) {
        ctx.usuario = usuario;
        ctx.rol = rol;
        registrarCanalesDeNotificacion();
        raiz.add(construirShell(), "shell");
        raizCards.show(raiz, "shell");
        ir(permitidos().contains("menu") ? "menu" : "orders");
    }

    /** El canal de las notificaciones es la GUI: los Observer escriben aquí, el dominio no lo sabe. */
    private void registrarCanalesDeNotificacion() {
        if (observadoresRegistrados) {
            return;
        }
        observadoresRegistrados = true;
        ctx.f.registrarObservador(new NotificadorCocina(m -> ctx.aviso("Cocina", m)));
        ctx.f.registrarObservador(new NotificadorCaja(m -> ctx.aviso("Caja", m)));
        ctx.f.registrarObservador(new NotificadorMesero(m -> ctx.aviso("Mesero", m)));
    }

    private void salir() {
        raizCards.show(raiz, "login");
    }

    private List<String> permitidos() {
        switch (ctx.rol) {
            case "Cliente": return Arrays.asList("menu", "orders", "settings");
            case "Cocina": return Arrays.asList("orders", "settings");
            case "Mesero": return Arrays.asList("menu", "orders", "settings");
            case "Cajera": return Arrays.asList("menu", "orders", "cash", "reports", "settings");
            default: return Arrays.asList(IDS);
        }
    }

    // --------------------------------------------------------------- shell

    private JPanel construirShell() {
        vistas.clear();
        botonesNav.clear();
        zonaVistas.removeAll();
        vistas.put("menu", new VistaMenu(ctx));
        vistas.put("orders", new VistaPedidos(ctx));
        vistas.put("cash", new VistaCaja(ctx));
        vistas.put("promos", new VistaPromociones(ctx));
        vistas.put("reports", new VistaReportes(ctx));
        vistas.put("settings", new VistaConfiguracion(ctx));
        vistas.forEach((id, v) -> zonaVistas.add(v, id));
        zonaVistas.setBackground(Tema.FONDO);

        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(Tema.FONDO);
        shell.add(construirSidebar(), BorderLayout.WEST);

        JPanel principal = new JPanel(new BorderLayout());
        principal.setBackground(Tema.FONDO);
        cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(Tema.FONDO);
        principal.add(cabecera, BorderLayout.NORTH);
        principal.add(zonaVistas, BorderLayout.CENTER);
        shell.add(principal, BorderLayout.CENTER);

        panelPedido = new PanelPedido(ctx);
        shell.add(panelPedido, BorderLayout.EAST);
        return shell;
    }

    private JPanel construirSidebar() {
        JPanel s = new JPanel(new BorderLayout());
        s.setBackground(Color.WHITE);
        s.setPreferredSize(new Dimension(220, 0));
        s.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1, Tema.BORDE));

        JPanel marca = Tema.transparente(new FlowLayout(FlowLayout.LEFT, 11, 0));
        marca.setBorder(Tema.margen(30, 18, 26, 10));
        marca.add(new JComponent() {
            {
                setPreferredSize(new Dimension(39, 39));
            }

            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Tema.suavizar(g);
                g.setColor(Tema.ACENTO);
                g.fillRoundRect(0, 0, 38, 38, 13, 13);
                Iconos.de("coffee", 24, Color.WHITE).paintIcon(this, g, 7, 7);
                g.dispose();
            }
        });
        JLabel nombre = new JLabel("Rosetta");
        nombre.setFont(Tema.titulo(24));
        nombre.setForeground(new Color(0x5B3436));
        nombre.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        marca.add(nombre);

        sidebarNav = Tema.transparente(null);
        sidebarNav.setLayout(new BoxLayout(sidebarNav, BoxLayout.Y_AXIS));
        sidebarNav.setBorder(Tema.margen(0, 14, 0, 14));
        String[][] items = {
            {"menu", ctx.rol.equals("Cliente") ? "Hacer pedido" : "Nuevo pedido", "grid"},
            {"orders", "Pedidos", "orders"},
            {"cash", "Caja", "receipt"},
            {"promos", "Promociones", "sparkle"},
            {"reports", "Reportes", "report"}};
        List<String> ok = permitidos();
        for (String[] it : items) {
            if (ok.contains(it[0]) && !(it[0].equals("promos") && !(ctx.rol.equals("Administrador") || ctx.rol.equals("Cajera")))) {
                sidebarNav.add(navBoton(it[0], it[1], it[2]));
                sidebarNav.add(Box.createVerticalStrut(6));
            }
        }

        JPanel inferior = Tema.transparente(null);
        inferior.setLayout(new BoxLayout(inferior, BoxLayout.Y_AXIS));
        inferior.setBorder(Tema.margen(0, 14, 20, 14));
        inferior.add(navBoton("settings", "Configuración", "settings"));
        inferior.add(Box.createVerticalStrut(12));
        Tema.Boton perfil = Tema.boton("", Tema.Variante.TEXTO);
        perfil.setLayout(new BorderLayout(10, 0));
        perfil.add(VistaConfiguracion.avatar(ctx.usuario), BorderLayout.WEST);
        JPanel tx = Tema.transparente(null);
        tx.setLayout(new BoxLayout(tx, BoxLayout.Y_AXIS));
        tx.add(Tema.lbl(ctx.usuario, 12.5f, Font.BOLD, Tema.TEXTO));
        tx.add(Tema.lbl(ctx.rol + " · Cerrar sesión", 10.5f, Font.PLAIN, Tema.SUAVE));
        perfil.add(tx, BorderLayout.CENTER);
        perfil.setBorder(Tema.margen(6, 6, 6, 6));
        perfil.setToolTipText("Cerrar sesión");
        perfil.addActionListener(e -> salir());
        perfil.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        perfil.setAlignmentX(Component.LEFT_ALIGNMENT);
        inferior.add(perfil);

        s.add(marca, BorderLayout.NORTH);
        s.add(sidebarNav, BorderLayout.CENTER);
        s.add(inferior, BorderLayout.SOUTH);
        return s;
    }

    private Tema.Boton navBoton(String id, String texto, String icono) {
        Tema.Boton b = Tema.boton(texto, Tema.Variante.TEXTO);
        b.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        b.setIconTextGap(13);
        b.setBorder(Tema.margen(12, 14, 12, 14));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.putClientProperty("icono", icono);
        b.addActionListener(e -> ir(id));
        botonesNav.put(id, b);
        return b;
    }

    private void pintarNavegacion() {
        for (Map.Entry<String, Tema.Boton> e : botonesNav.entrySet()) {
            boolean activo = e.getKey().equals(actual);
            Tema.Boton b = e.getValue();
            b.setForeground(activo ? new Color(0x964F57) : new Color(0x8A7B77));
            b.setIcon(Iconos.de((String) b.getClientProperty("icono"), 20, activo ? new Color(0x964F57) : new Color(0x8A7B77)));
            b.setFont(Tema.fuente(Font.BOLD, 13));
            b.setOpaque(false);
            if (e.getKey().equals("orders")) {
                int activos = 0;
                for (Pedido p : ctx.f.pedidosEnviados()) {
                    EstadoPedido es = p.getEstado();
                    if (es == EstadoPedido.PENDIENTE || es == EstadoPedido.EN_PREPARACION || es == EstadoPedido.LISTO_PARA_ENTREGAR) {
                        activos++;
                    }
                }
                b.setText(activos > 0 ? "Pedidos   ●" + activos : "Pedidos");
            }
            b.repaint();
        }
    }

    // ------------------------------------------------------------ cabecera

    private void construirCabecera() {
        cabecera.removeAll();
        cabecera.setBorder(Tema.margen(30, 32, 18, 32));
        JPanel izq = Tema.transparente(null);
        izq.setLayout(new BoxLayout(izq, BoxLayout.Y_AXIS));
        String fecha = ctx.f.ahora().format(DateTimeFormatter.ofPattern("EEEE, d 'DE' MMMM", new Locale("es", "CO"))).toUpperCase(new Locale("es", "CO"));
        izq.add(Tema.lbl(fecha + " · " + ctx.rol.toUpperCase(), 11, Font.BOLD, Tema.ACENTO));
        String titulo;
        String sub;
        switch (actual) {
            case "menu":
                titulo = ctx.saludo() + ", " + ctx.usuario.split(" ")[0];
                sub = "¿Qué vamos a preparar hoy?";
                break;
            case "orders": titulo = "Pedidos"; sub = "Todo bajo control en un solo lugar"; break;
            case "cash": titulo = "Caja"; sub = "Cobros, cierre de turno y consolidado"; break;
            case "promos": titulo = "Promociones"; sub = "Reglas de descuento por día y horario"; break;
            case "reports": titulo = "Reportes"; sub = "Ventas, productos y métodos de pago"; break;
            default: titulo = "Configuración"; sub = "Perfil, menú y opciones del local";
        }
        JLabel h1 = new JLabel(titulo);
        h1.setFont(Tema.titulo(30));
        h1.setForeground(Tema.TEXTO);
        h1.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        izq.add(h1);
        izq.add(Tema.lbl(sub, 13, Font.PLAIN, Tema.SUAVE));
        cabecera.add(izq, BorderLayout.WEST);

        JPanel der = Tema.transparente(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        Tema.Boton campana = Tema.boton("", Tema.Variante.BORDE);
        campana.setIcon(Iconos.de("bell", 20, Tema.TEXTO));
        campana.setBorder(Tema.margen(10, 11, 10, 11));
        campana.setToolTipText("Notificaciones");
        campana.addActionListener(e -> popupAvisos(campana));
        if (!ctx.avisos.isEmpty()) {
            campana.setText("●");
            campana.setForeground(Tema.ACENTO_OSC);
            campana.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        }
        der.add(campana);
        if (!ctx.rol.equals("Cliente")) {
            boolean ab = ctx.f.cajaAbierta();
            der.add(new Componentes.Pildora(ab ? "●  Caja abierta" : "○  Caja cerrada", ab ? Tema.VERDE : Tema.SUAVE, ab ? Tema.VERDE_BG : new Color(0xEFEAE8)));
        }
        cabecera.add(der, BorderLayout.EAST);
        cabecera.revalidate();
        cabecera.repaint();
    }

    private void popupAvisos(Component origen) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(javax.swing.BorderFactory.createLineBorder(Tema.BORDE_FUERTE));
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(Tema.margen(14, 16, 10, 16));
        JLabel t = new JLabel("Notificaciones");
        t.setFont(Tema.titulo(17));
        t.setForeground(Tema.TEXTO);
        t.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        p.add(t);
        p.add(Box.createVerticalStrut(8));
        if (ctx.avisos.isEmpty()) {
            p.add(Tema.lbl("Sin avisos todavía. Aparecerán al cambiar el estado de un pedido.", 12, Font.PLAIN, Tema.SUAVE));
        }
        int n = 0;
        for (String[] a : ctx.avisos) {
            if (n++ >= 8) {
                break;
            }
            JPanel fila = Tema.transparente(new BorderLayout(10, 0));
            fila.setBorder(Tema.margen(7, 0, 7, 0));
            fila.add(Tema.lbl(n == 1 ? "●" : "○", 12, Font.BOLD, n == 1 ? Tema.VERDE : Tema.SUAVE), BorderLayout.WEST);
            JPanel tx = Tema.transparente(null);
            tx.setLayout(new BoxLayout(tx, BoxLayout.Y_AXIS));
            tx.add(Tema.lbl(a[0], 12.5f, Font.BOLD, Tema.TEXTO));
            tx.add(Tema.lbl("<html><body style='width:240px'>" + a[1].replace("<", "&lt;") + "</body></html>", 11.5f, Font.PLAIN, Tema.SUAVE));
            tx.add(Tema.lbl(a[2], 10.5f, Font.PLAIN, new Color(0xB6A6A2)));
            fila.add(tx, BorderLayout.CENTER);
            p.add(fila);
        }
        menu.add(p);
        menu.show(origen, origen.getWidth() - 330, origen.getHeight() + 6);
    }

    // ------------------------------------------------------------ navegación

    private void ir(String id) {
        if (!permitidos().contains(id)) {
            return;
        }
        actual = id;
        vistasCards.show(zonaVistas, id);
        vistas.get(id).refrescar();
        panelPedido.setVisible(id.equals("menu"));
        if (id.equals("menu")) {
            panelPedido.refrescar();
        }
        construirCabecera();
        pintarNavegacion();
    }

    private void refrescarTodo() {
        if (vistas.isEmpty() || !vistas.containsKey(actual)) {
            return;
        }
        vistas.get(actual).refrescar();
        if (actual.equals("menu")) {
            panelPedido.refrescar();
        }
        construirCabecera();
        pintarNavegacion();
    }

    // ----------------------------------------------------------------- toast

    /** Aviso flotante inferior (equivalente al "toast" de la referencia). */
    private static class Toast extends JComponent {
        private String texto = "";
        private final Timer reloj = new Timer(3000, e -> {
            texto = "";
            repaint();
        });

        Toast() {
            setOpaque(false);
            reloj.setRepeats(false);
        }

        void mostrar(String t) {
            texto = t;
            reloj.restart();
            repaint();
        }

        @Override
        public boolean contains(int x, int y) {
            return false;   // no bloquea los clics de la ventana
        }

        @Override
        protected void paintComponent(Graphics g0) {
            if (texto.isEmpty()) {
                return;
            }
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            g.setFont(Tema.fuente(Font.BOLD, 13));
            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(texto) + 56;
            int h = 40;
            int x = (getWidth() - w) / 2;
            int y = getHeight() - h - 34;
            g.setColor(new Color(0x342825));
            g.fillRoundRect(x, y, w, h, 20, 20);
            g.setColor(new Color(0x75AD88));
            g.drawString("✓", x + 18, y + 25);
            g.setColor(Color.WHITE);
            g.drawString(texto, x + 38, y + 25);
            g.dispose();
        }
    }
}
