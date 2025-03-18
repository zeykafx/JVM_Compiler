package compiler.Parser.ASTNodes.Statements.Expressions.Access;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Statements.Expressions.Expression;
import compiler.Parser.ASTNodes.Statements.Expressions.Term;

public class RecordAccess extends Term {

	private final Expression headExpression;
	private final Symbol identifier;

	public RecordAccess(Expression head, Symbol identifier) {
		this.headExpression = head;
		this.identifier = identifier;
	}

	@Override
	public String toString() {
		return "RecordAccess [headExpression=" + headExpression + ", identifier=" + identifier + "]";
	}
}