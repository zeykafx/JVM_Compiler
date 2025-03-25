package compiler.Parser.ASTNodes.Statements.Statements;

import compiler.Parser.ASTNodes.ASTNode;
import compiler.SemanticAnalysis.Visitor;

public class Statement extends ASTNode {

	@Override
	public void accept(Visitor v) {
		v.visitStatement(this);
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
