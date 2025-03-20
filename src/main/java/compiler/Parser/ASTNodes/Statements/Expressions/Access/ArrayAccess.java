package compiler.Parser.ASTNodes.Statements.Expressions.Access;

import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.Term;

public class ArrayAccess extends Term {

	private final Expression headExpression;
	private final Expression indexExpression;

	public ArrayAccess(Expression head, Expression index) {
		this.headExpression = head;
		this.indexExpression = index;
	}

	public Expression getHeadExpression() {
		return headExpression;
	}

	public Expression getIndexExpression() {
		return indexExpression;
	}

	@Override
	public String toString() {
		return "ArrayAccess [headExpression=" + headExpression + ", indexExpression=" + indexExpression + "]";
	}
}

// a.x[myId]