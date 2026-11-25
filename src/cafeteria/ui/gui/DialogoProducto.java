package cafeteria.ui.gui;

import cafeteria.dominio.Producto;
import cafeteria.util.Formato;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Personalización de un producto: adiciones (Decorator) y modificación libre. */
class DialogoProducto {

    static void abrir(Component origen, Contexto ctx, Producto base) {
        Componentes.Modal m = new Componentes.Modal(origen, 460);
        Map<String, Double> opciones = ctx.f.adicionesDisponibles(base.getId());
        Map<String, Componentes.FilaOpcion> filas = new LinkedHashMap<>();
        Componentes.Campo nota = new Componentes.Campo("Modificación o retiro (ej. Sin azúcar, sin cebolla)", 24);
        Tema.Boton agregar = Tema.boton("", Tema.Variante.PRIMARIO);
        agregar.setLayout(new BorderLayout());
        JLabel izq = Tema.lbl("Agregar al pedido", 14, Font.BOLD, java.awt.Color.WHITE);
        JLabel der = Tema.lbl("", 14, Font.BOLD, java.awt.Color.WHITE);
        agregar.add(izq, BorderLayout.WEST);
        agregar.add(der, BorderLayout.EAST);

        Runnable recalcular = () -> {
            List<String> sel = new ArrayList<>();
            filas.forEach((n, b) -> {
                if (b.isSelected()) {
                    sel.add(n);
                }
            });
            der.setText(Formato.moneda(ctx.f.personalizar(base, sel, nota.getText()).getPrecio()));
        };

        JPanel col = Tema.transparente(null);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        String tipo = ctx.f.tipoDe(base.getId());
        ImagenCafe.Foto foto = ImagenCafe.fotoProducto(base.getNombre(), tipo, ctx.f.categoriaDe(base.getId()), 408, 120, 16);
        foto.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(foto);
        col.add(Box.createVerticalStrut(14));
        col.add(izq(Tema.lbl("PERSONALIZA TU PRODUCTO", 11, Font.BOLD, Tema.ACENTO)));
        JLabel titulo = new JLabel(base.getNombre());
        titulo.setFont(Tema.titulo(24));
        titulo.setForeground(Tema.TEXTO);
        titulo.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        col.add(izq(titulo));
        col.add(izq(Tema.lbl("<html><body style='width:380px'>" + base.getDescripcion()
                + (opciones.isEmpty() ? "" : ". Elige todas las adiciones que quieras.") + "</body></html>", 12.5f, Font.PLAIN, Tema.SUAVE)));
        col.add(Box.createVerticalStrut(12));
        for (Map.Entry<String, Double> e : opciones.entrySet()) {
            Componentes.FilaOpcion fila = new Componentes.FilaOpcion(e.getKey(), "+" + Formato.moneda(e.getValue()), null);
            fila.addActionListener(ev -> recalcular.run());
            filas.put(e.getKey(), fila);
            col.add(fila);
            col.add(Box.createVerticalStrut(7));
        }
        col.add(Box.createVerticalStrut(4));
        col.add(izq(nota));
        col.add(Box.createVerticalStrut(14));
        col.add(izq(agregar));
        nota.getDocument().addDocumentListener(new SimpleDocListener(recalcular));
        recalcular.run();

        agregar.addActionListener(e -> {
            List<String> sel = new ArrayList<>();
            filas.forEach((n, b) -> {
                if (b.isSelected()) {
                    sel.add(n);
                }
            });
            Producto listo = ctx.f.personalizar(base, sel, nota.getText());
            if (ctx.intentar(() -> ctx.f.agregarAlPedido(ctx.borrador, listo, 1))) {
                ctx.toast(base.getNombre() + " agregado al pedido");
                ctx.refrescarPedido.run();
                m.dispose();
            }
        });
        m.cuerpo.add(col, BorderLayout.CENTER);
        m.abrir();
    }

    private static javax.swing.JComponent izq(javax.swing.JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (c instanceof Componentes.Campo || c instanceof Tema.Boton) {
            c.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        }
        return c;
    }

    /** Escucha cambios de un campo sin repetir los tres métodos. */
    static class SimpleDocListener implements javax.swing.event.DocumentListener {
        private final Runnable r;

        SimpleDocListener(Runnable r) {
            this.r = r;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            r.run();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            r.run();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            r.run();
        }
    }
}
