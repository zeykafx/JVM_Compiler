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
    
    @Override
    public String prettyPrint(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append("  ".repeat(indent)).append("BinaryExpression\n");
        
        sb.append("  ".repeat(indent + 1)).append("Left:\n");
        sb.append(leftTerm.prettyPrint(indent + 2)).append("\n");
        
        sb.append("  ".repeat(indent + 1)).append("Operator: ").append(operator.prettyPrint(0)).append("\n");
        
        sb.append("  ".repeat(indent + 1)).append("Right:\n");
        sb.append(rightTerm.prettyPrint(indent + 2));
        
        return sb.toString();
    }
}
