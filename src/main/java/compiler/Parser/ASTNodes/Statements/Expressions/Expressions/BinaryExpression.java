package compiler.Parser.ASTNodes.Statements.Expressions.Expressions;

import compiler.Parser.ASTNodes.Statements.Expressions.Operators.Operator;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.Term;


public class BinaryExpression extends Expression {
    private final Term leftTerm;
    private final Operator operator;
    private final Term rightTerm;

    public BinaryExpression(Term leftTerm, Operator operator, Term rightTerm) {
        this.leftTerm = leftTerm;
        this.operator = operator;
        this.rightTerm = rightTerm;
    }

    public Term getLeftTerm() {
        return leftTerm;
    }

    public Operator getOperator() {
        return operator;
    }

    public Term getRightTerm() {
        return rightTerm;
    }

    @Override
    public String toString() {
        return "BinaryExpression [leftTerm=" + leftTerm + ", operator=" + operator + ", rightTerm=" + rightTerm + "]";
    }
}
