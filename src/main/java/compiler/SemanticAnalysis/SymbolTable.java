package compiler.SemanticAnalysis;
import compiler.SemanticAnalysis.Types.SemType;

import java.util.HashMap;

public class SymbolTable {
    private final SymbolTable parent;
    private final Integer scope;
    private final HashMap<String, SemType> symbols;
    private String localFunctionName;


    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
        this.scope = parent == null ? 0 : parent.scope + 1;
        this.symbols = new HashMap<>();
    }

    public SymbolTable(SymbolTable parent, String localFunctionName) {
        this.parent = parent;
        this.scope = parent == null ? 0 : parent.scope + 1;
        this.symbols = new HashMap<>();
        this.localFunctionName = localFunctionName;
    }

    public SymbolTable getParent() {
        return parent;
    }

    public void addSymbol(String name, SemType node) {
        symbols.put(name, node);
    }

    public SemType lookup(String name) {
        SemType node = symbols.get(name);
        if (node == null && parent != null) {
            return parent.lookup(name);
        }
        return node;
    }

    public SemType lookupSameScope(String name){
        return symbols.get(name);
    }

    public void removeSymbol(String name) {
        symbols.remove(name);
    }

    public Integer getScope() {
        return scope;
    }

    public void setLocalFunctionName(String localFunctionName) {
        this.localFunctionName = localFunctionName;
    }

    public String getLocalFunctionName() {
        return localFunctionName;
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


    @Override
    public String toString() {
        return "SymbolTable{" +
                "parent=" + parent +
                ", scope=" + scope +
                ", symbols=" + symbols.toString() +
                ", localFunctionName='" + localFunctionName + '\'' +
                '}';
    }
}
