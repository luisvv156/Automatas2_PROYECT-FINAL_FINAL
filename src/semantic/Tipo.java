package semantic;

public enum Tipo {
    INT,
    FLOAT, 
    STRING,
    BOOLEAN,
    VOID,
    UNKNOWN,
    // Agregar tipos de array
    INT_ARRAY,
    FLOAT_ARRAY, 
    STRING_ARRAY,
    BOOLEAN_ARRAY;

    public static boolean esCompatible(Tipo tipo1, Tipo tipo2) {
        if (tipo1 == UNKNOWN || tipo2 == UNKNOWN) {
            return true; // Permitir durante desarrollo
        }
        
        // Arrays son compatibles solo con arrays del mismo tipo base
        if (esTipoArray(tipo1) && esTipoArray(tipo2)) {
            return getTipoBaseArray(tipo1) == getTipoBaseArray(tipo2);
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
        
        return variable == valor;
    }

    public static boolean esNumerico(Tipo tipo) {
        return tipo == INT || tipo == FLOAT;
    }

    public static boolean esLogico(Tipo tipo) {
        return tipo == BOOLEAN;
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

    @Override
    public String toString() {
        switch (this) {
            case INT_ARRAY: return "int[]";
            case FLOAT_ARRAY: return "float[]";
            case STRING_ARRAY: return "string[]";
            case BOOLEAN_ARRAY: return "boolean[]";
            default: return super.toString().toLowerCase();
        }
    }
}