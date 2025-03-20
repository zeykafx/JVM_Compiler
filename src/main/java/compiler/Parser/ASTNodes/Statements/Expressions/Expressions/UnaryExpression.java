package compiler.Parser.ASTNodes.Statements.Expressions.Expressions;

import compiler.Parser.ASTNodes.Statements.Expressions.Operators.Operator;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.Term;

public class UnaryExpression extends Expression {

    private final Operator operator;
    private final Term term;

    public UnaryExpression(Operator operator, Term term) {
        this.operator = operator;
        this.term = term;
    }

    public Operator getOperator() {
        return operator;
    }

    public Term getTerm() {
        return term;
    }

    @Override
    public String toString() {
        return "UnaryExpression [operator=" + operator + ", term=" + term + "]";
    }
}
