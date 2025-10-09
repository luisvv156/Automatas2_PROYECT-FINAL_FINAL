package parser;

import ast.*;
import semantic.*;
import util.ManejadorErrores;
import java.util.HashMap;
import java.util.Map;

public class AnalizadorSemanticoAvanzado implements ASTVisitor {
    private ScopeManager scopeManager;
    private ManejadorErrores errors;
    private String currentFunction;
    private Tipo currentReturnType;
    private int analysisDepth;
    private static final int MAX_ANALYSIS_DEPTH = 1000;
    private Map<String, Boolean> fastTypeCache;

    public AnalizadorSemanticoAvanzado() {
        this.scopeManager = new ScopeManager();
        this.errors = new ManejadorErrores();
        this.currentFunction = "global";
        this.currentReturnType = Tipo.VOID;
        this.analysisDepth = 0;
        this.fastTypeCache = new HashMap<>();
    }

    private void enterAnalysis() {
        if (++analysisDepth > MAX_ANALYSIS_DEPTH) {
            throw new RuntimeException("Profundidad de análisis excedida");
        }
    }

    private void exitAnalysis() {
        analysisDepth--;
    }

    public ManejadorErrores getErrors() {
        return errors;
    }

    @Override
    public void visit(ProgramNode node) {
        enterAnalysis();
        scopeManager.enterScope();
        for (ASTNode child : node.getDeclarations()) {
            child.accept(this);
        }
        scopeManager.exitScope();
        exitAnalysis();
    }

    @Override
    public void visit(FunctionNode node) {
        enterAnalysis();
        
        String functionName = node.getFunctionName();
        String cacheKey = "func_" + functionName;
        
        if (fastTypeCache.containsKey(cacheKey)) {
            exitAnalysis();
            return;
        }
        fastTypeCache.put(cacheKey, true);

        // CORREGIDO: Cambiar containsSymbol por resolve para verificar si ya existe
        if (scopeManager.resolve(functionName) != null) {
            errors.agregarError(node.getLineNumber(), 
                "Función '" + functionName + "' ya declarada", "Semántico");
            exitAnalysis();
            return;
        }

        Symbol functionSymbol = new Symbol(
            functionName,
            Tipo.fromString(node.getReturnType()),
            null,
            true
        );
        scopeManager.declareSymbol(functionName, functionSymbol);

        String previousFunction = currentFunction;
        Tipo previousReturnType = currentReturnType;
        
        currentFunction = functionName;
        currentReturnType = Tipo.fromString(node.getReturnType());
        
        scopeManager.enterScope();
        
        for (VariableDeclNode param : node.getParameters()) { // CORREGIDO: Tipo específico
            param.accept(this);
        }
        
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }
        
        scopeManager.exitScope();
        
