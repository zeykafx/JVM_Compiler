package compiler.Parser.ASTNodes.Statements.Expressions;

import compiler.Lexer.Symbol;

import java.util.ArrayList;

public class FunctionCall extends Term {
	private final Symbol identifier;
	private final ArrayList<ParamCall> parameters;

	public FunctionCall(Symbol identifier, ArrayList<ParamCall> parameters) {
		this.identifier = identifier;
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return "FunctionCall [identifier=" + identifier + ", parameters=" + parameters + "]";
	}
}
