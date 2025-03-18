package compiler.Parser.ASTNodes.Types;

import compiler.Lexer.Symbol;

public class NumType extends Type {
	boolean isFloat;

	public NumType(Symbol type, boolean isFloat) {
		super(type, false);
		this.isFloat = isFloat;
	}

	public NumType(Symbol type) {
		super(type, false);
		this.isFloat = false;
	}

	@Override
	public String toString() {
		return "NumType, " + (isFloat ? "float" : "int");
	}
}
