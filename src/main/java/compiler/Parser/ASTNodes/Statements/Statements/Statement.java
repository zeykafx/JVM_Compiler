package compiler.Parser.ASTNodes.Statements.Statements;

import compiler.Parser.ASTNodes.ASTNode;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class Statement extends ASTNode {

	@Override
	public SemType accept(Visitor<SemType> v, SymbolTable table) {
		return v.visitStatement(this, table);
	}

	@Override
	public String toString() {
		return "Statement []";
	}

	@Override
	public String prettyPrint(int indent) {
		return "";
	}
}
