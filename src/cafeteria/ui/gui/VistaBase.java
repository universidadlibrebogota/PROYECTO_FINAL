package cafeteria.ui.gui;

import javax.swing.JPanel;
import java.awt.LayoutManager;

/** Vista de la aplicación: se reconstruye con los datos actuales cada vez que se muestra. */
abstract class VistaBase extends JPanel {

    protected final Contexto ctx;

    VistaBase(Contexto ctx, LayoutManager lm) {
        super(lm);
        this.ctx = ctx;
        setBackground(Tema.FONDO);
    }

    abstract void refrescar();
}
