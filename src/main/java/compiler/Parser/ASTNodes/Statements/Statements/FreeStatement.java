package compiler.Parser.ASTNodes.Statements.Statements;

import compiler.Parser.ASTNodes.Statements.Expressions.Access.IdentifierAccess;

public class FreeStatement extends Statement {
	private final IdentifierAccess identifierAccess;

	public FreeStatement(IdentifierAccess identifierAccess) {
		this.identifierAccess = identifierAccess;
	}

	public IdentifierAccess getIdentifierAccess() {
		return identifierAccess;
	}

	@Override
	public String toString() {
		return "FreeStatement [identifierAccess=" + identifierAccess + "]";
	}

	@Override
	public String prettyPrint(int indent) {
		return "  ".repeat(indent) + "Free: \n" + identifierAccess.prettyPrint(indent + 1);
	}
}
