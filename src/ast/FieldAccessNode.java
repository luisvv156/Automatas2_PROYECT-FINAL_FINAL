package ast;

public class FieldAccessNode extends ASTNode {
    private ASTNode object;
    private String fieldName;
    private boolean isMethodCall; // NUEVO: indica si es parte de una llamada a método
    private String className; // NUEVO: nombre de la clase del objeto (para análisis semántico)
    private String fullPath; // NUEVO: ruta completa (obj.campo.subcampo)

    public FieldAccessNode(int lineNumber, ASTNode object, String fieldName) {
        super(lineNumber);
        this.object = object;
        this.fieldName = fieldName;
        this.isMethodCall = false;
        this.className = null;
        this.fullPath = null;
    }
    
    // NUEVO: Constructor con información adicional
    public FieldAccessNode(int lineNumber, ASTNode object, String fieldName, boolean isMethodCall) {
        this(lineNumber, object, fieldName);
        this.isMethodCall = isMethodCall;
    }

    // ==================== GETTERS Y SETTERS BÁSICOS ====================
    
    public ASTNode getObject() { 
        return object; 
    }
    
    public void setObject(ASTNode object) { 
        this.object = object; 
    }
    
    public String getFieldName() { 
        return fieldName; 
    }
    
    public void setFieldName(String fieldName) { 
        this.fieldName = fieldName; 
    }
    
    // NUEVO: Obtener nombre completo del campo accedido
    public String getFullFieldName() {
        if (object instanceof IdentifierNode) {
            return ((IdentifierNode) object).getName() + "." + fieldName;
        } else if (object.getClass() == FieldAccessNode.class) {
            return ((FieldAccessNode) object).getFullFieldName() + "." + fieldName;
        }
        return "?.?" + fieldName;
    }

    // ==================== MÉTODOS PARA LLAMADAS A MÉTODO ====================
    
    public boolean isMethodCall() {
        return isMethodCall;
    }
    
    public void setMethodCall(boolean isMethodCall) {
        this.isMethodCall = isMethodCall;
    }
    
    // NUEVO: Verificar si es acceso a campo (no método)
    public boolean isFieldAccess() {
        return !isMethodCall;
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
    
    public String getFullPath() {
        if (fullPath != null) {
            return fullPath;
        }
        return calculateFullPath();
    }
    
    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }
    
    // NUEVO: Calcular ruta completa recursivamente
    private String calculateFullPath() {
        if (object instanceof IdentifierNode) {
            return ((IdentifierNode) object).getName() + "." + fieldName;
        } else if (object.getClass() == FieldAccessNode.class) {
            return ((FieldAccessNode) object).getFullPath() + "." + fieldName;
        } else if (object.getClass() == ClassInstanceNode.class) {
            return "new " + ((ClassInstanceNode) object).getClassName() + "()." + fieldName;
        }
        return "object." + fieldName;
    }
    
    // NUEVO: Obtener el objeto raíz (primer identificador en la cadena)
    public String getRootObjectName() {
        if (object instanceof IdentifierNode) {
            return ((IdentifierNode) object).getName();
        } else if (object.getClass() == FieldAccessNode.class) {
            return ((FieldAccessNode) object).getRootObjectName();
        } else if (object.getClass() == ClassInstanceNode.class) {
            return "new " + ((ClassInstanceNode) object).getClassName();
        }
        return "object";
    }
    
    // NUEVO: Obtener profundidad del acceso (obj.campo.subcampo = profundidad 2)
    public int getAccessDepth() {
        if (object instanceof IdentifierNode) {
            return 1;
        } else if (object.getClass() == FieldAccessNode.class) {
            return ((FieldAccessNode) object).getAccessDepth() + 1;
        }
        return 1;
    }

    // ==================== MÉTODOS DE UTILIDAD ====================
    
    // NUEVO: Verificar si el objeto es 'this'
    public boolean isThisAccess() {
        if (object instanceof IdentifierNode) {
            return "this".equals(((IdentifierNode) object).getName());
        }
        return false;
    }
    
