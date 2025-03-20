package compiler.Parser.ASTNodes.Statements;

import compiler.Parser.ASTNodes.Block;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;

public class IfStatement extends Statement {

	private final Expression condition;
	private final Block thenSBlock;
	private final boolean isElse;
	private Block elseBlock; // can be null if there is no else statement

	public IfStatement(Expression condition, Block thenBlock) {
		this.condition = condition;
		this.thenSBlock = thenBlock;
		this.isElse = false;
	}

	public IfStatement(Expression condition, Block thenBlock, Block elseBlock) {
		this.condition = condition;
		this.thenSBlock = thenBlock;
		this.elseBlock = elseBlock;
		this.isElse = true;
	}

	public Expression getCondition() {
		return condition;
	}

	public Block getThenBlock() {
		return thenSBlock;
	}

	public Block getElseBlock() {
		return elseBlock;
	}

	public boolean isElse() {
		return isElse;
	}

	@Override
	public String toString() {
		return "IfStatement, " + condition.toString() + ", " + thenSBlock.toString() + ", " + (isElse ? "else" : "") + ", " + (elseBlock != null ? elseBlock.toString() : "");
	}
}
