package cafeteria.ui.gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Paleta, tipografías y componentes base de la interfaz (réplica del estilo de la referencia Rosetta). */
public final class Tema {

    public static final Color FONDO = new Color(0xFCF9F7);
    public static final Color BLANCO = Color.WHITE;
    public static final Color BORDE = new Color(0xEEE4E0);
    public static final Color BORDE_FUERTE = new Color(0xE2D5D1);
    public static final Color TEXTO = new Color(0x342825);
    public static final Color SUAVE = new Color(0x8A7B77);
    public static final Color ACENTO = new Color(0xAD6268);
    public static final Color ACENTO_OSC = new Color(0x9F5058);
    public static final Color ROSA = new Color(0xF8E9E9);
    public static final Color ROSA_HOVER = new Color(0xFBF3F2);
    public static final Color VERDE = new Color(0x4F8765);
    public static final Color VERDE_BG = new Color(0xE6F2E9);
    public static final Color MORADO = new Color(0x776293);
    public static final Color MORADO_BG = new Color(0xEEE9F5);
    public static final Color AMBAR = new Color(0xA06F47);
    public static final Color AMBAR_BG = new Color(0xF7ECE2);
    public static final Color ROJO = new Color(0xA6525B);
    public static final Color ROJO_BG = new Color(0xF8E9EA);

    private Tema() { }

    public static Font fuente(int estilo, float tam) {
        return new Font(Font.SANS_SERIF, estilo, 12).deriveFont(estilo, tam);
    }

    public static Font titulo(float tam) {
        return new Font(Font.SERIF, Font.BOLD, 12).deriveFont(Font.BOLD, tam);
    }

    public static JLabel lbl(String texto, float tam, int estilo, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(estilo, tam));
        l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    public static Border margen(int arriba, int izq, int abajo, int der) {
        return BorderFactory.createEmptyBorder(arriba, izq, abajo, der);
    }

    public static JPanel transparente(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setOpaque(false);
        return p;
    }

    /** Contenedor que sigue el ancho del viewport: el contenido se ajusta a la ventana y solo hay scroll vertical. */
    private static class Desplazable extends JPanel implements javax.swing.Scrollable {
        Desplazable(Component c) {
            super(new java.awt.BorderLayout());
            setOpaque(false);
            add(c, java.awt.BorderLayout.CENTER);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle r, int o, int d) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle r, int o, int d) {
            return Math.max(60, r.height - 40);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(new Desplazable(c));
        sp.setBorder(null);
        sp.getViewport().setBackground(FONDO);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(9, 0));
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = BORDE_FUERTE;
                trackColor = FONDO;
            }

            @Override
            protected JButton createDecreaseButton(int o) {
                return vacio();
            }

            @Override
            protected JButton createIncreaseButton(int o) {
                return vacio();
            }

            private JButton vacio() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }

            @Override
            protected void paintThumb(Graphics g0, JComponent c, java.awt.Rectangle r) {
                Graphics2D g = (Graphics2D) g0.create();
                suavizar(g);
                g.setColor(thumbColor);
                g.fillRoundRect(r.x + 1, r.y, r.width - 2, r.height, 8, 8);
                g.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, java.awt.Rectangle r) {
                g.setColor(FONDO);
                g.fillRect(r.x, r.y, r.width, r.height);
            }
        });
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return sp;
    }

    static void suavizar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    /** Panel con esquinas redondeadas, relleno y borde. */
    public static class Tarjeta extends JPanel {
        private final int arco;
        private Color relleno;
        private Color linea;

        public Tarjeta(LayoutManager lm, int arco, Color relleno, Color linea) {
            super(lm);
            this.arco = arco;
            this.relleno = relleno;
            this.linea = linea;
            setOpaque(false);
        }

        public Tarjeta(LayoutManager lm) {
            this(lm, 16, BLANCO, BORDE);
        }

        public void colores(Color relleno, Color linea) {
            this.relleno = relleno;
            this.linea = linea;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            suavizar(g);
            g.setColor(relleno);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
            if (linea != null) {
                g.setColor(linea);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }

    public enum Variante { PRIMARIO, SUAVE, BORDE, TEXTO }

    /** Botón redondeado con tres variantes (primario, suave, con borde). */
    public static class Boton extends JButton {
        private final Variante variante;
        private boolean encima;
        private int arco = 12;

        public Boton(String texto, Variante variante) {
            super(texto);
            this.variante = variante;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(fuente(Font.BOLD, 12.5f));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(margen(9, 16, 9, 16));
            setForeground(variante == Variante.PRIMARIO ? BLANCO : variante == Variante.SUAVE ? ACENTO_OSC : TEXTO);
            if (variante == Variante.TEXTO) {
                setForeground(ACENTO_OSC);
            }
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    repaint();
                }
            });
        }

        public Boton arco(int a) {
            this.arco = a;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            suavizar(g);
            if (!isEnabled()) {
                g.setComposite(AlphaComposite.SrcOver.derive(0.45f));
            }
            Color fondo;
            switch (variante) {
                case PRIMARIO: fondo = encima ? new Color(0x8E444C) : ACENTO_OSC; break;
                case SUAVE: fondo = encima ? new Color(0xF3DDDD) : ROSA; break;
                case BORDE: fondo = encima ? ROSA_HOVER : BLANCO; break;
                default: fondo = encima ? ROSA_HOVER : new Color(0, 0, 0, 0);
            }
            g.setColor(fondo);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
            if (variante == Variante.BORDE) {
                g.setColor(BORDE_FUERTE);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
            }
            g.dispose();
            super.paintComponent(g0);
        }

        @Override
        public Dimension getMaximumSize() {
            return isMaximumSizeSet() ? super.getMaximumSize() : getPreferredSize();
        }
    }

    public static Boton boton(String texto, Variante v) {
        return new Boton(texto, v);
    }
}
