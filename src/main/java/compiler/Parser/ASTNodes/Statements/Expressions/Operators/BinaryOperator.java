package compiler.Parser.ASTNodes.Statements.Expressions.Operators;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.ASTNode;

/**
 * >= <= == != + - * / % && ||
 */
public class BinaryOperator extends Operator {

    private final Symbol operator;

    public BinaryOperator(Symbol operator) {
        this.operator = operator;
    }

    @Override
    public String toString() {
        return "BinaryOperator [operator=" + operator.lexeme + "]";
    }
}
