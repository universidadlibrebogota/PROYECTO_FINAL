package cafeteria.ui.gui;

import cafeteria.dominio.Producto;
import cafeteria.repository.ConfiguracionProducto;
import cafeteria.util.Formato;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Configuración: perfil y, para el administrador, menú, adiciones y combos. */
class VistaConfiguracion extends VistaBase {

    private String seccion = "Mi perfil";

    VistaConfiguracion(Contexto ctx) {
        super(ctx, new BorderLayout());
    }

    @Override
    void refrescar() {
        removeAll();
        boolean admin = ctx.rol.equals("Administrador");
        if (!admin) {
            seccion = "Mi perfil";
        }
        JPanel raiz = new JPanel(new BorderLayout(18, 0));
        raiz.setBackground(Tema.FONDO);
        raiz.setBorder(Tema.margen(6, 32, 36, 32));

        Tema.Tarjeta menu = new Tema.Tarjeta(new BorderLayout());
        JPanel items = Tema.transparente(null);
        items.setLayout(new BoxLayout(items, BoxLayout.Y_AXIS));
        items.setBorder(Tema.margen(10, 10, 10, 10));
        List<String> secciones = new ArrayList<>();
        secciones.add("Mi perfil");
        if (admin) {
            secciones.add("Menú y productos");
            secciones.add("Adiciones");
            secciones.add("Combos");
        }
        for (String s : secciones) {
            Tema.Boton b = Tema.boton(s, s.equals(seccion) ? Tema.Variante.SUAVE : Tema.Variante.TEXTO);
            b.setForeground(s.equals(seccion) ? Tema.ACENTO_OSC : Tema.SUAVE);
            b.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.addActionListener(e -> {
                seccion = s;
                refrescar();
            });
            items.add(b);
            items.add(Box.createVerticalStrut(4));
        }
        menu.add(items, BorderLayout.NORTH);
        JPanel izq = Tema.transparente(new BorderLayout());
        izq.setPreferredSize(new Dimension(210, 10));
        izq.add(menu, BorderLayout.NORTH);
        raiz.add(izq, BorderLayout.WEST);

        Tema.Tarjeta contenido = new Tema.Tarjeta(new BorderLayout());
        JPanel c;
        switch (seccion) {
            case "Menú y productos": c = productos(); break;
            case "Adiciones": c = adiciones(); break;
            case "Combos": c = combos(); break;
            default: c = perfil();
        }
        c.setBorder(Tema.margen(22, 26, 24, 26));
        contenido.add(c, BorderLayout.CENTER);
        JPanel der = Tema.transparente(new BorderLayout());
        der.add(contenido, BorderLayout.NORTH);
        raiz.add(Tema.scroll(der), BorderLayout.CENTER);
        add(raiz, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private static JLabel titulo(String t) {
        JLabel l = new JLabel(t);
        l.setFont(Tema.titulo(20));
        l.setForeground(Tema.TEXTO);
        l.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JPanel vertical() {
        JPanel p = Tema.transparente(null);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    // --------------------------------------------------------------- perfil

    private JPanel perfil() {
        JPanel p = vertical();
        JPanel cab = Tema.transparente(new FlowLayout(FlowLayout.LEFT, 14, 0));
        cab.setAlignmentX(Component.LEFT_ALIGNMENT);
        cab.add(avatar(ctx.usuario));
        JPanel t = vertical();
        t.add(Tema.lbl(ctx.usuario, 18, Font.BOLD, Tema.TEXTO));
        t.add(Tema.lbl(ctx.rol + " · Sede principal", 12.5f, Font.PLAIN, Tema.SUAVE));
        cab.add(t);
        p.add(cab);
        p.add(Box.createVerticalStrut(16));
        p.add(titulo("Información"));
        p.add(Box.createVerticalStrut(10));
        p.add(dato("Nombre", ctx.usuario));
        p.add(dato("Rol", ctx.rol));
        p.add(dato("Almacenamiento", ctx.f.descripcionPersistencia()));
        p.add(Box.createVerticalStrut(14));
        JPanel fila = Tema.transparente(new BorderLayout());
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel tx = vertical();
        tx.add(Tema.lbl("Notificaciones operativas", 13, Font.BOLD, Tema.TEXTO));
        tx.add(Tema.lbl("Mostrar avisos emergentes al cambiar el estado de los pedidos.", 12, Font.PLAIN, Tema.SUAVE));
        fila.add(tx, BorderLayout.WEST);
        Componentes.Interruptor sw = new Componentes.Interruptor(ctx.avisosActivos);
        sw.addActionListener(e -> ctx.avisosActivos = sw.isSelected());
        fila.add(sw, BorderLayout.EAST);
        p.add(fila);
        return p;
    }

    private JPanel dato(String etiqueta, String valor) {
        JPanel f = Tema.transparente(new BorderLayout());
        f.setBorder(Tema.margen(5, 0, 5, 0));
        f.add(Tema.lbl(etiqueta, 12.5f, Font.PLAIN, Tema.SUAVE), BorderLayout.WEST);
        f.add(Tema.lbl(valor, 12.5f, Font.BOLD, Tema.TEXTO), BorderLayout.EAST);
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, f.getPreferredSize().height));
        return f;
    }

    static Component avatar(String nombre) {
        return new javax.swing.JComponent() {
            {
                setPreferredSize(new Dimension(48, 48));
            }

            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Tema.suavizar(g);
                g.setColor(Tema.ROSA);
                g.fillOval(0, 0, 47, 47);
                g.setColor(Tema.ACENTO_OSC);
                g.setFont(Tema.fuente(Font.BOLD, 16));
                String s = nombre.length() < 2 ? nombre.toUpperCase() : nombre.substring(0, 2).toUpperCase();
                g.drawString(s, 24 - g.getFontMetrics().stringWidth(s) / 2, 30);
                g.dispose();
            }
        };
    }

    // ------------------------------------------------------------- productos

    private JPanel productos() {
        JPanel p = vertical();
        JPanel cab = Tema.transparente(new BorderLayout());
        cab.setAlignmentX(Component.LEFT_ALIGNMENT);
        cab.add(titulo("Menú y productos"), BorderLayout.WEST);
        Tema.Boton nuevo = Tema.boton("＋  Nuevo producto", Tema.Variante.PRIMARIO);
        nuevo.addActionListener(e -> formulario(null));
        Tema.Boton renombrar = Tema.boton("Renombrar categoría", Tema.Variante.BORDE);
        renombrar.addActionListener(e -> renombrarCategoria());
        JPanel botones = Tema.transparente(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(renombrar);
        botones.add(nuevo);
        cab.add(botones, BorderLayout.EAST);
        p.add(cab);
        p.add(Box.createVerticalStrut(10));
        for (Producto prod : ctx.f.productos()) {
            JPanel fila = new JPanel(new BorderLayout(12, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(Tema.BORDE);
                    g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                }
            };
            fila.setBackground(Color.WHITE);
            fila.setBorder(Tema.margen(9, 0, 9, 0));
            JPanel t = vertical();
            t.add(Tema.lbl(prod.getNombre(), 13, Font.BOLD, Tema.TEXTO));
            t.add(Tema.lbl(ctx.f.tipoDe(prod.getId()) + " · " + ctx.f.categoriaDe(prod.getId()) + " · " + Formato.moneda(prod.getPrecio()),
                    11.5f, Font.PLAIN, Tema.SUAVE));
            fila.add(t, BorderLayout.CENTER);
            JPanel acc = Tema.transparente(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            acc.add(Tema.lbl(prod.isDisponible() ? "Disponible" : "Agotado", 11.5f, Font.BOLD, prod.isDisponible() ? Tema.VERDE : Tema.SUAVE));
            Componentes.Interruptor sw = new Componentes.Interruptor(prod.isDisponible());
            sw.addActionListener(e -> {
                ctx.f.cambiarDisponibilidad(prod.getId(), sw.isSelected());
                refrescar();
            });
            acc.add(sw);
            Tema.Boton ed = Tema.boton("Editar", Tema.Variante.BORDE);
            ed.setBorder(Tema.margen(5, 12, 5, 12));
            ed.addActionListener(e -> formulario(prod));
            acc.add(ed);
            fila.add(acc, BorderLayout.EAST);
            fila.setAlignmentX(Component.LEFT_ALIGNMENT);
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, fila.getPreferredSize().height));
            p.add(fila);
        }
        return p;
    }

    private void renombrarCategoria() {
        Object[] cats = ctx.f.categorias().toArray();
        if (cats.length == 0) {
            return;
        }
        Object vieja = javax.swing.JOptionPane.showInputDialog(this, "Categoría a renombrar:", "Renombrar categoría",
                javax.swing.JOptionPane.PLAIN_MESSAGE, null, cats, cats[0]);
        if (vieja == null) {
            return;
        }
        String nueva = javax.swing.JOptionPane.showInputDialog(this, "Nuevo nombre para \"" + vieja + "\":", vieja);
        if (nueva != null && ctx.intentar(() -> ctx.f.renombrarCategoria(vieja.toString(), nueva))) {
            ctx.toast("Categoría renombrada");
            ctx.refrescarTodo.run();
        }
    }

    private void formulario(Producto editar) {
        ConfiguracionProducto cfg = editar == null ? null : ctx.f.configuracionProducto(editar.getId());
        Componentes.Modal m = new Componentes.Modal(this, 460);
        JPanel col = vertical();
        col.add(Tema.lbl(editar == null ? "NUEVO PRODUCTO" : "EDITAR PRODUCTO", 11, Font.BOLD, Tema.ACENTO));
        col.add(titulo(editar == null ? "Registrar en el menú" : editar.getNombre()));
        col.add(Box.createVerticalStrut(10));
        JComboBox<String> tipo = new JComboBox<>(ctx.f.tiposProducto().toArray(new String[0]));
        JComboBox<String> categoria = new JComboBox<>(ctx.f.categorias().toArray(new String[0]));
        categoria.setEditable(true);
        Componentes.Campo nombre = new Componentes.Campo("Nombre", 24);
        Componentes.Campo desc = new Componentes.Campo("Descripción", 24);
        Componentes.Campo precio = new Componentes.Campo("Precio (en pesos)", 12);
        Componentes.Campo extra = new Componentes.Campo("", 16);
        JLabel ayuda = Tema.lbl(" ", 11.5f, Font.PLAIN, Tema.SUAVE);
        Runnable alCambiarTipo = () -> ayuda.setText(ctx.f.ayudaExtra((String) tipo.getSelectedItem()));
        tipo.addActionListener(e -> alCambiarTipo.run());
        if (cfg != null) {
            tipo.setSelectedItem(cfg.tipo);
            tipo.setEnabled(false);
            categoria.setSelectedItem(cfg.categoria);
            nombre.setText(cfg.nombre);
            desc.setText(cfg.descripcion);
            precio.setText(String.valueOf((long) cfg.precio));
            extra.setText(cfg.extra);
        } else {
            categoria.setSelectedItem("");
        }
        alCambiarTipo.run();
        for (java.awt.Component k : new java.awt.Component[]{tipo, categoria, nombre, desc, precio, ayuda, extra}) {
            ((javax.swing.JComponent) k).setAlignmentX(Component.LEFT_ALIGNMENT);
            if (!(k instanceof JLabel)) {
                ((javax.swing.JComponent) k).setMaximumSize(new Dimension(Integer.MAX_VALUE, k.getPreferredSize().height));
            }
            col.add(k);
            col.add(Box.createVerticalStrut(7));
        }
        Tema.Boton guardar = Tema.boton("Guardar", Tema.Variante.PRIMARIO);
        guardar.setAlignmentX(Component.LEFT_ALIGNMENT);
        guardar.addActionListener(e -> {
            try {
                double pr = Double.parseDouble(precio.getText().trim().replace(',', '.'));
                String cat = String.valueOf(categoria.getSelectedItem());
                if (cfg == null) {
                    ctx.f.registrarProducto((String) tipo.getSelectedItem(), cat, nombre.getText(), desc.getText(), pr, extra.getText());
                } else {
                    ctx.f.modificarProducto(cfg.id, cat, nombre.getText(), desc.getText(), pr, extra.getText());
                }
                ctx.toast("Producto guardado");
                m.dispose();
                ctx.refrescarTodo.run();
            } catch (RuntimeException ex) {
                ctx.toast("⚠ " + ex.getMessage());
            }
        });
        col.add(Box.createVerticalStrut(6));
        col.add(guardar);
        m.cuerpo.add(col, BorderLayout.CENTER);
        m.abrir();
    }

    // ------------------------------------------------------------- adiciones

    private JPanel adiciones() {
        JPanel p = vertical();
        p.add(titulo("Adiciones por tipo de producto"));
        p.add(Tema.lbl("Aparecen al personalizar. Agregar una no requiere cambios de código.", 12, Font.PLAIN, Tema.SUAVE));
        p.add(Box.createVerticalStrut(12));
        for (String tipo : ctx.f.tiposProducto()) {
            JLabel h = Tema.lbl(tipo, 13, Font.BOLD, Tema.ACENTO);
            h.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(h);
            Map<String, Double> mapa = ctx.f.adiciones(tipo);
            if (mapa.isEmpty()) {
                JLabel v = Tema.lbl("Sin adiciones", 12, Font.PLAIN, Tema.SUAVE);
                v.setAlignmentX(Component.LEFT_ALIGNMENT);
                p.add(v);
            }
            for (Map.Entry<String, Double> e : mapa.entrySet()) {
                JPanel f = Tema.transparente(new BorderLayout());
                f.setBorder(Tema.margen(4, 0, 4, 0));
                f.add(Tema.lbl(e.getKey() + "  ·  +" + Formato.moneda(e.getValue()), 12.5f, Font.PLAIN, Tema.TEXTO), BorderLayout.WEST);
                Tema.Boton x = Tema.boton("Quitar", Tema.Variante.TEXTO);
                x.setBorder(Tema.margen(1, 6, 1, 6));
                x.addActionListener(ev -> {
                    ctx.f.eliminarAdicion(tipo, e.getKey());
                    refrescar();
                });
                f.add(x, BorderLayout.EAST);
                f.setAlignmentX(Component.LEFT_ALIGNMENT);
                f.setMaximumSize(new Dimension(Integer.MAX_VALUE, f.getPreferredSize().height));
                p.add(f);
            }
            p.add(Box.createVerticalStrut(10));
        }
        JPanel nueva = Tema.transparente(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nueva.setAlignmentX(Component.LEFT_ALIGNMENT);
        JComboBox<String> tipo = new JComboBox<>(ctx.f.tiposProducto().toArray(new String[0]));
        Componentes.Campo nombre = new Componentes.Campo("Nueva adición", 14);
        Componentes.Campo costo = new Componentes.Campo("Costo", 6);
        Tema.Boton add = Tema.boton("Agregar", Tema.Variante.PRIMARIO);
        add.addActionListener(e -> {
            try {
                ctx.f.registrarAdicion((String) tipo.getSelectedItem(), nombre.getText(), Double.parseDouble(costo.getText().trim().replace(',', '.')));
                ctx.toast("Adición registrada");
                refrescar();
            } catch (RuntimeException ex) {
                ctx.toast("⚠ " + ex.getMessage());
            }
        });
        nueva.add(tipo);
        nueva.add(nombre);
        nueva.add(costo);
        nueva.add(add);
        p.add(nueva);
        return p;
    }

    // ---------------------------------------------------------------- combos

    private JPanel combos() {
        JPanel p = vertical();
        p.add(titulo("Combos"));
        p.add(Tema.lbl("Descuento aplicado al precio de los componentes de cada combo armado.", 12, Font.PLAIN, Tema.SUAVE));
        p.add(Box.createVerticalStrut(14));
        JPanel f = Tema.transparente(new FlowLayout(FlowLayout.LEFT, 8, 0));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        Componentes.Campo d = new Componentes.Campo("%", 6);
        d.setText(String.valueOf((long) ctx.f.descuentoCombo()));
        Tema.Boton g = Tema.boton("Guardar descuento", Tema.Variante.PRIMARIO);
        g.addActionListener(e -> {
            try {
                ctx.f.setDescuentoCombo(Double.parseDouble(d.getText().trim().replace(',', '.')));
                ctx.toast("Descuento de combos actualizado");
                ctx.refrescarTodo.run();
            } catch (RuntimeException ex) {
                ctx.toast("⚠ " + ex.getMessage());
            }
        });
        f.add(Tema.lbl("Descuento (%)", 12.5f, Font.BOLD, Tema.TEXTO));
        f.add(d);
        f.add(g);
        p.add(f);
        return p;
    }
}
