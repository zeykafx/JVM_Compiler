package compiler.Parser.ASTNodes;

public abstract class ASTNode {

    public abstract String toString();

    public String prettyPrint(int nbIndent) {
        return "    ".repeat(nbIndent) + this.toString();
    }
}


// myfunc(add(1, 2))