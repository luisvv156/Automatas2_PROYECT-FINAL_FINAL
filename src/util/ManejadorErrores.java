package util;  // 👈 cámbialo a semantic para que tu SemanticAnalyzer lo encuentre

import java.util.ArrayList;
import java.util.List;

public class ManejadorErrores {
    private List<ErrorSemantico> errores;

    public ManejadorErrores() {
        this.errores = new ArrayList<>();
    }

    /**
     * Agregar un error semántico indicando línea, mensaje y tipo.
     */
    public void agregarError(int linea, String mensaje, String tipo) {
        ErrorSemantico error = new ErrorSemantico(linea, mensaje, tipo);
        errores.add(error);
        System.err.println("❌ ERROR [" + tipo + "] Línea " + linea + ": " + mensaje);
    }

    /**
     * Agregar un error ya creado.
     */
    public void agregarError(ErrorSemantico error) {
        errores.add(error);
        System.err.println("❌ ERROR [" + error.getTipo() + "] Línea " + 
                          error.getLinea() + ": " + error.getMensaje());
    }

    /**
     * Verifica si hay errores registrados.
     */
    public boolean hayErrores() {
        return !errores.isEmpty();
    }

    /**
     * Devuelve la lista de errores registrados.
     */
    public List<ErrorSemantico> getErrores() {
        return new ArrayList<>(errores);
    }

    /**
     * Limpia todos los errores.
     */
    public void limpiar() {
        errores.clear();
    }

    /**
     * Devuelve el número total de errores registrados.
     */
    public int getCantidadErrores() {
        return errores.size();
    }
}
