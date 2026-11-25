package cafeteria.ui.gui;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

/** Imagen de la cafetería (recurso de la referencia) y recortes por producto. */
public final class ImagenCafe {

    private static BufferedImage hero;
    private static boolean intentado;

    private static final Map<String, String> ARCHIVOS = new HashMap<>();
    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    static {
        ARCHIVOS.put("latte rosa", "latte-rosa.jpg");
        ARCHIVOS.put("capuccino canela", "capuccino-canela.jpg");
        ARCHIVOS.put("cappuccino canela", "capuccino-canela.jpg");
        ARCHIVOS.put("americano", "americano.jpg");
        ARCHIVOS.put("fresa helada", "fresa-helada.jpg");
        ARCHIVOS.put("jugo de naranja", "jugo-naranja.jpg");
        ARCHIVOS.put("agua mineral", "agua-mineral.jpg");
        ARCHIVOS.put("croissant clasico", "croissant-clasico.jpg");
        ARCHIVOS.put("pan de bono", "pan-de-bono.jpg");
        ARCHIVOS.put("tostada brunch", "tostada-brunch.jpg");
        ARCHIVOS.put("sandwich de pollo", "sandwich-pollo.jpg");
        ARCHIVOS.put("bowl de frutas", "bowl-frutas.jpg");
        ARCHIVOS.put("tarta del dia", "tarta-del-dia.jpg");
        ARCHIVOS.put("brownie", "brownie.jpg");
        ARCHIVOS.put("arma tu combo", "arma-tu-combo.jpg");
    }

    private ImagenCafe() { }

    private static String normalizar(String s) {
        if (s == null) {
            return "";
        }
        return Normalizer.normalize(s.trim().toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private static synchronized BufferedImage leer(String archivo) {
        if (CACHE.containsKey(archivo)) {
            return CACHE.get(archivo);
        }
        BufferedImage img = null;
        try (InputStream in = ImagenCafe.class.getResourceAsStream("recursos/" + archivo)) {
            if (in != null) {
                img = ImageIO.read(in);
            }
        } catch (IOException ignorada) {
            img = null;
        }
        CACHE.put(archivo, img);
        return img;
    }

    /** Foto propia del producto (por su nombre), o null si no tiene una. */
    public static BufferedImage imagen(String nombreProducto) {
        String clave = normalizar(nombreProducto);
        String archivo = ARCHIVOS.get(clave);
        if (archivo == null) {
            String slug = clave.replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
            if (slug.isEmpty()) {
                return null;
            }
            archivo = slug + ".jpg";
        }
        return leer(archivo);
    }

    /** Foco para la foto propia del producto; si no la tiene, el recorte de la imagen general. */
    public static double[] focoProducto(String nombreProducto, String tipo, String categoria) {
        if (imagen(nombreProducto) != null) {
            return new double[]{0.55, 0.58, 1.0};
        }
        return foco(tipo, categoria);
    }

    /** Foto del producto con su imagen propia (o el recorte general si no tiene). */
    public static Foto fotoProducto(String nombreProducto, String tipo, String categoria, int ancho, int alto, int arco) {
        return new Foto(imagen(nombreProducto), focoProducto(nombreProducto, tipo, categoria), ancho, alto, arco);
    }

    private static synchronized BufferedImage hero() {
        if (!intentado) {
            intentado = true;
            try (InputStream in = ImagenCafe.class.getResourceAsStream("recursos/cafe-hero.jpg")) {
                if (in != null) {
                    hero = ImageIO.read(in);
                }
            } catch (IOException ignorada) {
                hero = null;
            }
        }
        return hero;
    }

    /** {focoX, focoY, zoom} según el tipo y la categoría del producto. */
    public static double[] foco(String tipo, String categoria) {
        String c = categoria == null ? "" : categoria.toLowerCase();
        if ("Bebida".equals(tipo) && (c.contains("caf") || c.contains("espresso"))) {
            return new double[]{0.40, 0.25, 2.3};
        }
        if ("Bebida".equals(tipo)) {
            return new double[]{0.72, 0.22, 2.3};
        }
        if ("Postre".equals(tipo)) {
            return new double[]{0.52, 0.78, 1.9};
        }
        if (tipo == null) {
            return new double[]{0.52, 0.45, 1.2};   // combos / sin tipo
        }
        return new double[]{0.62, 0.62, 2.0};
    }

    public static void pintar(Graphics2D g, int x, int y, int w, int h, double fx, double fy, double zoom) {
        pintar(g, x, y, w, h, null, fx, fy, zoom);
    }

    /** Igual que el anterior pero con una imagen propia (si es null usa la imagen general). */
    public static void pintar(Graphics2D g, int x, int y, int w, int h, BufferedImage propia, double fx, double fy, double zoom) {
        BufferedImage img = propia != null ? propia : hero();
        if (img == null) {
            g.setPaint(new GradientPaint(0, 0, new Color(0xE9C5C0), w, h, new Color(0xF4DDD8)));
            g.fillRect(x, y, w, h);
            return;
        }
        double s = Math.max((double) w / img.getWidth(), (double) h / img.getHeight()) * zoom;
        double sw = w / s;
        double sh = h / s;
        double sx = Math.max(0, Math.min(img.getWidth() - sw, fx * img.getWidth() - sw / 2));
        double sy = Math.max(0, Math.min(img.getHeight() - sh, fy * img.getHeight() - sh / 2));
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(img, x, y, x + w, y + h, (int) sx, (int) sy, (int) (sx + sw), (int) (sy + sh), null);
    }

    /** Componente que dibuja un recorte, con esquinas redondeadas opcionales y una insignia. */
    public static class Foto extends JComponent {
        private final BufferedImage propia;
        private final double fx;
        private final double fy;
        private final double zoom;
        private final int arco;
        private String insignia;
        private boolean oscurecer;

        public Foto(double[] foco, int ancho, int alto, int arco) {
            this(null, foco, ancho, alto, arco);
        }

        public Foto(BufferedImage propia, double[] foco, int ancho, int alto, int arco) {
            this.propia = propia;
            this.fx = foco[0];
            this.fy = foco[1];
            this.zoom = foco[2];
            this.arco = arco;
            setPreferredSize(new Dimension(ancho, alto));
        }

        public Foto insignia(String texto) {
            this.insignia = texto;
            return this;
        }

        public Foto oscurecer() {
            this.oscurecer = true;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            Shape forma = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arco, arco);
            g.setClip(forma);
            pintar(g, 0, 0, getWidth(), getHeight(), propia, fx, fy, zoom);
            if (oscurecer) {
                g.setPaint(new GradientPaint(0, 0, new Color(0x5A2B2E, false), getWidth(), 0, new Color(0x00000000, true)));
                g.setColor(new Color(60, 30, 30, 90));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            if (insignia != null) {
                g.setFont(Tema.fuente(Font.BOLD, 10.5f));
                FontMetrics fm = g.getFontMetrics();
                int w = fm.stringWidth(insignia) + 16;
                g.setColor(new Color(255, 255, 255, 235));
                g.fillRoundRect(9, 9, w, 21, 21, 21);
                g.setColor(Tema.ACENTO_OSC);
                g.drawString(insignia, 17, 9 + 14);
            }
            g.dispose();
        }
    }
}
