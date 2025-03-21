package compiler.Parser.ASTNodes.Statements.Expressions.Terms;

import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;

public class ParenthesesTerm extends Term {
    private final Expression expression;

    public ParenthesesTerm(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return "ParenthesesTerm [expression=" + expression + "]";
    }
    
    @Override
    public String prettyPrint(int indent) {
        return "  ".repeat(indent) + "ParenthesesTerm \n" + expression.prettyPrint(indent+1);
    }
}
