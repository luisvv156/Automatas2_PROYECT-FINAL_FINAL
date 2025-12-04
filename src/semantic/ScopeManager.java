package semantic;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class ScopeManager {
    private final Scope globalScope;
    private final Deque<Scope> scopeStack;
    
    // NUEVO: almacenar clases definidas
    private final Map<String, ClassSymbol> classes;

    public ScopeManager() {
        this.globalScope = new Scope(null);
        this.scopeStack = new ArrayDeque<>();
        this.scopeStack.push(globalScope); // push global scope
        
        // NUEVO: inicializar mapa de clases
        this.classes = new HashMap<>();
    }

    // ==================== MÉTODOS NUEVOS PARA CLASES ====================
    
    /**
     * Declara una nueva clase en el ámbito global.
     */
    public void declareClass(String className, ClassSymbol classSymbol) {
        System.out.println("SCOPE MANAGER - Declarando clase: " + className);
        classes.put(className, classSymbol);
        // Registrar la clase como símbolo en el scope global
        Symbol classSymbolWrapper = Symbol.forClass(className, classSymbol);
        globalScope.declareSymbol(className, classSymbolWrapper);
    }
    
    /**
     * Obtiene una clase por nombre.
     */
    public ClassSymbol getClass(String className) {
        return classes.get(className);
    }
    
    /**
     * Verifica si una clase existe.
     */
    public boolean classExists(String className) {
        return classes.containsKey(className);
    }
    
    /**
     * Entra en un scope de instancia (para métodos de clase).
     */
    public Scope enterInstanceScope(String className, ClassSymbol classSymbol) {
        // Crear un scope especial para la instancia
        InstanceScope instanceScope = new InstanceScope(getCurrentScope(), className, classSymbol);
        scopeStack.push(instanceScope);
        System.out.println("SCOPE MANAGER - Entrando a scope de instancia: " + className);
        return instanceScope;
    }
    
    // ==================== MÉTODOS ORIGINALES MODIFICADOS ====================
    
    /**
     * Entra en un nuevo scope y lo devuelve.
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
            Scope popped = scopeStack.pop();
            System.out.println("SCOPE MANAGER - Saliendo de scope: " + 
                             (popped instanceof InstanceScope ? "instancia" : "normal"));
        } else {
            // opcional: no permitir pop del global
            // throw new RuntimeException("No se puede salir del scope global");
        }
    }
    
    // CORREGIDO: Método para debug
    public void debugScopes() {
        System.out.println("=== DEBUG SCOPES ===");
        System.out.println("Total scopes: " + scopeStack.size());
        System.out.println("Clases definidas: " + classes.keySet());
        
        // Convertir Deque a Array para poder acceder por índice
        Scope[] scopesArray = scopeStack.toArray(new Scope[0]);
        
        for (int i = 0; i < scopesArray.length; i++) {
            Scope scope = scopesArray[i];
            String scopeType = "normal";
            if (scope instanceof InstanceScope) {
                scopeType = "instancia (" + ((InstanceScope) scope).getClassName() + ")";
            } else if (scope == globalScope) {
                scopeType = "global";
            }
            
            System.out.println("Scope " + i + " (" + scopeType + ", parent: " + 
                             (scope.getParent() != null) + ")");
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
            String scopeType = scope instanceof InstanceScope ? 
                "instancia-" + ((InstanceScope) scope).getClassName() : 
                "normal";
            
            System.out.println("  Scope " + depth + " (" + scopeType + "): " + 
                             (s != null ? "ENCONTRADO" : "no encontrado") + 
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
        String scopeType = "normal";
        if (current instanceof InstanceScope) {
            scopeType = "instancia (" + ((InstanceScope) current).getClassName() + ")";
        } else if (current == globalScope) {
            scopeType = "global";
        }
        
        System.out.println("=== SCOPE ACTUAL ===");
        System.out.println("Tipo: " + scopeType);
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
     */
    public void declareSymbol(String name, Symbol symbol) {
        System.out.println("SCOPE MANAGER - Declarando símbolo: " + name);
        Scope current = getCurrentScope();
        
        String scopeType = current instanceof InstanceScope ? 
            "instancia" : "normal";
        System.out.println("Scope actual (" + scopeType + "): " + current.getSymbols().keySet());
        
        current.declareSymbol(name, symbol);
        
        System.out.println("Después de declarar: " + current.getSymbols().keySet());
        System.out.println("¿Símbolo '" + name + "' ahora en scope?: " + current.containsSymbol(name));
    }

    /**
     * Verifica si el scope actual contiene el símbolo (sólo en el scope actual).
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
    
    /**
     * Resuelve una clase por nombre.
     */
    public ClassSymbol resolveClass(String className) {
        return classes.get(className);
    }
    
    // NUEVO: Método para obtener todas las clases definidas
    public Map<String, ClassSymbol> getClasses() {
        return new HashMap<>(classes); // Retorna una copia para evitar modificaciones externas
    }
    
    // NUEVO: Método para verificar si un símbolo es una clase
    public boolean isClassSymbol(String name) {
        Symbol symbol = resolve(name);
        return symbol != null && symbol.isClass();
    }
    
    // NUEVO: Método para entrar en scope de clase (para analizar cuerpo de clase)
    public Scope enterClassScope(ClassSymbol classSymbol) {
        Scope classScope = new Scope(getCurrentScope());
        // Inicializar con campos de la clase
        for (Map.Entry<String, Symbol> field : classSymbol.getFields().entrySet()) {
            classScope.declareSymbol(field.getKey(), field.getValue());
        }
        scopeStack.push(classScope);
        System.out.println("SCOPE MANAGER - Entrando a scope de clase: " + classSymbol.getName());
        return classScope;
    }
    
    // NUEVO: Método para obtener el scope de instancia actual si existe
    public InstanceScope getCurrentInstanceScope() {
        Scope current = getCurrentScope();
        if (current instanceof InstanceScope) {
            return (InstanceScope) current;
        }
        return null;
    }
    
    // NUEVO: Método para verificar si estamos en un scope de instancia
    public boolean isInInstanceScope() {
        return getCurrentScope() instanceof InstanceScope;
    }
    
    // NUEVO: Método para obtener el nombre de la clase actual si estamos en scope de instancia
    public String getCurrentClassName() {
        InstanceScope instanceScope = getCurrentInstanceScope();
        if (instanceScope != null) {
            return instanceScope.getClassName();
        }
        return null;
    }
    
    // NUEVO: Método para resolver un miembro de clase (campo o método)
    public Symbol resolveClassMember(String className, String memberName) {
        ClassSymbol classSymbol = getClass(className);
        if (classSymbol == null) {
            return null;
        }
        
        // Buscar campo
        Symbol field = classSymbol.getField(memberName);
        if (field != null) {
            return field;
        }
        
        // Buscar método
        return classSymbol.getMethod(memberName);
    }
}