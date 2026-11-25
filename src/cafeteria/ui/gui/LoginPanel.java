package cafeteria.ui.gui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.util.function.BiConsumer;

/** Pantalla de ingreso: selección de perfil (como en la referencia). */
class LoginPanel extends JPanel {

    private static final String[] ROLES = {"Cliente", "Cajera", "Mesero", "Cocina", "Administrador"};
    private String rol = "Cajera";

    LoginPanel(BiConsumer<String, String> alEntrar) {
        super(new GridLayout(1, 2));
        setBackground(Color.WHITE);

        JPanel imagen = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Tema.suavizar(g);
                ImagenCafe.pintar(g, 0, 0, getWidth(), getHeight(), 0.5, 0.5, 1.15);
                g.setPaint(new GradientPaint(0, 0, new Color(75, 38, 38, 20), 0, getHeight(), new Color(75, 38, 38, 190)));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.dispose();
            }
        };
        JPanel cita = Tema.transparente(null);
        cita.setLayout(new BoxLayout(cita, BoxLayout.Y_AXIS));
        cita.setBorder(Tema.margen(0, 48, 56, 48));
        JLabel marca = Tema.lbl("☕  Rosetta", 26, Font.BOLD, Color.WHITE);
        marca.setFont(Tema.titulo(26));
        JLabel frase = new JLabel("<html>Momentos que<br>saben mejor.</html>");
        frase.setFont(Tema.titulo(38));
        frase.setForeground(Color.WHITE);
        frase.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        JLabel sub = Tema.lbl("Pedidos simples, servicio excepcional.", 14, Font.PLAIN, new Color(255, 255, 255, 220));
        cita.add(marca);
        cita.add(Box.createVerticalStrut(18));
        cita.add(frase);
        cita.add(Box.createVerticalStrut(8));
        cita.add(sub);
        imagen.add(cita, BorderLayout.SOUTH);
        add(imagen);

        JPanel derecha = new JPanel(new java.awt.GridBagLayout());
        derecha.setBackground(Color.WHITE);
        JPanel form = Tema.transparente(null);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(400, 560));

        form.add(izq(Tema.lbl("BIENVENIDO A ROSETTA", 11, Font.BOLD, Tema.ACENTO)));
        JLabel h1 = new JLabel("Iniciar sesión");
        h1.setFont(Tema.titulo(32));
        h1.setForeground(Tema.TEXTO);
        h1.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        form.add(izq(h1));
        form.add(Box.createVerticalStrut(6));
        form.add(izq(Tema.lbl("Selecciona tu perfil para entrar al sistema.", 13, Font.PLAIN, Tema.SUAVE)));
        form.add(Box.createVerticalStrut(18));

        JPanel grid = Tema.transparente(new GridLayout(2, 3, 10, 10));
        java.util.List<JToggleButton> botones = new java.util.ArrayList<>();
        for (String r : ROLES) {
            JToggleButton b = new JToggleButton(r) {
                @Override
                protected void paintComponent(Graphics g0) {
                    Graphics2D g = (Graphics2D) g0.create();
                    Tema.suavizar(g);
                    g.setColor(isSelected() ? Tema.ROSA : Color.WHITE);
                    g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                    g.setColor(isSelected() ? Tema.ACENTO : Tema.BORDE_FUERTE);
                    g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                    g.dispose();
                    super.paintComponent(g0);
                }
            };
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setOpaque(false);
            b.setFont(Tema.fuente(Font.BOLD, 12.5f));
            b.setForeground(Tema.TEXTO);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setVerticalTextPosition(JToggleButton.BOTTOM);
            b.setHorizontalTextPosition(JToggleButton.CENTER);
            b.setIcon(Iconos.de(r.equals("Cliente") ? "coffee" : r.equals("Cocina") ? "plate" : r.equals("Administrador") ? "settings" : "orders",
                    22, Tema.ACENTO_OSC));
            b.setPreferredSize(new Dimension(120, 74));
            b.setSelected(r.equals(rol));
            b.addActionListener(e -> {
                rol = r;
                for (JToggleButton o : botones) {
                    o.setSelected(o == b);
                }
            });
            botones.add(b);
            grid.add(b);
        }
        form.add(izq(grid));
        form.add(Box.createVerticalStrut(18));

        form.add(izq(Tema.lbl("Nombre o usuario", 12, Font.BOLD, Tema.TEXTO)));
        form.add(Box.createVerticalStrut(6));
        Componentes.Campo usuario = new Componentes.Campo("usuario@rosetta", 20);
        form.add(izq(usuario));
        form.add(Box.createVerticalStrut(12));
        form.add(izq(Tema.lbl("Contraseña", 12, Font.BOLD, Tema.TEXTO)));
        form.add(Box.createVerticalStrut(6));
        JPasswordField clave = new JPasswordField("123456", 20);
        clave.setBorder(new Componentes.Redondo());
        form.add(izq(clave));
        form.add(Box.createVerticalStrut(18));

        Tema.Boton entrar = Tema.boton("Entrar  →", Tema.Variante.PRIMARIO);
        entrar.setFont(Tema.fuente(Font.BOLD, 14));
        entrar.setBorder(Tema.margen(13, 20, 13, 20));
        entrar.addActionListener(e -> {
            String nombre = usuario.getText().trim();
            if (nombre.isEmpty()) {
                nombre = rol.equals("Cliente") ? "Invitado" : rol.equals("Cajera") ? "María C." : rol;
            }
            alEntrar.accept(nombre, rol);
        });
        form.add(izq(entrar));
        form.add(Box.createVerticalStrut(12));
        form.add(izq(Tema.lbl("Acceso de demostración · No necesitas credenciales reales", 11, Font.PLAIN, new Color(0xB6A6A2))));
        derecha.add(form);
        add(derecha);
    }

    private static JComponent izq(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (c instanceof JPanel || c instanceof javax.swing.text.JTextComponent) {
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        }
        return c;
    }
}
