package compiler.Parser.ASTNodes.Types;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.ASTNode;
import compiler.SemanticAnalysis.Visitor;

public class Type extends ASTNode {
	public Symbol symbol;
//	public TokenTypes type;

	public boolean isList;


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
	public void accept(Visitor v) {
		v.visitType(this);
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
