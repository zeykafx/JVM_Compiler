package compiler.Parser.ASTNodes.Statements.Expressions.Terms;

import compiler.Lexer.Symbol;
import compiler.SemanticAnalysis.Visitor;

public class ConstVal extends Term {

    private final Object value;
    private final Symbol symbol;

    public ConstVal(Object value, Symbol symbol) {
        this.value = value;
        this.symbol = symbol;
    }

    public Object getValue() {
        return value;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return "ConstVal [value=" + value + ", symbol=" + symbol + "]";
    }

    @Override
    public String prettyPrint(int indent) {
        String typeStr = "";
        switch (symbol.type) {
            case INT_LITERAL:
                typeStr = "Integer";
                break;
            case FLOAT_LITERAL:
                typeStr = "Float";
                break;
            case STRING_LITERAL:
                typeStr = "String";
                break;
            case BOOL_TRUE:
            case BOOL_FALSE:
                typeStr = "Boolean";
                break;
            default:
                typeStr = "Literal";
        }

        return "  ".repeat(indent) + typeStr + ", " + value;
    }

    @Override
    public void accept(Visitor v) {
        v.visitConstValue(this);
    }
}
