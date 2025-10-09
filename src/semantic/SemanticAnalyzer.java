package semantic;

import ast.*;
import util.ManejadorErrores;
import java.util.HashMap;
import java.util.Map;

public class SemanticAnalyzer implements ASTVisitor {

    private ScopeManager scopeManager;
    private ManejadorErrores manejadorErrores;
    private Map<ASTNode, Tipo> tiposExpresiones; // Para almacenar tipos inferidos

    public SemanticAnalyzer() {
        this.scopeManager = new ScopeManager();
        this.manejadorErrores = new ManejadorErrores();
        this.tiposExpresiones = new HashMap<>();
        registrarFuncionesPredefinidas();
    }

    private void registrarFuncionesPredefinidas() {
        Symbol printSymbol = new Symbol("print", Tipo.VOID, null, true);
        scopeManager.getGlobalScope().declareSymbol("print", printSymbol);
    }

    public ManejadorErrores getManejadorErrores() {
        return manejadorErrores;
    }

    public void analyze(ProgramNode program) {
        try {
            // Primera pasada: declarar funciones
            for (ASTNode declaration : program.getDeclarations()) {
                if (declaration instanceof FunctionNode) {
                    FunctionNode func = (FunctionNode) declaration;
                    
                    if (scopeManager.resolve(func.getFunctionName()) != null) {
                        manejadorErrores.agregarError(
                            func.getLineNumber(),
                            "Función '" + func.getFunctionName() + "' ya declarada",
                            "Semántico"
                        );
                        continue;
                    }
                    
                    Symbol funcSymbol = new Symbol(
                        func.getFunctionName(),
                        Tipo.fromString(func.getReturnType()),
                        null,
                        true
                    );
                    funcSymbol.setFunctionNode(func);
                    scopeManager.getGlobalScope().declareSymbol(func.getFunctionName(), funcSymbol);
                }
            }

            // Segunda pasada: analizar cuerpos
            for (ASTNode declaration : program.getDeclarations()) {
                declaration.accept(this);
            }

        } catch (Exception e) {
            manejadorErrores.agregarError(
                0, "Error durante análisis semántico: " + e.getMessage(), "Semántico"
            );
        }
    }

    // ========== MÉTODOS DE INFERENCIA DE TIPOS ==========

    private Tipo getTipoExpresion(ASTNode node) {
        if (node instanceof ArrayAccessNode) {
            return inferirTipoArrayAccess((ArrayAccessNode) node);
        }
        return tiposExpresiones.getOrDefault(node, Tipo.UNKNOWN);
    }

    private void setTipoExpresion(ASTNode node, Tipo tipo) {
        tiposExpresiones.put(node, tipo);
    }

    private Tipo inferirTipoLiteral(LiteralNode node) {
        Object value = node.getValue();
        
        // Si el valor es null, intentar inferir del contexto (para compatibilidad)
        if (value == null) {
            return Tipo.UNKNOWN;
        }
        
        // Para literales numéricos, usar el tipo basado en la clase
        if (value instanceof Integer) {
            return Tipo.INT;
        } else if (value instanceof Double || value instanceof Float) {
            return Tipo.FLOAT;
        } else if (value instanceof String) {
            return Tipo.STRING;
        } else if (value instanceof Boolean) {
            return Tipo.BOOLEAN;
        }
        
        return Tipo.UNKNOWN;
    }

    private Tipo inferirTipoIdentificador(IdentifierNode node) {
        if (node.isBooleanLiteral()) {
            return Tipo.BOOLEAN;
        }
        
        Symbol symbol = scopeManager.resolve(node.getName());
        if (symbol == null) {
            return Tipo.UNKNOWN;
        }
        
        String typeStr = symbol.getType().toString();
        
        // CORREGIDO: Manejar tipos de array
        if (typeStr.endsWith("[]")) {
            // Para arrays, devolver el tipo base + "[]"
            return Tipo.fromString(typeStr);
        }
        
        return symbol.getType();
    }
    // método para manejar tipos de array
    private Tipo inferirTipoArrayAccess(ArrayAccessNode node) {
        Symbol arraySymbol = scopeManager.resolve(node.getArrayName());
        if (arraySymbol == null) {
            return Tipo.UNKNOWN;
        }
        
        String arrayType = arraySymbol.getType().toString();
        if (!arrayType.endsWith("[]")) {
            return Tipo.UNKNOWN;
        }
        
        // El tipo del acceso es el tipo base del array (sin [])
        String baseType = arrayType.substring(0, arrayType.length() - 2);
        return Tipo.fromString(baseType);
    }

