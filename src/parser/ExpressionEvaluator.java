package parser;

import ast.*;
import java.util.Map;

public class ExpressionEvaluator implements ASTVisitor {
    private Map<String, Object> symbolTable;
    private Object result;

    public ExpressionEvaluator(Map<String, Object> symbolTable) {
        this.symbolTable = symbolTable;
    }

    public Object evaluate(ASTNode node) {
        node.accept(this);
        return result;
    }

    // Método auxiliar para evaluar recursivamente
    private Object evaluateNode(ASTNode node) {
        node.accept(this);
        return result;
    }

    @Override
    public void visit(BinaryExpression node) {
        Object leftVal = evaluateNode(node.getLeft());
        Object rightVal = evaluateNode(node.getRight());

        if (leftVal == null || rightVal == null) {
            throw new RuntimeException("Variable no definida en expresión");
        }

        // Verificar tipos compatibles
        if (!leftVal.getClass().equals(rightVal.getClass())) {
            throw new RuntimeException("Tipos incompatibles: " + 
                leftVal.getClass().getSimpleName() + " y " + 
                rightVal.getClass().getSimpleName());
        }

        switch (node.getOperator()) {
            case "+":
                if (leftVal instanceof Number) {
                    result = ((Number) leftVal).doubleValue() + ((Number) rightVal).doubleValue();
                } else if (leftVal instanceof String) {
                    result = leftVal.toString() + rightVal.toString();
                }
                break;
            case "-":
                result = ((Number) leftVal).doubleValue() - ((Number) rightVal).doubleValue();
                break;
            case "*":
                result = ((Number) leftVal).doubleValue() * ((Number) rightVal).doubleValue();
                break;
            case "/":
                if (((Number) rightVal).doubleValue() == 0) {
                    throw new RuntimeException("División por cero");
                }
                result = ((Number) leftVal).doubleValue() / ((Number) rightVal).doubleValue();
                break;
            case "==":
                result = leftVal.equals(rightVal);
                break;
            case "!=":
                result = !leftVal.equals(rightVal);
                break;
            case "<":
                result = ((Number) leftVal).doubleValue() < ((Number) rightVal).doubleValue();
                break;
            case ">":
                result = ((Number) leftVal).doubleValue() > ((Number) rightVal).doubleValue();
                break;
            case "<=":
                result = ((Number) leftVal).doubleValue() <= ((Number) rightVal).doubleValue();
                break;
            case ">=":
                result = ((Number) leftVal).doubleValue() >= ((Number) rightVal).doubleValue();
                break;
            case "&&":
                result = (Boolean) leftVal && (Boolean) rightVal;
                break;
            case "||":
                result = (Boolean) leftVal || (Boolean) rightVal;
                break;
            default:
                throw new RuntimeException("Operador no soportado: " + node.getOperator());
        }
    }

    @Override
    public void visit(UnaryExpressionNode node) {
        Object exprValue = evaluateNode(node.getExpression());
        
        switch (node.getOperator()) {
            case "-":
                if (exprValue instanceof Number) {
                    result = -((Number) exprValue).doubleValue();
                } else {
                    throw new RuntimeException("Operador '-' no aplicable a tipo: " + exprValue.getClass().getSimpleName());
                }
                break;
            case "!":
                if (exprValue instanceof Boolean) {
                    result = !(Boolean) exprValue;
                } else {
                    throw new RuntimeException("Operador '!' no aplicable a tipo: " + exprValue.getClass().getSimpleName());
                }
                break;
            default:
                throw new RuntimeException("Operador unario no soportado: " + node.getOperator());
        }
    }

    @Override
    public void visit(IdentifierNode node) {
        result = symbolTable.get(node.getName());
        if (result == null && !node.isBooleanLiteral()) {
            throw new RuntimeException("Variable no definida: " + node.getName());
        }
        
        // Manejar booleanos literales "true" y "false"
        if (node.isBooleanLiteral()) {
            result = "true".equals(node.getName());
        }
    }

    @Override
    public void visit(LiteralNode node) {
        result = node.getValue();
    }

    @Override
    public void visit(PrintNode node) {
        // Para evaluación de expresiones, simplemente evaluamos el valor
        if (node.getValue() != null) {
            result = evaluateNode(node.getValue());
        } else {
            result = null;
        }
    }

    @Override
    public void visit(ArrayNode node) {
        // Para evaluación de expresiones, no necesitas implementar esto
        // a menos que estés haciendo evaluación estática
    }

    @Override
    public void visit(ArrayAccessNode node) {
        // Para evaluación de expresiones, no necesitas implementar esto
        // a menos que estés haciendo evaluación estática
    }

    // ========== NUEVOS MÉTODOS PARA CLASES ==========
    
    @Override
    public void visit(ClassDeclNode node) {
        // Para evaluación de expresiones, las declaraciones de clase no producen valor
        result = null;
    }
    
    @Override
    public void visit(MethodDeclNode node) {
        // Para evaluación de expresiones, las declaraciones de método no producen valor
        result = null;
    }
    
    @Override
    public void visit(ClassInstanceNode node) {
        // La creación de instancias podría evaluarse si estamos en tiempo de ejecución
        // pero para evaluación estática, simplemente retornamos null
        result = null;
    }
    
    @Override
    public void visit(FieldAccessNode node) {
        // Para evaluación de expresiones, el acceso a campos podría evaluarse
        // pero para evaluación estática, simplemente retornamos null
        result = null;
    }
    
    @Override
    public void visit(MethodCallNode node) {
        // Para evaluación de expresiones, las llamadas a método podrían evaluarse
        // pero para evaluación estática, simplemente retornamos null
        result = null;
    }

    // Implementaciones vacías para otros nodos
    @Override public void visit(AssignmentNode node) {}
    @Override public void visit(BlockNode node) {}
    @Override public void visit(CallNode node) {}
    @Override public void visit(ExpressionStatementNode node) {}
    @Override public void visit(FunctionNode node) {}
    @Override public void visit(IfNode node) {}
    @Override public void visit(ProgramNode node) {}
    @Override public void visit(ReturnNode node) {}
    @Override public void visit(TypeNode node) {}
    @Override public void visit(VariableDeclNode node) {}
    @Override public void visit(WhileNode node) {}
}