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
        
        for (VariableDeclNode param : node.getParameters()) { 
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
        String varName = node.getName();
        
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
        
        if (symbol == null && !node.isBooleanLiteral()) {
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
        if (node.getValue() != null) {
            node.getValue().accept(this);
        }
        exitAnalysis();
    }

    @Override
    public void visit(UnaryExpressionNode node) {
        enterAnalysis();
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
    
    // ========== NUEVOS MÉTODOS PARA CLASES ==========
    
    @Override
    public void visit(ClassDeclNode node) {
        enterAnalysis();
        
        String className = node.getClassName();
        
        // Verificar si la clase ya existe
        if (scopeManager.classExists(className)) {
            errors.agregarError(node.getLineNumber(), 
                "Clase '" + className + "' ya declarada", "Semántico");
            exitAnalysis();
            return;
        }
        
        // Crear símbolo de clase
        ClassSymbol classSymbol = new ClassSymbol(className);
        
        // Manejar herencia
        if (node.getSuperClassName() != null) {
            classSymbol.setSuperClassName(node.getSuperClassName());
        }
        
        // Registrar la clase
        scopeManager.declareClass(className, classSymbol);
        
        // Entrar al scope de la clase
        scopeManager.enterScope();
        
        // Analizar campos
        for (VariableDeclNode field : node.getFields()) {
            field.accept(this);
            
            // Registrar campo en la clase
            Tipo fieldType = Tipo.fromString(field.getType());
            Symbol fieldSymbol = new Symbol(field.getName(), fieldType, null, false);
            classSymbol.addField(field.getName(), fieldSymbol);
        }
        
        // Analizar métodos
        for (MethodDeclNode method : node.getMethods()) {
            method.accept(this);
            
            // Registrar método en la clase
            Tipo returnType = Tipo.fromString(method.getReturnType().getTypeName());
            Symbol methodSymbol = Symbol.forMethod(method.getMethodName(), returnType, method);
            classSymbol.addMethod(method.getMethodName(), methodSymbol);
        }
        
        // Salir del scope de la clase
        scopeManager.exitScope();
        
        exitAnalysis();
    }
    
    @Override
    public void visit(MethodDeclNode node) {
        enterAnalysis();
        
        String methodName = node.getMethodName();
        
        // Entrar al scope del método
        scopeManager.enterScope();
        
        // Registrar parámetros
        for (VariableDeclNode param : node.getParameters()) {
            param.accept(this);
            
            // Registrar en el scope del método
            Tipo paramType = Tipo.fromString(param.getType());
            Symbol paramSymbol = new Symbol(param.getName(), paramType, null, false);
            scopeManager.declareSymbol(param.getName(), paramSymbol);
        }
        
        // Analizar cuerpo del método
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }
        
        // Salir del scope del método
        scopeManager.exitScope();
        
        exitAnalysis();
    }
    
    @Override
    public void visit(ClassInstanceNode node) {
        enterAnalysis();
        
        String className = node.getClassName();
        
        // Verificar que la clase exista
        if (!scopeManager.classExists(className)) {
            errors.agregarError(node.getLineNumber(), 
                "Clase '" + className + "' no declarada", "Semántico");
            exitAnalysis();
            return;
        }
        
        // Analizar argumentos del constructor
        for (ASTNode arg : node.getArguments()) {
            arg.accept(this);
        }
        
        exitAnalysis();
    }
    
    @Override
    public void visit(FieldAccessNode node) {
        enterAnalysis();
        
        // Analizar el objeto
        node.getObject().accept(this);
        
        // El campo se verifica en tiempo de ejecución o en análisis más avanzado
        // Aquí solo nos aseguramos de que el objeto sea válido
        
        exitAnalysis();
    }
    
    @Override
    public void visit(MethodCallNode node) {
        enterAnalysis();
        
        // Analizar el objeto
        node.getObject().accept(this);
        
        // Analizar argumentos
        for (ASTNode arg : node.getArguments()) {
            arg.accept(this);
        }
        
        // La verificación del método se hace en análisis más avanzado
        // Aquí solo nos aseguramos de que la sintaxis sea válida
        
        exitAnalysis();
    }
}