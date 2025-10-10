package parser;

import lexer.*;
import ast.*;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final Lexer lexer;
    private Token currentToken;
    private Token peekToken;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        nextToken();
        nextToken();
    }

    public ProgramNode parse() {
        ProgramNode program = new ProgramNode();
        while (currentToken.getType() != TokenType.EOF) {
            try {
                ASTNode declaration = parseDeclaration();
                if (declaration != null) program.addDeclaration(declaration);
            } catch (Exception e) {
                System.err.println("Error de parsing: " + e.getMessage());
                synchronize();
            }
        }
        return program;
    }

    private boolean isTypeToken(TokenType type) {
        return type == TokenType.INT || type == TokenType.FLOAT || 
               type == TokenType.STRING || type == TokenType.BOOLEAN ||
               type == TokenType.VOID;
    }

    private ASTNode parseDeclaration() {
        if (check(TokenType.FUNCTION)) return parseFunctionDeclaration();
        if (check(TokenType.VAR)) return parseVariableDeclaration();
        if (check(TokenType.SEMICOLON)) { nextToken(); return null; } // declaración vacía
        return parseStatement();
    }

    private FunctionNode parseFunctionDeclaration() {
        expect(TokenType.FUNCTION);
        int line = currentToken.getLine();
        String functionName = expect(TokenType.IDENTIFIER).getLexeme();
        expect(TokenType.LEFT_PAREN);

        FunctionNode function = new FunctionNode(line, functionName, "void");

        // Parámetros
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                int paramLine = currentToken.getLine();
                String paramName = expect(TokenType.IDENTIFIER).getLexeme();
                expect(TokenType.COLON);
                Token typeToken = expectTypeToken();
                VariableDeclNode paramNode = new VariableDeclNode(
                        paramLine, paramName, typeToken.getLexeme(), null
                );
                function.addParameter(paramNode);
            } while (match(TokenType.COMMA));
        }

        expect(TokenType.RIGHT_PAREN);
        expect(TokenType.LEFT_BRACE);
        function.setBody(parseBlock());

        return function;
    }

    private Token expectTypeToken() {
        System.out.println("DEBUG PARSER: expectTypeToken - currentToken: " + currentToken.getType());
        
        if (isTypeToken(currentToken.getType())) {
            Token result = nextToken();
            System.out.println("DEBUG PARSER: expectTypeToken OK - tipo: " + result.getLexeme());
            return result;
        }
        
        System.out.println("DEBUG PARSER: ERROR expectTypeToken - Se esperaba tipo, se encontró: " + currentToken.getType());
        throw new RuntimeException("Se esperaba tipo de dato, se encontró: " + currentToken.getType());
    }

    private VariableDeclNode parseVariableDeclaration() {
        System.out.println("DEBUG PARSER: === INICIANDO parseVariableDeclaration ===");
        
        expect(TokenType.VAR);
        System.out.println("DEBUG PARSER: Token VAR OK");
        
        int line = currentToken.getLine();
        System.out.println("DEBUG PARSER: Línea: " + line);
        
        String varName = expect(TokenType.IDENTIFIER).getLexeme();
        System.out.println("DEBUG PARSER: Identificador: " + varName);
        
        expect(TokenType.COLON);
        System.out.println("DEBUG PARSER: Token COLON OK");
        
        // Verificar si es tipo array (ej: int[])
        String typeName;
        if (check(TokenType.INT) && peekToken.getType() == TokenType.LEFT_BRACKET) {
            System.out.println("DEBUG PARSER: Es array INT");
            nextToken(); // consume INT
            expect(TokenType.LEFT_BRACKET);
            expect(TokenType.RIGHT_BRACKET);
            typeName = "int[]";
        } else if (check(TokenType.FLOAT) && peekToken.getType() == TokenType.LEFT_BRACKET) {
            System.out.println("DEBUG PARSER: Es array FLOAT");
            nextToken(); // consume FLOAT
            expect(TokenType.LEFT_BRACKET);
            expect(TokenType.RIGHT_BRACKET);
            typeName = "float[]";
        } else if (check(TokenType.STRING) && peekToken.getType() == TokenType.LEFT_BRACKET) {
            System.out.println("DEBUG PARSER: Es array STRING");
            nextToken(); // consume STRING
            expect(TokenType.LEFT_BRACKET);
            expect(TokenType.RIGHT_BRACKET);
            typeName = "string[]";
        } else if (check(TokenType.BOOLEAN) && peekToken.getType() == TokenType.LEFT_BRACKET) {
            System.out.println("DEBUG PARSER: Es array BOOLEAN");
            nextToken(); // consume BOOLEAN
            expect(TokenType.LEFT_BRACKET);
            expect(TokenType.RIGHT_BRACKET);
            typeName = "boolean[]";
        } else {
            // Tipo normal (no array)
            System.out.println("DEBUG PARSER: Buscando tipo normal - currentToken: " + currentToken.getType());
            Token typeToken = expectTypeToken();
            typeName = typeToken.getLexeme();
            System.out.println("DEBUG PARSER: Tipo encontrado: " + typeName);
        }

        ASTNode initialValue = null;
        if (match(TokenType.ASSIGN)) {
            System.out.println("DEBUG PARSER: Hay asignación, parseando valor inicial...");
            if (typeName.endsWith("[]")) {
                initialValue = parseArrayLiteral();
            } else {
                initialValue = parseExpression();
            }
            System.out.println("DEBUG PARSER: Valor inicial parseado");
        }

        expect(TokenType.SEMICOLON);
        System.out.println("DEBUG PARSER: Token SEMICOLON OK");
        
        System.out.println("DEBUG PARSER: Creando VariableDeclNode: " + varName + " : " + typeName);
        System.out.println("DEBUG PARSER: === FIN parseVariableDeclaration ===");
        
        return new VariableDeclNode(line, varName, typeName, initialValue);
    }

    private BlockNode parseBlock() {
        BlockNode block = new BlockNode(currentToken.getLine());
        expect(TokenType.LEFT_BRACE);
        while (!check(TokenType.RIGHT_BRACE) && !check(TokenType.EOF)) {
            ASTNode stmt = parseDeclaration();
            if (stmt != null) block.addStatement(stmt);
        }
        expect(TokenType.RIGHT_BRACE);
        return block;
    }

    private ASTNode parseStatement() {
        if (check(TokenType.IF)) return parseIfStatement();
        if (check(TokenType.WHILE)) return parseWhileStatement();
        if (check(TokenType.RETURN)) return parseReturnStatement();
        if (check(TokenType.LEFT_BRACE)) return parseBlock();
        if (check(TokenType.IDENTIFIER) && peekToken.getType() == TokenType.ASSIGN) return parseAssignment();
        if (check(TokenType.PRINT)) return parsePrintStatement();
        return parseExpressionStatement();
    }

    private ASTNode parseIfStatement() {
        int line = currentToken.getLine();
        expect(TokenType.IF);
        expect(TokenType.LEFT_PAREN);
        ASTNode condition = parseExpression();
        expect(TokenType.RIGHT_PAREN);
        BlockNode thenBlock = parseBlock();
        IfNode ifNode = new IfNode(line, condition, thenBlock);
        if (match(TokenType.ELSE)) ifNode.setElseBlock(parseBlock());
        return ifNode;
    }

    private ASTNode parseWhileStatement() {
        int line = currentToken.getLine();
        expect(TokenType.WHILE);
        expect(TokenType.LEFT_PAREN);
        ASTNode condition = parseExpression();
        expect(TokenType.RIGHT_PAREN);
        BlockNode body = parseBlock();
        return new WhileNode(line, condition, body);
    }

    private ASTNode parseReturnStatement() {
        int line = currentToken.getLine();
        expect(TokenType.RETURN);
        ASTNode value = null;
        if (!check(TokenType.SEMICOLON)) value = parseExpression();
        expect(TokenType.SEMICOLON);
        return new ReturnNode(line, value);
    }

    private ASTNode parseAssignment() {
        int line = currentToken.getLine();
        String varName = expect(TokenType.IDENTIFIER).getLexeme();
        expect(TokenType.ASSIGN);
        ASTNode value = parseExpression();
        expect(TokenType.SEMICOLON);
        return new AssignmentNode(line, varName, value);
    }

    private ASTNode parseExpressionStatement() {
        int line = currentToken.getLine();
        ASTNode expr = parseExpression();
        expect(TokenType.SEMICOLON);
        return new ExpressionStatementNode(line, expr);
    }

    private ASTNode parsePrintStatement() {
        int line = currentToken.getLine();
        expect(TokenType.PRINT);
        expect(TokenType.LEFT_PAREN);
        List<ASTNode> args = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                args.add(parseExpression());
            } while (match(TokenType.COMMA));
        }
        expect(TokenType.RIGHT_PAREN);
        expect(TokenType.SEMICOLON);
        CallNode printCall = new CallNode(line, "print");
        args.forEach(printCall::addArgument);
        return printCall;
    }

    private ASTNode parseExpression() {
        return parseBinaryExpression(0);
    }

    private ASTNode parseBinaryExpression(int precedence) {
        ASTNode left = parseUnaryExpression();

        while (true) {
            Token op = currentToken;
            int opPrecedence = getPrecedence(op.getType());
            if (opPrecedence <= precedence) break;

            nextToken();
            ASTNode right = parseBinaryExpression(opPrecedence);
            left = new BinaryExpression(op.getLine(), left, op.getLexeme(), right);
        }

        return left;
    }

    private ASTNode parseUnaryExpression() {
        if (check(TokenType.NOT) || check(TokenType.MINUS)) {
            int line = currentToken.getLine();
            Token op = nextToken();
            ASTNode expr = parseUnaryExpression();
            return new UnaryExpressionNode(line, op.getLexeme(), expr);
        }
        return parsePrimary();
    }

    private ASTNode parsePrimary() {
        int line = currentToken.getLine();
        System.out.println("DEBUG PARSER: parsePrimary - currentToken: " + currentToken.getType() + " : " + currentToken.getLexeme());
        
        if (check(TokenType.INTEGER) || check(TokenType.FLOAT_LITERAL) || check(TokenType.STRING_LITERAL) || check(TokenType.TRUE) || check(TokenType.FALSE)) {
            Object value = currentToken.getLiteral();
            System.out.println("DEBUG PARSER: Literal encontrado: " + value + " (tipo: " + currentToken.getType() + ")");
            nextToken();
            return new LiteralNode(line, value);
        }
        if (check(TokenType.IDENTIFIER)) {
            String name = currentToken.getLexeme();
            nextToken();
            if (check(TokenType.LEFT_BRACKET)) {
                return parseArrayAccess(name);
            }
            if (check(TokenType.LEFT_PAREN)) return parseFunctionCall(name);
            return new IdentifierNode(line, name);
        }
        if (match(TokenType.LEFT_PAREN)) {
            ASTNode expr = parseExpression();
            expect(TokenType.RIGHT_PAREN);
            return expr;
        }
        if (check(TokenType.LEFT_BRACKET)) {
            return parseArrayLiteral();
        }
        
        throw new RuntimeException("Expresión inválida en línea " + line);
    }
    private ArrayAccessNode parseArrayAccess(String arrayName) {
        int line = currentToken.getLine();
        expect(TokenType.LEFT_BRACKET);
        ASTNode index = parseExpression();
        expect(TokenType.RIGHT_BRACKET);
        return new ArrayAccessNode(line, arrayName, index);
    }

    // Nuevo método para parsear literales de array
    private ArrayNode parseArrayLiteral() {
        int line = currentToken.getLine();
        expect(TokenType.LEFT_BRACKET);
        
        List<ASTNode> elements = new ArrayList<>();
        String elementType = null;
        
        if (!check(TokenType.RIGHT_BRACKET)) {
            do {
                ASTNode element = parseExpression();
                elements.add(element);
                
                // Inferir tipo del primer elemento
                if (elementType == null && element instanceof LiteralNode) {
                    Object value = ((LiteralNode) element).getValue();
                    if (value instanceof Integer) elementType = "int";
                    else if (value instanceof Double) elementType = "float";
                    else if (value instanceof String) elementType = "string";
                    else if (value instanceof Boolean) elementType = "boolean";
                }
            } while (match(TokenType.COMMA));
        }
        
        expect(TokenType.RIGHT_BRACKET);
        return new ArrayNode(line, elements, elementType != null ? elementType : "unknown");
    }


    private CallNode parseFunctionCall(String functionName) {
        int line = currentToken.getLine();
        expect(TokenType.LEFT_PAREN);
        CallNode call = new CallNode(line, functionName);
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                call.addArgument(parseExpression());
            } while (match(TokenType.COMMA));
        }
        expect(TokenType.RIGHT_PAREN);
        return call;
    }

    private int getPrecedence(TokenType type) {
        switch (type) {
            case OR: return 1;
            case AND: return 2;
            case EQUALS: case NOT_EQUALS: return 3;
            case LESS: case GREATER: case LESS_EQUAL: case GREATER_EQUAL: return 4;
            case PLUS: case MINUS: return 5;
            case MULTIPLY: case DIVIDE: return 6;
            default: return 0;
        }
    }

    private Token expect(TokenType type) {
        System.out.println("DEBUG PARSER: expect(" + type + ") - currentToken: " + currentToken.getType());
        
        if (currentToken.getType() == type) {
            Token result = nextToken();
            System.out.println("DEBUG PARSER: expect OK - siguiente token: " + currentToken.getType());
            return result;
        }
        
        System.out.println("DEBUG PARSER: ERROR expect - Se esperaba " + type + ", se encontró " + currentToken.getType());
        throw new RuntimeException("Se esperaba " + type + ", se encontró " + currentToken.getType() + " en línea " + currentToken.getLine());
    }

    private boolean check(TokenType type) {
        return currentToken.getType() == type;
    }

    private boolean match(TokenType type) {
        if (check(type)) { nextToken(); return true; }
        return false;
    }

    private Token nextToken() {
        Token prev = currentToken;
        currentToken = peekToken;
        peekToken = lexer.nextToken();
        return prev;
    }

    private void synchronize() {
        while (currentToken.getType() != TokenType.EOF) {
            if (currentToken.getType() == TokenType.SEMICOLON) { nextToken(); return; }
            if (currentToken.getType() == TokenType.FUNCTION ||
                currentToken.getType() == TokenType.VAR ||
                currentToken.getType() == TokenType.IF ||
                currentToken.getType() == TokenType.WHILE ||
                currentToken.getType() == TokenType.RETURN) return;
            nextToken();
        }
    }
}
