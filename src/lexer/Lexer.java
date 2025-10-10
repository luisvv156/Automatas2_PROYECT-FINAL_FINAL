package lexer;

import java.util.HashMap;
import java.util.Map;

public class Lexer {
    private final String source;
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        // Palabras reservadas
        keywords.put("function", TokenType.FUNCTION);
        keywords.put("var", TokenType.VAR);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("return", TokenType.RETURN);
        keywords.put("print", TokenType.PRINT);

        // Tipos de datos
        keywords.put("int", TokenType.INT);
        keywords.put("float", TokenType.FLOAT);
        keywords.put("string", TokenType.STRING);
        keywords.put("boolean", TokenType.BOOLEAN);
        keywords.put("void", TokenType.VOID);

        // Booleanos
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
    }

    public Lexer(String source) {
        this.source = source;
    }

    public Token nextToken() {
        skipWhitespace();
        start = current;

        if (isAtEnd()) return makeToken(TokenType.EOF, "");

        char c = advance();

        if (Character.isDigit(c)) return number();
        if (Character.isLetter(c)) return identifier();
        if (c == '"') return string();

        switch (c) {
            case '(': return makeToken(TokenType.LEFT_PAREN, null);
            case ')': return makeToken(TokenType.RIGHT_PAREN, null);
            case '{': return makeToken(TokenType.LEFT_BRACE, null);
            case '}': return makeToken(TokenType.RIGHT_BRACE, null);
            case ',': return makeToken(TokenType.COMMA, null);
            case ';': return makeToken(TokenType.SEMICOLON, null);
            case ':': return makeToken(TokenType.COLON, null);

            case '[': return makeToken(TokenType.LEFT_BRACKET, null);
            case ']': return makeToken(TokenType.RIGHT_BRACKET, null);

            case '+': return makeToken(TokenType.PLUS, null);
            case '-': return makeToken(TokenType.MINUS, null);
            case '*': return makeToken(TokenType.MULTIPLY, null);
            case '/': return makeToken(TokenType.DIVIDE, null);

            case '=': return match('=') ? makeToken(TokenType.EQUALS, null) : makeToken(TokenType.ASSIGN, null);
            case '!': return match('=') ? makeToken(TokenType.NOT_EQUALS, null) : makeToken(TokenType.NOT, null);
            case '<': return match('=') ? makeToken(TokenType.LESS_EQUAL, null) : makeToken(TokenType.LESS, null);
            case '>': return match('=') ? makeToken(TokenType.GREATER_EQUAL, null) : makeToken(TokenType.GREATER, null);

            case '&': 
                if (match('&')) return makeToken(TokenType.AND, null);
                throw new RuntimeException("Caracter inválido '&' en línea " + line);

            case '|': 
                if (match('|')) return makeToken(TokenType.OR, null);
                throw new RuntimeException("Caracter inválido '|' en línea " + line);
        }

        throw new RuntimeException("Caracter inesperado '" + c + "' en línea " + line);
    }

    private Token number() {
        boolean isFloat = false;
        
        // Consumir parte entera
        while (Character.isDigit(peek())) advance();

        // Verificar si tiene parte decimal
        if (peek() == '.' && Character.isDigit(peekNext())) {
            isFloat = true;
            advance(); // Consumir el punto
            while (Character.isDigit(peek())) advance();
        }

        String numberStr = source.substring(start, current);
        
        if (isFloat) {
            double value = Double.parseDouble(numberStr);
            return makeToken(TokenType.FLOAT_LITERAL, value);
        } else {
            int value;
            try {
                value = Integer.parseInt(numberStr);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Número demasiado grande: " + numberStr + " en línea " + line);
            }
            return makeToken(TokenType.INTEGER, value);
        }
    }

    
    private Token identifier() {
        while (Character.isLetterOrDigit(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.getOrDefault(text, TokenType.IDENTIFIER);
        
        // CORREGIR: Para booleanos literales, usar Boolean en lugar de String
        if (type == TokenType.TRUE) {
            return makeToken(type, true);  // Boolean true, no String "true"
        } else if (type == TokenType.FALSE) {
            return makeToken(type, false); // Boolean false, no String "false"
        }
        
        return makeToken(type, text);
    }

    private Token string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;  // ← ESTE ES EL PROBLEMA
            advance();
        }

        if (isAtEnd()) throw new RuntimeException("Cadena no cerrada al final del archivo");

        advance(); // consume la comilla de cierre
        String value = source.substring(start + 1, current - 1);
        return makeToken(TokenType.STRING_LITERAL, value);
    }

    private Token makeToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, current);
        return new Token(type, lexeme, literal, line);
    }

    private char advance() {
        return source.charAt(current++);
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private char peekNext() {
        return current + 1 >= source.length() ? '\0' : source.charAt(current + 1);
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }
    private void skipWhitespace() {
        while (!isAtEnd()) {
            char c = peek();
            switch (c) {
                case ' ': case '\r': case '\t': 
                    advance(); 
                    break;
                case '\n': 
                    line++; 
                    advance(); 
                    break;
                case '/':
                    if (peekNext() == '/') {
                        // Comentario de una línea - consumir hasta newline o EOF
                        while (peek() != '\n' && !isAtEnd()) advance();
                    } else if (peekNext() == '*') {
                        // Comentario multilínea
                        advance(); advance(); // Consume "/*"
                        while (!isAtEnd()) {
                            if (peek() == '*' && peekNext() == '/') {
                                advance(); advance(); // Consume "*/"
                                break;
                            }
                            if (peek() == '\n') line++;
                            advance();
                        }
                        if (isAtEnd()) {
                            throw new RuntimeException("Comentario multilínea no cerrado en línea " + line);
                        }
                    } else {
                        return; // Es una división, no comentario
                    }
                    break;
                default: 
                    return;
            }
        }
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}
