package compiler.Lexer;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

public class Lexer {

	private final PushbackReader input;
	private int currentChar;
	private int line;
	private int column;

	public Lexer(Reader input) {
		this.input = new PushbackReader(input);
		this.line = 1;
		this.column = 0;
		this.currentChar = readChar();
	}

	/// Read the next character from the input
	private int readChar() {
		try {
			int c = input.read();
			if (c != -1) {
				column++;
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
				column = 0;
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
			column = 0;
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
			return new Symbol(TokenTypes.EOF, "EOF", line, column);
		}

		// if c is a letter, read until it's not letter then check if it's keyword or identifier
		if (Character.isLetter(currentChar)) {
			return buildIdentifierOrSymbol();
		} else if (Character.isDigit(currentChar) || (currentChar == '.' && (peekChar() != -1 && Character.isDigit(peekChar())))) { // if c is a digit or a float without a leading 0, read until it's not digit
			return buildNumber();
		} else if (currentChar == '"') { // if c is a quote, then we have a string next so read until next quote
			return buildString();
		} else {
			// Else, we have a symbol
			return buildSymbol();
		}
	}

	/// Build an identifier or a keyword
	private Symbol buildIdentifierOrSymbol() {
		StringBuilder str = new StringBuilder();
		int startLine = line;
		int startColumn = column;

		// identifiers can start with a letter, a digit, or an underscore
		while (Character.isLetterOrDigit(currentChar) || currentChar == '_') {
			str.append((char) currentChar);
			moveCurrentChar();
		}

		// If the first char is an uppercase letter, it's a record type
		if (Character.isUpperCase(str.charAt(0))) {
			return new Symbol(TokenTypes.RECORD, str.toString(), startLine, startColumn);
		}

		String identifier = str.toString();
		return switch (identifier) {
			case "final" -> new Symbol(TokenTypes.FINAL, identifier, startLine, startColumn);
			case "var" -> new Symbol(TokenTypes.VAR, identifier, startLine, startColumn);
			case "rec" -> new Symbol(TokenTypes.REC, identifier, startLine, startColumn);
			case "int" -> new Symbol(TokenTypes.INT, identifier, startLine, startColumn);
			case "float" -> new Symbol(TokenTypes.FLOAT, identifier, startLine, startColumn);
			case "bool" -> new Symbol(TokenTypes.BOOL, identifier, startLine, startColumn);
			case "string" -> new Symbol(TokenTypes.STRING, identifier, startLine, startColumn);
			case "fun" -> new Symbol(TokenTypes.FUN, identifier, startLine, startColumn);
			case "return" -> new Symbol(TokenTypes.RETURN, identifier, startLine, startColumn);
			case "void" -> new Symbol(TokenTypes.VOID, identifier, startLine, startColumn);
			case "if" -> new Symbol(TokenTypes.IF, identifier, startLine, startColumn);
			case "else" -> new Symbol(TokenTypes.ELSE, identifier, startLine, startColumn);
			case "for" -> new Symbol(TokenTypes.FOR, identifier, startLine, startColumn);
			case "while" -> new Symbol(TokenTypes.WHILE, identifier, startLine, startColumn);
			case "array" -> new Symbol(TokenTypes.ARRAY, identifier, startLine, startColumn);
			case "of" -> new Symbol(TokenTypes.OF, identifier, startLine, startColumn);
			case "free" -> new Symbol(TokenTypes.FREE, identifier, startLine, startColumn);
			case "true" -> new Symbol(TokenTypes.BOOL_TRUE, identifier, startLine, startColumn, true);
			case "false" -> new Symbol(TokenTypes.BOOL_FALSE, identifier, startLine, startColumn, false);
//			case "chr" -> new Symbol(TokenTypes.CHR, identifier, startLine, startColumn);
//			case "len" -> new Symbol(TokenTypes.LEN, identifier, startLine, startColumn);
//			case "floor" -> new Symbol(TokenTypes.FLOOR, identifier, startLine, startColumn);
//			case "readInt" -> new Symbol(TokenTypes.READ_INT, identifier, startLine, startColumn);
//			case "readFloat" -> new Symbol(TokenTypes.READ_FLOAT, identifier, startLine, startColumn);
//			case "readString" -> new Symbol(TokenTypes.READ_STRING, identifier, startLine, startColumn);
//			case "writeInt" -> new Symbol(TokenTypes.WRITE_INT, identifier, startLine, startColumn);
//			case "writeFloat" -> new Symbol(TokenTypes.WRITE_FLOAT, identifier, startLine, startColumn);
//			case "write" -> new Symbol(TokenTypes.WRITE, identifier, startLine, startColumn);
//			case "writeln" -> new Symbol(TokenTypes.WRITELN, identifier, startLine, startColumn);
			default -> new Symbol(TokenTypes.IDENTIFIER, identifier, startLine, startColumn);
		};
	}

	/// Build a number literal
	private Symbol buildNumber() throws Exception {
		StringBuilder lexeme = new StringBuilder();
		int startLine = line;
		int startColumn = column;
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
				return new Symbol(TokenTypes.FLOAT_LITERAL, numberStr, startLine, column, value);
			} catch (NumberFormatException e) {
				throw new Exception("Invalid float at line " + startLine + " column " + startColumn + ": " + numberStr);
			}
		} else {
			// remove leading zeros:
			// ^ = start of string, 0+ = one or more 0s, () = no?!$t at the end of the string
			// so it shouldn't remove a single 0
			numberStr = numberStr.replaceFirst("^0+(?!$)", "");
			try {
				int value = Integer.parseInt(numberStr);
				return new Symbol(TokenTypes.INT_LITERAL, numberStr, startLine, column,  value);
			} catch (NumberFormatException e) {
				throw new Exception("Invalid integer at line " + startLine + " column " + startColumn + ": " + numberStr);
			}
		}
	}


	private Symbol buildSymbol() throws Exception {
		switch (currentChar) {
			case '+':
				moveCurrentChar();
				return new Symbol(TokenTypes.PLUS, "+", line, column);
			case '-':
				moveCurrentChar();
				return new Symbol(TokenTypes.MINUS, "-", line, column);
			case '*':
				moveCurrentChar();
				return new Symbol(TokenTypes.MULTIPLY, "*", line, column);
			case '/':
				moveCurrentChar();
				return new Symbol(TokenTypes.DIVIDE, "/", line, column);
			case '%':
				moveCurrentChar();
				return new Symbol(TokenTypes.MODULO, "%", line, column);
			case '(':
				moveCurrentChar();
				return new Symbol(TokenTypes.LEFT_PAR, "(", line, column);
			case ')':
				moveCurrentChar();
				return new Symbol(TokenTypes.RIGHT_PAR, ")", line, column);
			case '{':
				moveCurrentChar();
				return new Symbol(TokenTypes.LEFT_BRACKET, "{", line, column);
			case '}':
				moveCurrentChar();
				return new Symbol(TokenTypes.RIGHT_BRACKET, "}", line, column);
			case ';':
				moveCurrentChar();
				return new Symbol(TokenTypes.SEMICOLON, ";", line, column);
			case ',':
				moveCurrentChar();
				return new Symbol(TokenTypes.COMMA, ",", line, column);
			case '[':
				moveCurrentChar();
				return new Symbol(TokenTypes.LEFT_SQUARE_BRACKET, "[", line, column);
			case ']':
				moveCurrentChar();
				return new Symbol(TokenTypes.RIGHT_SQUARE_BRACKET, "]", line, column);
			case '.':
				moveCurrentChar();
				return new Symbol(TokenTypes.DOT, ".", line, column);
			case '=':
				moveCurrentChar();
				if (currentChar == '=') {
					moveCurrentChar();
					return new Symbol(TokenTypes.EQUAL_EQUAL, "==", line - 1, column);
				} else {
					return new Symbol(TokenTypes.ASSIGN, "=", line, column);
				}
			case '!':
				moveCurrentChar();
				if (currentChar == '=') {
					moveCurrentChar();
					return new Symbol(TokenTypes.NOT_EQUAL, "!=", line - 1, column);
				} else {
					return new Symbol(TokenTypes.NOT, "!", line, column);
				}
			case '<':
				moveCurrentChar();
				if (currentChar == '=') {
					moveCurrentChar();
					return new Symbol(TokenTypes.LESS_THAN_EQUAL, "<=", line - 1, column);
				} else {
					return new Symbol(TokenTypes.LESS_THAN, "<", line, column);
				}
			case '>':
				moveCurrentChar();
				if (currentChar == '=') {
					moveCurrentChar();
					return new Symbol(TokenTypes.GREATER_THAN_EQUAL, ">=", line - 1, column);
				} else {
					return new Symbol(TokenTypes.GREATER_THAN, ">", line, column);
				}
			case '&':
				moveCurrentChar();
				if (currentChar == '&') {
					moveCurrentChar();
					return new Symbol(TokenTypes.AND, "&&", line - 1, column);
				} else {
					throw new Exception("Unexpected '&' at line " + line + ", column " + column);
				}
			case '|':
				moveCurrentChar();
				if (currentChar == '|') {
					moveCurrentChar();
					return new Symbol(TokenTypes.OR, "||", line - 1, column);
				} else {
					throw new Exception("Unexpected '|' at line " + line + ", column " + column);
				}

			default:
				moveCurrentChar();
				throw new Exception("Unknown character at line " + line + ", column " + column);
		}
	}

	/// Build a string literal
	private Symbol buildString() {
		StringBuilder lexeme = new StringBuilder();
		int startLine = line;
		int startColumn = column;

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

		return new Symbol(TokenTypes.STRING_LITERAL, lexeme.toString(), startLine, startColumn, lexeme.toString());
	}
}
