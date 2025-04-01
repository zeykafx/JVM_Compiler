package compiler.SemanticAnalysis;
import compiler.SemanticAnalysis.Types.SemType;

import java.util.HashMap;

public class SymbolTable {
    private final SymbolTable parent;
    private final Integer scope;
    private final HashMap<String, SemType> symbols;


    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
        this.scope = parent == null ? 0 : parent.scope + 1;
        this.symbols = new HashMap<>();
    }

    public SymbolTable getParent() {
        return parent;
    }

    public void addSymbol(String name, SemType node) {
        symbols.put(name, node);
    }

    public SemType getSymbol(String name) {
        SemType node = symbols.get(name);
        if (node == null && parent != null) {
            return parent.getSymbol(name);
        }
        return node;
    }

    public Integer getScope() {
        return scope;
    }



    // root new SymbolTable(null);
    // for elem in root :
    //     if elem is a function:
    //          functionTable = new SymbolTable(root);
    //          for param in elem.getParams():
    //              functionTable.addSymbol(param.getName(), getType(param));

    //          for elem in elem.getBody():
    //              if elem in an Expression:
    //                  typeCheck(elem);
    //              if elem is a variable:
    //                  functionTable.addSymbol(elem.getName(), getType(elem));

    //     addSymbol(elem.getName(), getType(elem));

    // fun main(int x, int y)

}
