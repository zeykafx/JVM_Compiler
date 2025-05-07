package compiler.Parser.ASTNodes.Statements.Expressions.Terms;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.Access;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

import java.util.ArrayList;

public class FunctionCall extends Term {
	private final Symbol identifier;
	private final ArrayList<ParamCall> parameters;
	public Access recordAccess;


	public FunctionCall(Symbol identifier, ArrayList<ParamCall> parameters, int line, int column) {
		super(line, column);

		this.identifier = identifier;
		this.parameters = parameters;
		recordAccess = null;
	}

	public void setRecordAccess(Access access){
		recordAccess = access;
	}

	public boolean hasRecordAccess(){
		return recordAccess != null;
	}

	public Symbol getIdentifier() {
		return identifier;
	}

	public ArrayList<ParamCall> getParameters() {
		return parameters;
	}

	@Override
	public String toString() {
		return "FunctionCall [identifier=" + identifier + ", parameters=" + parameters + "]";
	}
	
	@Override
	public String prettyPrint(int indent) {
        StringBuilder str = new StringBuilder("  ".repeat(indent));
		if (hasRecordAccess()) {
			str.append("MethodCall (").append(recordAccess.prettyPrint(0)).append(") ").append(identifier.lexeme).append("\n");
		} else {
			str.append("FunctionCall: ");
			str.append(identifier.lexeme + "\n");
		}
        for (ParamCall param : parameters) {
            str.append(param.prettyPrint(indent+1)).append("\n");
        }


        return str.toString();
    }

	@Override
	public <R, T> R accept(Visitor<R, T> v, T table) throws Exception {
		return v.visitFunctionCall(this, table);
	}
}
