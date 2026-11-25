package cafeteria.service;

/** Etapas secuenciales del asistente de combos (RF-12). */
public enum PasoCombo {
    PLATO_PRINCIPAL("Elige tu plato principal"),
    BEBIDA("Elige tu bebida"),
    ACOMPANAMIENTO("Elige tu acompañamiento");

    private final String titulo;

    PasoCombo(String titulo) {
        this.titulo = titulo;
    }

    public String titulo() {
        return titulo;
    }

    public int numero() {
        return ordinal() + 1;
    }
}