    // NUEVO: Verificar si es acceso a campo de clase estático (ej: Clase.campo)
    public boolean isStaticAccess() {
        if (object instanceof IdentifierNode) {
            String objName = ((IdentifierNode) object).getName();
            // Si el objeto empieza con mayúscula, podría ser nombre de clase
            return objName != null && objName.length() > 0 && 
                   Character.isUpperCase(objName.charAt(0));
        }
        return false;
    }
    
    // NUEVO: Verificar si es acceso válido
    public boolean isValid() {
        return object != null && fieldName != null && !fieldName.isEmpty();
    }
    
    // NUEVO: Método para debug
    public void debugFieldAccess() {
        System.out.println("=== ACCESO A CAMPO ===");
        System.out.println("Línea: " + getLineNumber());
        System.out.println("Objeto tipo: " + object.getClass().getSimpleName());
        System.out.println("Campo: " + fieldName);
        System.out.println("Es llamada a método: " + isMethodCall);
        System.out.println("Clase: " + (className != null ? className : "desconocida"));
        System.out.println("Ruta completa: " + getFullPath());
        System.out.println("Objeto raíz: " + getRootObjectName());
        System.out.println("Profundidad: " + getAccessDepth());
        System.out.println("Es 'this': " + isThisAccess());
        System.out.println("Es estático: " + isStaticAccess());
        System.out.println("Válido: " + isValid());
        System.out.println("======================");
    }
    
    // NUEVO: Método para clonar (útil para análisis)
    public FieldAccessNode cloneNode() {
        FieldAccessNode clone = new FieldAccessNode(getLineNumber(), object, fieldName, isMethodCall);
        clone.setClassName(className);
        clone.setFullPath(fullPath);
        return clone;
    }
    
    // NUEVO: Método para convertir a cadena de acceso
    public String toAccessString() {
        if (object instanceof IdentifierNode) {
            return ((IdentifierNode) object).getName() + "." + fieldName;
        } else if (object.getClass() == FieldAccessNode.class) {
            return ((FieldAccessNode) object).toAccessString() + "." + fieldName;
        }
        return "object." + fieldName;
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
        return "FieldAccessNode{" +
               "line=" + getLineNumber() +
               ", field='" + fieldName + '\'' +
               ", methodCall=" + isMethodCall +
               ", class='" + (className != null ? className : "unknown") + '\'' +
               ", path='" + getFullPath() + '\'' +
               '}';
    }
    
    // NUEVO: Método para comparar con otro acceso (útil para testing)
    public boolean equals(FieldAccessNode other) {
        if (other == null) return false;
        
        if (!fieldName.equals(other.fieldName)) return false;
        if (isMethodCall != other.isMethodCall) return false;
        
        // Comparar objetos (por simplicidad, comparamos sus representaciones de string)
        if (!object.toString().equals(other.object.toString())) {
            return false;
        }
        
        return true;
    }
    
    // NUEVO: Método para generar código fuente
    public String toSourceCode() {
        StringBuilder sb = new StringBuilder();
        
        if (object instanceof IdentifierNode) {
            sb.append(((IdentifierNode) object).getName());
        } else if (object.getClass() == FieldAccessNode.class) {
            sb.append(((FieldAccessNode) object).toSourceCode());
        } else if (object.getClass() == ClassInstanceNode.class) {
            sb.append("new ").append(((ClassInstanceNode) object).getClassName()).append("()");
        } else {
            sb.append("object");
        }
        
        sb.append(".").append(fieldName);
        
        return sb.toString();
    }
    
    // NUEVO: Verificar si es acceso encadenado (obj.campo.subcampo)
    public boolean isChainedAccess() {
        return object.getClass() == FieldAccessNode.class;
    }
    
    // NUEVO: Obtener todos los campos en la cadena
    public java.util.List<String> getFieldChain() {
        java.util.List<String> chain = new java.util.ArrayList<>();
        chain.add(fieldName);
        
        if (object.getClass() == FieldAccessNode.class) {
            chain.addAll(0, ((FieldAccessNode) object).getFieldChain());
        } else if (object instanceof IdentifierNode) {
            chain.add(0, ((IdentifierNode) object).getName());
        }
        
        return chain;
    }
}