package lexer;

public enum TokenType {
    // Palabras reservadas
    FUNCTION, VAR, IF, ELSE, WHILE, RETURN, PRINT,
    INT, FLOAT, STRING, BOOLEAN, VOID,
    
    // Literales (MODIFICADO: separar INTEGER y FLOAT)
    INTEGER, FLOAT_LITERAL, STRING_LITERAL, IDENTIFIER, TRUE, FALSE,
    
    // Operadores
    PLUS, MINUS, MULTIPLY, DIVIDE, ASSIGN, EQUALS, NOT_EQUALS,
    LESS, GREATER, LESS_EQUAL, GREATER_EQUAL, AND, OR, NOT,
    
    // Símbolos
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, SEMICOLON, COLON,
    
    // Fin de archivo
    EOF
}