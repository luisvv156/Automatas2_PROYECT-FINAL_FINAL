package semantic;

public enum Tipo {
    INT,
    FLOAT, 
    STRING,
    BOOLEAN,
    VOID,
    UNKNOWN,
    // Tipos de array
    INT_ARRAY,
    FLOAT_ARRAY, 
    STRING_ARRAY,
    BOOLEAN_ARRAY,
    // NUEVO: Tipo para clases
    CLASE,
    // NUEVO: Tipo para instancias de clase específicas (se manejará dinámicamente)
    INSTANCIA;

    public static boolean esCompatible(Tipo tipo1, Tipo tipo2) {
        if (tipo1 == UNKNOWN || tipo2 == UNKNOWN) {
            return true; // Permitir durante desarrollo
        }
        
        // Arrays son compatibles solo con arrays del mismo tipo base
        if (esTipoArray(tipo1) && esTipoArray(tipo2)) {
            return getTipoBaseArray(tipo1) == getTipoBaseArray(tipo2);
        }
        
        // NUEVO: Las clases son compatibles con otras clases del mismo tipo
        if (tipo1 == CLASE && tipo2 == CLASE) {
            return true; // La compatibilidad específica se maneja en el análisis semántico
        }
        
        // NUEVO: Las instancias son compatibles con instancias del mismo tipo
        if (tipo1 == INSTANCIA && tipo2 == INSTANCIA) {
            return true; // La compatibilidad específica se maneja en el análisis semántico
        }
        
        return tipo1 == tipo2;
    }

    public static boolean esCompatibleParaAsignacion(Tipo variable, Tipo valor) {
        if (variable == UNKNOWN || valor == UNKNOWN) {
            return true;
        }
        
        // Permite asignar int a float (pero no float a int)
        if (variable == FLOAT && valor == INT) {
            return true;
        }
        
        // Para arrays, deben ser del mismo tipo
        if (esTipoArray(variable) && esTipoArray(valor)) {
            return getTipoBaseArray(variable) == getTipoBaseArray(valor);
        }
        
        // NUEVO: Permite asignar una clase a una variable de tipo CLASE
        if (variable == CLASE && valor == CLASE) {
            return true; // La compatibilidad específica se maneja en el análisis semántico
        }
        
        // NUEVO: Permite asignar una instancia a una variable de tipo INSTANCIA
        if (variable == INSTANCIA && valor == INSTANCIA) {
            return true; // La compatibilidad específica se maneja en el análisis semántico
        }
        
        return variable == valor;
    }

    public static boolean esNumerico(Tipo tipo) {
        return tipo == INT || tipo == FLOAT;
    }

    public static boolean esLogico(Tipo tipo) {
        return tipo == BOOLEAN;
    }
    
    // NUEVO: Verificar si es tipo clase
    public static boolean esClase(Tipo tipo) {
        return tipo == CLASE || tipo == INSTANCIA;
    }

    public static boolean esTipoArray(Tipo tipo) {
        return tipo == INT_ARRAY || tipo == FLOAT_ARRAY || 
               tipo == STRING_ARRAY || tipo == BOOLEAN_ARRAY;
    }

    public static Tipo getTipoBaseArray(Tipo arrayType) {
        switch (arrayType) {
            case INT_ARRAY: return INT;
            case FLOAT_ARRAY: return FLOAT;
            case STRING_ARRAY: return STRING;
            case BOOLEAN_ARRAY: return BOOLEAN;
            default: return arrayType;
        }
    }

    public static Tipo fromString(String typeStr) {
        if (typeStr == null) return UNKNOWN;
        
        // NUEVO: Verificar si es un nombre de clase (no empieza con minúscula)
        if (!typeStr.isEmpty()) {
            char firstChar = typeStr.charAt(0);
            if (Character.isUpperCase(firstChar) && !typeStr.endsWith("[]")) {
                // Posiblemente es el nombre de una clase
                return CLASE;
            }
        }
        
        // Tipos básicos y arrays
        switch (typeStr.toLowerCase()) {
            case "int": return INT;
            case "float": return FLOAT;
            case "string": return STRING;
            case "boolean": return BOOLEAN;
            case "void": return VOID;
            case "int[]": return INT_ARRAY;
            case "float[]": return FLOAT_ARRAY;
            case "string[]": return STRING_ARRAY;
            case "boolean[]": return BOOLEAN_ARRAY;
            case "class": return CLASE;
            default: return UNKNOWN;
        }
    }
    
    // NUEVO: Método para obtener tipo de array a partir de tipo base
    public static Tipo getTipoArray(Tipo baseType) {
        switch (baseType) {
            case INT: return INT_ARRAY;
            case FLOAT: return FLOAT_ARRAY;
            case STRING: return STRING_ARRAY;
            case BOOLEAN: return BOOLEAN_ARRAY;
            default: return UNKNOWN;
        }
    }

    public static Tipo getTipoLiteral(Object value) {
        if (value instanceof Integer) return INT;
        if (value instanceof Double || value instanceof Float) return FLOAT;
        if (value instanceof String) return STRING;
        if (value instanceof Boolean) return BOOLEAN;
        return UNKNOWN;
    }
    
    // NUEVO: Método para verificar compatibilidad para operaciones
    public static boolean esCompatibleParaOperacion(Tipo tipo1, Tipo tipo2, String operador) {
        if (tipo1 == UNKNOWN || tipo2 == UNKNOWN) {
            return true;
        }
        
        // Operaciones aritméticas: ambos deben ser numéricos
        if (operador.equals("+") || operador.equals("-") || 
            operador.equals("*") || operador.equals("/")) {
            return esNumerico(tipo1) && esNumerico(tipo2);
        }
        
        // Operaciones lógicas: ambos deben ser booleanos
        if (operador.equals("&&") || operador.equals("||")) {
            return tipo1 == BOOLEAN && tipo2 == BOOLEAN;
        }
        
        // Operaciones de comparación: compatibles numéricos o del mismo tipo
        if (operador.equals("<") || operador.equals(">") || 
            operador.equals("<=") || operador.equals(">=")) {
            return esNumerico(tipo1) && esNumerico(tipo2);
        }
        
        // Operaciones de igualdad: compatibles
        if (operador.equals("==") || operador.equals("!=")) {
            return esCompatible(tipo1, tipo2);
        }
        
        // Concatenación: al menos uno debe ser string
        if (operador.equals("+")) {
            return tipo1 == STRING || tipo2 == STRING || 
                   (esNumerico(tipo1) && esNumerico(tipo2));
        }
        
        return false;
    }

    @Override
    public String toString() {
        switch (this) {
            case INT_ARRAY: return "int[]";
            case FLOAT_ARRAY: return "float[]";
            case STRING_ARRAY: return "string[]";
            case BOOLEAN_ARRAY: return "boolean[]";
            case CLASE: return "class";
            case INSTANCIA: return "instance";
            default: return super.toString().toLowerCase();
        }
    }
}