package compiler.Lexer;

public enum TokenTypes {
    IDENTIFIER,
    LEFT_PAR,
    RIGHT_PAR,
    LEFT_BRACKET,
    LEFT_SQUARE_BRACKET,
    RIGHT_BRACKET,
    RIGHT_SQUARE_BRACKET,
    SEMICOLON,
    COMMENT_SYMBOL,
    WHITESPACE,
    EOF,

    // Base types
    INT,
    FLOAT,
    BOOL,
    STRING,

    // Operations
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    EQUAL,
    NOT_EQUAL,
    LESS_THAN,
    GREATER_THAN,
    LESS_THAN_EQUAL,
    GREATER_THAN_EQUAL,
    MODULO,
    AND,
    OR,
    NOT,

    // Special Keywords
    FUN,
    FINAL,
    RETURN,
    ARRAY,
    OF,
    DOT,
    COMMA,

    // Control structures
    FOR,
    WHILE,
    IF,
    ELSE,
    FREE,

}