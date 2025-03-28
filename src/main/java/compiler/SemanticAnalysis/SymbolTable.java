package compiler.SemanticAnalysis;
import compiler.SemanticAnalysis.Types.Type;

import java.util.HashMap;

public class SymbolTable {
    private final SymbolTable parent;
    private final HashMap<String, Type> symbols;


    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
        this.symbols = new HashMap<>();
    }

    public SymbolTable getParent() {
        return parent;
    }

    public void addSymbol(String name, Type node) {
        symbols.put(name, node);
    }

    public Type getSymbol(String name) {
        Type node = symbols.get(name);
        if (node == null && parent != null) {
            return parent.getSymbol(name);
        }
        return node;
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
