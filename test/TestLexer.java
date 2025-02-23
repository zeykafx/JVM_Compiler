import static org.junit.Assert.*;
import org.junit.Test;
import java.io.StringReader;
import compiler.Lexer.*;

public class TestLexer {

    @Test
    public void testVariableDeclaration() throws Exception {
        String input = "var x int = 2;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);

        Symbol symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.VAR, symbol.type);
        assertEquals("var", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("x", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.INT, symbol.type);
        assertEquals("int", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.ASSIGN, symbol.type);
        assertEquals("=", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.INT_LITERAL, symbol.type);
        assertEquals("2", symbol.lexeme);
        assertEquals(2, symbol.value);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.SEMICOLON, symbol.type);
        assertEquals(";", symbol.lexeme);
    }

    @Test
    public void testMultilineVariableDecl() throws Exception {
        String input = "var x\n int = 2;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);

        Symbol symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.VAR, symbol.type);
        assertEquals("var", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("x", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.INT, symbol.type);
        assertEquals("int", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.ASSIGN, symbol.type);
        assertEquals("=", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.INT_LITERAL, symbol.type);
        assertEquals("2", symbol.lexeme);
        assertEquals(2, symbol.value);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.SEMICOLON, symbol.type);
        assertEquals(";", symbol.lexeme);
    }

    @Test
    public void testConstants() throws Exception {
        String input = "final i int = 3;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);

        Symbol symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.FINAL, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("i", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.INT, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.ASSIGN, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.INT_LITERAL, symbol.type);
        assertEquals(3, symbol.value);
    }

    @Test
    public void testRecordDefinition() throws Exception {
        String input = "Point rec { x int; y int; }";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);

        Symbol symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("Point", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.REC, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.LEFT_BRACKET, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("x", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.INT, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.SEMICOLON, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("y", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.INT, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.SEMICOLON, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.RIGHT_BRACKET, symbol.type);
    }

    @Test
    public void testFunction() throws Exception {
        String input = "fun square(int v) int { return v*v; }";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);

        Symbol symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.FUN, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("square", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.LEFT_PAR, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.INT, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("v", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.RIGHT_PAR, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.INT, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.LEFT_BRACKET, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.RETURN, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("v", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.MULTIPLY, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("v", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.SEMICOLON, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.RIGHT_BRACKET, symbol.type);
    }
}