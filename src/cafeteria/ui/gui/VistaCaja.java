package cafeteria.ui.gui;

import cafeteria.repository.RegistroVenta;
import cafeteria.service.ReporteVentas;
import cafeteria.util.Formato;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.List;

/** Caja: estado del turno, movimientos y cierre con reporte (RF-29). */
class VistaCaja extends VistaBase {

    VistaCaja(Contexto ctx) {
        super(ctx, new BorderLayout());
    }

    static void verReporte(Component padre, String titulo, ReporteVentas r) {
        JTextArea area = new JTextArea(r.toString());
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setBorder(Tema.margen(10, 12, 10, 12));
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(520, 380));
        JOptionPane.showMessageDialog(padre, sp, titulo, JOptionPane.PLAIN_MESSAGE);
    }

    @Override
    void refrescar() {
        removeAll();
        boolean abierta = ctx.f.cajaAbierta();
        ReporteVentas r = ctx.f.reporteTurnoActual();
        double efectivo = r.ingresosEn("Efectivo");

        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(Tema.FONDO);
        col.setBorder(Tema.margen(6, 32, 36, 32));

        Tema.Tarjeta banner = new Tema.Tarjeta(new BorderLayout(), 16, Tema.ROSA, null);
        banner.setBorder(Tema.margen(18, 22, 18, 22));
        JPanel izq = Tema.transparente(null);
        izq.setLayout(new BoxLayout(izq, BoxLayout.Y_AXIS));
        izq.add(Tema.lbl((abierta ? "●  " : "○  ") + "ESTADO DE CAJA", 10.5f, Font.BOLD, abierta ? Tema.VERDE : Tema.SUAVE));
        JLabel h = new JLabel(abierta ? "Turno abierto" : "Caja cerrada");
        h.setFont(Tema.titulo(22));
        h.setForeground(Tema.TEXTO);
        h.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        izq.add(h);
        izq.add(Tema.lbl("Pagos desde el último cierre: " + r.numeroPedidos, 12, Font.PLAIN, Tema.SUAVE));
        banner.add(izq, BorderLayout.WEST);
        Tema.Boton accion = Tema.boton(abierta ? "Realizar cierre de caja" : "Abrir caja", Tema.Variante.PRIMARIO);
        accion.addActionListener(e -> {
            if (abierta) {
                if (JOptionPane.showConfirmDialog(this, "¿Cerrar la caja y consolidar los pagos del turno?", "Cierre de caja",
                        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    try {
                        ReporteVentas cierre = ctx.f.cerrarCaja();
                        ctx.toast("Cierre registrado y reporte diario generado.");
                        ctx.refrescarTodo.run();
                        verReporte(this, "Cierre de caja", cierre);
                    } catch (RuntimeException ex) {
                        ctx.toast("⚠ " + ex.getMessage());
                    }
                }
            } else {
                ctx.f.abrirCaja();
                ctx.toast("Caja abierta correctamente.");
                ctx.refrescarTodo.run();
            }
        });
        JPanel der = Tema.transparente(new java.awt.GridBagLayout());
        der.add(accion);
        banner.add(der, BorderLayout.EAST);
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        col.add(banner);
        col.add(Box.createVerticalStrut(14));

        JPanel k = Tema.transparente(new GridLayout(1, 4, 12, 0));
        k.setAlignmentX(Component.LEFT_ALIGNMENT);
        k.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        k.add(VistaPedidos.kpi("Ventas netas", Formato.moneda(r.ingresos), Formato.plural(r.numeroPedidos, "pedido cobrado", "pedidos cobrados")));
        k.add(VistaPedidos.kpi("Efectivo", Formato.moneda(efectivo), "Pagos en efectivo"));
        k.add(VistaPedidos.kpi("Pagos digitales", Formato.moneda(r.ingresos - efectivo), "Tarjeta y otros métodos"));
        k.add(VistaPedidos.kpi("Descuentos", Formato.moneda(r.descuentos), "Promociones aplicadas"));
        col.add(k);
        col.add(Box.createVerticalStrut(14));

        JPanel dos = Tema.transparente(new GridLayout(1, 2, 14, 0));
        dos.setAlignmentX(Component.LEFT_ALIGNMENT);

        Tema.Tarjeta mov = new Tema.Tarjeta(new BorderLayout());
        JPanel lista = Tema.transparente(null);
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setBorder(Tema.margen(18, 22, 18, 22));
        JLabel t1 = new JLabel("Movimientos recientes");
        t1.setFont(Tema.titulo(18));
        t1.setForeground(Tema.TEXTO);
        t1.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        lista.add(t1);
        lista.add(Tema.lbl("Pagos del turno actual", 12, Font.PLAIN, Tema.SUAVE));
        lista.add(Box.createVerticalStrut(10));
        List<RegistroVenta> ventas = ctx.f.movimientosRecientes(7);
        if (ventas.isEmpty()) {
            lista.add(Tema.lbl("Aún no hay pagos en este turno.", 12.5f, Font.PLAIN, Tema.SUAVE));
        }
        for (RegistroVenta v : ventas) {
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
            fila.add(Tema.lbl("↗", 16, Font.BOLD, Tema.VERDE), BorderLayout.WEST);
            JPanel txt = Tema.transparente(null);
            txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
            txt.add(Tema.lbl("Venta · " + v.metodoPago, 12.5f, Font.BOLD, Tema.TEXTO));
            txt.add(Tema.lbl("#" + v.idPedido + " · " + Formato.hora(v.fechaHora.toLocalTime()), 11, Font.PLAIN, Tema.SUAVE));
            fila.add(txt, BorderLayout.CENTER);
            fila.add(Tema.lbl("+" + Formato.moneda(v.total), 13, Font.BOLD, Tema.VERDE), BorderLayout.EAST);
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, fila.getPreferredSize().height));
            lista.add(fila);
        }
        mov.add(lista, BorderLayout.NORTH);
        dos.add(mov);

        Tema.Tarjeta res = new Tema.Tarjeta(new BorderLayout());
        JPanel c = Tema.transparente(null);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(Tema.margen(18, 22, 18, 22));
        JLabel t2 = new JLabel("Resumen del cierre");
        t2.setFont(Tema.titulo(18));
        t2.setForeground(Tema.TEXTO);
        t2.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        c.add(t2);
        c.add(Tema.lbl("Consolidado esperado al finalizar el turno", 12, Font.PLAIN, Tema.SUAVE));
        c.add(Box.createVerticalStrut(12));
        c.add(linea("Ventas brutas", Formato.moneda(r.subtotal), false));
        c.add(linea("Descuentos", "-" + Formato.moneda(r.descuentos), false));
        c.add(linea("Ingresos netos", Formato.moneda(r.ingresos), true));
        r.ingresosPorMetodo.forEach((m, v) -> c.add(linea("   · " + m, Formato.moneda(v), false)));
        c.add(linea("Ticket promedio", Formato.moneda(r.ticketPromedio()), false));
        c.add(Box.createVerticalStrut(14));
        Tema.Boton prev = Tema.boton("Generar reporte preliminar", Tema.Variante.BORDE);
        prev.setAlignmentX(Component.LEFT_ALIGNMENT);
        prev.addActionListener(e -> verReporte(this, "Reporte preliminar del turno", ctx.f.reporteTurnoActual()));
        c.add(prev);
        res.add(c, BorderLayout.NORTH);
        dos.add(res);
        dos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10000));
        col.add(dos);
        add(Tema.scroll(col), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private static JPanel linea(String a, String b, boolean total) {
        JPanel p = Tema.transparente(new BorderLayout());
        p.setBorder(Tema.margen(5, 0, 5, 0));
        p.add(Tema.lbl(a, total ? 14 : 12.5f, total ? Font.BOLD : Font.PLAIN, total ? Tema.TEXTO : Tema.SUAVE), BorderLayout.WEST);
        p.add(Tema.lbl(b, total ? 16 : 12.5f, Font.BOLD, Tema.TEXTO), BorderLayout.EAST);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }
}
