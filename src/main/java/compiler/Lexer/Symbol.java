package compiler.Lexer;

public class Symbol {
	public final TokenTypes type;
	public final String lexeme;
	public final int line;
	public final int column;
	public Object value; // optional value for literals

	public Symbol(TokenTypes tokenType, String lexeme, int line, int column) {
		this.type = tokenType;
		this.lexeme = lexeme;
		this.line = line;
		this.column = column;
		this.value = null;
	}

	public Symbol(TokenTypes tokenType, String lexeme, int line, int column, Object value) {
		this.type = tokenType;
		this.lexeme = lexeme;
		this.line = line;
		this.column = column;
		this.value = value;
	}

	@Override
	public String toString() {
		return "Symbol{" +
				"type=" + type +
				", lexeme='" + lexeme + '\'' +
				", line=" + line +
				", column=" + column +
				(value != null ? ", value=" + value : "") +
				'}';
	}
}
