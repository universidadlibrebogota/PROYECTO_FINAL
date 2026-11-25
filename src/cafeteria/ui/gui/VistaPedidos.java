package cafeteria.ui.gui;

import cafeteria.dominio.EstadoPedido;
import cafeteria.dominio.Pedido;
import cafeteria.repository.RegistroPedido;
import cafeteria.util.Formato;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Seguimiento de pedidos: estados, notificaciones automáticas (Observer) y cobro. */
class VistaPedidos extends VistaBase {

    private EstadoPedido filtro;   // null = todos

    VistaPedidos(Contexto ctx) {
        super(ctx, new BorderLayout());
    }

    private boolean rol(String... roles) {
        return Arrays.asList(roles).contains(ctx.rol);
    }

    @Override
    void refrescar() {
        removeAll();
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(Tema.FONDO);
        col.setBorder(Tema.margen(6, 32, 36, 32));

        List<Pedido> pedidos = ctx.f.pedidosEnviados();
        col.add(tabs(pedidos));
        col.add(Box.createVerticalStrut(16));
        col.add(kpis(pedidos));
        col.add(Box.createVerticalStrut(16));

        Tema.Tarjeta tabla = new Tema.Tarjeta(new BorderLayout());
        tabla.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel cab = Tema.transparente(new BorderLayout());
        cab.setBorder(Tema.margen(18, 22, 8, 22));
        JPanel t = Tema.transparente(null);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        JLabel h = new JLabel("Pedidos recientes");
        h.setFont(Tema.titulo(18));
        h.setForeground(Tema.TEXTO);
        h.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        t.add(h);
        t.add(Tema.lbl("Los cambios notifican automáticamente a cocina, caja y mesero.", 12, Font.PLAIN, Tema.SUAVE));
        cab.add(t, BorderLayout.WEST);
        cab.add(new Componentes.Pildora("● EN VIVO", Tema.VERDE, Tema.VERDE_BG), BorderLayout.EAST);
        tabla.add(cab, BorderLayout.NORTH);

        JPanel filas = Tema.transparente(null);
        filas.setLayout(new BoxLayout(filas, BoxLayout.Y_AXIS));
        filas.setBorder(Tema.margen(0, 22, 12, 22));
        filas.add(fila(null, null, true));
        Set<Integer> enSesion = new HashSet<>();
        int mostrados = 0;
        for (Pedido p : pedidos) {
            enSesion.add(p.getIdPedido());
            if (filtro == null || p.getEstado() == filtro) {
                filas.add(fila(p, null, false));
                mostrados++;
            }
        }
        for (RegistroPedido r : ctx.f.historialPedidos()) {
            if (!enSesion.contains(r.id) && (filtro == null || filtro.name().equals(r.estado))) {
                filas.add(fila(null, r, false));
                mostrados++;
            }
        }
        if (mostrados == 0) {
            JLabel vacio = Tema.lbl("No hay pedidos en esta vista.", 13, Font.PLAIN, Tema.SUAVE);
            vacio.setBorder(Tema.margen(22, 0, 14, 0));
            filas.add(vacio);
        }
        tabla.add(filas, BorderLayout.CENTER);
        col.add(tabla);

        if (ctx.rol.equals("Cocina")) {
            col.add(Box.createVerticalStrut(14));
            Tema.Tarjeta tip = new Tema.Tarjeta(new FlowLayout(FlowLayout.LEFT, 14, 12), 14, Tema.ROSA, null);
            tip.setAlignmentX(Component.LEFT_ALIGNMENT);
            tip.add(new JLabel(Iconos.de("plate", 24, Tema.ACENTO_OSC)));
            tip.add(Tema.lbl("<html><b>Vista de cocina activa</b><br>Prioriza los pedidos por tiempo y marca “Listo” cuando termines.</html>", 12.5f, Font.PLAIN, Tema.TEXTO));
            tip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
            col.add(tip);
        }
        add(Tema.scroll(col), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel tabs(List<Pedido> pedidos) {
        JPanel p = Tema.transparente(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        p.add(tab("Todos", null, pedidos.size()));
        for (EstadoPedido e : EstadoPedido.values()) {
            int n = 0;
            for (Pedido x : pedidos) {
                if (x.getEstado() == e) {
                    n++;
                }
            }
            p.add(tab(nombre(e), e, n));
        }
        return p;
    }

    private Tema.Boton tab(String texto, EstadoPedido e, int n) {
        boolean activo = filtro == e;
        Tema.Boton b = Tema.boton(texto + "  " + n, activo ? Tema.Variante.PRIMARIO : Tema.Variante.BORDE);
        b.arco(20);
        b.addActionListener(ev -> {
            filtro = e;
            refrescar();
        });
        return b;
    }

    private JPanel kpis(List<Pedido> pedidos) {
        int activos = 0;
        int porCobrar = 0;
        for (Pedido p : pedidos) {
            EstadoPedido e = p.getEstado();
            if (e == EstadoPedido.PENDIENTE || e == EstadoPedido.EN_PREPARACION || e == EstadoPedido.LISTO_PARA_ENTREGAR) {
                activos++;
            }
            if ((e == EstadoPedido.LISTO_PARA_ENTREGAR || e == EstadoPedido.ENTREGADO) && !ctx.f.estaPagado(p)) {
                porCobrar++;
            }
        }
        JPanel g = Tema.transparente(new GridLayout(1, 3, 12, 0));
        g.setAlignmentX(Component.LEFT_ALIGNMENT);
        g.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        g.add(kpi("Pedidos activos", String.valueOf(activos), "En este momento"));
        g.add(kpi("Por cobrar", String.valueOf(porCobrar), "Listos o entregados sin pago"));
        g.add(kpi("Ventas del turno", Formato.moneda(ctx.f.reporteTurnoActual().ingresos), Formato.plural(ctx.f.reporteTurnoActual().numeroPedidos, "pedido cobrado", "pedidos cobrados")));
        return g;
    }

    static JPanel kpi(String titulo, String valor, String pie) {
        Tema.Tarjeta k = new Tema.Tarjeta(new BorderLayout());
        JPanel c = Tema.transparente(null);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(Tema.margen(16, 18, 16, 18));
        c.add(Tema.lbl(titulo, 12, Font.PLAIN, Tema.SUAVE));
        c.add(Tema.lbl(valor, 24, Font.BOLD, Tema.TEXTO));
        c.add(Tema.lbl(pie, 11, Font.PLAIN, Tema.SUAVE));
        k.add(c, BorderLayout.CENTER);
        return k;
    }

    static String nombre(EstadoPedido e) {
        switch (e) {
            case PENDIENTE: return "Pendiente";
            case EN_PREPARACION: return "En preparación";
            case LISTO_PARA_ENTREGAR: return "Listo";
            case ENTREGADO: return "Entregado";
            default: return "Cancelado";
        }
    }

    private Componentes.Pildora pildora(String estado) {
        EstadoPedido e = EstadoPedido.valueOf(estado);
        switch (e) {
            case PENDIENTE: return new Componentes.Pildora(nombre(e), Tema.ROJO, Tema.ROJO_BG);
            case EN_PREPARACION: return new Componentes.Pildora(nombre(e), Tema.AMBAR, Tema.AMBAR_BG);
            case LISTO_PARA_ENTREGAR: return new Componentes.Pildora(nombre(e), Tema.VERDE, Tema.VERDE_BG);
            case ENTREGADO: return new Componentes.Pildora(nombre(e), Tema.MORADO, Tema.MORADO_BG);
            default: return new Componentes.Pildora(nombre(e), Tema.SUAVE, new Color(0xEFEAE8));
        }
    }

    private JPanel fila(Pedido p, RegistroPedido r, boolean encabezado) {
        JPanel f = new JPanel(new GridBagLayout());
        f.setBackground(Color.WHITE);
        f.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE), Tema.margen(12, 0, 12, 0)));
        double[] pesos = {1.0, 2.6, 0.8, 1.2, 2.4};
        Component[] celdas = new Component[5];
        if (encabezado) {
            String[] t = {"Pedido", "Detalle", "Hora", "Estado", "Acción"};
            for (int i = 0; i < 5; i++) {
                celdas[i] = Tema.lbl(t[i], 11, Font.BOLD, Tema.SUAVE);
            }
        } else if (p != null) {
            String texto = p.toString();
            celdas[0] = doble("#" + p.getIdPedido(), p.getEstrategiaPromocion().toString().equals("Sin promoción") ? "" : "con promoción");
            celdas[1] = doble(texto.substring(texto.indexOf(": ") + 2), Formato.moneda(p.calcularTotal()) + (ctx.f.estaPagado(p) ? " · pagado" : ""));
            celdas[2] = Tema.lbl(Formato.hora(ctx.f.fechaDelPedido(p).toLocalTime()), 12.5f, Font.PLAIN, Tema.TEXTO);
            celdas[3] = pildora(p.getEstado().name());
            celdas[4] = acciones(p);
        } else {
            celdas[0] = doble("#" + r.id, "sesión anterior");
            celdas[1] = doble(r.detalle, Formato.moneda(r.total) + (r.pagado ? " · pagado" : ""));
            celdas[2] = Tema.lbl(Formato.hora(r.fecha.toLocalTime()), 12.5f, Font.PLAIN, Tema.SUAVE);
            celdas[3] = pildora(r.estado);
            celdas[4] = Tema.lbl("—", 12, Font.PLAIN, Tema.SUAVE);
        }
        for (int i = 0; i < 5; i++) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = i;
            c.weightx = pesos[i];
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new java.awt.Insets(0, 0, 0, 10);
            f.add(celdas[i], c);
        }
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, f.getPreferredSize().height));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    private static JPanel doble(String a, String b) {
        JPanel p = Tema.transparente(null);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = Tema.lbl("<html><body style='width:230px'>" + a.replace("<", "&lt;") + "</body></html>", 12.5f, Font.BOLD, Tema.TEXTO);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        if (!b.isEmpty()) {
            p.add(Tema.lbl(b, 11, Font.PLAIN, Tema.SUAVE));
        }
        return p;
    }

    private JPanel acciones(Pedido p) {
        JPanel a = Tema.transparente(new FlowLayout(FlowLayout.LEFT, 6, 0));
        EstadoPedido e = p.getEstado();
        if (e == EstadoPedido.PENDIENTE && rol("Cocina", "Administrador")) {
            a.add(accion("Preparar →", Tema.Variante.PRIMARIO, () -> ctx.f.cambiarEstado(p, EstadoPedido.EN_PREPARACION)));
        } else if (e == EstadoPedido.EN_PREPARACION && rol("Cocina", "Administrador")) {
            a.add(accion("Marcar listo →", Tema.Variante.PRIMARIO, () -> ctx.f.cambiarEstado(p, EstadoPedido.LISTO_PARA_ENTREGAR)));
        } else if (e == EstadoPedido.LISTO_PARA_ENTREGAR && rol("Mesero", "Cajera", "Administrador")) {
            a.add(accion("Entregar →", Tema.Variante.PRIMARIO, () -> ctx.f.cambiarEstado(p, EstadoPedido.ENTREGADO)));
        }
        if ((e == EstadoPedido.LISTO_PARA_ENTREGAR || e == EstadoPedido.ENTREGADO) && !ctx.f.estaPagado(p) && rol("Cajera", "Administrador")) {
            Tema.Boton cobrar = Tema.boton("Cobrar", Tema.Variante.SUAVE);
            cobrar.setBorder(Tema.margen(6, 10, 6, 10));
            cobrar.addActionListener(ev -> cobrar(p));
            a.add(cobrar);
        }
        if ((e == EstadoPedido.PENDIENTE || e == EstadoPedido.EN_PREPARACION) && rol("Cajera", "Mesero", "Administrador")) {
            a.add(accion("Cancelar", Tema.Variante.BORDE, () -> ctx.f.cancelarPedido(p)));
        }
        if (a.getComponentCount() == 0) {
            a.add(Tema.lbl("—", 12, Font.PLAIN, Tema.SUAVE));
        }
        return a;
    }

    private Tema.Boton accion(String texto, Tema.Variante v, Runnable r) {
        Tema.Boton b = Tema.boton(texto, v);
        b.setBorder(Tema.margen(6, 10, 6, 10));
        b.setFont(Tema.fuente(Font.BOLD, 11.5f));
        b.addActionListener(e -> {
            ctx.intentar(r);
            ctx.refrescarTodo.run();
        });
        return b;
    }

    private void cobrar(Pedido p) {
        Object[] metodos = ctx.f.metodosPago().toArray();
        Object m = JOptionPane.showInputDialog(this, "Total a cobrar: " + Formato.moneda(p.calcularTotal()) + "\nMétodo de pago (simulado):",
                "Cobrar pedido #" + p.getIdPedido(), JOptionPane.PLAIN_MESSAGE, null, metodos, metodos[0]);
        if (m != null) {
            ctx.intentar(() -> {
                ctx.f.pagarPedido(p, m.toString());
                ctx.toast("Pago aprobado (simulado): " + Formato.moneda(p.calcularTotal()) + " · " + m);
            });
            ctx.refrescarTodo.run();
        }
    }
}
