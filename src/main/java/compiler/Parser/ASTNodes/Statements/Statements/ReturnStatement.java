package compiler.Parser.ASTNodes.Statements.Statements;

import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class ReturnStatement extends Statement {

    private final Expression expression;

    public ReturnStatement(Expression expression, int line, int column) {
        super(line, column);

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
        if (expression == null) {
            return "  ".repeat(indent) + "Return: void";
        }
        return (
            "  ".repeat(indent) +
            "Return: \n" +
            expression.prettyPrint(indent + 1)
        );
    }

    @Override
    public SemType accept(Visitor<SemType> v, SymbolTable table) throws SemanticException {
        return v.visitReturnStatement(this, table);
    }
}
