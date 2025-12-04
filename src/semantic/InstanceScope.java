package semantic;

import java.util.HashMap;
import java.util.Map;

public class InstanceScope extends Scope {
    private String className;
    private ClassSymbol classSymbol;
    private Map<String, Object> fieldValues; // NUEVO: almacena valores actuales de campos

    public InstanceScope(Scope parent, String className, ClassSymbol classSymbol) {
        super(parent);
        this.className = className;
        this.classSymbol = classSymbol;
        this.fieldValues = new HashMap<>();
        initializeFields();
    }

    private void initializeFields() {
        if (classSymbol != null) {
            for (Map.Entry<String, Symbol> entry : classSymbol.getFields().entrySet()) {
                String fieldName = entry.getKey();
                Symbol fieldSymbol = entry.getValue();
                
                // Crear una copia del símbolo para esta instancia
                Symbol instanceField = new Symbol(
                    fieldName, 
                    fieldSymbol.getType(), 
                    getDefaultValue(fieldSymbol.getType()), 
                    false
                );
                
                // Almacenar en tabla de símbolos del scope
                declareSymbol(fieldName, instanceField);
                
                // NUEVO: También almacenar en mapa de valores para acceso rápido
                fieldValues.put(fieldName, getDefaultValue(fieldSymbol.getType()));
            }
        }
    }
    
    // NUEVO: Obtener valor por defecto según tipo
    private Object getDefaultValue(Tipo type) {
        switch (type) {
            case INT:
            case INT_ARRAY:
                return 0;
            case FLOAT:
            case FLOAT_ARRAY:
                return 0.0;
            case STRING:
            case STRING_ARRAY:
                return "";
            case BOOLEAN:
            case BOOLEAN_ARRAY:
                return false;
            case CLASE:
                return null; // Para referencias a otras clases
            default:
                return null;
        }
    }

    public String getClassName() { 
        return className; 
    }
    
    public ClassSymbol getClassSymbol() { 
        return classSymbol; 
    }
    
    // NUEVO: Obtener valor de un campo
    public Object getFieldValue(String fieldName) {
        return fieldValues.get(fieldName);
    }
    
    // NUEVO: Establecer valor de un campo
    public void setFieldValue(String fieldName, Object value) {
        fieldValues.put(fieldName, value);
        
        // Actualizar también en la tabla de símbolos
        Symbol fieldSymbol = resolve(fieldName);
        if (fieldSymbol != null) {
            fieldSymbol.setValue(value);
        }
    }
    
    // NUEVO: Verificar si un campo existe en esta instancia
    public boolean hasField(String fieldName) {
        return fieldValues.containsKey(fieldName);
    }
    
    // NUEVO: Obtener todos los campos de la instancia
    public Map<String, Object> getFieldValues() {
        return new HashMap<>(fieldValues); // Retorna copia para evitar modificaciones externas
    }
    
    // NUEVO: Obtener símbolo de campo de la instancia
    public Symbol getInstanceFieldSymbol(String fieldName) {
        return resolve(fieldName);
    }
    
    // NUEVO: Actualizar símbolo de campo (para análisis semántico)
    public void updateFieldSymbol(String fieldName, Symbol newSymbol) {
        // Actualizar en tabla de símbolos
        getSymbols().put(fieldName, newSymbol);
        
        // Actualizar en mapa de valores si es necesario
        if (!fieldValues.containsKey(fieldName)) {
            fieldValues.put(fieldName, getDefaultValue(newSymbol.getType()));
        }
    }
    
    // NUEVO: Método para clonar el scope de instancia (útil para crear nuevas instancias)
    public InstanceScope cloneForNewInstance(Scope parent) {
        InstanceScope newInstance = new InstanceScope(parent, className, classSymbol);
        
        // Copiar valores actuales de campos
        for (Map.Entry<String, Object> entry : fieldValues.entrySet()) {
            newInstance.setFieldValue(entry.getKey(), entry.getValue());
        }
        
        return newInstance;
    }
    
    // NUEVO: Método para inicializar campos con valores específicos
    public void initializeFieldsWithValues(Map<String, Object> initialValues) {
        for (Map.Entry<String, Object> entry : initialValues.entrySet()) {
            setFieldValue(entry.getKey(), entry.getValue());
        }
    }
    
    // NUEVO: Sobrescribir toString para mejor debugging
    @Override
    public String toString() {
        return "InstanceScope{" +
               "className='" + className + '\'' +
               ", fields=" + fieldValues.keySet() +
               ", parent=" + (getParent() != null) +
               '}';
    }
    
    // NUEVO: Método para debug
    public void debugInstance() {
        System.out.println("=== INSTANCIA DE CLASE: " + className + " ===");
        System.out.println("Campos y valores:");
        for (Map.Entry<String, Object> entry : fieldValues.entrySet()) {
            System.out.println("  " + entry.getKey() + " = " + entry.getValue() + 
                             " (tipo: " + getInstanceFieldSymbol(entry.getKey()).getType() + ")");
        }
        System.out.println("Total campos: " + fieldValues.size());
        System.out.println("===============================");
    }
}