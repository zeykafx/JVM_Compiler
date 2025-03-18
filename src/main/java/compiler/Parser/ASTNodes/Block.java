package compiler.Parser.ASTNodes;

import compiler.Parser.ASTNodes.Statements.Statement;

import java.util.ArrayList;

public class Block extends ASTNode {

	ArrayList<Statement> statements;
	Statement returnStatement;

	@Override
	public String toString() {
		return "Block [statements=" + statements + ", returnStatement=" + returnStatement + "]";
	}
}
