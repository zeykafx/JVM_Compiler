package compiler.Parser.ASTNodes.Statements;

import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;

public class ReturnStatement extends Statement {
	private final Expression expression;

	public ReturnStatement(Expression expression) {
		this.expression = expression;
	}

	public Expression getExpression() {
		return expression;
	}

	@Override
	public String toString() {
		return "ReturnStatement [expression=" + expression + "]";
	}
}
