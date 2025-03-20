package compiler.Parser.ASTNodes.Statements.Expressions.Terms;

import compiler.Lexer.Symbol;

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
}
