package compiler.Parser.ASTNodes.Types;

import compiler.Lexer.Symbol;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class NumType extends Type {
	private final boolean isFloat;

	public NumType(Symbol type, boolean isFloat) {
		super(type, false);
		this.isFloat = isFloat;
	}

	public NumType(Symbol type) {
		super(type, false);
		this.isFloat = false;
	}

	public boolean isFloat() {
		return isFloat;
	}

	public boolean isInt() {
		return !isFloat;
	}

	@Override
	public String toString() {
		return "NumType, " + (isFloat ? "float" : "int");
	}
	
	@Override
	public String prettyPrint(int indent) {
        return "  ".repeat(indent) + "NumType: " + (isFloat ? "float" : "int") + ": " + symbol.lexeme +"";
    }

	@Override
	public SemType accept(Visitor<SemType> v, SymbolTable table) throws SemanticException {
		return v.visitNumType(this, table);
	}
}
