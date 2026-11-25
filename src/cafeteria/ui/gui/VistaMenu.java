package cafeteria.ui.gui;

import cafeteria.dominio.Producto;
import cafeteria.util.Formato;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/** Catálogo: banner de combos, categorías, búsqueda y tarjetas de producto. */
class VistaMenu extends VistaBase {

    private static final String TODOS = "Todos";
    private static final String COMBOS = "Combos";
    private String categoria = TODOS;
    private String busqueda = "";

    VistaMenu(Contexto ctx) {
        super(ctx, new BorderLayout());
    }

    @Override
    void refrescar() {
        removeAll();
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(Tema.FONDO);
        col.setBorder(Tema.margen(6, 32, 36, 32));

        col.add(banner());
        col.add(Box.createVerticalStrut(22));
        col.add(barraHerramientas());
        col.add(Box.createVerticalStrut(18));

        List<Producto> productos = filtrados();
        boolean mostrarCombo = categoria.equals(TODOS) || categoria.equals(COMBOS);
        int total = productos.size() + (mostrarCombo && coincideCombo() ? 1 : 0);

        JPanel titulo = Tema.transparente(new BorderLayout());
        JPanel tt = Tema.transparente(null);
        tt.setLayout(new BoxLayout(tt, BoxLayout.Y_AXIS));
        JLabel h2 = new JLabel(categoria.equals(TODOS) ? "Favoritos de la casa" : categoria);
        h2.setFont(Tema.titulo(20));
        h2.setForeground(Tema.TEXTO);
        h2.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        tt.add(h2);
        tt.add(Tema.lbl(total + " productos disponibles", 12, Font.PLAIN, Tema.SUAVE));
        titulo.add(tt, BorderLayout.WEST);
        Tema.Boton ver = Tema.boton("Ver menú completo  →", Tema.Variante.TEXTO);
        ver.addActionListener(e -> {
            categoria = TODOS;
            busqueda = "";
            refrescar();
        });
        Componentes.Campo buscar = new Componentes.Campo("Buscar producto...", 12);
        buscar.setText(busqueda);
        buscar.addActionListener(e -> {
            busqueda = buscar.getText().trim();
            refrescar();
        });
        JPanel acciones = Tema.transparente(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.add(buscar);
        acciones.add(ver);
        titulo.add(acciones, BorderLayout.EAST);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(titulo);
        col.add(Box.createVerticalStrut(12));

        JPanel grid = Tema.transparente(new GridLayout(0, 3, 14, 14));
        for (Producto p : productos) {
            grid.add(tarjeta(p));
        }
        if (mostrarCombo && coincideCombo()) {
            grid.add(tarjetaCombo());
        }
        while (grid.getComponentCount() % 3 != 0) {
            grid.add(Tema.transparente(null));
        }
        JPanel contenedor = Tema.transparente(new BorderLayout());
        contenedor.add(grid, BorderLayout.NORTH);
        contenedor.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(contenedor);
        if (total == 0) {
            col.add(Box.createVerticalStrut(20));
            col.add(Tema.lbl("No encontramos productos con ese filtro.", 13, Font.PLAIN, Tema.SUAVE));
        }
        add(Tema.scroll(col), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private boolean coincideCombo() {
        return busqueda.isEmpty() || "combo".contains(busqueda.toLowerCase()) || "arma tu combo".contains(busqueda.toLowerCase());
    }

    private List<Producto> filtrados() {
        List<Producto> r = new ArrayList<>();
        if (categoria.equals(COMBOS)) {
            return r;
        }
        for (Producto p : ctx.f.productos()) {
            boolean cat = categoria.equals(TODOS) || categoria.equalsIgnoreCase(ctx.f.categoriaDe(p.getId()));
            boolean txt = busqueda.isEmpty() || p.getNombre().toLowerCase().contains(busqueda.toLowerCase());
            if (cat && txt) {
                r.add(p);
            }
        }
        return r;
    }

    private JPanel banner() {
        JPanel b = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Tema.suavizar(g);
                Shape forma = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 22, 22);
                g.setClip(forma);
                ImagenCafe.pintar(g, 0, 0, getWidth(), getHeight(), 0.5, 0.45, 1.0);
                g.setPaint(new GradientPaint(0, 0, new Color(70, 30, 30, 200), getWidth() * 0.65f, 0, new Color(70, 30, 30, 0)));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.dispose();
            }
        };
        b.setOpaque(false);
        b.setPreferredSize(new Dimension(100, 200));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel e = Tema.lbl("PROMO DE LA MAÑANA", 10.5f, Font.BOLD, new Color(255, 255, 255, 220));
        e.setBounds(34, 24, 300, 18);
        JLabel t = new JLabel("<html>Un comienzo<br>más dulce</html>");
        t.setFont(Tema.titulo(30));
        t.setForeground(Color.WHITE);
        t.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        t.setBounds(34, 46, 360, 80);
        JLabel s = Tema.lbl(Formato.porcentaje(ctx.f.descuentoCombo()) + " de descuento al armar tu combo", 13, Font.PLAIN, new Color(255, 255, 255, 230));
        s.setBounds(34, 128, 380, 20);
        Tema.Boton boton = Tema.boton("Armar mi combo  →", Tema.Variante.BORDE);
        boton.setBounds(34, 154, 170, 36);
        boton.addActionListener(ev -> DialogoCombo.abrir(this, ctx));
        b.add(e);
        b.add(t);
        b.add(s);
        b.add(boton);
        return b;
    }

