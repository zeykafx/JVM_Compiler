package compiler.Parser.ASTNodes.Statements.Statements;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.Access;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class VariableAssignment extends Statement {
	private final Access access;
	private final Expression expression;


	public VariableAssignment(Access access, Expression expression, int line, int column) {
		super(line, column);

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

	@Override
	public <R, T> R accept(Visitor<R, T> v, T table) throws Exception {
		return v.visitVariableAssignment(this, table);
	}
}


// a[i].x.r[j] = 5