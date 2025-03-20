package compiler.Parser.ASTNodes;

import compiler.Parser.ASTNodes.Statements.Statement;

import java.util.ArrayList;

public class Block extends ASTNode {

	ArrayList<Statement> statements;
	Statement returnStatement;

	public Block(ArrayList<Statement> statements, Statement returnStatement) {
		this.statements = statements;
		this.returnStatement = returnStatement;
	}

	public ArrayList<Statement> getStatements() {
		return statements;
	}

	public Statement getReturnStatement() {
		return returnStatement;
	}

	@Override
	public String toString() {
		return "Block [statements=" + statements + ", returnStatement=" + returnStatement + "]";
	}
}
