package ast;

import java.util.ArrayList;
import java.util.List;

public class MethodCallNode extends ASTNode {
    private ASTNode object;
    private String methodName;
    private List<ASTNode> arguments;
    private String className; // NUEVO: nombre de la clase del objeto
    private boolean isStaticCall; // NUEVO: indica si es llamada estática
    private boolean isConstructorCall; // NUEVO: indica si es llamada a constructor
    private MethodDeclNode methodDeclaration; // NUEVO: referencia a la declaración del método

    public MethodCallNode(int lineNumber, ASTNode object, String methodName) {
        super(lineNumber);
        this.object = object;
        this.methodName = methodName;
        this.arguments = new ArrayList<>();
        this.className = null;
        this.isStaticCall = false;
        this.isConstructorCall = false;
        this.methodDeclaration = null;
    }
    
    // NUEVO: Constructor con información adicional
    public MethodCallNode(int lineNumber, ASTNode object, String methodName, boolean isStaticCall) {
        this(lineNumber, object, methodName);
        this.isStaticCall = isStaticCall;
    }

    // ==================== GETTERS Y SETTERS BÁSICOS ====================
    
    public ASTNode getObject() { 
        return object; 
    }
    
    public void setObject(ASTNode object) { 
        this.object = object; 
    }
    
    public String getMethodName() { 
        return methodName; 
    }
    
    public void setMethodName(String methodName) { 
        this.methodName = methodName; 
    }
    
    public List<ASTNode> getArguments() { 
        return arguments; 
    }
    
    public void addArgument(ASTNode arg) { 
        this.arguments.add(arg); 
    }
    
    // NUEVO: Agregar múltiples argumentos
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
    
    // NUEVO: Obtener nombres de los argumentos (para debug)
    public List<String> getArgumentTypes() {
        List<String> types = new ArrayList<>();
        for (ASTNode arg : arguments) {
            types.add(arg.getClass().getSimpleName());
        }
        return types;
    }

    // ==================== MÉTODOS PARA ANÁLISIS SEMÁNTICO ====================
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public boolean hasClassName() {
        return className != null && !className.isEmpty();
    }
    
    public boolean isStaticCall() {
        return isStaticCall;
    }
    
    public void setStaticCall(boolean isStaticCall) {
        this.isStaticCall = isStaticCall;
    }
    
    public boolean isConstructorCall() {
        return isConstructorCall;
    }
    
    public void setConstructorCall(boolean isConstructorCall) {
        this.isConstructorCall = isConstructorCall;
    }
    
    public MethodDeclNode getMethodDeclaration() {
        return methodDeclaration;
    }
    
    public void setMethodDeclaration(MethodDeclNode methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
    }
    
    // NUEVO: Verificar si tiene declaración asociada
    public boolean hasMethodDeclaration() {
        return methodDeclaration != null;
    }
    
    // NUEVO: Obtener firma de la llamada (para comparación)
    public String getCallSignature() {
        StringBuilder sb = new StringBuilder();
        if (className != null) {
            sb.append(className).append(".");
        }
        sb.append(methodName).append("(");
        
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("arg").append(i);
        }
        
