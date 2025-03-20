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
    public void testFloatWithLeadingDot() throws Exception {
        String input = "final i float = .123;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);

        Symbol symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.FINAL, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("i", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.FLOAT, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.ASSIGN, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.FLOAT_LITERAL, symbol.type);
        assertEquals(0.123f, symbol.value);
    }

    @Test
    public void testIntWithTrailingDot() throws Exception {
        String input = "final i int = 1.;"; // Per the assistant's teams message, "1." should be an integer
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
        assertEquals(1, symbol.value);
    }

    @Test
    public void testStringLiteral() throws Exception {
        String input = "final s string = \"Hello, World!\";";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);

        Symbol symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.FINAL, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("s", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.STRING, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.ASSIGN, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.STRING_LITERAL, symbol.type);
        assertEquals("Hello, World!", symbol.lexeme);
    }

    @Test
    public void testIntWithLeadingZeros() throws Exception {
        String input = "final i int = 001;";
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
        assertEquals(1, symbol.value);
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
        assertEquals(TokenTypes.RECORD, symbol.type);
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


    @Test
    public void testArrayCreation() throws Exception {
        String input = "c int[] = array [5] of int;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);

        Symbol symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("c", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.INT, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.LEFT_SQUARE_BRACKET, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.RIGHT_SQUARE_BRACKET, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.ASSIGN, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.ARRAY, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.LEFT_SQUARE_BRACKET, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.INT_LITERAL, symbol.type);
        assertEquals(5, symbol.value);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.RIGHT_SQUARE_BRACKET, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.OF, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.INT, symbol.type);
        assertEquals("int", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.SEMICOLON, symbol.type);
    }

    @Test
    public void methodWithRecAccess() throws Exception {
        String input = "fun copyPoints(Point[] p) Point { return Point(p[0].x+p[1].x, p[0].y+p[1].y); }";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);

        Symbol symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.FUN, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("copyPoints", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.LEFT_PAR, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.RECORD, symbol.type);
        assertEquals("Point", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.LEFT_SQUARE_BRACKET, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.RIGHT_SQUARE_BRACKET, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("p", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.RIGHT_PAR, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.RECORD, symbol.type);
        assertEquals("Point", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.LEFT_BRACKET, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.RETURN, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.RECORD, symbol.type);
        assertEquals("Point", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.LEFT_PAR, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("p", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.LEFT_SQUARE_BRACKET, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.INT_LITERAL, symbol.type);
        assertEquals(0, symbol.value);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.RIGHT_SQUARE_BRACKET, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.DOT, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("x", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.PLUS, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("p", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.LEFT_SQUARE_BRACKET, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.INT_LITERAL, symbol.type);
        assertEquals(1, symbol.value);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.RIGHT_SQUARE_BRACKET, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.DOT, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("x", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.COMMA, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("p", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.LEFT_SQUARE_BRACKET, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.INT_LITERAL, symbol.type);
        assertEquals(0, symbol.value);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.RIGHT_SQUARE_BRACKET, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.DOT, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("y", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.PLUS, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("p", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.LEFT_SQUARE_BRACKET, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.INT_LITERAL, symbol.type);
        assertEquals(1, symbol.value);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.RIGHT_SQUARE_BRACKET, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.DOT, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.IDENTIFIER, symbol.type);
        assertEquals("y", symbol.lexeme);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.RIGHT_PAR, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.SEMICOLON, symbol.type);

        symbol = lexer.getNextSymbol();
        assertEquals(TokenTypes.RIGHT_BRACKET, symbol.type);
    }
}