package compiler.Parser.ASTNodes.Statements.Expressions.Operators;

import compiler.Lexer.Symbol;

/*
 * ! -
 */
public class UnaryOperator extends Operator {
    private final Symbol operator;

    public UnaryOperator(Symbol operator) {
        this.operator = operator;
    }

   @Override
    public String toString() {
        return "UnaryOperator [operator=" + operator.lexeme + "]";
    }
}
