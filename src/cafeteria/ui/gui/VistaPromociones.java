package cafeteria.ui.gui;

import cafeteria.dominio.strategy.EstrategiaPromocion;

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
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Promociones (Strategy): crear, activar/desactivar y eliminar reglas por día y horario. */
class VistaPromociones extends VistaBase {

    private static final Pattern FORMATO = Pattern.compile("^(.*) \\(([^·]+) · (.*) · ([^·]+)\\)$");

    VistaPromociones(Contexto ctx) {
        super(ctx, new BorderLayout());
    }

    @Override
    void refrescar() {
        removeAll();
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(Tema.FONDO);
        col.setBorder(Tema.margen(6, 32, 36, 32));

        JPanel barra = Tema.transparente(new BorderLayout());
        barra.setAlignmentX(Component.LEFT_ALIGNMENT);
        barra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        JPanel t = Tema.transparente(null);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        JLabel h = new JLabel("Promociones activas");
        h.setFont(Tema.titulo(20));
        h.setForeground(Tema.TEXTO);
        h.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        t.add(h);
        t.add(Tema.lbl("Configura descuentos automáticos por día y horario. Se aplica la mejor promoción vigente a cada pedido.", 12, Font.PLAIN, Tema.SUAVE));
        barra.add(t, BorderLayout.WEST);
        Tema.Boton nueva = Tema.boton("＋  Nueva promoción", Tema.Variante.PRIMARIO);
        nueva.addActionListener(e -> nuevaPromocion());
        JPanel der = Tema.transparente(new java.awt.GridBagLayout());
        der.add(nueva);
        barra.add(der, BorderLayout.EAST);
        col.add(barra);
        col.add(Box.createVerticalStrut(14));

        JPanel grid = Tema.transparente(new GridLayout(0, 3, 14, 14));
        List<EstrategiaPromocion> promos = ctx.f.promociones();
        for (EstrategiaPromocion p : promos) {
            grid.add(tarjeta(p));
        }
        while (grid.getComponentCount() % 3 != 0 || grid.getComponentCount() == 0) {
            grid.add(Tema.transparente(null));
        }
        JPanel cont = Tema.transparente(new BorderLayout());
        cont.add(grid, BorderLayout.NORTH);
        cont.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(cont);
        if (promos.isEmpty()) {
            col.add(Tema.lbl("No hay promociones registradas.", 13, Font.PLAIN, Tema.SUAVE));
        }
        add(Tema.scroll(col), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel tarjeta(EstrategiaPromocion p) {
        String texto = p.toString();
        String nombre = texto;
        String descuento = "";
        String horario = "";
        String alcance = "";
        Matcher m = FORMATO.matcher(texto);
        if (m.matches()) {
            nombre = m.group(1);
            descuento = m.group(2).trim();
            horario = m.group(3).trim();
            alcance = m.group(4).trim();
        }
        boolean activa = ctx.f.promocionActiva(p);
        Tema.Tarjeta t = new Tema.Tarjeta(new BorderLayout());
        double[] foco = alcance.equals("Bebidas") ? ImagenCafe.foco("Bebida", "Bebidas")
                : alcance.equals("Platos") ? ImagenCafe.foco("Plato", "Platos") : ImagenCafe.foco(null, null);
        ImagenCafe.Foto foto = new ImagenCafe.Foto(foco, 200, 125, 16);
        if (!descuento.isEmpty()) {
            foto.insignia(descuento);
        }
        t.add(foto, BorderLayout.NORTH);
        JPanel c = Tema.transparente(null);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(Tema.margen(12, 16, 14, 16));
        JPanel fila1 = Tema.transparente(new BorderLayout());
        fila1.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila1.add(Tema.lbl(alcance.isEmpty() ? "Promoción" : alcance, 11, Font.BOLD, Tema.ACENTO), BorderLayout.WEST);
        Componentes.Interruptor sw = new Componentes.Interruptor(activa);
        sw.addActionListener(e -> {
            ctx.f.activarPromocion(p, sw.isSelected());
            refrescar();
        });
        fila1.add(sw, BorderLayout.EAST);
        c.add(fila1);
        c.add(Box.createVerticalStrut(4));
        JLabel nom = new JLabel("<html><body style='width:170px'>" + nombre.replace("<", "&lt;") + "</body></html>");
        nom.setFont(Tema.titulo(16));
        nom.setForeground(Tema.TEXTO);
        nom.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        nom.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(nom);
        JLabel hor = Tema.lbl("<html><body style='width:170px'>" + horario + "</body></html>", 12, Font.PLAIN, Tema.SUAVE);
        hor.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(hor);
        c.add(Box.createVerticalStrut(8));
        JPanel pie = Tema.transparente(new BorderLayout());
        pie.setAlignmentX(Component.LEFT_ALIGNMENT);
        pie.add(Tema.lbl(activa ? "● Activa ahora" : "○ Inactiva", 11.5f, Font.BOLD, activa ? Tema.VERDE : Tema.SUAVE), BorderLayout.WEST);
        Tema.Boton del = Tema.boton("Eliminar", Tema.Variante.TEXTO);
        del.setBorder(Tema.margen(2, 6, 2, 6));
        del.addActionListener(e -> {
            ctx.f.eliminarPromocion(p);
            refrescar();
        });
        pie.add(del, BorderLayout.EAST);
        c.add(pie);
        t.add(c, BorderLayout.CENTER);
        return t;
    }

    private void nuevaPromocion() {
        Componentes.Modal m = new Componentes.Modal(this, 480);
        JPanel col = Tema.transparente(null);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.add(izq(Tema.lbl("NUEVA PROMOCIÓN", 11, Font.BOLD, Tema.ACENTO)));
        JLabel tit = new JLabel("Regla de descuento");
        tit.setFont(Tema.titulo(24));
        tit.setForeground(Tema.TEXTO);
        tit.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        col.add(izq(tit));
        col.add(Box.createVerticalStrut(10));

        Componentes.Campo nombre = new Componentes.Campo("Nombre (ej. Lunes de café)", 24);
        Componentes.Campo pct = new Componentes.Campo("Porcentaje (1-100)", 8);
        Componentes.Campo desde = new Componentes.Campo("Hora inicio HH:mm", 8);
        Componentes.Campo hasta = new Componentes.Campo("Hora fin HH:mm", 8);
        Componentes.Campo vigDesde = new Componentes.Campo("Vigente desde AAAA-MM-DD (opcional)", 12);
        Componentes.Campo vigHasta = new Componentes.Campo("Vigente hasta AAAA-MM-DD (opcional)", 12);
        JComboBox<String> tipo = new JComboBox<>(new String[]{"Por día de la semana", "Por franja horaria"});
        JComboBox<String> alcance = new JComboBox<>(ctx.f.alcancesPromocion().toArray(new String[0]));
        String[] nd = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        javax.swing.JToggleButton[] dias = new javax.swing.JToggleButton[7];
        JPanel pd = Tema.transparente(new FlowLayout(FlowLayout.LEFT, 4, 0));
        for (int i = 0; i < 7; i++) {
            dias[i] = new javax.swing.JToggleButton(nd[i]);
            dias[i].setFocusPainted(false);
            pd.add(dias[i]);
        }
        col.add(izq(nombre));
        col.add(Box.createVerticalStrut(8));
        col.add(izq(tipo));
        col.add(Box.createVerticalStrut(8));
        col.add(izq(Tema.lbl("Días (en 'Por franja horaria', ninguno = todos los días)", 11.5f, Font.PLAIN, Tema.SUAVE)));
        col.add(izq(pd));
        col.add(Box.createVerticalStrut(8));
        JPanel horas = Tema.transparente(new GridLayout(1, 3, 8, 0));
        horas.add(pct);
        horas.add(desde);
        horas.add(hasta);
        col.add(izq(horas));
        col.add(Box.createVerticalStrut(8));
        col.add(izq(Tema.lbl("Aplica a", 11.5f, Font.PLAIN, Tema.SUAVE)));
        col.add(izq(alcance));
        col.add(Box.createVerticalStrut(8));
        JPanel vig = Tema.transparente(new GridLayout(1, 2, 8, 0));
        vig.add(vigDesde);
        vig.add(vigHasta);
        col.add(izq(vig));
        col.add(Box.createVerticalStrut(14));
        Tema.Boton crear = Tema.boton("Crear promoción", Tema.Variante.PRIMARIO);
        crear.addActionListener(e -> {
            try {
                Set<DayOfWeek> sel = EnumSet.noneOf(DayOfWeek.class);
                for (int i = 0; i < 7; i++) {
                    if (dias[i].isSelected()) {
                        sel.add(DayOfWeek.of(i + 1));
                    }
                }
                double porcentaje = Double.parseDouble(pct.getText().trim().replace(',', '.'));
                LocalDate vd = vigDesde.getText().trim().isEmpty() ? null : LocalDate.parse(vigDesde.getText().trim());
                LocalDate vh = vigHasta.getText().trim().isEmpty() ? null : LocalDate.parse(vigHasta.getText().trim());
                String nom = nombre.getText().trim().isEmpty() ? "Promoción" : nombre.getText().trim();
                if (tipo.getSelectedIndex() == 0) {
                    ctx.f.crearPromocionPorDia(nom, sel, porcentaje, (String) alcance.getSelectedItem(), vd, vh);
                } else {
                    ctx.f.crearPromocionPorHorario(nom, sel, LocalTime.parse(desde.getText().trim()), LocalTime.parse(hasta.getText().trim()),
                            porcentaje, (String) alcance.getSelectedItem(), vd, vh);
                }
                ctx.toast("Promoción creada");
                m.dispose();
                refrescar();
            } catch (RuntimeException ex) {
                ctx.toast("⚠ Revisa los datos: " + ex.getMessage());
            }
        });
        col.add(izq(crear));
        m.cuerpo.add(col, BorderLayout.CENTER);
        m.abrir();
    }

    private static javax.swing.JComponent izq(javax.swing.JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (!(c instanceof JLabel)) {
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        }
        return c;
    }
}
