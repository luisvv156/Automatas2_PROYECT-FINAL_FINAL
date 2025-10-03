package semantic;

import java.util.HashMap;
import java.util.Map;

public class Scope {
    private final Scope parent;                  // Scope padre (para resolver símbolos hacia arriba)
    private final Map<String, Symbol> symbols;   // Tabla de símbolos en este scope

    public Scope(Scope parent) {
        this.parent = parent;
        this.symbols = new HashMap<>();
    }

    /**
     * Devuelve el scope padre (null si es el global).
     */
    public Scope getParent() {
        return parent;
    }

    /**
     * Declara un símbolo en el scope actual.
     */
    public void declareSymbol(String name, Symbol symbol) {
        symbols.put(name, symbol);
    }

    /**
     * Verifica si este scope contiene un símbolo con ese nombre.
     */
    public boolean containsSymbol(String name) {
        return symbols.containsKey(name);
    }

    /**
     * Resuelve un símbolo en este scope (NO sube a los padres).
     * Para resolver en toda la cadena, usa ScopeManager.resolve().
     */
    public Symbol resolve(String name) {
        return symbols.get(name);
    }

    /**
     * Devuelve el símbolo si existe en este scope, null si no.
     */
    public Symbol getSymbol(String name) {
        return symbols.get(name);
    }

    /**
     * Obtiene todos los símbolos de este scope.
     */
    public Map<String, Symbol> getSymbols() {
        return symbols;
    }
}
