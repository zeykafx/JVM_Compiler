package compiler.Parser.ASTNodes.Statements.Expressions.Expressions;

import compiler.Lexer.Symbol;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

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
	
	@Override
	public String prettyPrint(int indent) {
        return "  ".repeat(indent) + "Assignment: \n" + symbol.lexeme + "\n" + rightExpression.prettyPrint(indent+1);
    }

	@Override
	public SemType accept(Visitor<SemType> v, SymbolTable table) {
		return v.visitAssignment(this, table);
	}
}
