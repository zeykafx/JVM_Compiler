package compiler.Lexer;

public enum TokenTypes {
    IDENTIFIER,
    VAR,
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
    RECORD,

    // Base types
    INT,
    INT_LITERAL,
    FLOAT,
    FLOAT_LITERAL,
    BOOL,
    BOOL_TRUE,
    BOOL_FALSE,
    STRING,
    STRING_LITERAL,

    // Operations
    PLUS,
    MINUS,

    // boolean operators
    OR,
    AND,
    NOT,
    EQUAL_EQUAL,
    NOT_EQUAL,
    LESS_THAN,
    GREATER_THAN,
    LESS_THAN_EQUAL,
    GREATER_THAN_EQUAL,

    // Special Keywords
    REC,
    FUN,
    FINAL,
    RETURN,
    ARRAY,
    OF,
    DOT,
    COMMA,
    VOID,

    // built-in functions
    CHR,
    LEN,
    FLOOR,
    READ_INT,
    READ_FLOAT,
    READ_STRING,
    WRITE_INT,
    WRITE_FLOAT,
    WRITE,
    WRITELN,

    // Control structures
    FOR,
    WHILE,
    IF,
    ELSE,
    FREE,
}