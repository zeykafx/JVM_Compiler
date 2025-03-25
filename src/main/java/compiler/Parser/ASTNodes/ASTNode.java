package compiler.Parser.ASTNodes;

import compiler.SemanticAnalysis.Visitor;

public abstract class ASTNode {

    public abstract void accept(Visitor v);

    public abstract String toString();

    // public String prettyPrint(int nbIndent) {
    //     return "    ".repeat(nbIndent) + this.toString();
    // }
    public abstract String prettyPrint(int indent);
//        StringBuilder sb = new StringBuilder();
//        sb.append("  ".repeat(indent)).append(this.getNodeLabel());
//
//        // Subclasses should override this method to add their children
//        return sb.toString();
//    }

    // Returns the label for this node in the pretty-printed tree
    protected String getNodeLabel() {
        return this.getClass().getSimpleName();
    }
}
