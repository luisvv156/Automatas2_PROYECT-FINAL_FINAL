package ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ClassDeclNode extends ASTNode {
    private String className;
    private String superClassName;
    private List<VariableDeclNode> fields;
    private List<MethodDeclNode> methods;
    private List<MethodDeclNode> constructors; // CORREGIDO: Ahora es List<MethodDeclNode>
    private String visibility; // "public", "private", o null para default
    private boolean isAbstract; // NUEVO: para clases abstractas
    private Map<String, Object> metadata; // NUEVO: metadatos adicionales

    public ClassDeclNode(int lineNumber, String className) {
        super(lineNumber);
        this.className = className;
        this.superClassName = null;
        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.constructors = new ArrayList<>();
        this.visibility = null;
        this.isAbstract = false; // NUEVO
        this.metadata = new HashMap<>(); // NUEVO
    }

    // ==================== GETTERS Y SETTERS BÁSICOS ====================
    
    public String getClassName() { 
        return className; 
    }
    
    public void setClassName(String className) { 
        this.className = className; 
    }
    
    public String getSuperClassName() { 
        return superClassName; 
    }
    
    public void setSuperClassName(String superClassName) { 
        this.superClassName = superClassName; 
    }
    
    public List<VariableDeclNode> getFields() { 
        return fields; 
    }
    
    public void addField(VariableDeclNode field) { 
        this.fields.add(field); 
    }
    
    // NUEVO: Agregar múltiples campos
    public void addFields(List<VariableDeclNode> fields) {
        this.fields.addAll(fields);
    }
    
    // NUEVO: Obtener campo por nombre
    public VariableDeclNode getField(String fieldName) {
        for (VariableDeclNode field : fields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }
    
    // NUEVO: Verificar si tiene campo
    public boolean hasField(String fieldName) {
        return getField(fieldName) != null;
    }

    public List<MethodDeclNode> getMethods() { 
        return methods; 
    }
    
    public void addMethod(MethodDeclNode method) { 
        this.methods.add(method); 
    }
    
    // NUEVO: Agregar múltiples métodos
    public void addMethods(List<MethodDeclNode> methods) {
        this.methods.addAll(methods);
    }
    
    // NUEVO: Obtener método por nombre
    public MethodDeclNode getMethod(String methodName) {
        for (MethodDeclNode method : methods) {
            if (method.getMethodName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }
    
    // NUEVO: Verificar si tiene método
    public boolean hasMethod(String methodName) {
        return getMethod(methodName) != null;
    }
    
    // CORREGIDO: Ahora constructores son MethodDeclNode
    public List<MethodDeclNode> getConstructors() { 
        return constructors; 
    }
    
    public void addConstructor(MethodDeclNode constructor) { 
        this.constructors.add(constructor); 
    }
    
    // NUEVO: Obtener constructor por número de parámetros
    public MethodDeclNode getConstructor(int paramCount) {
        for (MethodDeclNode constructor : constructors) {
            if (constructor.getParameters().size() == paramCount) {
                return constructor;
            }
        }
        return null;
    }
    
    // NUEVO: Obtener constructor por defecto (sin parámetros)
    public MethodDeclNode getDefaultConstructor() {
        return getConstructor(0);
    }
    
    // NUEVO: Verificar si tiene constructores
    public boolean hasConstructors() {
        return !constructors.isEmpty();
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

    // ==================== MÉTODOS PARA CLASES ABSTRACTAS ====================
    
    public boolean isAbstract() {
        return isAbstract;
    }
    
    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }
    
    // NUEVO: Verificar si es concreta
    public boolean isConcrete() {
        return !isAbstract;
    }

    // ==================== MÉTODOS PARA METADATOS ====================
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }

    // ==================== MÉTODOS DE UTILIDAD ====================
    
    // NUEVO: Obtener número total de miembros
    public int getTotalMembers() {
        return fields.size() + methods.size() + constructors.size();
    }
    
    // NUEVO: Obtener todos los miembros (campos y métodos)
    public List<ASTNode> getAllMembers() {
        List<ASTNode> allMembers = new ArrayList<>();
        allMembers.addAll(fields);
        allMembers.addAll(methods);
        allMembers.addAll(constructors);
        return allMembers;
    }
    
    // NUEVO: Verificar si la clase está vacía
    public boolean isEmpty() {
        return fields.isEmpty() && methods.isEmpty() && constructors.isEmpty();
    }
    
    // NUEVO: Obtener nombres de todos los campos
    public List<String> getFieldNames() {
        List<String> names = new ArrayList<>();
        for (VariableDeclNode field : fields) {
            names.add(field.getName());
        }
        return names;
    }
    
    // NUEVO: Obtener nombres de todos los métodos
    public List<String> getMethodNames() {
        List<String> names = new ArrayList<>();
        for (MethodDeclNode method : methods) {
            names.add(method.getMethodName());
        }
        return names;
    }
    
    // NUEVO: Verificar si hereda de otra clase
    public boolean hasSuperClass() {
        return superClassName != null && !superClassName.isEmpty();
    }
    
    // NUEVO: Obtener información de herencia
    public String getInheritanceInfo() {
        if (hasSuperClass()) {
            return className + " extends " + superClassName;
        } else {
            return className + " (no inheritance)";
        }
    }
    
    // NUEVO: Método para validar la clase
    public boolean isValid() {
        return className != null && !className.isEmpty();
    }
    
    // NUEVO: Método para clonar la clase (útil para análisis)
    public ClassDeclNode cloneNode() {
        ClassDeclNode clone = new ClassDeclNode(getLineNumber(), className);
        clone.setSuperClassName(superClassName);
        clone.setVisibility(visibility);
        clone.setAbstract(isAbstract);
        
        // Clonar campos
        for (VariableDeclNode field : fields) {
            clone.addField(field);
        }
        
        // Clonar métodos
        for (MethodDeclNode method : methods) {
            clone.addMethod(method);
        }
        
        // Clonar constructores
        for (MethodDeclNode constructor : constructors) {
            clone.addConstructor(constructor);
        }
        
        // Clonar metadatos
        clone.metadata.putAll(metadata);
        
        return clone;
    }
    
    // NUEVO: Método para debug
    public void debugClass() {
        System.out.println("=== DECLARACIÓN DE CLASE ===");
        System.out.println("Línea: " + getLineNumber());
        System.out.println("Nombre: " + className);
        System.out.println("Herencia: " + (hasSuperClass() ? "extends " + superClassName : "ninguna"));
        System.out.println("Visibilidad: " + (visibility != null ? visibility : "default"));
        System.out.println("Abstracta: " + isAbstract);
        System.out.println("Campos (" + fields.size() + "):");
        for (VariableDeclNode field : fields) {
            System.out.println("  - " + field.getName() + " : " + field.getType());
        }
        System.out.println("Métodos (" + methods.size() + "):");
        for (MethodDeclNode method : methods) {
            System.out.println("  - " + method.getMethodName() + "() : " + 
                             method.getReturnType().getTypeName());
        }
        System.out.println("Constructores (" + constructors.size() + "):");
        for (MethodDeclNode constructor : constructors) {
            System.out.println("  - " + constructor.getMethodName() + "(" + 
                             constructor.getParameters().size() + " params)");
        }
        System.out.println("Total miembros: " + getTotalMembers());
        System.out.println("============================");
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    
    // NUEVO: Sobrescribir toString para mejor debugging
    @Override
    public String toString() {
        return "ClassDeclNode{" +
               "line=" + getLineNumber() +
               ", className='" + className + '\'' +
               ", superClass='" + (superClassName != null ? superClassName : "none") + '\'' +
               ", fields=" + fields.size() +
               ", methods=" + methods.size() +
               ", constructors=" + constructors.size() +
               ", abstract=" + isAbstract +
               '}';
    }
    
    // NUEVO: Método para comparar con otra clase (útil para testing)
    public boolean equals(ClassDeclNode other) {
        if (other == null) return false;
        
        if (!className.equals(other.className)) return false;
        if (fields.size() != other.fields.size()) return false;
        if (methods.size() != other.methods.size()) return false;
        if (constructors.size() != other.constructors.size()) return false;
        
        // Comparar superclase
        if (superClassName == null) {
            if (other.superClassName != null) return false;
        } else if (!superClassName.equals(other.superClassName)) {
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
        
        // Palabra clave class
        sb.append("class ").append(className);
        
        // Herencia
        if (superClassName != null) {
            sb.append(" extends ").append(superClassName);
        }
        
        sb.append(" {\n");
        
        // Campos
        for (VariableDeclNode field : fields) {
            sb.append("  ").append(field.getName()).append(" : ").append(field.getType()).append(";\n");
        }
        
        if (!fields.isEmpty() && !methods.isEmpty()) {
            sb.append("\n");
        }
        
        // Métodos
        for (MethodDeclNode method : methods) {
            sb.append("  function ").append(method.getMethodName()).append("(...) {\n");
            sb.append("    // método\n");
            sb.append("  }\n\n");
        }
        
        sb.append("}");
        return sb.toString();
    }
}