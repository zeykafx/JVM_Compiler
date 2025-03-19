package compiler.Parser.ASTNodes.Statements.Expressions.Terms;

import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;

public class ParamCall extends Term {
	private final Expression paramExpression;

	public ParamCall(Expression paramExpression) {
		this.paramExpression = paramExpression;
	}

	@Override
	public String toString() {
		return "ParamCall [paramExpression=" + paramExpression + "]";
	}
}

// !myfunc()