package compiler.Parser.ASTNodes.Statements.Expressions.Access;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.Term;

public class IdentifierAccess extends Access {
	private final Symbol identifier;

	public IdentifierAccess(Symbol identifier) {
		this.identifier = identifier;
	}

	public Symbol getIdentifier() {
		return identifier;
	}

	@Override
	public String toString() {
		return "IdentifierAccess [identifier=" + identifier + "]";
	}

}
