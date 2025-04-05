package compiler.Parser.ASTNodes.Statements.Expressions.Access;

import compiler.Lexer.Symbol;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class IdentifierAccess extends Access {
	private final Symbol identifier;

	public IdentifierAccess(Symbol identifier, int line, int column) {
		super(line, column);

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

	@Override
	public SemType accept(Visitor<SemType> v, SymbolTable table) throws SemanticException {
		return v.visitIdentifierAccess(this, table);
	}
}
