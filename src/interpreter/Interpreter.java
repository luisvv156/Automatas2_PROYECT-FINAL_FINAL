package interpreter;
import ast.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class Interpreter implements Evaluator {
    @SuppressWarnings("unused") // Suprimir warning de variable no usada
    private Map<String, Object> variables;
    private Map<String, FunctionNode> functions;
    private Stack<Map<String, Object>> scopeStack;
    private int executionDepth;
    private static final int MAX_EXECUTION_DEPTH = 1000;
    private Map<String, ClassDeclNode> classes;  // Almacenar definiciones de clases
    private Object currentInstance;  // Instancia actual para 'this'


    public Interpreter() {
        this.variables = new HashMap<>();
        this.functions = new HashMap<>();
        this.scopeStack = new Stack<>();
        this.scopeStack.push(new HashMap<>()); // Scope global
        this.executionDepth = 0;
        this.classes = new HashMap<>();  // NUEVO
        this.currentInstance = null;     // NUEVO


    }
    public void interpret(ProgramNode program) {
        // NUEVO: Registrar clases primero
        for (ASTNode node : program.getDeclarations()) {
            if (node instanceof ClassDeclNode) {
                ClassDeclNode classNode = (ClassDeclNode) node;
                classes.put(classNode.getClassName(), classNode);
            }
        }
        
        // Registrar funciones
        for (ASTNode node : program.getDeclarations()) {
            if (node instanceof FunctionNode) {
                FunctionNode func = (FunctionNode) node;
                functions.put(func.getFunctionName(), func);
            }
        }

        // Ejecutar código global
        for (ASTNode node : program.getDeclarations()) {
            if (!(node instanceof FunctionNode) && !(node instanceof ClassDeclNode)) {
                evaluate(node);
            }
        }
    }

    public Object evaluate(ASTNode node) {
        if (node instanceof AssignmentNode) return evaluate((AssignmentNode) node);
        if (node instanceof BinaryExpression) return evaluate((BinaryExpression) node);
        if (node instanceof BlockNode) return evaluate((BlockNode) node);
        if (node instanceof CallNode) return evaluate((CallNode) node);
        if (node instanceof ExpressionStatementNode) return evaluate((ExpressionStatementNode) node);
        if (node instanceof FunctionNode) return evaluate((FunctionNode) node);
        if (node instanceof IdentifierNode) return evaluate((IdentifierNode) node);
        if (node instanceof IfNode) return evaluate((IfNode) node);
        if (node instanceof LiteralNode) return evaluate((LiteralNode) node);
        if (node instanceof PrintNode) return evaluate((PrintNode) node);
        if (node instanceof ProgramNode) return evaluate((ProgramNode) node);
        if (node instanceof ReturnNode) return evaluate((ReturnNode) node);
        if (node instanceof TypeNode) return evaluate((TypeNode) node);
        if (node instanceof VariableDeclNode) return evaluate((VariableDeclNode) node);
        if (node instanceof WhileNode) return evaluate((WhileNode) node);
        if (node instanceof UnaryExpressionNode) return evaluate((UnaryExpressionNode) node); // CORREGIDO: Agregado
        if (node instanceof ArrayNode) { return evaluate((ArrayNode) node);}
        if (node instanceof ArrayAccessNode) return evaluate((ArrayAccessNode) node);
        // NUEVO: Soporte para clases
        if (node instanceof ClassDeclNode) return evaluate((ClassDeclNode) node);
        if (node instanceof ClassInstanceNode) return evaluate((ClassInstanceNode) node);
        if (node instanceof FieldAccessNode) return evaluate((FieldAccessNode) node);
        if (node instanceof MethodCallNode) return evaluate((MethodCallNode) node);
        if (node instanceof MethodDeclNode) return evaluate((MethodDeclNode) node);

        return null;
        
    }

    private void checkExecutionDepth() {
        if (executionDepth++ > MAX_EXECUTION_DEPTH) {
            throw new RuntimeException("Profundidad de ejecución excedida");
        }
    }

    private void resetExecutionDepth() {
        executionDepth = 0;
    }

    private boolean isTruthy(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).doubleValue() != 0;
        if (value instanceof String) return !((String) value).isEmpty();
        return value != null;
    }

    @Override
    public Object evaluate(AssignmentNode node) {
        Object value = evaluate(node.getValue());
        
        // NUEVO: Manejar asignaciones a campos (p.nombre = "Juan")
        if (node.isFieldAssignment()) {
            ASTNode target = node.getTarget();
            
            if (target instanceof FieldAccessNode) {
                FieldAccessNode fieldAccess = (FieldAccessNode) target;
                Object object = evaluate(fieldAccess.getObject());
                
                if (!(object instanceof ClassInstance)) {
                    throw new RuntimeException("No se puede asignar campo a tipo no-clase");
                }
                
                ClassInstance instance = (ClassInstance) object;
                instance.setField(fieldAccess.getFieldName(), value);
                return value;
            }
        }
        
        // ORIGINAL: Asignaciones simples (x = 5)
        // Buscar en todos los scopes desde el más interno
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            Map<String, Object> scope = scopeStack.get(i);
            if (scope.containsKey(node.getVariableName())) {
                scope.put(node.getVariableName(), value);
                return value;
            }
        }
        
        // Si no se encontró, asignar en el scope actual
        scopeStack.peek().put(node.getVariableName(), value);
        return value;
    }
    private Object evaluateArrayAssignment(AssignmentNode node) {
        // Parsear nombre del array e índice
        String[] parts = node.getVariableName().split("\\[");
        String arrayName = parts[0];
        String indexStr = parts[1].substring(0, parts[1].length() - 1); // quitar ] final
        
        // Evaluar array e índice
        Object array = evaluate(new IdentifierNode(node.getLineNumber(), arrayName));
        if (!(array instanceof List)) {
            throw new RuntimeException(arrayName + " no es un array");
        }
        
        @SuppressWarnings("unchecked")
        List<Object> arrayList = (List<Object>) array;
        
        // Evaluar índice (necesitarías parsear la expresión del índice)
        // Esto es una simplificación - en un caso real necesitarías más lógica
        Object indexObj = evaluate(new LiteralNode(node.getLineNumber(), Integer.parseInt(indexStr)));
        if (!(indexObj instanceof Integer)) {
            throw new RuntimeException("Índice de array debe ser entero");
        }
        
        int index = (Integer) indexObj;
        if (index < 0 || index >= arrayList.size()) {
            throw new RuntimeException("Índice fuera de rango: " + index);
        }
        
        // Evaluar y asignar valor
        Object value = evaluate(node.getValue());
        arrayList.set(index, value);
        return value;
    }

    @Override
    public Object evaluate(BinaryExpression node) {
        Object left = evaluate(node.getLeft());
        Object right = evaluate(node.getRight());

        switch (node.getOperator()) {
            case "+":
                // Concatenación de strings o suma numérica
                if (left instanceof String || right instanceof String) {
                    return left.toString() + right.toString();
                } else if (left instanceof Integer && right instanceof Integer) {
                    return (Integer) left + (Integer) right;
                } else if (left instanceof Number && right instanceof Number) {
                    double leftNum = ((Number) left).doubleValue();
                    double rightNum = ((Number) right).doubleValue();
                    return leftNum + rightNum;
                }
                break;
                
            case "-": 
                if (left instanceof Integer && right instanceof Integer) {
                    return (Integer) left - (Integer) right; // Preservar enteros
                } else if (left instanceof Number && right instanceof Number) {
                    double leftNum = ((Number) left).doubleValue();
                    double rightNum = ((Number) right).doubleValue();
                    return leftNum - rightNum;
                }
                break;
                
            case "*":
                if (left instanceof Integer && right instanceof Integer) {
                    return (Integer) left * (Integer) right; // Preservar enteros
                } else if (left instanceof Number && right instanceof Number) {
                    double leftNum = ((Number) left).doubleValue();
                    double rightNum = ((Number) right).doubleValue();
                    return leftNum * rightNum;
                }
                break;
                
            case "/":
                if (left instanceof Integer && right instanceof Integer) {
                    // División entera
                    int rightInt = (Integer) right;
                    if (rightInt == 0) throw new RuntimeException("División por cero");
                    return (Integer) left / rightInt;
                } else if (left instanceof Number && right instanceof Number) {
                    double rightNum = ((Number) right).doubleValue();
                    if (rightNum == 0) throw new RuntimeException("División por cero");
                    return ((Number) left).doubleValue() / rightNum;
                }
                break;
                
            // ... (operadores de comparación y lógicos se mantienen igual)
            case "<": 
                if (left instanceof Number && right instanceof Number) {
                    return ((Number) left).doubleValue() < ((Number) right).doubleValue();
                }
                break;
            case ">": 
                if (left instanceof Number && right instanceof Number) {
                    return ((Number) left).doubleValue() > ((Number) right).doubleValue();
                }
                break;
            case "<=": 
                if (left instanceof Number && right instanceof Number) {
                    return ((Number) left).doubleValue() <= ((Number) right).doubleValue();
                }
                break;
            case ">=": 
                if (left instanceof Number && right instanceof Number) {
                    return ((Number) left).doubleValue() >= ((Number) right).doubleValue();
                }
                break;
            case "==": case "!=":
                boolean equal = left.equals(right);
                return node.getOperator().equals("==") ? equal : !equal;
                
            case "&&": case "||":
                if (left instanceof Boolean && right instanceof Boolean) {
                    boolean leftBool = (Boolean) left;
                    boolean rightBool = (Boolean) right;
                    return node.getOperator().equals("&&") ? 
                        leftBool && rightBool : leftBool || rightBool;
                }
                break;
        }
        
        throw new RuntimeException("Operación inválida: " + 
            left.getClass().getSimpleName() + " " + node.getOperator() + " " + 
            right.getClass().getSimpleName());
    }

    public Object evaluate(UnaryExpressionNode node) {
        Object exprValue = evaluate(node.getExpression());
        
        switch (node.getOperator()) {
            case "-":
                if (exprValue instanceof Integer) {
                    return -(Integer) exprValue; // Preservar entero
                } else if (exprValue instanceof Number) {
                    return -((Number) exprValue).doubleValue();
                } else {
                    throw new RuntimeException("Operador '-' no aplicable a tipo: " + 
                        exprValue.getClass().getSimpleName());
                }
            case "!":
                if (exprValue instanceof Boolean) {
                    return !(Boolean) exprValue;
                } else {
                    throw new RuntimeException("Operador '!' no aplicable a tipo: " + 
                        exprValue.getClass().getSimpleName());
                }
            default:
                throw new RuntimeException("Operador unario no soportado: " + node.getOperator());
        }
    }
    @Override
    public Object evaluate(BlockNode node) {
        scopeStack.push(new HashMap<>());
        Object result = null;
        for (ASTNode stmt : node.getStatements()) {
            result = evaluate(stmt);
        }
        scopeStack.pop();
        return result;
    }

    @Override
    public Object evaluate(CallNode node) {
        // Si la función es 'print', ejecuta como PrintNode
        if (node.getFunctionName().equals("print")) {
            if (node.getArguments().size() > 0) {
                Object value = evaluate(node.getArguments().get(0));
                System.out.println(value);
                return value;
            }
            return null;
        }
    
        FunctionNode function = functions.get(node.getFunctionName());
        if (function == null) {
            throw new RuntimeException("Función no encontrada: " + node.getFunctionName());
        }

        // Guardar scope actual
        Map<String, Object> currentScope = scopeStack.peek();
        
        // Crear nuevo scope para la función
        scopeStack.push(new HashMap<>());
        
        // Pasar parámetros
        // NOTA: Necesitarías implementar la lógica para pasar argumentos a parámetros
        
        // Ejecutar cuerpo de la función
        Object result = null;
        try {
            if (function.getBody() != null) {
                result = evaluate(function.getBody());
            }
        } catch (ReturnException e) {
            result = e.getValue();
        }
        
        // Restaurar scope
        scopeStack.pop();
        scopeStack.push(currentScope);
        
        return result;
    }

    @Override
    public Object evaluate(ExpressionStatementNode node) {
        return evaluate(node.getExpression());
    }

    @Override
    public Object evaluate(FunctionNode node) {
        // Las funciones se registran pero no se ejecutan directamente
        functions.put(node.getFunctionName(), node);
        return null;
    }

    // NUEVO: Evaluar declaración de clase (solo registra, no ejecuta)
    public Object evaluate(ClassDeclNode node) {
        classes.put(node.getClassName(), node);
        return null;
    }

    // NUEVO: Evaluar creación de instancia (new Persona())
    public Object evaluate(ClassInstanceNode node) {
        String className = node.getClassName();
        ClassDeclNode classDecl = classes.get(className);
        
        if (classDecl == null) {
            throw new RuntimeException("Clase no encontrada: " + className);
        }
        
        // Crear nueva instancia
        ClassInstance instance = new ClassInstance(className, classDecl);
        
        // TODO: Ejecutar constructor si existe
        // Por ahora, las instancias se crean con campos en null
        
        return instance;
    }

    // NUEVO: Evaluar acceso a campo (p.nombre)
    public Object evaluate(FieldAccessNode node) {
        Object object = evaluate(node.getObject());
        
        if (!(object instanceof ClassInstance)) {
            throw new RuntimeException("No se puede acceder a campo de tipo no-clase");
        }
        
        ClassInstance instance = (ClassInstance) object;
        Object value = instance.getField(node.getFieldName());
        
        if (value == null && !instance.getClassDecl().hasField(node.getFieldName())) {
            throw new RuntimeException("Campo '" + node.getFieldName() + "' no existe en clase '" + instance.getClassName() + "'");
        }
        
        return value;
    }

    // NUEVO: Evaluar llamada a método (p.saludar())
    public Object evaluate(MethodCallNode node) {
        Object object = evaluate(node.getObject());
        
        if (!(object instanceof ClassInstance)) {
            throw new RuntimeException("No se puede llamar método en tipo no-clase");
        }
        
        ClassInstance instance = (ClassInstance) object;
        String methodName = node.getMethodName();
        
        // Buscar el método en la clase
        MethodDeclNode method = instance.getClassDecl().getMethod(methodName);
        if (method == null) {
            throw new RuntimeException("Método '" + methodName + "' no existe en clase '" + instance.getClassName() + "'");
        }
        
        // Guardar instancia actual (para 'this')
        Object savedInstance = currentInstance;
        currentInstance = instance;
        
        // Crear nuevo scope para el método
        scopeStack.push(new HashMap<>());
        
        // Agregar 'this' al scope
        scopeStack.peek().put("this", instance);
        
        // TODO: Pasar argumentos a parámetros
        // Por ahora, métodos sin parámetros funcionarán
        
        // Ejecutar cuerpo del método
        Object result = null;
        try {
            if (method.getBody() != null) {
                result = evaluate(method.getBody());
            }
        } catch (ReturnException e) {
            result = e.getValue();
        } finally {
            // Restaurar scope e instancia
            scopeStack.pop();
            currentInstance = savedInstance;
        }
        
        return result;
    }

    // NUEVO: Evaluar declaración de método (solo registra, no ejecuta)
    public Object evaluate(MethodDeclNode node) {
        // Los métodos se ejecutan cuando se llaman, no cuando se declaran
        return null;
    }

    @Override
    public Object evaluate(IdentifierNode node) {
        String name = node.getName();
        
        // Manejar booleanos literales
        if (node.isBooleanLiteral()) {
            return "true".equals(name);
        }
        
        // NUEVO: Manejar 'this'
        if ("this".equals(name)) {
            if (currentInstance == null) {
                throw new RuntimeException("'this' no está disponible fuera de un método de instancia");
            }
            return currentInstance;
        }
        
        // Buscar en scopes desde el más interno al más externo
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            Map<String, Object> scope = scopeStack.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        
        throw new RuntimeException("Variable no definida: " + name);
    }

    @Override
    public Object evaluate(IfNode node) {
        Object condition = evaluate(node.getCondition());
        if (isTruthy(condition)) {
            return evaluate(node.getThenBlock());
        } else if (node.getElseBlock() != null) {
            return evaluate(node.getElseBlock());
        }
        return null;
    }

    @Override
    public Object evaluate(LiteralNode node) {
        return node.getValue();
    }

    @Override
    public Object evaluate(PrintNode node) {
        Object value = evaluate(node.getValue());
        
        // Formatear la salida para evitar .0 en enteros
        if (value instanceof Integer) {
            System.out.println(value);
        } else if (value instanceof Double) {
            double doubleValue = (Double) value;
            // Si es un número entero representado como double, imprimir sin .0
            if (doubleValue == Math.floor(doubleValue) && !Double.isInfinite(doubleValue)) {
                System.out.println((int) doubleValue);
            } else {
                System.out.println(doubleValue);
            }
        } else {
            System.out.println(value);
        }
        
        return value;
    }

    @Override
    public Object evaluate(ProgramNode node) {
        Object result = null;
        for (ASTNode declaration : node.getDeclarations()) {
            result = evaluate(declaration);
        }
        return result;
    }

    @Override
    public Object evaluate(ReturnNode node) {
        Object value = node.getValue() != null ? evaluate(node.getValue()) : null;
        throw new ReturnException(value);
    }

    @Override
    public Object evaluate(TypeNode node) {
        return null;
    }

    @Override
    public Object evaluate(VariableDeclNode node) {
        Object value = null;
        if (node.getInitialValue() != null) {
            value = evaluate(node.getInitialValue());
        }
        
        // Para arrays, crear una lista si no hay valor inicial
        if (node.getType().endsWith("[]") && value == null) {
            value = new ArrayList<Object>();
        }
        
        scopeStack.peek().put(node.getName(), value);
        return value;
    }

    @Override
    public Object evaluate(WhileNode node) {
        Object result = null;
        resetExecutionDepth();
        
        while (true) {
            Object condition = evaluate(node.getCondition());
            if (!isTruthy(condition)) {
                break;
            }
            result = evaluate(node.getBody());
            checkExecutionDepth();
        }
        return result;
    }

    @Override
    public Object evaluate(ArrayNode node) {
        List<Object> arrayValues = new ArrayList<>();
        for (ASTNode element : node.getElements()) {
            arrayValues.add(evaluate(element));
        }
        return arrayValues;
    }
    @Override
    public Object evaluate(ArrayAccessNode node) {
        // Obtener el array
        Object array = evaluate(new IdentifierNode(node.getLineNumber(), node.getArrayName()));
        if (!(array instanceof List)) {
            throw new RuntimeException(node.getArrayName() + " no es un array");
        }
        
        @SuppressWarnings("unchecked")
        List<Object> arrayList = (List<Object>) array;
        
        // Obtener y verificar el índice
        Object indexObj = evaluate(node.getIndex());
        if (!(indexObj instanceof Integer)) {
            throw new RuntimeException("Índice de array debe ser entero");
        }
        
        int index = (Integer) indexObj;
        if (index < 0 || index >= arrayList.size()) {
            throw new RuntimeException("Índice fuera de rango: " + index + ", tamaño del array: " + arrayList.size());
        }
        
        return arrayList.get(index);
    }


    // CORREGIDO: Hacer pública la clase ReturnException para que sea accesible
    public static class ReturnException extends RuntimeException {
        private final Object value;
        
        public ReturnException(Object value) {
            this.value = value;
        }
        
        public Object getValue() {
            return value;
        }
    }
    public static class ClassInstance {
        private String className;
        private Map<String, Object> fields;
        private ClassDeclNode classDecl;
        
        public ClassInstance(String className, ClassDeclNode classDecl) {
            this.className = className;
            this.classDecl = classDecl;
            this.fields = new HashMap<>();
            
            // Inicializar campos con valores por defecto
            for (VariableDeclNode field : classDecl.getFields()) {
                fields.put(field.getName(), null);
            }
        }
        
        public String getClassName() {
            return className;
        }
        
        public Object getField(String fieldName) {
            return fields.get(fieldName);
        }
        
        public void setField(String fieldName, Object value) {
            fields.put(fieldName, value);
        }
        
        public ClassDeclNode getClassDecl() {
            return classDecl;
        }
        
        @Override
        public String toString() {
            return className + "@" + Integer.toHexString(hashCode());
        }
    }
}