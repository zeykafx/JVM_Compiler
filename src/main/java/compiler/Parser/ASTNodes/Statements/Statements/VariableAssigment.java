package compiler.Parser.ASTNodes.Statements.Statements;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.Access;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;

public class VariableAssigment extends Statement {
	private final Access access;
	private final Expression expression;


	public VariableAssigment(Access access, Expression expression) {
		this.access = access;
		this.expression = expression;
	}

	public Access getAccess() {
		return access;
	}

	public Expression getExpression() {
		return expression;
	}

	@Override
	public String toString() {
		return "VariableAssigment [access=" + access + ", expression=" + expression + "]";
	}
	
	@Override
	public String prettyPrint(int indent) {
        return "  ".repeat(indent) + "VariableAssigment: \n" + access.prettyPrint(indent) + " \n" + expression.prettyPrint(indent+1);
    }
}


// a[i].x.r[j] = 5