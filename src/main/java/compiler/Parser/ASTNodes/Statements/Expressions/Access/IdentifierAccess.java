package compiler.Parser.ASTNodes.Statements.Expressions.Access;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Statements.Expressions.Expression;
import compiler.Parser.ASTNodes.Statements.Expressions.Term;

public class IdentifierAccess extends Term {
	private final Symbol identifier;

	public IdentifierAccess(Symbol identifier) {
		this.identifier = identifier;
	}

	@Override
	public String toString() {
		return "IdentifierAccess [identifier=" + identifier + "]";
	}
}
