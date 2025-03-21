package compiler.Parser.ASTNodes.Statements.Expressions.Terms;

import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;

public class ParamCall extends Term {
	private final Expression paramExpression;

	public ParamCall(Expression paramExpression) {
		this.paramExpression = paramExpression;
	}

	public Expression getParamExpression() {
		return paramExpression;
	}

	@Override
	public String toString() {
		return "ParamCall [paramExpression=" + paramExpression + "]";
	}
	
	@Override
	public String prettyPrint(int indent) {
        return "  ".repeat(indent) + "ParamCall: " + paramExpression.prettyPrint(0);
    }
}

// !myfunc()