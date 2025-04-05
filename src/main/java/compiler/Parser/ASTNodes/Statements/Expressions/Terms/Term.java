package compiler.Parser.ASTNodes.Statements.Expressions.Terms;

import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;

public abstract class Term extends Expression {

	public Term(int line, int column) {
		super(line, column);
	}
}
