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
     // CORREGIDO: Método para debug
    public void debugScopes() {
        System.out.println("=== DEBUG SCOPES ===");
        System.out.println("Total scopes: " + scopeStack.size());
        
        // Convertir Deque a Array para poder acceder por índice
        Scope[] scopesArray = scopeStack.toArray(new Scope[0]);
        
        for (int i = 0; i < scopesArray.length; i++) {
            Scope scope = scopesArray[i];
            System.out.println("Scope " + i + " (parent: " + (scope.getParent() != null) + ")");
            System.out.println("  Símbolos: " + scope.getSymbols().keySet());
        }
        System.out.println("===================");
    }

    // CORREGIDO: Método para buscar en todos los scopes con debug
    public Symbol debugResolve(String name) {
        System.out.println("Buscando símbolo: '" + name + "'");
        Scope scope = getCurrentScope();
        int depth = 0;
        
        while (scope != null) {
            Symbol s = scope.resolve(name);
            System.out.println("  Scope " + depth + ": " + (s != null ? "ENCONTRADO" : "no encontrado") + 
                             " - símbolos: " + scope.getSymbols().keySet());
            if (s != null) {
                System.out.println("  ✓ Símbolo '" + name + "' ENCONTRADO en scope " + depth);
                return s;
            }
            scope = scope.getParent();
            depth++;
        }
        
        System.out.println("  ✗ Símbolo '" + name + "' NO ENCONTRADO en ningún scope");
        return null;
    }

    // NUEVO: Método para ver el scope actual
    public void debugCurrentScope() {
        Scope current = getCurrentScope();
        System.out.println("=== SCOPE ACTUAL ===");
        System.out.println("Símbolos: " + current.getSymbols().keySet());
        System.out.println("Tiene parent: " + (current.getParent() != null));
        System.out.println("===================");
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
        System.out.println("SCOPE MANAGER - Declarando símbolo: " + name);
        Scope current = getCurrentScope();
        System.out.println("Scope actual: " + current.getSymbols().keySet());
        
        current.declareSymbol(name, symbol);
        
        System.out.println("Después de declarar: " + current.getSymbols().keySet());
        System.out.println("¿Símbolo '" + name + "' ahora en scope?: " + current.containsSymbol(name));
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
