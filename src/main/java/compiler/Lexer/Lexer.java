package compiler.Lexer;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

public class Lexer {

    private final PushbackReader input;
    private int currentChar;
    private int line;

    public Lexer(Reader input) {
        this.input = new PushbackReader(input);
        this.line = 1;
        this.currentChar = readChar();
    }

    /// Read the next character from the input
    private int readChar() {
        try {
            int c = input.read();
            if (c != -1) {
                return c;
            } else {
                return -1; // EOF
            }
        } catch (IOException e) {
            return -1; // we treat IO error like EOF I guess
        }
    }

    /// Peek the next character without consuming it
    private int peekChar() {
        try {
            int c = input.read();
            input.unread(c);
            return c;
        } catch (IOException e) {
            return -1;
        }
    }


    /// Skip whitespaces, comments, and newlines until we find something else
    private void skipWhitespaceAndComments() {
        while (true) {
            if (Character.isWhitespace(currentChar)) {
                skipWhitespace();
            } else if (currentChar == '$') {
                skipComment();
            } else {
                break;
            }
        }
    }


    /// Skip all whitespace characters
    private void skipWhitespace() {

        while (Character.isWhitespace(currentChar)) {
            if (currentChar == '\n') {
                line++;
            }
            moveCurrentChar();
        }
    }

    ///  Skip comment
    private void skipComment() {
        while (currentChar != '\n' && currentChar != -1) {
            moveCurrentChar();
        }
        if (currentChar == '\n') {
            line++;
            //consume newline after comment, if there is one
            moveCurrentChar();
        }
    }


    /// Move to the next character
    private void moveCurrentChar() {
        currentChar = readChar();
    }

    /// Get the next symbol from the input
    public Symbol getNextSymbol() throws Exception {
        // We always skip the newlines and comments
        skipWhitespaceAndComments();

        if (currentChar == -1) {
            return new Symbol(TokenTypes.EOF, "EOF", line);
        }

        // if c is a letter, read until it's not letter then check if it's keyword or identifier
        if (Character.isLetter(currentChar)) {
            return buildIdentifierOrSymbol();
        } else if (Character.isDigit(currentChar) || (currentChar == '.' && (peekChar() != -1 && Character.isDigit(peekChar())))) { // if c is a digit or a float without a leading 0, read until it's not digit
            return buildNumber();
        } else if (currentChar =='"') { // if c is a quote, then we have a string next so read until next quote
            return buildString();
        } else {
            // Else, we have a symbol
            return buildSymbol(line);
        }
    }

    /// Build an identifier or a keyword
    private Symbol buildIdentifierOrSymbol() {
        StringBuilder str = new StringBuilder();
        int startLine = line;

        // identifiers can start with a letter, a digit, or an underscore
        while (Character.isLetterOrDigit(currentChar) || currentChar == '_') {
            str.append((char) currentChar);
            moveCurrentChar();
        }

        // If the first char is an uppercase letter, it's a record type
        if (Character.isUpperCase(str.charAt(0))) {
            return new Symbol(TokenTypes.RECORD, str.toString(), startLine);
        }

        String identifier = str.toString();
        switch (identifier) {
            case "final": return new Symbol(TokenTypes.FINAL, identifier, startLine);
            case "var": return new Symbol(TokenTypes.VAR, identifier, startLine);
            case "rec": return new Symbol(TokenTypes.REC, identifier, startLine);
            case "int": return new Symbol(TokenTypes.INT, identifier, startLine);
            case "float": return new Symbol(TokenTypes.FLOAT, identifier, startLine);
            case "bool": return new Symbol(TokenTypes.BOOL, identifier, startLine);
            case "string": return new Symbol(TokenTypes.STRING, identifier, startLine);
            case "fun": return new Symbol(TokenTypes.FUN, identifier, startLine);
            case "return": return new Symbol(TokenTypes.RETURN, identifier, startLine);
            case "void": return new Symbol(TokenTypes.VOID, identifier, startLine);
            case "if": return new Symbol(TokenTypes.IF, identifier, startLine);
            case "else": return new Symbol(TokenTypes.ELSE, identifier, startLine);
            case "for": return new Symbol(TokenTypes.FOR, identifier, startLine);
            case "while": return new Symbol(TokenTypes.WHILE, identifier, startLine);
            case "array": return new Symbol(TokenTypes.ARRAY, identifier, startLine);
            case "of": return new Symbol(TokenTypes.OF, identifier, startLine);
            case "free": return new Symbol(TokenTypes.FREE, identifier, startLine);
            case "true": return new Symbol(TokenTypes.BOOL_TRUE, identifier, startLine, true);
            case "false": return new Symbol(TokenTypes.BOOL_FALSE, identifier, startLine, false);
            case "chr": return new Symbol(TokenTypes.CHR, identifier, startLine);
            case "len": return new Symbol(TokenTypes.LEN, identifier, startLine);
            case "floor": return new Symbol(TokenTypes.FLOOR, identifier, startLine);
            case "readInt": return new Symbol(TokenTypes.READ_INT, identifier, startLine);
            case "readFloat": return new Symbol(TokenTypes.READ_FLOAT, identifier, startLine);
            case "readString": return new Symbol(TokenTypes.READ_STRING, identifier, startLine);
            case "writeInt": return new Symbol(TokenTypes.WRITE_INT, identifier, startLine);
            case "writeFloat": return new Symbol(TokenTypes.WRITE_FLOAT, identifier, startLine);
            case "write": return new Symbol(TokenTypes.WRITE, identifier, startLine);
            case "writeln": return new Symbol(TokenTypes.WRITELN, identifier, startLine);

            default: return new Symbol(TokenTypes.IDENTIFIER, identifier, startLine);
        }
    }

