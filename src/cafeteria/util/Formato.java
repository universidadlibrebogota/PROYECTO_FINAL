package cafeteria.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/** Utilidades de formato compartidas por consola, GUI y dominio (sin dependencias de negocio). */
public final class Formato {

    private static final Locale CO = new Locale("es", "CO");
    private static final String[] DIAS = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
    public static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private Formato() { }

    /** Formatea un valor en pesos: 12500 -> "$12.500". */
    public static String moneda(double valor) {
        DecimalFormat df = new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(CO));
        String texto = df.format(Math.abs(valor));
        return (valor < 0 ? "-$" : "$") + texto;
    }

    /** 15 -> "15%", 12.5 -> "12,5%". */
    public static String porcentaje(double valor) {
        DecimalFormat df = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(CO));
        return df.format(valor) + "%";
    }

    /** "Café con Leche!" -> "cafe-con-leche". Sirve para generar ids estables. */
    public static String slug(String texto) {
        String s = Normalizer.normalize(texto == null ? "" : texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return s.isEmpty() ? "x" : s;
    }

    /** {LUNES..VIERNES} -> "Lun–Vie"; {SABADO,DOMINGO} -> "Sáb, Dom"; vacío -> "Todos los días". */
    public static String dias(Set<DayOfWeek> dias) {
        if (dias == null || dias.isEmpty()) {
            return "Todos los días";
        }
        List<String> partes = new ArrayList<>();
        List<DayOfWeek> ordenados = new ArrayList<>(new TreeSet<>(dias));
        int i = 0;
        while (i < ordenados.size()) {
            int j = i;
            while (j + 1 < ordenados.size()
                    && ordenados.get(j + 1).getValue() == ordenados.get(j).getValue() + 1) {
                j++;
            }
            if (j - i >= 2) {
                partes.add(DIAS[ordenados.get(i).getValue() - 1] + "–" + DIAS[ordenados.get(j).getValue() - 1]);
            } else {
                for (int k = i; k <= j; k++) {
                    partes.add(DIAS[ordenados.get(k).getValue() - 1]);
                }
            }
            i = j + 1;
        }
        return String.join(", ", partes);
    }

    /** plural(1,"pedido","pedidos") -> "1 pedido"; plural(3,...) -> "3 pedidos". */
    public static String plural(int n, String singular, String plural) {
        return n + " " + (n == 1 ? singular : plural);
    }

    public static String hora(LocalTime t) {
        return t.format(HORA);
    }
}
