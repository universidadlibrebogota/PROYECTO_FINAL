package cafeteria.ui.gui;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/** Iconos vectoriales de trazo (equivalentes a los SVG de la referencia). */
public final class Iconos {

    private Iconos() { }

    public static Icon de(String nombre, int tam, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g0, int x, int y) {
                Graphics2D g = (Graphics2D) g0.create();
                Tema.suavizar(g);
                g.translate(x, y);
                g.scale(tam / 24.0, tam / 24.0);
                g.setColor(color);
                g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                dibujar(nombre, g);
                g.dispose();
            }

            @Override
            public int getIconWidth() {
                return tam;
            }

            @Override
            public int getIconHeight() {
                return tam;
            }
        };
    }

    private static void dibujar(String n, Graphics2D g) {
        switch (n) {
            case "grid":
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        g.draw(new RoundRectangle2D.Double(3 + i * 11, 3 + j * 11, 7, 7, 3, 3));
                    }
                }
                break;
            case "coffee": {
                Path2D p = new Path2D.Double();
                p.moveTo(4, 8);
                p.lineTo(17, 8);
                p.lineTo(17, 15);
                p.append(new Arc2D.Double(7, 10, 10, 10, 0, -180, Arc2D.OPEN), true);
                p.lineTo(4, 8);
                g.draw(p);
                g.draw(new Arc2D.Double(14.5, 10, 5, 5, 90, -180, Arc2D.OPEN));
                g.draw(new Line2D.Double(8, 4, 8, 5));
                g.draw(new Line2D.Double(12, 3, 12, 5));
                break;
            }
            case "plate":
                g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                g.draw(new Ellipse2D.Double(7, 7, 10, 10));
                break;
            case "sparkle": {
                Polygon s = new Polygon(new int[]{12, 14, 19, 14, 12, 10, 5, 10}, new int[]{3, 8, 10, 12, 17, 12, 10, 8}, 8);
                g.draw(s);
                g.draw(new Line2D.Double(5, 17, 5, 21));
                g.draw(new Line2D.Double(3, 19, 7, 19));
                break;
            }
            case "search":
                g.draw(new Ellipse2D.Double(4, 4, 14, 14));
                g.draw(new Line2D.Double(20, 20, 16, 16));
                break;
            case "bell": {
                Path2D p = new Path2D.Double();
                p.moveTo(6, 17);
                p.curveTo(6, 11, 6, 6, 12, 5);
                p.curveTo(18, 6, 18, 11, 18, 17);
                p.closePath();
                g.draw(p);
                g.draw(new Line2D.Double(4, 17, 20, 17));
                g.draw(new Line2D.Double(10, 21, 14, 21));
                break;
            }
            case "plus":
                g.draw(new Line2D.Double(12, 5, 12, 19));
                g.draw(new Line2D.Double(5, 12, 19, 12));
                break;
            case "minus":
                g.draw(new Line2D.Double(5, 12, 19, 12));
                break;
            case "trash":
                g.draw(new Line2D.Double(4, 7, 20, 7));
                g.draw(new Line2D.Double(9, 7, 9, 4));
                g.draw(new Line2D.Double(9, 4, 15, 4));
                g.draw(new Line2D.Double(15, 4, 15, 7));
                g.draw(new RoundRectangle2D.Double(6, 7, 12, 14, 3, 3));
                g.draw(new Line2D.Double(10, 11, 10, 17));
                g.draw(new Line2D.Double(14, 11, 14, 17));
                break;
            case "receipt": {
                Path2D p = new Path2D.Double();
                p.moveTo(6, 3);
                p.lineTo(18, 3);
                p.lineTo(18, 22);
                p.lineTo(15, 20);
                p.lineTo(12, 22);
                p.lineTo(9, 20);
                p.lineTo(6, 22);
                p.closePath();
                g.draw(p);
                g.draw(new Line2D.Double(9, 8, 15, 8));
                g.draw(new Line2D.Double(9, 12, 15, 12));
                g.draw(new Line2D.Double(9, 16, 12, 16));
                break;
            }
            case "orders":
                g.draw(new RoundRectangle2D.Double(8, 4, 12, 16, 2, 2));
                for (int i = 0; i < 3; i++) {
                    g.draw(new Line2D.Double(4, 8 + i * 4, 7, 8 + i * 4));
                }
                g.draw(new Line2D.Double(11, 9, 17, 9));
                g.draw(new Line2D.Double(11, 13, 17, 13));
                break;
            case "report":
                g.draw(new Line2D.Double(4, 20, 4, 10));
                g.draw(new Line2D.Double(10, 20, 10, 4));
                g.draw(new Line2D.Double(16, 20, 16, 13));
                g.draw(new Line2D.Double(2, 20, 22, 20));
                break;
            case "settings":
                g.draw(new Ellipse2D.Double(9, 9, 6, 6));
                g.draw(new Ellipse2D.Double(4, 4, 16, 16));
                for (int i = 0; i < 8; i++) {
                    double a = Math.toRadians(i * 45);
                    g.draw(new Line2D.Double(12 + 8 * Math.cos(a), 12 + 8 * Math.sin(a), 12 + 10.5 * Math.cos(a), 12 + 10.5 * Math.sin(a)));
                }
                break;
            default:
                g.draw(new Ellipse2D.Double(6, 6, 12, 12));
        }
    }
}