    /// Build a number literal
    private Symbol buildNumber() throws Exception {
        StringBuilder lexeme = new StringBuilder();
        int startLine = line;
        boolean isFloat = false;

        while (Character.isDigit(currentChar)) {
            lexeme.append((char) currentChar);
            moveCurrentChar();
        }

        // If we have a dot, we have a float, but not if the dot is the last character
        if (currentChar == '.' && peekChar() != -1 && Character.isDigit(peekChar())) {
            isFloat = true;
			do {
				lexeme.append((char) currentChar);
				moveCurrentChar();
			} while (Character.isDigit(currentChar));
        }

        String numberStr = lexeme.toString();
        if (isFloat) {
            try {
                float value = Float.parseFloat(numberStr);
                return new Symbol(TokenTypes.FLOAT_LITERAL, numberStr, startLine, value);
            } catch (NumberFormatException e) {
                throw new Exception("Invalid float at line " + startLine);
            }
        } else {
            // remove leading zeros
            numberStr = numberStr.replaceFirst("^0+(?!$)", "");
            try {
                int value = Integer.parseInt(numberStr);
                return new Symbol(TokenTypes.INT_LITERAL, numberStr, startLine, value);
            } catch (NumberFormatException e) {
                throw new Exception("Invalid integer at line " + startLine + ": " + numberStr);
            }
        }
	}


    private Symbol buildSymbol(int currentLine) throws Exception {
        switch (currentChar) {
            case '+':
                moveCurrentChar();
                return new Symbol(TokenTypes.PLUS, "+", currentLine);
            case '-':
                moveCurrentChar();
                return new Symbol(TokenTypes.MINUS, "-", currentLine);
            case '*':
                moveCurrentChar();
                return new Symbol(TokenTypes.MULTIPLY, "*", currentLine);
            case '/':
                moveCurrentChar();
                return new Symbol(TokenTypes.DIVIDE, "/", currentLine);
            case '%':
                moveCurrentChar();
                return new Symbol(TokenTypes.MODULO, "%", currentLine);
            case '(':
                moveCurrentChar();
                return new Symbol(TokenTypes.LEFT_PAR, "(", currentLine);
            case ')':
                moveCurrentChar();
                return new Symbol(TokenTypes.RIGHT_PAR, ")", currentLine);
            case '{':
                moveCurrentChar();
                return new Symbol(TokenTypes.LEFT_BRACKET, "{", currentLine);
            case '}':
                moveCurrentChar();
                return new Symbol(TokenTypes.RIGHT_BRACKET, "}", currentLine);
            case ';':
                moveCurrentChar();
                return new Symbol(TokenTypes.SEMICOLON, ";", currentLine);
            case ',':
                moveCurrentChar();
                return new Symbol(TokenTypes.COMMA, ",", currentLine);
            case '[':
                moveCurrentChar();
                return new Symbol(TokenTypes.LEFT_SQUARE_BRACKET, "[", currentLine);
            case ']':
                moveCurrentChar();
                return new Symbol(TokenTypes.RIGHT_SQUARE_BRACKET, "]", currentLine);
            case '.':
                moveCurrentChar();
                return new Symbol(TokenTypes.DOT, ".", currentLine);
            case '=':
                moveCurrentChar();
                if (currentChar == '=') {
                    moveCurrentChar();
                    return new Symbol(TokenTypes.EQUAL_EQUAL, "==", currentLine - 1);
                } else {
                    return new Symbol(TokenTypes.ASSIGN, "=", currentLine);
                }
            case '!':
                moveCurrentChar();
                if (currentChar == '=') {
                    moveCurrentChar();
                    return new Symbol(TokenTypes.NOT_EQUAL, "!=", currentLine - 1);
                } else {
                    return new Symbol(TokenTypes.NOT, "!", currentLine);
                }
            case '<':
                moveCurrentChar();
                if (currentChar == '=') {
                    moveCurrentChar();
                    return new Symbol(TokenTypes.LESS_THAN_EQUAL, "<=", currentLine - 1);
                } else {
                    return new Symbol(TokenTypes.LESS_THAN, "<", currentLine);
                }
            case '>':
                moveCurrentChar();
                if (currentChar == '=') {
                    moveCurrentChar();
                    return new Symbol(TokenTypes.GREATER_THAN_EQUAL, ">=", currentLine - 1);
                } else {
                    return new Symbol(TokenTypes.GREATER_THAN, ">", currentLine);
                }
            case '&':
                moveCurrentChar();
                if (currentChar == '&') {
                    moveCurrentChar();
                    return new Symbol(TokenTypes.AND, "&&", currentLine - 1);
                } else {
                    throw new Exception("Unexpected '&' at line " + currentLine);
                }
            case '|':
                moveCurrentChar();
                if (currentChar == '|') {
                    moveCurrentChar();
                    return new Symbol(TokenTypes.OR, "||", currentLine - 1);
                } else {
                    throw new Exception("Unexpected '|' at line " + currentLine);
                }

            default:
                moveCurrentChar();
                throw new Exception("Unknown character at line " + currentLine);
        }
    }

    /// Build a string literal
    private Symbol buildString() {
        StringBuilder lexeme = new StringBuilder();
        int startLine = line;

        // consume the opening quote "
        moveCurrentChar();

        do {
            if (currentChar == -1 || currentChar == '\n') {
                throw new RuntimeException("Unterminated string at line " + startLine);
            }
            lexeme.append((char) currentChar);
            moveCurrentChar();
        } while (currentChar != '"');

        // consume closing quote "
        moveCurrentChar();

        return new Symbol(TokenTypes.STRING, lexeme.toString(), startLine, lexeme.toString());
    }
}
