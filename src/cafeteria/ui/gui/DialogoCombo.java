package cafeteria.ui.gui;

import cafeteria.dominio.Combo;
import cafeteria.dominio.Producto;
import cafeteria.dominio.builder.ComboBuilder;
import cafeteria.service.PasoCombo;
import cafeteria.util.Formato;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
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
import java.util.List;

/** Asistente de combos por pasos (Builder): plato principal → bebida → acompañamiento. */
class DialogoCombo {

    private final Contexto ctx;
    private final Componentes.Modal modal;
    private final ComboBuilder builder;
    private final Producto[] elegidos = new Producto[3];
    private int paso = 0;

    private DialogoCombo(Component origen, Contexto ctx) {
        this.ctx = ctx;
        this.modal = new Componentes.Modal(origen, 480);
        this.builder = ctx.f.nuevoConstructorCombo();
    }

    static void abrir(Component origen, Contexto ctx) {
        DialogoCombo d = new DialogoCombo(origen, ctx);
        d.pintarPaso();
        d.modal.abrir();
    }

    private void pintarPaso() {
        PasoCombo actual = PasoCombo.values()[paso];
        JPanel col = Tema.transparente(null);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        JPanel progreso = Tema.transparente(new FlowLayout(FlowLayout.LEFT, 6, 0));
        for (int i = 0; i < 3; i++) {
            final int n = i;
            progreso.add(new JComponent() {
                {
                    setPreferredSize(new Dimension(n == 2 ? 30 : 74, 30));
                }

                @Override
                protected void paintComponent(Graphics g0) {
                    Graphics2D g = (Graphics2D) g0.create();
                    Tema.suavizar(g);
                    boolean activo = n <= paso;
                    g.setColor(activo ? Tema.ACENTO_OSC : Tema.BORDE);
                    g.fillOval(0, 0, 28, 28);
                    g.setColor(activo ? Color.WHITE : Tema.SUAVE);
                    g.setFont(Tema.fuente(Font.BOLD, 12));
                    g.drawString(String.valueOf(n + 1), 10, 19);
                    if (n < 2) {
                        g.setColor(n < paso ? Tema.ACENTO_OSC : Tema.BORDE);
                        g.fillRoundRect(34, 13, 36, 3, 3, 3);
                    }
                    g.dispose();
                }
            });
        }
        progreso.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(progreso);
        col.add(Box.createVerticalStrut(14));
        col.add(izq(Tema.lbl("ARMA TU COMBO · PASO " + actual.numero() + " DE 3", 11, Font.BOLD, Tema.ACENTO)));
        JLabel titulo = new JLabel(actual.titulo());
        titulo.setFont(Tema.titulo(24));
        titulo.setForeground(Tema.TEXTO);
        titulo.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        col.add(izq(titulo));
        col.add(izq(Tema.lbl("Selecciona una opción para continuar.", 12.5f, Font.PLAIN, Tema.SUAVE)));
        col.add(Box.createVerticalStrut(12));

        List<Producto> opciones = ctx.f.opcionesCombo(actual);
        Componentes.FilaOpcion[] filas = new Componentes.FilaOpcion[opciones.size()];
        Tema.Boton siguiente = Tema.boton(paso < 2 ? "Continuar  →" : "Agregar combo", Tema.Variante.PRIMARIO);
        siguiente.setEnabled(elegidos[paso] != null);
        if (opciones.isEmpty()) {
            col.add(izq(Tema.lbl("No hay productos disponibles para este paso.", 13, Font.PLAIN, Tema.ROJO)));
        }
        for (int i = 0; i < opciones.size(); i++) {
            Producto p = opciones.get(i);
            String tipo = ctx.f.tipoDe(p.getId());
            Componentes.FilaOpcion fila = new Componentes.FilaOpcion(p.getNombre(), Formato.moneda(p.getPrecio()),
                    ImagenCafe.focoProducto(p.getNombre(), tipo, ctx.f.categoriaDe(p.getId())),
                    ImagenCafe.imagen(p.getNombre()));
            fila.setSelected(elegidos[paso] != null && elegidos[paso].getId().equals(p.getId()));
            final int idx = i;
            fila.addActionListener(e -> {
                for (int k = 0; k < filas.length; k++) {
                    filas[k].setSelected(k == idx);
                }
                elegidos[paso] = p;
                siguiente.setEnabled(true);
            });
            filas[i] = fila;
            col.add(fila);
            col.add(Box.createVerticalStrut(7));
        }
        col.add(Box.createVerticalStrut(8));

        JPanel acciones = Tema.transparente(new FlowLayout(FlowLayout.LEFT, 10, 0));
        if (paso > 0) {
            Tema.Boton atras = Tema.boton("←  Atrás", Tema.Variante.BORDE);
            atras.addActionListener(e -> {
                paso--;
                redibujar();
            });
            acciones.add(atras);
        }
        siguiente.addActionListener(e -> {
            if (paso < 2) {
                paso++;
                redibujar();
            } else {
                terminar();
            }
        });
        acciones.add(siguiente);
        acciones.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(acciones);
        modal.cuerpo.removeAll();
        modal.cuerpo.add(col, BorderLayout.CENTER);
    }

    private void redibujar() {
        pintarPaso();
        modal.cuerpo.revalidate();
        modal.cuerpo.repaint();
        modal.pack();
    }

    private void terminar() {
        builder.setPlatoPrincipal(elegidos[0]).setBebida(elegidos[1]).setAcompanamiento(elegidos[2]);
        try {
            Combo combo = builder.build();     // valida los componentes obligatorios (RF-14)
            if (ctx.intentar(() -> ctx.f.agregarAlPedido(ctx.borrador, combo, 1))) {
                ctx.toast(combo.getNombre() + " agregado · " + Formato.moneda(combo.getPrecio()));
                ctx.refrescarPedido.run();
                modal.dispose();
            }
        } catch (IllegalStateException e) {
            ctx.toast("⚠ " + e.getMessage());
        }
    }

    private static JComponent izq(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }
}
