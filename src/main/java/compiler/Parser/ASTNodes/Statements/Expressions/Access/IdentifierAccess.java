package compiler.Parser.ASTNodes.Statements.Expressions.Access;

import compiler.Lexer.Symbol;

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

	@Override
	public String prettyPrint(int indent) {
        return "  ".repeat(indent) + "IdentifierAccess: " + identifier.lexeme;
    }
}
