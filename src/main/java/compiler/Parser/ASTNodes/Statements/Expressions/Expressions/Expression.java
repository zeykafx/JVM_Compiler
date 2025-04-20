package compiler.Parser.ASTNodes.Statements.Expressions.Expressions;

import compiler.Parser.ASTNodes.Statements.Statements.Statement;

public abstract class Expression extends Statement {

	public Expression(int line, int column) {
		super(line, column);
	}

	public String toString() {
		return "";
	}

	public boolean canBeStaticallyEval = false;
	public Object staticValue = null;
}