        currentFunction = previousFunction;
        currentReturnType = previousReturnType;
        exitAnalysis();
    }

    @Override
    public void visit(VariableDeclNode node) {
        enterAnalysis();
        String varName = node.getName(); // CORREGIDO: Cambiar getVariableName() por getName()
        
        // CORREGIDO: Cambiar containsSymbol por resolve
        if (scopeManager.resolve(varName) != null) {
            errors.agregarError(node.getLineNumber(), 
                "Variable '" + varName + "' ya declarada", "Semántico");
            exitAnalysis();
            return;
        }

        Symbol varSymbol = new Symbol(
            varName,
            Tipo.fromString(node.getType()),
            null,
            false
        );
        scopeManager.declareSymbol(varName, varSymbol);

        if (node.getInitialValue() != null) {
            node.getInitialValue().accept(this);
        }
        exitAnalysis();
    }

    @Override
    public void visit(AssignmentNode node) {
        enterAnalysis();
        String varName = node.getVariableName();
        Symbol symbol = scopeManager.resolve(varName);
        
        if (symbol == null) {
            errors.agregarError(node.getLineNumber(), 
                "Variable '" + varName + "' no declarada", "Semántico");
            exitAnalysis();
            return;
        }

        if (symbol.isFunction()) {
            errors.agregarError(node.getLineNumber(), 
                "'" + varName + "' es una función, no una variable", "Semántico");
            exitAnalysis();
            return;
        }

        node.getValue().accept(this);
        exitAnalysis();
    }

    @Override
    public void visit(ReturnNode node) {
        enterAnalysis();
        if ("global".equals(currentFunction)) {
            errors.agregarError(node.getLineNumber(), 
                "Return fuera de función", "Semántico");
            exitAnalysis();
            return;
        }

        if (node.getValue() != null) {
            node.getValue().accept(this);
        } else if (currentReturnType != Tipo.VOID) {
            errors.agregarError(node.getLineNumber(), 
                "Función debe retornar valor de tipo " + currentReturnType, "Semántico");
        }
        exitAnalysis();
    }

    @Override
    public void visit(BinaryExpression node) {
        enterAnalysis();
        node.getLeft().accept(this);
        node.getRight().accept(this);
        exitAnalysis();
    }

    @Override
    public void visit(BlockNode node) {
        enterAnalysis();
        scopeManager.enterScope();
        for (ASTNode child : node.getStatements()) {
            child.accept(this);
        }
        scopeManager.exitScope();
        exitAnalysis();
    }

    @Override
    public void visit(CallNode node) {
        enterAnalysis();
        String functionName = node.getFunctionName();
        Symbol symbol = scopeManager.resolve(functionName);
        
        if (symbol == null) {
            errors.agregarError(node.getLineNumber(), 
                "Función '" + functionName + "' no declarada", "Semántico");
            exitAnalysis();
            return;
        }

        if (!symbol.isFunction()) {
            errors.agregarError(node.getLineNumber(), 
                "'" + functionName + "' no es una función", "Semántico");
            exitAnalysis();
            return;
        }

        for (ASTNode arg : node.getArguments()) {
            arg.accept(this);
        }
        exitAnalysis();
    }

    @Override
    public void visit(ExpressionStatementNode node) {
        enterAnalysis();
        node.getExpression().accept(this);
        exitAnalysis();
    }

    @Override
    public void visit(IdentifierNode node) {
        enterAnalysis();
        String varName = node.getName();
        Symbol symbol = scopeManager.resolve(varName);
        
        if (symbol == null && !node.isBooleanLiteral()) { // CORREGIDO: Agregar verificación de boolean literal
            errors.agregarError(node.getLineNumber(), 
                "Variable '" + varName + "' no declarada", "Semántico");
        }
        exitAnalysis();
    }

    @Override
    public void visit(IfNode node) {
        enterAnalysis();
        node.getCondition().accept(this);
        node.getThenBlock().accept(this);
        if (node.getElseBlock() != null) {
            node.getElseBlock().accept(this);
        }
        exitAnalysis();
    }

    @Override
    public void visit(LiteralNode node) {
        enterAnalysis();
        // No action needed for literals
        exitAnalysis();
    }

    @Override
    public void visit(TypeNode node) {
        enterAnalysis();
        // No action needed for type nodes
        exitAnalysis();
    }

    @Override
    public void visit(WhileNode node) {
        enterAnalysis();
        node.getCondition().accept(this);
        node.getBody().accept(this);
        exitAnalysis();
    }

    @Override
    public void visit(PrintNode node) {
        enterAnalysis();
        if (node.getValue() != null) { // CORREGIDO: Verificar si no es null
            node.getValue().accept(this);
        }
        exitAnalysis();
    }

    @Override
    public void visit(UnaryExpressionNode node) {
        enterAnalysis(); // CORREGIDO: Agregar enter/exit analysis
        node.getExpression().accept(this);
        exitAnalysis();
    }
        @Override
    public void visit(ArrayNode node) {
        enterAnalysis();
        
        // Analizar cada elemento del array
        for (ASTNode element : node.getElements()) {
            element.accept(this);
        }
        
        exitAnalysis();
    }

    @Override
    public void visit(ArrayAccessNode node) {
        enterAnalysis();
        
        // Verificar que el array exista
        String arrayName = node.getArrayName();
        Symbol symbol = scopeManager.resolve(arrayName);
        
        if (symbol == null) {
            errors.agregarError(node.getLineNumber(), 
                "Array no declarado: " + arrayName, "Semántico");
            exitAnalysis();
            return;
        }
        
        // Verificar que sea un array
        String type = symbol.getType().toString();
        if (!type.endsWith("[]")) {
            errors.agregarError(node.getLineNumber(), 
                "'" + arrayName + "' no es un array", "Semántico");
            exitAnalysis();
            return;
        }
        
        // Verificar que el índice sea numérico
        node.getIndex().accept(this);
        
        exitAnalysis();
    }
}