        sb.append(")");
        return sb.toString();
    }
    
    // NUEVO: Obtener objeto raíz (para llamadas encadenadas)
    public String getRootObjectName() {
        if (object instanceof IdentifierNode) {
            return ((IdentifierNode) object).getName();
        } else if (object.getClass() == FieldAccessNode.class) {
            // Usar método más simple sin reflexión
            try {
                FieldAccessNode fieldAccess = (FieldAccessNode) object;
                // Llamar recursivamente si el objeto es otro FieldAccessNode
                if (fieldAccess.getObject() instanceof IdentifierNode) {
                    return ((IdentifierNode) fieldAccess.getObject()).getName();
                }
                return "object";
            } catch (Exception e) {
                return "object";
            }
        } else if (object.getClass() == MethodCallNode.class) {
            return ((MethodCallNode) object).getRootObjectName();
        }
        return "object";
    }
    
    // NUEVO: Verificar si es llamada a 'this'
    public boolean isThisCall() {
        if (object instanceof IdentifierNode) {
            return "this".equals(((IdentifierNode) object).getName());
        }
        return false;
    }
    
    // NUEVO: Verificar si es llamada a super
    public boolean isSuperCall() {
        if (object instanceof IdentifierNode) {
            return "super".equals(((IdentifierNode) object).getName());
        }
        return false;
    }

    // ==================== MÉTODOS DE UTILIDAD ====================
    
    // NUEVO: Verificar si la llamada es válida
    public boolean isValid() {
        return object != null && methodName != null && !methodName.isEmpty();
    }
    
    // NUEVO: Verificar si los argumentos coinciden con una firma dada
    public boolean matchesSignature(int expectedParamCount) {
        return arguments.size() == expectedParamCount;
    }
    
    // NUEVO: Obtener llamada completa como string
    public String getFullCall() {
        StringBuilder sb = new StringBuilder();
        
        if (object instanceof IdentifierNode) {
            sb.append(((IdentifierNode) object).getName());
        } else if (object.getClass() == FieldAccessNode.class) {
            // Usar método simplificado
            try {
                FieldAccessNode fieldAccess = (FieldAccessNode) object;
                if (fieldAccess.getObject() instanceof IdentifierNode) {
                    sb.append(((IdentifierNode) fieldAccess.getObject()).getName())
                      .append(".")
                      .append(fieldAccess.getFieldName());
                } else {
                    sb.append("object.");
                    sb.append(fieldAccess.getFieldName());
                }
            } catch (Exception e) {
                sb.append("object");
            }
        } else {
            sb.append("object");
        }
        
        sb.append(".").append(methodName).append("(");
        
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("arg").append(i);
        }
        
        sb.append(")");
        return sb.toString();
    }
    
    // NUEVO: Método para debug
    public void debugMethodCall() {
        System.out.println("=== LLAMADA A MÉTODO ===");
        System.out.println("Línea: " + getLineNumber());
        System.out.println("Objeto tipo: " + object.getClass().getSimpleName());
        System.out.println("Método: " + methodName);
        System.out.println("Clase: " + (className != null ? className : "desconocida"));
        System.out.println("Es estática: " + isStaticCall);
        System.out.println("Es constructor: " + isConstructorCall);
        System.out.println("Es 'this': " + isThisCall());
        System.out.println("Es 'super': " + isSuperCall());
        System.out.println("Argumentos (" + arguments.size() + "):");
        for (int i = 0; i < arguments.size(); i++) {
            System.out.println("  [" + i + "] " + arguments.get(i).getClass().getSimpleName());
        }
        System.out.println("Firma: " + getCallSignature());
        System.out.println("Objeto raíz: " + getRootObjectName());
        System.out.println("Tiene declaración: " + hasMethodDeclaration());
        System.out.println("Válido: " + isValid());
        System.out.println("========================");
    }
    
    // NUEVO: Método para clonar (útil para análisis)
    public MethodCallNode cloneNode() {
        MethodCallNode clone = new MethodCallNode(getLineNumber(), object, methodName, isStaticCall);
        clone.setClassName(className);
        clone.setConstructorCall(isConstructorCall);
        clone.setMethodDeclaration(methodDeclaration);
        
        // Clonar argumentos (referencias, no copias profundas)
        for (ASTNode arg : arguments) {
            clone.addArgument(arg);
        }
        
        return clone;
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
        return "MethodCallNode{" +
               "line=" + getLineNumber() +
               ", method='" + methodName + '\'' +
               ", class='" + (className != null ? className : "unknown") + '\'' +
               ", static=" + isStaticCall +
               ", constructor=" + isConstructorCall +
               ", args=" + arguments.size() +
               '}';
    }
    
    // NUEVO: Método para comparar con otra llamada (útil para testing)
    public boolean equals(MethodCallNode other) {
        if (other == null) return false;
        
        if (!methodName.equals(other.methodName)) return false;
        if (arguments.size() != other.arguments.size()) return false;
        if (isStaticCall != other.isStaticCall) return false;
        if (isConstructorCall != other.isConstructorCall) return false;
        
        return true;
    }
    
    // NUEVO: Método para generar código fuente
    public String toSourceCode() {
        StringBuilder sb = new StringBuilder();
        
        // Objeto
        if (object instanceof IdentifierNode) {
            sb.append(((IdentifierNode) object).getName());
        } else if (object.getClass() == FieldAccessNode.class) {
            // Usar método simplificado
            try {
                FieldAccessNode fieldAccess = (FieldAccessNode) object;
                if (fieldAccess.getObject() instanceof IdentifierNode) {
                    sb.append(((IdentifierNode) fieldAccess.getObject()).getName())
                      .append(".")
                      .append(fieldAccess.getFieldName());
                } else {
                    sb.append("object.");
                    sb.append(fieldAccess.getFieldName());
                }
            } catch (Exception e) {
                sb.append("object");
            }
        } else {
            sb.append("object");
        }
        
        sb.append(".").append(methodName).append("(");
        
        // Argumentos
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("arg").append(i);
        }
        
        sb.append(")");
        return sb.toString();
    }
    
    // NUEVO: Verificar si es llamada encadenada (obj.metodo1().metodo2())
    public boolean isChainedCall() {
        return object.getClass() == MethodCallNode.class;
    }
    
    // NUEVO: Obtener tipo de retorno esperado (si hay declaración)
    public String getExpectedReturnType() {
        if (methodDeclaration != null) {
            return methodDeclaration.getReturnType().getTypeName();
        }
        return "void";
    }
    
    // NUEVO: Obtener información de parámetros esperados
    public int getExpectedParamCount() {
        if (methodDeclaration != null) {
            return methodDeclaration.getParameterCount();
        }
        return arguments.size(); // Asumir que los argumentos actuales son correctos
    }
}