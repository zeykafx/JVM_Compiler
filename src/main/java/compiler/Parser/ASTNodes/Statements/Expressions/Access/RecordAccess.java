package compiler.Parser.ASTNodes.Statements.Expressions.Access;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;

public class RecordAccess extends Access {

	private final Access headAccess;
	private final Symbol identifier;

	public RecordAccess(Access head, Symbol identifier) {
		this.headAccess = head;
		this.identifier = identifier;
	}

	public Symbol getIdentifier() {
		return identifier;
	}

	public Access getHeadAccess() {
		return headAccess;
	}

	@Override
	public String toString() {
		return "RecordAccess [headExpression=" + headAccess + ", identifier=" + identifier + "]";
	}
}