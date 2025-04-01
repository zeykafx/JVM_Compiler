package compiler.Parser.ASTNodes.Statements.Statements;

import compiler.Parser.ASTNodes.Statements.Expressions.Access.IdentifierAccess;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

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

	@Override
	public SemType accept(Visitor<SemType> v, SymbolTable table) {
		return v.visitFreeStatement(this, table);
	}
}
