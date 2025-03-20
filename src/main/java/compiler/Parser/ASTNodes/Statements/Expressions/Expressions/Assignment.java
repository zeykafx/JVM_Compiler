package compiler.Parser.ASTNodes.Statements.Expressions.Expressions;

import compiler.Lexer.Symbol;

public class Assignment extends Expression {
	private final Symbol symbol;
	private final Expression rightExpression;

	public Assignment(Symbol symbol, Expression rightExpression) {
		this.symbol = symbol;
		this.rightExpression = rightExpression;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	public Expression getRightExpression() {
		return rightExpression;
	}

	@Override
	public String toString() {
		return "Assignment [symbol=" + symbol + ", rightExpression=" + rightExpression + "]";
	}
}
