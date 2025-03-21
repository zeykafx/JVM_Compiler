package compiler.Parser.ASTNodes.Statements.Expressions.Terms;

import compiler.Lexer.Symbol;

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
        if (parameters.size() > 0) {
            str.delete(str.length() - 2, str.length());
        }

        return str.toString();
    }
}
