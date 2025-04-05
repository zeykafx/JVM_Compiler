package compiler.Parser.ASTNodes.Statements.Expressions.Terms;

import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class ParamCall extends Term {
	private final Expression paramExpression;
	private final Integer paramIndex;

	public ParamCall(Expression paramExpression, Integer paramIndex, int line, int column) {
		super(line, column);

		this.paramExpression = paramExpression;
		this.paramIndex = paramIndex;
	}

	public Expression getParamExpression() {
		return paramExpression;
	}

	public Integer getParamIndex() {
		return paramIndex;
	}

	@Override
	public String toString() {
		return "ParamCall [paramExpression=" + paramExpression + "]";
	}
	
	@Override
	public String prettyPrint(int indent) {
        return "  ".repeat(indent) + "ParamCall:\n" + paramExpression.prettyPrint(indent+1);
    }

	@Override
	public SemType accept(Visitor<SemType> v, SymbolTable table) throws SemanticException {
		return v.visitParamCall(this, table);
	}
}

// !myfunc()