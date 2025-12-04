package ast;

import java.util.ArrayList;
import java.util.List;

public class MethodDeclNode extends ASTNode {
    private String methodName;
    private TypeNode returnType;
    private List<VariableDeclNode> parameters;
    private BlockNode body;
    private boolean isStatic;
    private String visibility; // "public", "private", o null
    private boolean isAbstract; // NUEVO: para métodos abstractos
    private boolean isConstructor; // NUEVO: indica si es constructor
    private String className; // NUEVO: nombre de la clase contenedora

    public MethodDeclNode(int lineNumber, String methodName) {
        super(lineNumber);
        this.methodName = methodName;
        this.parameters = new ArrayList<>();
        this.isStatic = false;
        this.visibility = null;
        this.isAbstract = false;
        this.isConstructor = false;
        this.className = null;
        this.returnType = new TypeNode(lineNumber, "void");
    }
    
    // NUEVO: Constructor para métodos con nombre de clase
    public MethodDeclNode(int lineNumber, String methodName, String className) {
        this(lineNumber, methodName);
        this.className = className;
        this.isConstructor = methodName.equals(className); // Es constructor si tiene mismo nombre que clase
    }

    // ==================== GETTERS Y SETTERS BÁSICOS ====================
    
    public String getMethodName() { 
        return methodName; 
    }
    
    public void setMethodName(String methodName) { 
        this.methodName = methodName; 
    }
    
    public TypeNode getReturnType() { 
        return returnType; 
    }
    
    public void setReturnType(TypeNode returnType) { 
        this.returnType = returnType; 
    }
    
    public void setReturnType(String typeName) { 
        this.returnType = new TypeNode(getLineNumber(), typeName); 
    }
    
    public List<VariableDeclNode> getParameters() { 
        return parameters; 
    }
    
    public void addParameter(VariableDeclNode param) { 
        this.parameters.add(param); 
    }
    
    // NUEVO: Agregar múltiples parámetros
    public void addParameters(List<VariableDeclNode> params) {
        this.parameters.addAll(params);
    }
    
    // NUEVO: Obtener parámetro por índice
    public VariableDeclNode getParameter(int index) {
        if (index >= 0 && index < parameters.size()) {
            return parameters.get(index);
        }
        return null;
    }
    
    // NUEVO: Obtener número de parámetros
    public int getParameterCount() {
        return parameters.size();
    }
    
    // NUEVO: Obtener nombres de parámetros
    public List<String> getParameterNames() {
        List<String> names = new ArrayList<>();
        for (VariableDeclNode param : parameters) {
            names.add(param.getName());
        }
        return names;
    }
    
    public BlockNode getBody() { 
        return body; 
    }
    
    public void setBody(BlockNode body) { 
        this.body = body; 
    }
    
    public boolean isStatic() { 
        return isStatic; 
    }
    
    public void setStatic(boolean isStatic) { 
        this.isStatic = isStatic; 
    }
    
    public String getVisibility() { 
        return visibility; 
    }
    
    public void setVisibility(String visibility) { 
        this.visibility = visibility; 
    }
    
    // NUEVO: Métodos para visibilidad
    public boolean isPublic() {
        return "public".equals(visibility);
    }
    
    public boolean isPrivate() {
        return "private".equals(visibility);
    }
    
    public boolean hasVisibility() {
        return visibility != null && !visibility.isEmpty();
    }

    // ==================== MÉTODOS PARA MÉTODOS ABSTRACTOS ====================
    
