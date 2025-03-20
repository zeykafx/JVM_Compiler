package compiler.Parser.ASTNodes.Statements;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.Access;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.ArrayAccess;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;

public class VariableAssigment extends Statement {
	private final Access identifier;
	private final Expression expression;


	public VariableAssigment(Access identifier, Expression expression) {
		this.identifier = identifier;
		this.expression = expression;
	}

	public Access getIdentifier() {
		return identifier;
	}

	public Expression getExpression() {
		return expression;
	}

	@Override
	public String toString() {
		return "VariableAssigment [identifier=" + identifier + ", expression=" + expression + "]";
	}
}


// a[i].x.r[j] = 5