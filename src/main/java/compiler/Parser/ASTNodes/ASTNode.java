package compiler.Parser.ASTNodes;

import compiler.CodeGen.SlotTable;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public abstract class ASTNode {
    public int line;
    public int column;

    public ASTNode(int line, int column) {
        this.line = line;
        this.column = column;
    }

//    public abstract SemType accept(Visitor<SemType, SymbolTable> v, SymbolTable table) throws Exception;
    public abstract <R, T> R accept(Visitor<R, T> v, T table) throws Exception;

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
