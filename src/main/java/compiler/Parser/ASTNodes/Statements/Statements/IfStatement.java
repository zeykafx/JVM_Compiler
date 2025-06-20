package compiler.Parser.ASTNodes.Statements.Statements;

import compiler.Parser.ASTNodes.Block;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class IfStatement extends Statement {

    private final Expression condition;
    private final Block thenSBlock;
    private final boolean isElse;
    private Block elseBlock; // can be null if there is no else statement

    public IfStatement(Expression condition, Block thenBlock, int line, int column) {
        super(line, column);

        this.condition = condition;
        this.thenSBlock = thenBlock;
        this.isElse = false;
    }

    public IfStatement(Expression condition, Block thenBlock, Block elseBlock, int line, int column) {
        super(line, column);

        this.condition = condition;
        this.thenSBlock = thenBlock;
        this.elseBlock = elseBlock;
        this.isElse = elseBlock != null;
    }

    public Expression getCondition() {
        return condition;
    }

    public Block getThenBlock() {
        return thenSBlock;
    }

    public Block getElseBlock() {
        return elseBlock;
    }

    public boolean isElse() {
        return isElse;
    }

    @Override
    public String toString() {
        return (
            "IfStatement, " +
            condition.toString() +
            ", " +
            thenSBlock.toString() +
            ", " +
            (isElse ? "else" : "") +
            ", " +
            (elseBlock != null ? elseBlock.toString() : "")
        );
    }

    @Override
    public String prettyPrint(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append("  ".repeat(indent)).append("IfStatement\n");

        sb.append("  ".repeat(indent + 1)).append("Condition:\n");
        sb.append(condition.prettyPrint(indent + 2)).append("\n");

        sb.append("  ".repeat(indent + 1)).append("Then:\n");
        sb.append(thenSBlock.prettyPrint(indent + 2));

        if (isElse && elseBlock != null) {
            sb.append("\n").append("  ".repeat(indent + 1)).append("Else:\n");
            sb.append(elseBlock.prettyPrint(indent + 2));
        }

        return sb.toString();
    }

    @Override
    public <R, T> R accept(Visitor<R, T> v, T table) throws Exception {
        return v.visitIfStatement(this, table);
    }
}
