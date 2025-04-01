package compiler.Parser.ASTNodes.Statements.Statements;

import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class ReturnStatement extends Statement {

    private final Expression expression;

    public ReturnStatement(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return "ReturnStatement [expression=" + expression + "]";
    }

    @Override
    public String prettyPrint(int indent) {
        return (
            "  ".repeat(indent) +
            "Return: \n" +
            expression.prettyPrint(indent + 1)
        );
    }

    @Override
    public SemType accept(Visitor<SemType> v, SymbolTable table) {
        return v.visitReturnStatement(this, table);
    }
}
