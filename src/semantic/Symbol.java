package semantic;

import ast.FunctionNode;
import ast.MethodDeclNode;

/**
 * Representa un símbolo en la tabla de símbolos.
 * Puede ser una variable, función, método o clase.
 */
public class Symbol {
    private final String name;          // nombre del símbolo
    private final Tipo type;            // tipo (int, float, bool, etc.)
    private Object value;               // valor actual (si aplica, para variables)
    private final boolean isFunction;   // indica si es función
    private FunctionNode functionNode;  // si es función, guarda el nodo AST de la definición
    
    // NUEVO: para manejar métodos y clases
    private MethodDeclNode methodNode;  // si es método, guarda el nodo AST del método
    private boolean isClass;            // indica si es una clase
    private ClassSymbol classSymbol;    // si es una clase, referencia al símbolo de clase
    private String className;  // nombre de la clase si el tipo es CLASE

    public Symbol(String name, Tipo type, Object value, boolean isFunction) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.isFunction = isFunction;
        this.methodNode = null;
        this.isClass = false;
        this.classSymbol = null;
        this.className = null;
    }
    
    // NUEVO: Constructor para símbolos de clase
    public static Symbol forClass(String name, ClassSymbol classSymbol) {
        Symbol symbol = new Symbol(name, Tipo.CLASE, null, false);
        symbol.isClass = true;
        symbol.classSymbol = classSymbol;
        return symbol;
    }
    
    // NUEVO: Constructor para métodos
    public static Symbol forMethod(String name, Tipo returnType, MethodDeclNode methodNode) {
        Symbol symbol = new Symbol(name, returnType, null, true);
        symbol.methodNode = methodNode;
        return symbol;
    }

    // --- Getters ---
    public String getName() {
        return name;
    }

    public Tipo getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public boolean isFunction() {
        return isFunction;
    }

    public FunctionNode getFunctionNode() {
        return functionNode;
    }
    
    // NUEVO: Getters para funcionalidad de clases
    public MethodDeclNode getMethodNode() {
        return methodNode;
    }
    
    public boolean isClass() {
        return isClass;
    }
    
    public ClassSymbol getClassSymbol() {
        return classSymbol;
    }
    
    // NUEVO: Getter para verificar si es método
    public boolean isMethod() {
        return methodNode != null;
    }

    // --- Setters ---
    public void setValue(Object value) {
        this.value = value;
    }

    public void setFunctionNode(FunctionNode functionNode) {
        this.functionNode = functionNode;
    }
    
    // NUEVO: Setters para funcionalidad de clases
    public void setMethodNode(MethodDeclNode methodNode) {
        this.methodNode = methodNode;
    }
    
    public void setClass(boolean isClass) {
        this.isClass = isClass;
    }
    
    public void setClassSymbol(ClassSymbol classSymbol) {
        this.classSymbol = classSymbol;
    }

    @Override
    public String toString() {
        if (isClass) {
            return "Clase " + name + " : " + type;
        } else if (methodNode != null) {
            return "Método " + name + " : " + type;
        } else if (isFunction) {
            return "Función " + name + " : " + type;
        } else {
            return "Símbolo " + name + " : " + type + " = " + value;
        }
    }
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}