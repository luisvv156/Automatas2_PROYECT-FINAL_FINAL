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
        if (isTypeToken(currentToken.getType())) return nextToken();
        throw new RuntimeException("Se esperaba tipo de dato, se encontró: " + currentToken.getType());
    }

    private VariableDeclNode parseVariableDeclaration() {
        expect(TokenType.VAR);
        int line = currentToken.getLine();
        String varName = expect(TokenType.IDENTIFIER).getLexeme();
        expect(TokenType.COLON);
        Token typeToken = expectTypeToken();

        ASTNode initialValue = null;
        if (match(TokenType.ASSIGN)) initialValue = parseExpression();

        expect(TokenType.SEMICOLON);
        return new VariableDeclNode(line, varName, typeToken.getLexeme(), initialValue);
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
        
        if (check(TokenType.INTEGER) || check(TokenType.FLOAT_LITERAL) || check(TokenType.STRING_LITERAL)) {
            Object value = currentToken.getLiteral();
            nextToken();
            return new LiteralNode(line, value);
        }
        if (check(TokenType.IDENTIFIER)) {
            String name = currentToken.getLexeme();
            nextToken();
            if (check(TokenType.LEFT_PAREN)) return parseFunctionCall(name);
            return new IdentifierNode(line, name);
        }
        if (match(TokenType.LEFT_PAREN)) {
            ASTNode expr = parseExpression();
            expect(TokenType.RIGHT_PAREN);
            return expr;
        }
        throw new RuntimeException("Expresión inválida en línea " + line);
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
        if (currentToken.getType() == type) return nextToken();
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
