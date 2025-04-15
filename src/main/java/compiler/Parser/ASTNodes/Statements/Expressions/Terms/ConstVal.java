package compiler.Parser.ASTNodes.Statements.Expressions.Terms;

import compiler.Lexer.Symbol;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class ConstVal extends Term {

    private final Object value;
    private final Symbol symbol;

    public ConstVal(Object value, Symbol symbol, int line, int column) {
        super(line, column);

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
        String typeStr = switch (symbol.type) {
			case INT_LITERAL -> "Integer";
			case FLOAT_LITERAL -> "Float";
			case STRING_LITERAL -> "String";
			case BOOL_TRUE, BOOL_FALSE -> "Boolean";
			default -> "Literal";
		};

		return "  ".repeat(indent) + typeStr + ", " + value;
    }

    @Override
	public <R, T> R accept(Visitor<R, T> v, T table) throws Exception {
        return v.visitConstValue(this, table);
    }
}