    private Tipo inferirTipoBinario(BinaryExpression node) {
        Tipo leftType = getTipoExpresion(node.getLeft());
        Tipo rightType = getTipoExpresion(node.getRight());
        String operator = node.getOperator();

        // Operador + (concatenación o suma)
        if (operator.equals("+")) {
            // Concatenación de strings con cualquier tipo
            if (leftType == Tipo.STRING || rightType == Tipo.STRING) {
                return Tipo.STRING;
            }
            // Suma numérica
            else if (Tipo.esNumerico(leftType) && Tipo.esNumerico(rightType)) {
                return (leftType == Tipo.FLOAT || rightType == Tipo.FLOAT) ? Tipo.FLOAT : Tipo.INT;
            }
            return Tipo.UNKNOWN;
        }

        // Operadores aritméticos puros (-, *, /)
        if (operator.equals("-") || operator.equals("*") || operator.equals("/")) {
            if (!Tipo.esNumerico(leftType) || !Tipo.esNumerico(rightType)) {
                return Tipo.UNKNOWN;
            }
            return (leftType == Tipo.FLOAT || rightType == Tipo.FLOAT) ? Tipo.FLOAT : Tipo.INT;
        }

        // Operadores de comparación
        if (operator.equals("<") || operator.equals(">") || 
            operator.equals("<=") || operator.equals(">=")) {
            
            if (Tipo.esNumerico(leftType) && Tipo.esNumerico(rightType)) {
                return Tipo.BOOLEAN;
            }
            return Tipo.UNKNOWN;
        }

        // Operadores de igualdad
        if (operator.equals("==") || operator.equals("!=")) {
            if (Tipo.esCompatible(leftType, rightType)) {
                return Tipo.BOOLEAN;
            }
            return Tipo.UNKNOWN;
        }

        // Operadores lógicos
        if (operator.equals("&&") || operator.equals("||")) {
            if (leftType == Tipo.BOOLEAN && rightType == Tipo.BOOLEAN) {
                return Tipo.BOOLEAN;
            }
            return Tipo.UNKNOWN;
        }

        return Tipo.UNKNOWN;
    }

    // ========== IMPLEMENTACIÓN DE ASTVisitor ==========

    @Override
    public void visit(ProgramNode node) {
        for (ASTNode decl : node.getDeclarations()) {
            decl.accept(this);
        }
    }

    @Override
    public void visit(BlockNode node) {
        scopeManager.enterScope();
        for (ASTNode stmt : node.getStatements()) {
            stmt.accept(this);
        }
        scopeManager.exitScope();
    }

    @Override
    public void visit(FunctionNode node) {
        scopeManager.enterScope();

        // Declarar parámetros
        for (VariableDeclNode param : node.getParameters()) {
            Symbol paramSymbol = new Symbol(
                param.getName(), 
                Tipo.fromString(param.getType()), 
                null, 
                false
            );
            scopeManager.declareSymbol(param.getName(), paramSymbol);
        }

        // Analizar cuerpo
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }

