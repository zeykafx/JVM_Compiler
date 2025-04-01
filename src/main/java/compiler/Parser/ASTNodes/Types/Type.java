package compiler.Parser.ASTNodes.Types;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.ASTNode;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class Type extends ASTNode {
	public Symbol symbol;
//	public TokenTypes type;

	public boolean isList;

	public Type(Symbol type) {
		this.symbol = type;
		this.isList = false;
	}

	public Type(Symbol type, boolean isList) {
		this.symbol = type;
		this.isList = isList;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	public boolean isList() {
		return isList;
	}

	@Override
	public SemType accept(Visitor<SemType> v, SymbolTable table) {
		return v.visitType(this, table);
	}

	@Override
	public String toString() {
		return "Type, " + symbol.type + ", " + (isList ? "array" : "");
	}
	
	@Override
	public String prettyPrint(int indent) {
        return "  ".repeat(indent) + "Type " + symbol.lexeme + (isList ? " array" : "");
    }
}
