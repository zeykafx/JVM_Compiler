package compiler.Parser.ASTNodes.Statements.Expressions.Expressions;

import compiler.Parser.ASTNodes.Statements.Expressions.Operators.BinaryOperator;
import compiler.Parser.ASTNodes.Statements.Expressions.Operators.Operator;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.Term;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;


public class BinaryExpression extends Expression {
    private final Term leftTerm;
    private final BinaryOperator operator;
    private final Term rightTerm;

    public BinaryExpression(Term leftTerm, BinaryOperator operator, Term rightTerm, int line, int column) {
        super(line, column);

        this.leftTerm = leftTerm;
        this.operator = operator;
        this.rightTerm = rightTerm;
    }

    public Term getLeftTerm() {
        return leftTerm;
    }

    public BinaryOperator getOperator() {
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
        return "  ".repeat(indent) + "BinaryExpression:\n" +
                "  ".repeat(indent + 1) + "Left: \n" + leftTerm.prettyPrint(indent+2) + "\n" +
                "  ".repeat(indent + 1) + "Operator: " + operator.prettyPrint(0) + "\n" +
                "  ".repeat(indent + 1) + "Right: \n" + rightTerm.prettyPrint(indent+2);
    }

    @Override
    public <R, T> R accept(Visitor<R, T> v, T table) throws Exception{
        return v.visitBinaryExpression(this, table);
    }
}
