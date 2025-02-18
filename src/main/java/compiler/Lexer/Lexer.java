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

    private int readChar() {
        try {
            int c = input.read();
            if (c != -1) {
                return c;
            } else {
                return -1; // EOF
            }
        } catch (IOException e) {
            return -1; // IO error are like EOF i guess
        }
    }

    private int peekChar() {
        try {
            int c = input.read();
            input.unread(1);
            return c;
        } catch (IOException e) {
            return -1;
        }
    }
    
    public Symbol getNextSymbol() throws Exception {
        skipWhitespaceAndComments();

        if (currentChar == -1) {
            return new Symbol(TokenTypes.EOF, "EOF", line);
        }

        int currentLine = line;

        // if c is a letter, read until not letter then check if keyword or identifier
        if (Character.isLetter(currentChar)) {
            return buildIdentifierOrSymbol();
        } else if (Character.isDigit(currentChar)) { // if c is a digit, read until not digit
            return buildNumber();
        } else if (currentChar =='"') { // if c is a quote, then we have a string next so read until next quote
            return buildString();
        } else {
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
                        throw new Exception("Illegal '&' at line " + currentLine);
                    }
                case '|':
                    moveCurrentChar();
                    if (currentChar == '|') {
                        moveCurrentChar();
                        return new Symbol(TokenTypes.OR, "||", currentLine - 1);
                    } else {
                        throw new Exception("Illegal '|' at line " + currentLine);
                    }

                default:
                    moveCurrentChar();
                    throw new Exception("Unknown character at line " + currentLine);
            }
        }
    }


    private Symbol buildIdentifierOrSymbol() {
        StringBuilder str = new StringBuilder();
        int startLine = line;

        while (Character.isLetterOrDigit(currentChar) || currentChar == '_') { // identifiers can start with _
            str.append((char) currentChar);
            moveCurrentChar();
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

    private Symbol buildNumber() throws Exception {
        StringBuilder lexeme = new StringBuilder();
        int startLine = line;
        boolean isFloat = false;

        while (Character.isDigit(currentChar)) {
            lexeme.append((char) currentChar);
            moveCurrentChar();
        }

        if (currentChar == '.') {
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
            try {
                int value = Integer.parseInt(numberStr);
                return new Symbol(TokenTypes.INT_LITERAL, numberStr, startLine, value);
            } catch (NumberFormatException e) {
                throw new Exception("Invalid integer at line " + startLine);
            }
        }
	}

    private Symbol buildString() {
        StringBuilder lexeme = new StringBuilder();
        int startLine = line;
        moveCurrentChar(); // Consume the opening quote "

        while (currentChar != '"') {
            if (currentChar == -1 || currentChar == '\n') {
                throw new RuntimeException("Unterminated string at line " + startLine);
            }
            lexeme.append((char) currentChar);
            moveCurrentChar();
        }
        moveCurrentChar(); // closing quote "

        return new Symbol(TokenTypes.STRING, lexeme.toString(), startLine, lexeme.toString());
    }

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


    private void skipWhitespace() {
        while (Character.isWhitespace(currentChar)) {
            if (currentChar == '\n') {
                line++;
            }
            moveCurrentChar();
        }
    }

    private void skipComment() {
        while (currentChar != '\n' && currentChar != -1) {
            moveCurrentChar();
        }
        if (currentChar == '\n') {
            line++;
            moveCurrentChar(); //consume newline after comment, if there is one.
        }
    }


    private void moveCurrentChar() {
        currentChar = readChar();
    }
}
