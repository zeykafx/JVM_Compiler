package compiler.Parser.ASTNodes;

import compiler.Parser.ASTNodes.Statements.Statements.Statement;
import java.util.ArrayList;

public class Block extends ASTNode {

    ArrayList<Statement> statements;
    Statement returnStatement;

    public Block(ArrayList<Statement> statements, Statement returnStatement) {
        this.statements = statements;
        this.returnStatement = returnStatement;
    }

    public ArrayList<Statement> getStatements() {
        return statements;
    }

    public Statement getReturnStatement() {
        return returnStatement;
    }

    @Override
    public String toString() {
        return (
            "Block [statements=" +
            statements +
            ", returnStatement=" +
            returnStatement +
            "]"
        );
    }

    @Override
    public String prettyPrint(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append("  ".repeat(indent)).append("Block\n");

        if (!statements.isEmpty()) {
            sb.append("  ".repeat(indent + 1)).append("Statements:\n");
            for (Statement stmt : statements) {
                sb.append(stmt.prettyPrint(indent + 2)).append("\n");
            }
        }

        if (returnStatement != null) {
            sb.append("  ".repeat(indent + 1)).append("Return:\n");
            sb.append(returnStatement.prettyPrint(indent + 2));
        }

        return sb.toString();
    }
}