        scopeManager.exitScope();
    }

    @Override
    public void visit(VariableDeclNode node) {
        if (node.getName() == null) {
            manejadorErrores.agregarError(
                node.getLineNumber(), "Nombre de variable no puede ser null", "Semántico"
            );
            return;
        }

        if (scopeManager.getCurrentScope().resolve(node.getName()) != null) {
            manejadorErrores.agregarError(
                node.getLineNumber(),
                "Variable '" + node.getName() + "' ya declarada en este scope",
                "Semántico"
            );
            return;
        }

        // CORREGIDO: Registrar el tipo de array correctamente
        String typeName = node.getType();
        Tipo tipoVariable;
        
        if (typeName.endsWith("[]")) {
            // Para arrays, usar el tipo completo (ej: "int[]")
            tipoVariable = Tipo.fromString(typeName);
        } else {
            // Para tipos normales
            tipoVariable = Tipo.fromString(typeName);
        }

        Symbol varSymbol = new Symbol(node.getName(), tipoVariable, null, false);
        scopeManager.declareSymbol(node.getName(), varSymbol);

        // Verificar tipo del valor inicial
        if (node.getInitialValue() != null) {
            node.getInitialValue().accept(this);
            
            if (node.getInitialValue() instanceof ArrayNode) {
                // Para arrays, el tipo ya está verificado durante el parsing
                ArrayNode arrayNode = (ArrayNode) node.getInitialValue();
                setTipoExpresion(node.getInitialValue(), tipoVariable);
            } else {
                Tipo tipoValor = getTipoExpresion(node.getInitialValue());
                
                if (!Tipo.esCompatibleParaAsignacion(tipoVariable, tipoValor)) {
                    manejadorErrores.agregarError(
                        node.getLineNumber(),
                        "No se puede asignar valor de tipo " + tipoValor + " a variable de tipo " + tipoVariable,
                        "Semántico"
                    );
                }
            }
        }
    }

    @Override
    public void visit(ArrayNode node) {
        Tipo elementType = Tipo.UNKNOWN;
        
        // Analizar elementos y determinar tipo común
        for (ASTNode element : node.getElements()) {
            element.accept(this);
            Tipo currentType = getTipoExpresion(element);
            
            if (elementType == Tipo.UNKNOWN) {
                elementType = currentType;
            } else if (elementType != currentType && currentType != Tipo.UNKNOWN) {
                manejadorErrores.agregarError(
                    node.getLineNumber(),
                    "Tipos inconsistentes en array: " + elementType + " y " + currentType,
                    "Semántico"
                );
            }
        }
        
        // Convertir tipo base a tipo array
        Tipo arrayType;
        switch (elementType) {
            case INT: arrayType = Tipo.INT_ARRAY; break;
            case FLOAT: arrayType = Tipo.FLOAT_ARRAY; break;
            case STRING: arrayType = Tipo.STRING_ARRAY; break;
            case BOOLEAN: arrayType = Tipo.BOOLEAN_ARRAY; break;
            default: arrayType = Tipo.UNKNOWN; break;
        }
        
        setTipoExpresion(node, arrayType);
    }

    @Override
    public void visit(ArrayAccessNode node) {
        // Verificar que el array exista
        Symbol arraySymbol = scopeManager.resolve(node.getArrayName());
        if (arraySymbol == null) {
            manejadorErrores.agregarError(
                node.getLineNumber(),
                "Array no declarado: " + node.getArrayName(),
                "Semántico"
            );
            setTipoExpresion(node, Tipo.UNKNOWN);
            return;
        }
        
        // Verificar que sea un array
        String arrayType = arraySymbol.getType().toString();
        if (!arrayType.endsWith("[]")) {
            manejadorErrores.agregarError(
                node.getLineNumber(),
                node.getArrayName() + " no es un array",
                "Semántico"
            );
            setTipoExpresion(node, Tipo.UNKNOWN);
            return;
        }
        
        // Verificar que el índice sea numérico
        node.getIndex().accept(this);
        Tipo indexType = getTipoExpresion(node.getIndex());
        if (!Tipo.esNumerico(indexType) && indexType != Tipo.UNKNOWN) {
            manejadorErrores.agregarError(
                node.getLineNumber(),
                "Índice de array debe ser numérico, no " + indexType,
                "Semántico"
            );
        }
        
        // El tipo del acceso es el tipo base del array (sin [])
        String baseType = arrayType.substring(0, arrayType.length() - 2);
        Tipo resultType = Tipo.fromString(baseType);
        setTipoExpresion(node, resultType);
    }

    @Override
    public void visit(AssignmentNode node) {
        Symbol symbol = scopeManager.resolve(node.getVariableName());
        if (symbol == null) {
            manejadorErrores.agregarError(
                node.getLineNumber(),
                "Variable no declarada: " + node.getVariableName(),
                "Semántico"
            );
            return;
        }

        if (symbol.isFunction()) {
            manejadorErrores.agregarError(
                node.getLineNumber(),
                "'" + node.getVariableName() + "' es una función, no se puede asignar",
                "Semántico"
            );
            return;
        }

        // Analizar y verificar tipo del valor
        node.getValue().accept(this);
        Tipo tipoValor = getTipoExpresion(node.getValue());
        Tipo tipoVariable = symbol.getType();

        if (!Tipo.esCompatibleParaAsignacion(tipoVariable, tipoValor)) {
            manejadorErrores.agregarError(
                node.getLineNumber(),
                "No se puede asignar tipo " + tipoValor + " a variable de tipo " + tipoVariable,
                "Semántico"
            );
        }
    }

    @Override
    public void visit(BinaryExpression node) {
        // Primero analizar subexpresiones
        node.getLeft().accept(this);
        node.getRight().accept(this);

        Tipo leftType = getTipoExpresion(node.getLeft());
        Tipo rightType = getTipoExpresion(node.getRight());
        Tipo resultType = inferirTipoBinario(node);

        // Verificar compatibilidad
        if (resultType == Tipo.UNKNOWN) {
            manejadorErrores.agregarError(
                node.getLineNumber(),
                "Operación inválida: " + leftType + " " + node.getOperator() + " " + rightType,
                "Semántico"
            );
        }

        setTipoExpresion(node, resultType);
    }

    @Override
    public void visit(UnaryExpressionNode node) {
        node.getExpression().accept(this);
        Tipo exprType = getTipoExpresion(node.getExpression());
        
        // Para operador '-', la expresión debe ser numérica
        if (node.getOperator().equals("-")) {
            if (!Tipo.esNumerico(exprType)) {
                manejadorErrores.agregarError(
                    node.getLineNumber(),
                    "Operador '-' no aplicable a tipo " + exprType,
                    "Semántico"
                );
            }
            setTipoExpresion(node, exprType);
        }
        // Para operador '!', la expresión debe ser booleana
        else if (node.getOperator().equals("!")) {
            if (exprType != Tipo.BOOLEAN) {
                manejadorErrores.agregarError(
                    node.getLineNumber(),
                    "Operador '!' no aplicable a tipo " + exprType,
                    "Semántico"
                );
            }
            setTipoExpresion(node, Tipo.BOOLEAN);
        }
    }

    @Override
    public void visit(LiteralNode node) {
        Tipo tipo = inferirTipoLiteral(node);
        setTipoExpresion(node, tipo);
    }

    @Override
    public void visit(IdentifierNode node) {
        Tipo tipo = inferirTipoIdentificador(node);
        setTipoExpresion(node, tipo);

        if (tipo == Tipo.UNKNOWN && !node.isBooleanLiteral()) {
            manejadorErrores.agregarError(
                node.getLineNumber(),
                "Identificador no declarado: " + node.getName(),
                "Semántico"
            );
        }
    }

    @Override
    public void visit(CallNode node) {
        Symbol funcSymbol = scopeManager.resolve(node.getFunctionName());
        if (funcSymbol == null) {
            manejadorErrores.agregarError(
                node.getLineNumber(),
                "Función no declarada: " + node.getFunctionName(),
                "Semántico"
            );
            return;
        }

        if (!funcSymbol.isFunction()) {
            manejadorErrores.agregarError(
                node.getLineNumber(),
                "'" + node.getFunctionName() + "' no es una función",
                "Semántico"
            );
            return;
        }

        // Analizar argumentos
        for (ASTNode arg : node.getArguments()) {
            arg.accept(this);
        }

        setTipoExpresion(node, funcSymbol.getType());
    }

    @Override
    public void visit(IfNode node) {
        node.getCondition().accept(this);
        Tipo condType = getTipoExpresion(node.getCondition());
        
        if (condType != Tipo.BOOLEAN && condType != Tipo.UNKNOWN) {
            manejadorErrores.agregarError(
                node.getLineNumber(),
                "La condición del if debe ser booleana, no " + condType,
                "Semántico"
            );
        }

        node.getThenBlock().accept(this);
        if (node.getElseBlock() != null) {
            node.getElseBlock().accept(this);
        }
    }

    @Override
    public void visit(WhileNode node) {
        node.getCondition().accept(this);
        Tipo condType = getTipoExpresion(node.getCondition());
        
        if (condType != Tipo.BOOLEAN && condType != Tipo.UNKNOWN) {
            manejadorErrores.agregarError(
                node.getLineNumber(),
                "La condición del while debe ser booleana, no " + condType,
                "Semántico"
            );
        }

        node.getBody().accept(this);
    }

    @Override
    public void visit(ReturnNode node) {
        if (node.getValue() != null) {
            node.getValue().accept(this);
        }
    }

    @Override
    public void visit(ExpressionStatementNode node) {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(TypeNode node) {
        // No necesita implementación
    }

    @Override
    public void visit(PrintNode node) {
        if (node.getValue() != null) {
            node.getValue().accept(this);
        }
    }
}