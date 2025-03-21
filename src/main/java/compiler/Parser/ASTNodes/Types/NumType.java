package compiler.Parser.ASTNodes.Types;

import compiler.Lexer.Symbol;

public class NumType extends Type {
	private final boolean isFloat;

	public NumType(Symbol type, boolean isFloat) {
		super(type, false);
		this.isFloat = isFloat;
	}

	public NumType(Symbol type) {
		super(type, false);
		this.isFloat = false;
	}

	public boolean isFloat() {
		return isFloat;
	}

	public boolean isInt() {
		return !isFloat;
	}

	@Override
	public String toString() {
		return "NumType, " + (isFloat ? "float" : "int");
	}
	
	@Override
	public String prettyPrint(int indent) {
        return "  ".repeat(indent) + "NumType: " + (isFloat ? "float" : "int") + ": " + symbol.lexeme +"";
    }
}
