package compiler.Lexer;

public class Symbol {
	public final TokenTypes type;
	public final String lexeme;
	public final int line;
	public Object value; // optional value for literals

	public Symbol(TokenTypes tokenType, String lexeme, int line) {
		this.type = tokenType;
		this.lexeme = lexeme;
		this.line = line;
		this.value = null;
	}

	public Symbol(TokenTypes tokenType, String lexeme, int line, Object value) {
		this.type = tokenType;
		this.lexeme = lexeme;
		this.line = line;
		this.value = value;
	}

	@Override
	public String toString() {
		return "Symbol{" +
				"type=" + type +
				", lexeme='" + lexeme + '\'' +
				", line=" + line +
				(value != null ? ", value=" + value : "") +
				'}';
	}
}
