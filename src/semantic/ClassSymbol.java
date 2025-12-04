package semantic;

import java.util.HashMap;
import java.util.Map;
import ast.MethodDeclNode;

public class ClassSymbol extends Symbol {
    private Map<String, Symbol> fields;
    private Map<String, Symbol> methods;
    private Map<String, Symbol> constructors; // NUEVO: para constructores
    private String superClassName;
    private ClassSymbol superClass;
    private boolean isAbstract; // NUEVO: soporte para clases abstractas

    public ClassSymbol(String name) {
        super(name, Tipo.CLASE, null, false);
        this.fields = new HashMap<>();
        this.methods = new HashMap<>();
        this.constructors = new HashMap<>(); // NUEVO
        this.superClassName = null;
        this.superClass = null;
        this.isAbstract = false; // NUEVO
    }

    // ==================== MÉTODOS PARA CAMPOS ====================
    
    public Map<String, Symbol> getFields() { 
        return fields; 
    }
    
    public void addField(String name, Symbol field) { 
        fields.put(name, field); 
    }
    
    public Symbol getField(String name) {
        Symbol field = fields.get(name);
        if (field == null && superClass != null) {
            return superClass.getField(name);
        }
        return field;
    }
    
    public boolean hasField(String name) {
        if (fields.containsKey(name)) {
            return true;
        }
        if (superClass != null) {
            return superClass.hasField(name);
        }
        return false;
    }
    
    public Map<String, Symbol> getAllFields() {
        Map<String, Symbol> allFields = new HashMap<>();
        
        // Primero campos de la superclase (si existe)
        if (superClass != null) {
            allFields.putAll(superClass.getAllFields());
        }
        
        // Luego campos de esta clase (sobrescriben los heredados)
        allFields.putAll(fields);
        
        return allFields;
    }

    // ==================== MÉTODOS PARA MÉTODOS ====================
    
    public Map<String, Symbol> getMethods() { 
        return methods; 
    }
    
    public void addMethod(String name, Symbol method) { 
        methods.put(name, method); 
    }
    
    public Symbol getMethod(String name) {
        Symbol method = methods.get(name);
        if (method == null && superClass != null) {
            return superClass.getMethod(name);
        }
        return method;
    }
    
    public boolean hasMethod(String name) {
        if (methods.containsKey(name)) {
            return true;
        }
        if (superClass != null) {
            return superClass.hasMethod(name);
        }
        return false;
    }
    
    public Map<String, Symbol> getAllMethods() {
        Map<String, Symbol> allMethods = new HashMap<>();
        
        // Primero métodos de la superclase (si existe)
        if (superClass != null) {
            allMethods.putAll(superClass.getAllMethods());
        }
        
        // Luego métodos de esta clase (sobrescriben los heredados)
        allMethods.putAll(methods);
        
        return allMethods;
    }
    
    // NUEVO: Obtener método específico con información del nodo AST
    public MethodDeclNode getMethodNode(String methodName) {
        Symbol methodSymbol = getMethod(methodName);
        if (methodSymbol != null && methodSymbol.isMethod()) {
            return methodSymbol.getMethodNode();
        }
        return null;
    }

    // ==================== MÉTODOS PARA CONSTRUCTORES ====================
    
    public Map<String, Symbol> getConstructors() { 
        return constructors; 
    }
    
    public void addConstructor(String name, Symbol constructor) { 
        constructors.put(name, constructor); 
    }
    
    public Symbol getConstructor(String name) {
        return constructors.get(name);
    }
    
    public boolean hasConstructor(String name) {
        return constructors.containsKey(name);
    }
    
    // NUEVO: Obtener el constructor por defecto (si existe)
    public Symbol getDefaultConstructor() {
        return constructors.get(getName()); // Constructor con mismo nombre que la clase
    }

    // ==================== MÉTODOS PARA HERENCIA ====================
    
    public String getSuperClassName() { 
        return superClassName; 
    }
    
