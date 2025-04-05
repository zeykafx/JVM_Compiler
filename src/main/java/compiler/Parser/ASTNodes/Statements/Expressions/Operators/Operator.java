package compiler.Parser.ASTNodes.Statements.Expressions.Operators;

import compiler.Parser.ASTNodes.ASTNode;

public abstract class Operator extends ASTNode {

    public Operator(int line, int column) {
        super(line, column);
    }

    @Override
    public String toString() {
        return "Op";
    }
}