    public boolean isAbstract() {
        return isAbstract;
    }
    
    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }
    
    // NUEVO: Verificar si es concreto (no abstracto)
    public boolean isConcrete() {
        return !isAbstract;
    }
    
    // NUEVO: Verificar si tiene cuerpo
    public boolean hasBody() {
        return body != null;
    }

    // ==================== MÉTODOS PARA CONSTRUCTORES ====================
    
    public boolean isConstructor() {
        return isConstructor;
    }
    
    public void setConstructor(boolean isConstructor) {
        this.isConstructor = isConstructor;
    }
    
    // NUEVO: Verificar si es constructor por defecto (sin parámetros)
    public boolean isDefaultConstructor() {
        return isConstructor && parameters.isEmpty();
    }
    
    // NUEVO: Verificar si es constructor con parámetros
    public boolean isParameterizedConstructor() {
        return isConstructor && !parameters.isEmpty();
    }

    // ==================== MÉTODOS PARA CLASE CONTENEDORA ====================
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
        // Actualizar si es constructor
        if (methodName != null && methodName.equals(className)) {
            this.isConstructor = true;
        }
    }
    
    public boolean hasClassName() {
        return className != null && !className.isEmpty();
    }
    
    // NUEVO: Obtener nombre completo del método (Clase.metodo)
    public String getFullName() {
        if (className != null) {
            return className + "." + methodName;
        }
        return methodName;
    }

    // ==================== MÉTODOS DE UTILIDAD ====================
    
    // NUEVO: Obtener firma del método (para comparación)
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(methodName).append("(");
        
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(parameters.get(i).getType());
        }
        
        sb.append("):").append(returnType.getTypeName());
        return sb.toString();
    }
    
    // NUEVO: Obtener tipos de parámetros
    public List<String> getParameterTypes() {
        List<String> types = new ArrayList<>();
        for (VariableDeclNode param : parameters) {
            types.add(param.getType());
        }
        return types;
    }
    
    // NUEVO: Verificar si el método está bien formado
    public boolean isValid() {
        return methodName != null && !methodName.isEmpty() && returnType != null;
    }
    
    // NUEVO: Verificar si el método acepta un número específico de parámetros
    public boolean acceptsParameters(int count) {
        return parameters.size() == count;
    }
    
    // NUEVO: Método para debug
    public void debugMethod() {
        System.out.println("=== DECLARACIÓN DE MÉTODO ===");
        System.out.println("Línea: " + getLineNumber());
        System.out.println("Nombre: " + methodName);
        System.out.println("Clase: " + (className != null ? className : "(global)"));
        System.out.println("Constructor: " + isConstructor);
        System.out.println("Estático: " + isStatic);
        System.out.println("Abstracto: " + isAbstract);
        System.out.println("Visibilidad: " + (visibility != null ? visibility : "default"));
        System.out.println("Tipo retorno: " + returnType.getTypeName());
        System.out.println("Parámetros (" + parameters.size() + "):");
        for (VariableDeclNode param : parameters) {
            System.out.println("  - " + param.getName() + " : " + param.getType());
        }
        System.out.println("Tiene cuerpo: " + (body != null));
        System.out.println("Firma: " + getSignature());
        System.out.println("==============================");
    }
    
    // NUEVO: Método para clonar (útil para análisis)
    public MethodDeclNode cloneNode() {
        MethodDeclNode clone = new MethodDeclNode(getLineNumber(), methodName, className);
        clone.setReturnType(returnType);
        clone.setStatic(isStatic);
        clone.setVisibility(visibility);
        clone.setAbstract(isAbstract);
        clone.setConstructor(isConstructor);
        
        // Clonar parámetros
        for (VariableDeclNode param : parameters) {
            clone.addParameter(param);
        }
        
        // Clonar cuerpo (referencia, no copia profunda)
        clone.setBody(body);
        
        return clone;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    
    // NUEVO: Sobrescribir toString para mejor debugging
    @Override
    public String toString() {
        return "MethodDeclNode{" +
               "line=" + getLineNumber() +
               ", name='" + methodName + '\'' +
               ", class='" + (className != null ? className : "global") + '\'' +
               ", constructor=" + isConstructor +
               ", static=" + isStatic +
               ", abstract=" + isAbstract +
               ", params=" + parameters.size() +
               ", returnType='" + returnType.getTypeName() + '\'' +
               '}';
    }
    
    // NUEVO: Método para comparar con otro método (útil para testing)
    public boolean equals(MethodDeclNode other) {
        if (other == null) return false;
        
        if (!methodName.equals(other.methodName)) return false;
        if (parameters.size() != other.parameters.size()) return false;
        if (isConstructor != other.isConstructor) return false;
        if (isStatic != other.isStatic) return false;
        
        // Comparar tipo de retorno
        if (!returnType.getTypeName().equals(other.returnType.getTypeName())) {
            return false;
        }
        
        return true;
    }
    
    // NUEVO: Método para generar código fuente aproximado
    public String toSourceCode() {
        StringBuilder sb = new StringBuilder();
        
        // Visibilidad
        if (visibility != null) {
            sb.append(visibility).append(" ");
        }
        
        // Estático
        if (isStatic) {
            sb.append("static ");
        }
        
        // Tipo de retorno (excepto para constructores)
        if (!isConstructor) {
            sb.append(returnType.getTypeName()).append(" ");
        }
        
        // Nombre del método
        sb.append(methodName).append("(");
        
        // Parámetros
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) sb.append(", ");
            VariableDeclNode param = parameters.get(i);
            sb.append(param.getName()).append(" : ").append(param.getType());
        }
        
        sb.append(")");
        
        // Cuerpo o abstract
        if (isAbstract || body == null) {
            sb.append(";");
        } else {
            sb.append(" {\n");
            sb.append("  // cuerpo del método\n");
            sb.append("}");
        }
        
        return sb.toString();
    }
}