    public void setSuperClassName(String superClassName) { 
        this.superClassName = superClassName; 
    }
    
    public ClassSymbol getSuperClass() { 
        return superClass; 
    }
    
    public void setSuperClass(ClassSymbol superClass) { 
        this.superClass = superClass; 
    }
    
    // NUEVO: Verificar si esta clase hereda de otra clase
    public boolean inheritsFrom(String className) {
        if (getName().equals(className)) {
            return true; // Una clase se considera que hereda de sí misma
        }
        
        ClassSymbol current = superClass;
        while (current != null) {
            if (current.getName().equals(className)) {
                return true;
            }
            current = current.getSuperClass();
        }
        
        return false;
    }
    
    // NUEVO: Obtener la jerarquía de clases completa
    public java.util.List<String> getClassHierarchy() {
        java.util.List<String> hierarchy = new java.util.ArrayList<>();
        ClassSymbol current = this;
        
        while (current != null) {
            hierarchy.add(current.getName());
            current = current.getSuperClass();
        }
        
        return hierarchy;
    }

    // ==================== MÉTODOS PARA CLASES ABSTRACTAS ====================
    
    public boolean isAbstract() {
        return isAbstract;
    }
    
    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }
    
    // NUEVO: Verificar si la clase es concreta (no abstracta)
    public boolean isConcrete() {
        return !isAbstract;
    }

    // ==================== MÉTODOS DE UTILIDAD ====================
    
    // NUEVO: Verificar si un miembro es accesible desde esta clase
    public boolean isMemberAccessible(String memberName, boolean isMethod) {
        if (isMethod) {
            return hasMethod(memberName);
        } else {
            return hasField(memberName);
        }
    }
    
    // NUEVO: Obtener el tipo de un campo
    public Tipo getFieldType(String fieldName) {
        Symbol field = getField(fieldName);
        return field != null ? field.getType() : Tipo.UNKNOWN;
    }
    
    // NUEVO: Obtener el tipo de retorno de un método
    public Tipo getMethodReturnType(String methodName) {
        Symbol method = getMethod(methodName);
        return method != null ? method.getType() : Tipo.UNKNOWN;
    }
    
    // NUEVO: Verificar si la clase está bien formada (sin ciclos en herencia)
    public boolean isWellFormed() {
        java.util.Set<String> visited = new java.util.HashSet<>();
        ClassSymbol current = this;
        
        while (current != null) {
            if (visited.contains(current.getName())) {
                return false; // Ciclo detectado
            }
            visited.add(current.getName());
            current = current.getSuperClass();
        }
        
        return true;
    }
    
    // NUEVO: Contar número total de miembros
    public int getTotalMembers() {
        return fields.size() + methods.size() + constructors.size();
    }
    
    // NUEVO: Método para debug
    public void debugClass() {
        System.out.println("=== CLASE: " + getName() + " ===");
        System.out.println("Superclase: " + (superClassName != null ? superClassName : "ninguna"));
        System.out.println("Abstracta: " + isAbstract);
        System.out.println("Campos (" + fields.size() + "): " + fields.keySet());
        System.out.println("Métodos (" + methods.size() + "): " + methods.keySet());
        System.out.println("Constructores (" + constructors.size() + "): " + constructors.keySet());
        
        if (superClass != null) {
            System.out.println("--- Herencia ---");
            System.out.println("Jerarquía: " + getClassHierarchy());
        }
        
        System.out.println("Total miembros: " + getTotalMembers());
        System.out.println("===========================");
    }
    
    // NUEVO: Sobrescribir toString para mejor debugging
    @Override
    public String toString() {
        return "ClassSymbol{" +
               "name='" + getName() + '\'' +
               ", fields=" + fields.size() +
               ", methods=" + methods.size() +
               ", superClass=" + (superClassName != null ? superClassName : "none") +
               ", abstract=" + isAbstract +
               '}';
    }
}