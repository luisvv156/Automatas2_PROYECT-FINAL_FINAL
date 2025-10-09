package interpreter;

import ast.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Interpreter implements Evaluator {
    @SuppressWarnings("unused") // Suprimir warning de variable no usada
    private Map<String, Object> variables;
    private Map<String, FunctionNode> functions;
    private Stack<Map<String, Object>> scopeStack;
    private int executionDepth;
    private static final int MAX_EXECUTION_DEPTH = 1000;

    public Interpreter() {
        this.variables = new HashMap<>();
        this.functions = new HashMap<>();
        this.scopeStack = new Stack<>();
        this.scopeStack.push(new HashMap<>()); // Scope global
        this.executionDepth = 0;
    }

    public void interpret(ProgramNode program) {
        // Registrar funciones primero
        for (ASTNode node : program.getDeclarations()) {
            if (node instanceof FunctionNode) {
                FunctionNode func = (FunctionNode) node;
                functions.put(func.getFunctionName(), func);
            }
        }

        // Ejecutar código global
        for (ASTNode node : program.getDeclarations()) {
            if (!(node instanceof FunctionNode)) {
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
        scopeStack.peek().put(node.getVariableName(), value);
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
                    return (Integer) left + (Integer) right; // Preservar enteros
                } else if (left instanceof Number && right instanceof Number) {
                    // Si alguno es double, convertir ambos a double
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

    @Override
    public Object evaluate(IdentifierNode node) {
        String name = node.getName();
        
        // CORREGIDO: Manejar booleanos literales
        if (node.isBooleanLiteral()) {
            return "true".equals(name);
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
        // CORREGIDO: Cambiar getVariableName() por getName()
        scopeStack.peek().put(node.getName(), value);
        return value;
    }

    @Override
    public Object evaluate(WhileNode node) {
        Object result = null;
        resetExecutionDepth(); // Resetear contador al inicio del bucle
        while (true) {
            Object condition = evaluate(node.getCondition());
            if (!isTruthy(condition)) {
                break;
            }
            result = evaluate(node.getBody());
            
            // Control de profundidad para bucles infinitos
            checkExecutionDepth();
        }
        return result;
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
}