    private JPanel barraHerramientas() {
        JPanel barra = Tema.transparente(new BorderLayout(16, 0));
        barra.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel chips = Tema.transparente(new FlowLayout(FlowLayout.LEFT, 8, 0));
        List<String> cats = new ArrayList<>();
        cats.add(TODOS);
        cats.addAll(ctx.f.categorias());
        cats.add(COMBOS);
        for (String c : cats) {
            boolean activo = c.equalsIgnoreCase(categoria);
            Tema.Boton chip = Tema.boton(c, activo ? Tema.Variante.PRIMARIO : Tema.Variante.BORDE);
            chip.setIcon(Iconos.de(c.equals(TODOS) ? "grid" : c.equals(COMBOS) ? "sparkle" : c.toLowerCase().startsWith("caf") ? "coffee" : "plate", 15,
                    activo ? Color.WHITE : Tema.SUAVE));
            chip.setIconTextGap(6);
            chip.setBorder(Tema.margen(8, 10, 8, 12));
            chip.arco(20);
            chip.addActionListener(e -> {
                categoria = c;
                refrescar();
            });
            chips.add(chip);
        }
        barra.add(chips, BorderLayout.CENTER);
        barra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        return barra;
    }

    private JPanel tarjeta(Producto p) {
        Tema.Tarjeta t = new Tema.Tarjeta(new BorderLayout());
        String tipo = ctx.f.tipoDe(p.getId());
        ImagenCafe.Foto foto = ImagenCafe.fotoProducto(p.getNombre(), tipo, ctx.f.categoriaDe(p.getId()), 200, 115, 16);
        if (!p.isDisponible()) {
            foto.insignia("Agotado");
        }
        t.add(foto, BorderLayout.NORTH);
        JPanel info = Tema.transparente(new BorderLayout());
        info.setBorder(Tema.margen(12, 14, 14, 14));
        JPanel textos = Tema.transparente(null);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Tema.lbl(p.getNombre(), 14, Font.BOLD, p.isDisponible() ? Tema.TEXTO : Tema.SUAVE));
        String desc = ctx.f.configuracionProducto(p.getId()).descripcion;
        JLabel d = Tema.lbl(desc, 11.5f, Font.PLAIN, Tema.SUAVE);
        d.setToolTipText(desc);
        textos.add(d);
        info.add(textos, BorderLayout.CENTER);
        JPanel pie = Tema.transparente(new BorderLayout());
        pie.setBorder(Tema.margen(10, 0, 0, 0));
        pie.add(Tema.lbl(Formato.moneda(p.getPrecio()), 14.5f, Font.BOLD, Tema.TEXTO), BorderLayout.WEST);
        Tema.Boton mas = Tema.boton("", Tema.Variante.PRIMARIO);
        mas.setIcon(Iconos.de("plus", 16, Color.WHITE));
        mas.setBorder(Tema.margen(6, 6, 6, 6));
        mas.arco(14);
        mas.setEnabled(p.isDisponible());
        mas.setToolTipText("Personalizar " + p.getNombre());
        mas.addActionListener(e -> DialogoProducto.abrir(this, ctx, p));
        pie.add(mas, BorderLayout.EAST);
        info.add(pie, BorderLayout.SOUTH);
        t.add(info, BorderLayout.CENTER);
        return clip(t);
    }

    private JPanel tarjetaCombo() {
        Tema.Tarjeta t = new Tema.Tarjeta(new BorderLayout());
        ImagenCafe.Foto foto = ImagenCafe.fotoProducto("Arma tu combo", null, null, 200, 115, 16)
                .insignia("-" + Formato.porcentaje(ctx.f.descuentoCombo()));
        t.add(foto, BorderLayout.NORTH);
        JPanel info = Tema.transparente(new BorderLayout());
        info.setBorder(Tema.margen(12, 14, 14, 14));
        JPanel textos = Tema.transparente(null);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Tema.lbl("Arma tu combo", 14, Font.BOLD, Tema.TEXTO));
        textos.add(Tema.lbl("Plato + bebida + acompañamiento", 11.5f, Font.PLAIN, Tema.SUAVE));
        info.add(textos, BorderLayout.CENTER);
        JPanel pie = Tema.transparente(new BorderLayout());
        pie.setBorder(Tema.margen(10, 0, 0, 0));
        pie.add(Tema.lbl("3 pasos", 14.5f, Font.BOLD, Tema.TEXTO), BorderLayout.WEST);
        Tema.Boton mas = Tema.boton("", Tema.Variante.PRIMARIO);
        mas.setIcon(Iconos.de("sparkle", 16, Color.WHITE));
        mas.setBorder(Tema.margen(6, 6, 6, 6));
        mas.arco(14);
        mas.addActionListener(e -> DialogoCombo.abrir(this, ctx));
        pie.add(mas, BorderLayout.EAST);
        info.add(pie, BorderLayout.SOUTH);
        t.add(info, BorderLayout.CENTER);
        return clip(t);
    }

    private static JPanel clip(JPanel p) {
        p.setCursor(Cursor.getDefaultCursor());
        return p;
    }
}
