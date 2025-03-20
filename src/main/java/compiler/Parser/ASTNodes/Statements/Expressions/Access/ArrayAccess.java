package compiler.Parser.ASTNodes.Statements.Expressions.Access;

import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.Term;

public class ArrayAccess extends Access {

	private final Access headAccess;
	private final Expression indexExpression;

	public ArrayAccess(Access head, Expression index) {
		this.headAccess = head;
		this.indexExpression = index;
	}

	public Access getHeadAccess() {
		return headAccess;
	}

	public Expression getIndexExpression() {
		return indexExpression;
	}

	@Override
	public String toString() {
		return "ArrayAccess [headExpression=" + headAccess + ", indexExpression=" + indexExpression + "]";
	}
}

// a.x[myId]