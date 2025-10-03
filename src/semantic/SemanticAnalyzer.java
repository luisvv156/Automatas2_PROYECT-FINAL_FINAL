package semantic;

import ast.*;
import util.ManejadorErrores;

/**
 * Analizador semántico que recorre el AST y valida:
 * - Declaración de variables y funciones.
 * - Uso correcto de identificadores.
 * - Coincidencia de parámetros en llamadas a funciones.
 */
public class SemanticAnalyzer implements ASTVisitor {

    private ScopeManager scopeManager;
    private ManejadorErrores manejadorErrores;

    public SemanticAnalyzer() {
        this.scopeManager = new ScopeManager();
        this.manejadorErrores = new ManejadorErrores();
        registrarFuncionesPredefinidas();
    }

    private void registrarFuncionesPredefinidas() {
        // Registrar función print en el scope global
        Symbol printSymbol = new Symbol("print", Tipo.VOID, null, true);
        scopeManager.getGlobalScope().declareSymbol("print", printSymbol);
    }

    public ManejadorErrores getManejadorErrores() {
        return manejadorErrores;
    }

    // Analizar un programa completo
    public void analyze(ProgramNode program) {
        try {
            // Primera pasada: declarar todas las funciones
            for (ASTNode declaration : program.getDeclarations()) {
                if (declaration instanceof FunctionNode) {
                    FunctionNode func = (FunctionNode) declaration;
                    
                    // CORREGIDO: Verificar si la función ya existe
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
                    funcSymbol.setFunctionNode(func); // guardar nodo completo
                    scopeManager.getGlobalScope().declareSymbol(func.getFunctionName(), funcSymbol);
                }
            }

            // Segunda pasada: analizar cuerpos
            for (ASTNode declaration : program.getDeclarations()) {
                declaration.accept(this);
            }

        } catch (Exception e) {
            manejadorErrores.agregarError(
                0,
                "Error durante análisis semántico: " + e.getMessage(),
                "Semántico"
            );
        }
    }

    // ---------- Implementación de ASTVisitor ----------

    @Override
    public void visit(ProgramNode node) {
        for (ASTNode decl : node.getDeclarations()) {
            decl.accept(this);
        }
    }

    @Override
    public void visit(BlockNode node) {
        // CORREGIDO: Entrar a un nuevo scope para el bloque
        scopeManager.enterScope();
        for (ASTNode stmt : node.getStatements()) {
            stmt.accept(this);
        }
        scopeManager.exitScope();
    }

    @Override
    public void visit(FunctionNode node) {
        // CORREGIDO: Las funciones ya fueron declaradas en la primera pasada
        // Ahora analizamos el cuerpo con un nuevo scope
        scopeManager.enterScope();

        // Declarar parámetros en el scope de la función
        for (VariableDeclNode param : node.getParameters()) {
            Symbol paramSymbol = new Symbol(param.getName(), Tipo.fromString(param.getType()), null, false);
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
        // CORREGIDO: Verificar si la variable ya existe en el scope actual
        if (scopeManager.getCurrentScope().resolve(node.getName()) != null) {
            manejadorErrores.agregarError(
                node.getLineNumber(),
                "Variable '" + node.getName() + "' ya declarada en este scope",
                "Semántico"
            );
            return;
        }

        Symbol symbol = new Symbol(node.getName(), Tipo.fromString(node.getType()), null, false);
        scopeManager.declareSymbol(node.getName(), symbol);

        if (node.getInitialValue() != null) {
            node.getInitialValue().accept(this);
        }
    }

    @Override
    public void visit(AssignmentNode node) {
        // CORREGIDO: Usar scopeManager.resolve() que busca en todos los scopes
        Symbol symbol = scopeManager.resolve(node.getVariableName());
        if (symbol == null) {
            manejadorErrores.agregarError(
                node.getLineNumber(),
                "Variable no declarada: " + node.getVariableName(),
                "Semántico"
            );
        } else if (symbol.isFunction()) {
            manejadorErrores.agregarError(
                node.getLineNumber(),
                "'" + node.getVariableName() + "' es una función, no se puede asignar",
                "Semántico"
            );
        }
        node.getValue().accept(this);
    }

    @Override
    public void visit(BinaryExpression node) {
        node.getLeft().accept(this);
        node.getRight().accept(this);
    }

    @Override
    public void visit(UnaryExpressionNode node) {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(LiteralNode node) {
        // Literales siempre son válidos
    }

    @Override
    public void visit(IdentifierNode node) {
        // CORREGIDO: Usar scopeManager.resolve() que busca en todos los scopes
        Symbol symbol = scopeManager.resolve(node.getName());
        if (symbol == null && !node.isBooleanLiteral()) {
            manejadorErrores.agregarError(
                node.getLineNumber(),
                "Identificador no declarado: " + node.getName(),
                "Semántico"
            );
        }
    }

    @Override
    public void visit(CallNode node) {
        // CORREGIDO: Usar scopeManager.resolve() que busca en todos los scopes
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

        FunctionNode funcNode = funcSymbol.getFunctionNode();
        if (funcNode != null) {
            int expected = funcNode.getParameters().size();
            int provided = node.getArguments().size();

            if (expected != provided) {
                manejadorErrores.agregarError(
                    node.getLineNumber(),
                    "Número de argumentos incorrecto en '" + node.getFunctionName() +
                            "': esperado " + expected + ", recibido " + provided,
                    "Semántico"
                );
            }
        }

        for (ASTNode arg : node.getArguments()) {
            arg.accept(this);
        }
    }

    @Override
    public void visit(IfNode node) {
        node.getCondition().accept(this);
        node.getThenBlock().accept(this);

        if (node.getElseBlock() != null) {
            node.getElseBlock().accept(this);
        }
    }

    @Override
    public void visit(WhileNode node) {
        node.getCondition().accept(this);
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