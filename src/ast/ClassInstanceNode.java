package ast;

import java.util.ArrayList;
import java.util.List;

public class ClassInstanceNode extends ASTNode {
    private String className;
    private List<ASTNode> arguments;
    private String variableName; // NUEVO: para asignaciones como "var obj: Clase = new Clase()"
    private boolean hasExplicitType; // NUEVO: indica si se especificó tipo explícitamente

    public ClassInstanceNode(int lineNumber, String className) {
        super(lineNumber);
        this.className = className;
        this.arguments = new ArrayList<>();
        this.variableName = null;
        this.hasExplicitType = false;
    }
    
    // NUEVO: Constructor con nombre de variable
    public ClassInstanceNode(int lineNumber, String className, String variableName) {
        super(lineNumber);
        this.className = className;
        this.arguments = new ArrayList<>();
        this.variableName = variableName;
        this.hasExplicitType = false;
    }

    // ==================== GETTERS Y SETTERS BÁSICOS ====================
    
    public String getClassName() { 
        return className; 
    }
    
    public void setClassName(String className) { 
        this.className = className; 
    }
    
    public List<ASTNode> getArguments() { 
        return arguments; 
    }
    
    public void addArgument(ASTNode arg) { 
        this.arguments.add(arg); 
    }
    
    // NUEVO: Método para agregar múltiples argumentos
    public void addArguments(List<ASTNode> args) {
        this.arguments.addAll(args);
    }
    
    // NUEVO: Obtener argumento por índice
    public ASTNode getArgument(int index) {
        if (index >= 0 && index < arguments.size()) {
            return arguments.get(index);
        }
        return null;
    }
    
    // NUEVO: Obtener número de argumentos
    public int getArgumentCount() {
        return arguments.size();
    }

    // ==================== MÉTODOS PARA MANEJO DE VARIABLE ====================
    
    public String getVariableName() {
        return variableName;
    }
    
    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }
    
    public boolean hasVariableName() {
        return variableName != null && !variableName.isEmpty();
    }

    // ==================== MÉTODOS PARA TIPO EXPLÍCITO ====================
    
    public boolean hasExplicitType() {
        return hasExplicitType;
    }
    
    public void setHasExplicitType(boolean hasExplicitType) {
        this.hasExplicitType = hasExplicitType;
    }
    
    // NUEVO: Verificar si los argumentos coinciden con un constructor
    public boolean matchesConstructorSignature(int expectedParamCount) {
        return arguments.size() == expectedParamCount;
    }
    
    // NUEVO: Obtener tipos de argumentos (para análisis semántico)
    public List<String> getArgumentTypes() {
        List<String> types = new ArrayList<>();
        for (ASTNode arg : arguments) {
            if (arg instanceof LiteralNode) {
                LiteralNode literal = (LiteralNode) arg;
                Object value = literal.getValue();
                if (value instanceof Integer) {
                    types.add("int");
                } else if (value instanceof Double || value instanceof Float) {
                    types.add("float");
                } else if (value instanceof String) {
                    types.add("string");
                } else if (value instanceof Boolean) {
                    types.add("boolean");
                } else {
                    types.add("unknown");
                }
            } else if (arg instanceof IdentifierNode) {
                types.add("identifier");
            } else if (arg.getClass() == ClassInstanceNode.class) {
                types.add("class");
            } else if (arg instanceof ArrayNode) {
                types.add("array");
            } else {
                types.add("expression");
            }
        }
        return types;
    }
    
    // NUEVO: Método para verificar si es una creación anónima (sin asignar a variable)
    public boolean isAnonymousInstance() {
        return variableName == null;
    }
    
    // NUEVO: Método para obtener representación como expresión
    public String toExpressionString() {
        StringBuilder sb = new StringBuilder();
        sb.append("new ").append(className).append("(");
        
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("arg").append(i);
        }
        
        sb.append(")");
        return sb.toString();
    }
    
    // NUEVO: Método para clonar la instancia (útil para análisis)
    public ClassInstanceNode cloneNode() {
        ClassInstanceNode clone = new ClassInstanceNode(getLineNumber(), className, variableName);
        clone.setHasExplicitType(hasExplicitType);
        
        // Clonar argumentos (referencias, no copias profundas)
        for (ASTNode arg : arguments) {
            clone.addArgument(arg);
        }
        
        return clone;
    }
    
    // NUEVO: Método para debug
    public void debugInstance() {
        System.out.println("=== CREACIÓN DE INSTANCIA ===");
        System.out.println("Línea: " + getLineNumber());
        System.out.println("Clase: " + className);
        System.out.println("Variable: " + (variableName != null ? variableName : "(anónima)"));
        System.out.println("Tipo explícito: " + hasExplicitType);
        System.out.println("Argumentos (" + arguments.size() + "):");
        
        for (int i = 0; i < arguments.size(); i++) {
            ASTNode arg = arguments.get(i);
            System.out.println("  [" + i + "] " + arg.getClass().getSimpleName());
        }
        
        System.out.println("============================");
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    
    // NUEVO: Método para aceptar ExpressionVisitor
    public Object accept(ExpressionVisitor visitor) {
        return visitor.visit(this);
    }
    
    // NUEVO: Sobrescribir toString para mejor debugging
    @Override
    public String toString() {
        return "ClassInstanceNode{" +
               "line=" + getLineNumber() +
               ", className='" + className + '\'' +
               ", args=" + arguments.size() +
               ", variable=" + (variableName != null ? variableName : "none") +
               '}';
    }
    
    // NUEVO: Método para comparar con otra instancia (útil para testing)
    public boolean equals(ClassInstanceNode other) {
        if (other == null) return false;
        
        if (!className.equals(other.className)) return false;
        if (arguments.size() != other.arguments.size()) return false;
        
        // Comparar nombres de variable
        if (variableName == null) {
            if (other.variableName != null) return false;
        } else if (!variableName.equals(other.variableName)) {
            return false;
        }
        
        return true;
    }
    
    // NUEVO: Método para validar la instancia
    public boolean isValid() {
        return className != null && !className.isEmpty();
    }
}