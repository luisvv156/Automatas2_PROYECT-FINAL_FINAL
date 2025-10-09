package semantic;

public enum Tipo {
    INT,
    FLOAT, 
    STRING,
    BOOLEAN,
    VOID,
    UNKNOWN;

    public static boolean esCompatible(Tipo tipo1, Tipo tipo2) {
        if (tipo1 == UNKNOWN || tipo2 == UNKNOWN) {
            return true; // Permitir durante desarrollo
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
        
        return variable == valor;
    }

    public static boolean esNumerico(Tipo tipo) {
        return tipo == INT || tipo == FLOAT;
    }

    public static boolean esLogico(Tipo tipo) {
        return tipo == BOOLEAN;
    }

    public static Tipo fromString(String typeStr) {
        if (typeStr == null) return UNKNOWN;
        switch (typeStr.toLowerCase()) {
            case "int": return INT;
            case "float": return FLOAT;
            case "string": return STRING;
            case "boolean": return BOOLEAN;
            case "void": return VOID;
            default: return UNKNOWN;
        }
    }

    public static Tipo getTipoLiteral(Object value) {
        if (value instanceof Integer) return INT;
        if (value instanceof Double || value instanceof Float) return FLOAT;
        if (value instanceof String) return STRING;
        if (value instanceof Boolean) return BOOLEAN;
        
        // Para números que vienen como Double pero son enteros
        if (value instanceof Double) {
            double doubleValue = (Double) value;
            if (doubleValue == Math.floor(doubleValue) && !Double.isInfinite(doubleValue)) {
                return INT; // Es un número entero representado como double
            }
            return FLOAT;
        }
        
        return UNKNOWN;
    }
}