package cafeteria.ui.gui;

import cafeteria.dominio.Pedido;
import cafeteria.dominio.Producto;
import cafeteria.util.Formato;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;

/** Panel lateral "Pedido actual": líneas, cantidades, promoción, total y envío a cocina. */
class PanelPedido extends JPanel {

    private final Contexto ctx;
    private final JPanel lineas = Tema.transparente(null);
    private final JPanel resumen = Tema.transparente(null);
    private final JLabel numero = new JLabel();

    PanelPedido(Contexto ctx) {
        super(new BorderLayout());
        this.ctx = ctx;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(350, 0));
        setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 0, 0, Tema.BORDE));

        JPanel cab = Tema.transparente(new BorderLayout());
        cab.setBorder(Tema.margen(26, 22, 14, 22));
        JPanel t = Tema.transparente(null);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(Tema.lbl("PEDIDO ACTUAL", 10.5f, Font.BOLD, Tema.SUAVE));
        numero.setFont(Tema.titulo(24));
        numero.setForeground(Tema.TEXTO);
        numero.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        t.add(numero);
        cab.add(t, BorderLayout.CENTER);
        add(cab, BorderLayout.NORTH);

        lineas.setLayout(new BoxLayout(lineas, BoxLayout.Y_AXIS));
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setBackground(Color.WHITE);
        envoltorio.setBorder(Tema.margen(0, 22, 0, 22));
        envoltorio.add(lineas, BorderLayout.NORTH);
        add(Tema.scroll(envoltorio), BorderLayout.CENTER);
        ((javax.swing.JScrollPane) getComponent(1)).getViewport().setBackground(Color.WHITE);

        resumen.setLayout(new BoxLayout(resumen, BoxLayout.Y_AXIS));
        resumen.setBorder(Tema.margen(14, 22, 22, 22));
        add(resumen, BorderLayout.SOUTH);
        refrescar();
    }

    void refrescar() {
        Pedido p = ctx.borrador;
        numero.setText("Pedido #" + p.getIdPedido());
        lineas.removeAll();
        if (p.getItems().isEmpty()) {
            JPanel vacio = Tema.transparente(new FlowLayout(FlowLayout.CENTER));
            vacio.setBorder(Tema.margen(50, 0, 50, 0));
            JLabel l = new JLabel("<html><div style='text-align:center'><b>Tu pedido está vacío</b><br>"
                    + "<span style='color:#8A7B77'>Agrega productos desde el menú</span></div></html>", Iconos.de("receipt", 34, new Color(0xD9CECA)),
                    SwingConstants.CENTER);
            l.setVerticalTextPosition(SwingConstants.BOTTOM);
            l.setHorizontalTextPosition(SwingConstants.CENTER);
            l.setFont(Tema.fuente(Font.PLAIN, 12.5f));
            vacio.add(l);
            lineas.add(vacio);
        }
        for (int i = 0; i < p.getItems().size(); i++) {
            lineas.add(linea(p, i));
        }
        resumen.removeAll();
        double subtotal = p.calcularSubtotal();
        double total = p.calcularTotal();
        resumen.add(fila("Subtotal", Formato.moneda(subtotal), Tema.TEXTO, false));
        if (subtotal - total > 0) {
            resumen.add(Box.createVerticalStrut(6));
            resumen.add(fila("<html><body style='width:190px'>Promoción<br><span style='font-size:9px;color:#8A7B77'>" + esc(p.getEstrategiaPromocion().toString()) + "</span></body></html>",
                    "-" + Formato.moneda(subtotal - total), Tema.VERDE, false));
        }
        resumen.add(Box.createVerticalStrut(10));
        resumen.add(fila("Total", Formato.moneda(total), Tema.TEXTO, true));
        resumen.add(Box.createVerticalStrut(14));
        Tema.Boton enviar = Tema.boton("", Tema.Variante.PRIMARIO);
        enviar.setLayout(new BorderLayout());
        enviar.add(Tema.lbl("Enviar a cocina", 13.5f, Font.BOLD, Color.WHITE), BorderLayout.WEST);
        enviar.add(Tema.lbl(Formato.moneda(total) + "  →", 13.5f, Font.BOLD, Color.WHITE), BorderLayout.EAST);
        enviar.setEnabled(!p.getItems().isEmpty());
        enviar.setAlignmentX(Component.LEFT_ALIGNMENT);
        enviar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        enviar.addActionListener(e -> {
            int id = p.getIdPedido();
            if (ctx.intentar(() -> ctx.f.confirmarPedido(p))) {
                ctx.toast("Pedido #" + id + " enviado. Cocina fue notificada.");
                ctx.nuevoBorrador();
                ctx.irA("orders");
            }
        });
        resumen.add(enviar);
        resumen.add(Box.createVerticalStrut(10));
        JLabel nota = Tema.lbl("●  Cocina será notificada automáticamente", 11, Font.PLAIN, Tema.SUAVE);
        nota.setAlignmentX(Component.LEFT_ALIGNMENT);
        resumen.add(nota);
        revalidate();
        repaint();
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;");
    }

    private JPanel fila(String izq, String der, Color color, boolean grande) {
        JPanel f = Tema.transparente(new BorderLayout());
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel a = Tema.lbl(izq, grande ? 16 : 12.5f, grande ? Font.BOLD : Font.PLAIN, grande ? Tema.TEXTO : Tema.SUAVE);
        JLabel b = Tema.lbl(der, grande ? 20 : 12.5f, Font.BOLD, color);
        f.add(a, BorderLayout.WEST);
        f.add(b, BorderLayout.EAST);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, f.getPreferredSize().height + 4));
        return f;
    }

    private JPanel linea(Pedido p, int i) {
        Producto prod = p.getItems().get(i);
        int cant = p.getCantidades().get(i);
        JPanel fila = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Tema.BORDE);
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        fila.setBackground(Color.WHITE);
        fila.setBorder(Tema.margen(12, 0, 12, 0));
        ImagenCafe.Foto foto = ImagenCafe.fotoProducto(prod.getNombre(), ctx.f.tipoDe(prod.getId()), ctx.f.categoriaDe(prod.getId()), 54, 54, 12);
        JPanel izqFoto = Tema.transparente(new BorderLayout());
        izqFoto.add(foto, BorderLayout.NORTH);
        fila.add(izqFoto, BorderLayout.WEST);

        JPanel centro = Tema.transparente(null);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        JPanel titulo = Tema.transparente(new BorderLayout());
        titulo.add(Tema.lbl(prod.getNombre(), 13, Font.BOLD, Tema.TEXTO), BorderLayout.CENTER);
        Tema.Boton borrar = Tema.boton("", Tema.Variante.TEXTO);
        borrar.setIcon(Iconos.de("trash", 15, Tema.SUAVE));
        borrar.setBorder(Tema.margen(0, 4, 0, 0));
        borrar.addActionListener(e -> {
            ctx.intentar(() -> ctx.f.cambiarCantidad(p, i, 0));
            refrescar();
        });
        titulo.add(borrar, BorderLayout.EAST);
        centro.add(titulo);
        String desc = prod.getDescripcion();
        JLabel d = Tema.lbl("<html><body style='width:140px'>" + esc(desc) + "</body></html>", 10.5f, Font.PLAIN, Tema.SUAVE);
        d.setToolTipText(desc);
        centro.add(d);
        centro.add(Box.createVerticalStrut(6));
        JPanel pie = Tema.transparente(new BorderLayout());
        JPanel contador = Tema.transparente(new FlowLayout(FlowLayout.LEFT, 6, 0));
        contador.add(botonCantidad("minus", () -> ctx.intentar(() -> ctx.f.cambiarCantidad(p, i, cant - 1))));
        contador.add(Tema.lbl(String.valueOf(cant), 13, Font.BOLD, Tema.TEXTO));
        contador.add(botonCantidad("plus", () -> ctx.intentar(() -> ctx.f.cambiarCantidad(p, i, cant + 1))));
        pie.add(contador, BorderLayout.WEST);
        pie.add(Tema.lbl(Formato.moneda(prod.getPrecio() * cant), 13, Font.BOLD, Tema.TEXTO), BorderLayout.EAST);
        centro.add(pie);
        fila.add(centro, BorderLayout.CENTER);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, fila.getPreferredSize().height));
        return fila;
    }

    private Tema.Boton botonCantidad(String icono, Runnable accion) {
        Tema.Boton b = Tema.boton("", Tema.Variante.BORDE);
        b.setIcon(Iconos.de(icono, 12, Tema.TEXTO));
        b.setBorder(Tema.margen(5, 5, 5, 5));
        b.arco(8);
        b.addActionListener(e -> {
            accion.run();
            refrescar();
        });
        return b;
    }
}
