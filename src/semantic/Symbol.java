package semantic;

import ast.FunctionNode;

/**
 * Representa un símbolo en la tabla de símbolos.
 * Puede ser una variable o una función.
 */
public class Symbol {
    private final String name;          // nombre del símbolo
    private final Tipo type;            // tipo (int, float, bool, etc.)
    private Object value;               // valor actual (si aplica, para variables)
    private final boolean isFunction;   // indica si es función
    private FunctionNode functionNode;  // si es función, guarda el nodo AST de la definición

    public Symbol(String name, Tipo type, Object value, boolean isFunction) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.isFunction = isFunction;
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

    // --- Setters ---
    public void setValue(Object value) {
        this.value = value;
    }

    public void setFunctionNode(FunctionNode functionNode) {
        this.functionNode = functionNode;
    }

    @Override
    public String toString() {
        if (isFunction) {
            return "Función " + name + " : " + type;
        } else {
            return "Símbolo " + name + " : " + type + " = " + value;
        }
    }
}
