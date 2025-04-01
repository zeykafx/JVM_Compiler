package compiler.Parser.ASTNodes.Statements.Expressions.Terms;

import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

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

    @Override
    public SemType accept(Visitor<SemType> v, SymbolTable table) {
        return v.visitParenthesesTerm(this, table);
    }
}
