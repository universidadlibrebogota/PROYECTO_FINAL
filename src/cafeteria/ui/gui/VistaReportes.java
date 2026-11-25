package cafeteria.ui.gui;

import cafeteria.service.ReporteVentas;
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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/** Reportes de ventas (RF-30): KPIs, ingresos por día, productos más vendidos, métodos y promociones. */
class VistaReportes extends VistaBase {

    private int dias = 7;

    VistaReportes(Contexto ctx) {
        super(ctx, new BorderLayout());
    }

    @Override
    void refrescar() {
        removeAll();
        LocalDateTime ahora = ctx.f.ahora();
        LocalDateTime desde = dias == 1 ? ahora.toLocalDate().atStartOfDay() : ahora.toLocalDate().minusDays(dias - 1).atStartOfDay();
        ReporteVentas r = ctx.f.reporte(desde, ahora);

        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(Tema.FONDO);
        col.setBorder(Tema.margen(6, 32, 36, 32));

        JPanel barra = Tema.transparente(new BorderLayout());
        barra.setAlignmentX(Component.LEFT_ALIGNMENT);
        barra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        JPanel chips = Tema.transparente(new FlowLayout(FlowLayout.LEFT, 8, 0));
        int[] opciones = {1, 7, 30};
        String[] nombres = {"Hoy", "Últimos 7 días", "Últimos 30 días"};
        for (int i = 0; i < 3; i++) {
            final int d = opciones[i];
            Tema.Boton b = Tema.boton(nombres[i], dias == d ? Tema.Variante.PRIMARIO : Tema.Variante.BORDE);
            b.arco(20);
            b.addActionListener(e -> {
                dias = d;
                refrescar();
            });
            chips.add(b);
        }
        barra.add(chips, BorderLayout.WEST);
        Tema.Boton completo = Tema.boton("Ver reporte completo", Tema.Variante.SUAVE);
        completo.addActionListener(e -> VistaCaja.verReporte(this, "Reporte de ventas", r));
        JPanel der = Tema.transparente(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        der.add(completo);
        barra.add(der, BorderLayout.EAST);
        col.add(barra);
        col.add(Box.createVerticalStrut(14));

        JPanel k = Tema.transparente(new GridLayout(1, 4, 12, 0));
        k.setAlignmentX(Component.LEFT_ALIGNMENT);
        k.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        k.add(VistaPedidos.kpi("Ventas totales", Formato.moneda(r.ingresos), "Descuentos: " + Formato.moneda(r.descuentos)));
        k.add(VistaPedidos.kpi("Pedidos", String.valueOf(r.numeroPedidos), "Pedidos cobrados"));
        k.add(VistaPedidos.kpi("Ticket promedio", Formato.moneda(r.ticketPromedio()), "Por pedido"));
        String fav = r.productoFavorito();
        k.add(VistaPedidos.kpi("Producto favorito", fav == null ? "—" : fav,
                fav == null ? "Sin ventas" : Formato.plural(r.unidadesPorProducto.get(fav), "unidad vendida", "unidades vendidas")));
        col.add(k);
        col.add(Box.createVerticalStrut(14));

        double[] diario = new double[7];
        String[] etiquetas = new String[7];
        double max = 0;
        for (int i = 0; i < 7; i++) {
            LocalDate dia = ahora.toLocalDate().minusDays(6 - i);
            diario[i] = ctx.f.reporte(dia.atStartOfDay(), dia.plusDays(1).atStartOfDay().minusNanos(1)).ingresos;
            etiquetas[i] = new String[]{"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"}[dia.getDayOfWeek().getValue() - 1];
            max = Math.max(max, diario[i]);
        }
        double sumaSemana = 0;
        for (double v : diario) {
            sumaSemana += v;
        }

        JPanel dos = Tema.transparente(new GridLayout(1, 2, 14, 0));
        dos.setAlignmentX(Component.LEFT_ALIGNMENT);
        Tema.Tarjeta graf = new Tema.Tarjeta(new BorderLayout());
        JPanel gc = Tema.transparente(new BorderLayout());
        gc.setBorder(Tema.margen(18, 22, 18, 22));
        JPanel gt = Tema.transparente(new BorderLayout());
        JPanel gtt = Tema.transparente(null);
        gtt.setLayout(new BoxLayout(gtt, BoxLayout.Y_AXIS));
        JLabel h = new JLabel("Ventas de la semana");
        h.setFont(Tema.titulo(18));
        h.setForeground(Tema.TEXTO);
        h.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        gtt.add(h);
        gtt.add(Tema.lbl("Ingresos diarios (últimos 7 días)", 12, Font.PLAIN, Tema.SUAVE));
        gt.add(gtt, BorderLayout.WEST);
        gt.add(Tema.lbl(Formato.moneda(sumaSemana), 18, Font.BOLD, Tema.TEXTO), BorderLayout.EAST);
        gc.add(gt, BorderLayout.NORTH);
        gc.add(new Barras(diario, etiquetas, max), BorderLayout.CENTER);
        graf.add(gc, BorderLayout.CENTER);
        graf.setPreferredSize(new Dimension(400, 300));
        dos.add(graf);

        Tema.Tarjeta top = new Tema.Tarjeta(new BorderLayout());
        JPanel tc = Tema.transparente(null);
        tc.setLayout(new BoxLayout(tc, BoxLayout.Y_AXIS));
        tc.setBorder(Tema.margen(18, 22, 18, 22));
        JLabel h2 = new JLabel("Productos más vendidos");
        h2.setFont(Tema.titulo(18));
        h2.setForeground(Tema.TEXTO);
        h2.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        tc.add(h2);
        tc.add(Tema.lbl("Por unidades en el periodo", 12, Font.PLAIN, Tema.SUAVE));
        tc.add(Box.createVerticalStrut(8));
        int n = 0;
        if (r.unidadesPorProducto.isEmpty()) {
            tc.add(Tema.lbl("Sin ventas en el periodo.", 12.5f, Font.PLAIN, Tema.SUAVE));
        }
        for (Map.Entry<String, Integer> e : r.unidadesPorProducto.entrySet()) {
            if (++n > 5) {
                break;
            }
            JPanel fila = Tema.transparente(new BorderLayout(10, 0));
            fila.setBorder(Tema.margen(6, 0, 6, 0));
            fila.add(Tema.lbl(n + "", 12, Font.BOLD, Tema.ACENTO), BorderLayout.WEST);
            JPanel txt = Tema.transparente(null);
            txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
            txt.add(Tema.lbl(e.getKey(), 12.5f, Font.BOLD, Tema.TEXTO));
            txt.add(Tema.lbl(Formato.plural(e.getValue(), "vendido", "vendidos"), 11, Font.PLAIN, Tema.SUAVE));
            fila.add(txt, BorderLayout.CENTER);
            fila.add(Tema.lbl(Formato.moneda(r.ventasPorProducto.get(e.getKey())), 12.5f, Font.BOLD, Tema.TEXTO), BorderLayout.EAST);
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, fila.getPreferredSize().height));
            tc.add(fila);
        }
        tc.add(Box.createVerticalStrut(10));
        tc.add(Tema.lbl("Métodos de pago", 12, Font.BOLD, Tema.TEXTO));
        if (r.ingresosPorMetodo.isEmpty()) {
            tc.add(Tema.lbl("—", 12, Font.PLAIN, Tema.SUAVE));
        }
        r.ingresosPorMetodo.forEach((m, v) -> tc.add(Tema.lbl(m + ": " + Formato.moneda(v), 12, Font.PLAIN, Tema.SUAVE)));
        tc.add(Box.createVerticalStrut(8));
        tc.add(Tema.lbl("Promociones aplicadas", 12, Font.BOLD, Tema.TEXTO));
        if (r.promocionesAplicadas.isEmpty()) {
            tc.add(Tema.lbl("—", 12, Font.PLAIN, Tema.SUAVE));
        }
        r.promocionesAplicadas.forEach((m, v) -> tc.add(Tema.lbl("<html><body style='width:300px'>" + m.replace("<", "&lt;") + ": " + v + "×</body></html>", 11.5f, Font.PLAIN, Tema.SUAVE)));
        top.add(tc, BorderLayout.NORTH);
        dos.add(top);
        dos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10000));
        col.add(dos);
        add(Tema.scroll(col), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /** Gráfico de barras sencillo. */
    private static class Barras extends JComponent {
        private final double[] valores;
        private final String[] etiquetas;
        private final double max;

        Barras(double[] valores, String[] etiquetas, double max) {
            this.valores = valores;
            this.etiquetas = etiquetas;
            this.max = max <= 0 ? 1 : max;
            setPreferredSize(new Dimension(300, 200));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            int w = getWidth();
            int h = getHeight();
            int base = h - 24;
            int arriba = 14;
            g.setFont(Tema.fuente(Font.PLAIN, 10.5f));
            FontMetrics fm = g.getFontMetrics();
            for (int i = 0; i <= 4; i++) {
                int y = arriba + (base - arriba) * i / 4;
                g.setColor(Tema.BORDE);
                g.drawLine(48, y, w, y);
                g.setColor(Tema.SUAVE);
                String t = "$" + Math.round(max * (4 - i) / 4 / 1000) + "k";
                g.drawString(t, 44 - fm.stringWidth(t), y + 4);
            }
            int ancho = (w - 56) / valores.length;
            for (int i = 0; i < valores.length; i++) {
                int alto = (int) ((base - arriba) * valores[i] / max);
                int x = 56 + i * ancho + ancho / 6;
                g.setColor(i == valores.length - 1 ? Tema.ACENTO_OSC : new Color(0xE3B9BC));
                g.fillRoundRect(x, base - alto, ancho * 2 / 3, Math.max(alto, 2), 8, 8);
                g.setColor(Tema.SUAVE);
                g.drawString(etiquetas[i], x + (ancho * 2 / 3 - fm.stringWidth(etiquetas[i])) / 2, h - 6);
            }
            g.dispose();
        }
    }
}
