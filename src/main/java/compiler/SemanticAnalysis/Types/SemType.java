package compiler.SemanticAnalysis.Types;

import compiler.Lexer.Symbol;

public class SemType {

	public String type;
	public Boolean isConstant = false;

	public SemType(String type) {
		this.type = type;
		this.isConstant = false;
	}

	public SemType(String type, Boolean isConstant) {
		this.type = type;
		this.isConstant = isConstant;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return type;
	}

	public Boolean getConstant() {
		return isConstant;
	}
}
