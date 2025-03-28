package compiler.SemanticAnalysis;

import compiler.Parser.ASTNodes.ASTNode;

import java.util.HashMap;

public class SymbolTable {
    private final SymbolTable parent;
    private final HashMap<String, ASTNode> symbols;

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
        this.symbols = new HashMap<>();
    }

    public SymbolTable getParent() {
        return parent;
    }

    public void addSymbol(String name, ASTNode node) {
        symbols.put(name, node);
    }

    public ASTNode getSymbol(String name) {
        ASTNode node = symbols.get(name);
        if (node == null && parent != null) {
            return parent.getSymbol(name);
        }
        return node;
    }
}
