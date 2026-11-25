package cafeteria.ui.gui;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

/** Piezas reutilizables: ventana modal, filas seleccionables, interruptor, píldora y campo de texto. */
final class Componentes {

    private Componentes() { }

    /** Ventana modal sin decoración (como los modales de la referencia). */
    static class Modal extends JDialog {
        final JPanel cuerpo = Tema.transparente(new BorderLayout());

        Modal(Component origen, int ancho) {
            super(SwingUtilities.getWindowAncestor(origen), ModalityType.APPLICATION_MODAL);
            setUndecorated(true);
            JPanel raiz = new JPanel(new BorderLayout());
            raiz.setBackground(Tema.BLANCO);
            raiz.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.BORDE_FUERTE),
                    Tema.margen(10, 26, 24, 26)));
            Tema.Boton cerrar = Tema.boton("✕", Tema.Variante.TEXTO);
            cerrar.addActionListener(e -> dispose());
            JPanel norte = Tema.transparente(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            norte.add(cerrar);
            raiz.add(norte, BorderLayout.NORTH);
            raiz.add(cuerpo, BorderLayout.CENTER);
            setContentPane(raiz);
            raiz.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cerrar");
            raiz.getActionMap().put("cerrar", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            setMinimumSize(new Dimension(ancho, 0));
        }

        void abrir() {
            pack();
            setLocationRelativeTo(getOwner());
            setVisible(true);
        }
    }

    /** Fila seleccionable (casilla + texto + precio, con miniatura opcional). */
    static class FilaOpcion extends JToggleButton {
        private final String derecha;
        private final double[] miniatura;

        private final java.awt.image.BufferedImage imagenPropia;

        FilaOpcion(String texto, String derecha, double[] miniatura) {
            this(texto, derecha, miniatura, null);
        }

        FilaOpcion(String texto, String derecha, double[] miniatura, java.awt.image.BufferedImage imagenPropia) {
            super(texto);
            this.derecha = derecha;
            this.miniatura = miniatura;
            this.imagenPropia = imagenPropia;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(Tema.fuente(Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(420, miniatura == null ? 46 : 58));
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            boolean sel = isSelected();
            g.setColor(sel ? Tema.ROSA : Tema.BLANCO);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g.setColor(sel ? Tema.ACENTO : Tema.BORDE_FUERTE);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            int x = 14;
            if (miniatura != null) {
                Shape clip = new RoundRectangle2D.Double(10, 6, 50, getHeight() - 12, 8, 8);
                Graphics2D gm = (Graphics2D) g.create();
                gm.setClip(clip);
                ImagenCafe.pintar(gm, 10, 6, 50, getHeight() - 12, imagenPropia, miniatura[0], miniatura[1], miniatura[2]);
                gm.dispose();
                x = 72;
            } else {
                g.setColor(sel ? Tema.ACENTO : Tema.BLANCO);
                g.fillRoundRect(x, getHeight() / 2 - 9, 18, 18, 6, 6);
                g.setColor(sel ? Tema.ACENTO : Tema.BORDE_FUERTE);
                g.drawRoundRect(x, getHeight() / 2 - 9, 18, 18, 6, 6);
                if (sel) {
                    g.setColor(Color.WHITE);
                    g.setStroke(new java.awt.BasicStroke(2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                    int cy = getHeight() / 2;
                    g.drawLine(x + 4, cy, x + 8, cy + 4);
                    g.drawLine(x + 8, cy + 4, x + 14, cy - 3);
                }
                x += 30;
            }
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            g.setColor(Tema.TEXTO);
            g.drawString(getText(), x, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            if (derecha != null) {
                g.setColor(Tema.ACENTO_OSC);
                g.drawString(derecha, getWidth() - 16 - fm.stringWidth(derecha), (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            } else if (miniatura != null && sel) {
                g.setColor(Tema.ACENTO_OSC);
                g.drawString("✓", getWidth() - 28, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
            g.dispose();
        }
    }

    /** Interruptor tipo "switch". */
    static class Interruptor extends JToggleButton {
        Interruptor(boolean activo) {
            setSelected(activo);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(40, 22));
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            g.setColor(isSelected() ? Tema.ACENTO : new Color(0xDDD0CC));
            g.fillRoundRect(0, 0, 38, 21, 21, 21);
            g.setColor(Color.WHITE);
            g.fillOval(isSelected() ? 19 : 2, 2, 17, 17);
            g.dispose();
        }
    }

    /** Etiqueta con fondo redondeado (estados, insignias). */
    static class Pildora extends JLabel {
        private final Color fondo;

        Pildora(String texto, Color texto0, Color fondo) {
            super(texto);
            this.fondo = fondo;
            setForeground(texto0);
            setFont(Tema.fuente(Font.BOLD, 10.5f));
            setBorder(Tema.margen(4, 10, 4, 10));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            g.setColor(fondo);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g.dispose();
            super.paintComponent(g0);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }
    }

    /** Campo de texto con borde redondeado y texto de ayuda. */
    static class Campo extends JTextField {
        private final String ayuda;

        Campo(String ayuda, int columnas) {
            super(columnas);
            this.ayuda = ayuda;
            setFont(Tema.fuente(Font.PLAIN, 13));
            setForeground(Tema.TEXTO);
            setOpaque(false);
            setBorder(new Redondo());
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            g.setColor(Color.WHITE);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g.dispose();
            super.paintComponent(g0);
            if (getText().isEmpty() && ayuda != null) {
                Graphics2D h = (Graphics2D) g0.create();
                Tema.suavizar(h);
                h.setColor(new Color(0xB6A6A2));
                h.setFont(getFont());
                Insets in = getInsets();
                h.drawString(ayuda, in.left, (getHeight() + h.getFontMetrics().getAscent() - h.getFontMetrics().getDescent()) / 2);
                h.dispose();
            }
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }
    }

    static class Redondo implements Border {
        @Override
        public void paintBorder(Component c, Graphics g0, int x, int y, int w, int h) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            g.setColor(c.isFocusOwner() ? Tema.ACENTO : Tema.BORDE_FUERTE);
            g.drawRoundRect(x, y, w - 1, h - 1, 12, 12);
            g.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(9, 14, 9, 14);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}
