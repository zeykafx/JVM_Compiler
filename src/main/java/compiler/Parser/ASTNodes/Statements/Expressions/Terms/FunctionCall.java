package compiler.Parser.ASTNodes.Statements.Expressions.Terms;

import compiler.Lexer.Symbol;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

import java.util.ArrayList;

public class FunctionCall extends Term {
	private final Symbol identifier;
	private final ArrayList<ParamCall> parameters;

	public FunctionCall(Symbol identifier, ArrayList<ParamCall> parameters) {
		this.identifier = identifier;
		this.parameters = parameters;
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
        StringBuilder str = new StringBuilder("  ".repeat(indent) + "FunctionCall: " + identifier.lexeme + "\n");
        for (ParamCall param : parameters) {
            str.append(param.prettyPrint(indent+1)).append("\n");
        }


        return str.toString();
    }

	@Override
	public SemType accept(Visitor<SemType> v, SymbolTable table) throws SemanticException {
		return v.visitFunctionCall(this, table);
	}
}
