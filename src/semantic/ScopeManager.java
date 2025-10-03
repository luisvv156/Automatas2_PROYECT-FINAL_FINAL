package semantic;

import java.util.ArrayDeque;
import java.util.Deque;

public class ScopeManager {
    private final Scope globalScope;
    private final Deque<Scope> scopeStack;

    public ScopeManager() {
        this.globalScope = new Scope(null);
        this.scopeStack = new ArrayDeque<>();
        this.scopeStack.push(globalScope); // push global scope
    }

    /**
     * Entra en un nuevo scope y lo devuelve.
     * Ahora devuelve el Scope (esto evita "cannot convert from void to Scope").
     */
    public Scope enterScope() {
        Scope newScope = new Scope(getCurrentScope());
        scopeStack.push(newScope);
        return newScope;
    }

    /**
     * Sale del scope actual (no permite salir del global).
     */
    public void exitScope() {
        if (scopeStack.size() > 1) {
            scopeStack.pop();
        } else {
            // opcional: no permitir pop del global
            // throw new RuntimeException("No se puede salir del scope global");
        }
    }

    /**
     * Devuelve el scope actual (el top de la pila).
     */
    public Scope getCurrentScope() {
        return scopeStack.peek();
    }

    /**
     * Devuelve el scope global (raíz).
     */
    public Scope getGlobalScope() {
        return globalScope;
    }

    /**
     * Declara un símbolo en el scope actual.
     * (Asume que Scope tiene un método declareSymbol(name, symbol))
     */
    public void declareSymbol(String name, Symbol symbol) {
        getCurrentScope().declareSymbol(name, symbol);
    }

    /**
     * Verifica si el scope actual contiene el símbolo (sólo en el scope actual).
     * Si quieres buscar en todos los scopes hacia arriba, usa resolve.
     */
    public boolean containsSymbol(String name) {
        return getCurrentScope().containsSymbol(name);
    }

    /**
     * Resuelve un símbolo buscando desde el scope actual hacia arriba (padres).
     */
    public Symbol resolve(String name) {
        Scope scope = getCurrentScope();
        while (scope != null) {
            Symbol s = scope.resolve(name);
            if (s != null) return s;
            scope = scope.getParent();
        }
        return null;
    }
}
