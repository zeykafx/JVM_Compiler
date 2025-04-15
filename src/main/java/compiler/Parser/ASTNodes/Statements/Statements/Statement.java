package compiler.Parser.ASTNodes.Statements.Statements;

import compiler.Parser.ASTNodes.ASTNode;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class Statement extends ASTNode {

	public Statement(int line, int column) {
		super(line, column);
	}

	@Override
	public <R, T> R accept(Visitor<R, T> v, T table) throws Exception {
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
