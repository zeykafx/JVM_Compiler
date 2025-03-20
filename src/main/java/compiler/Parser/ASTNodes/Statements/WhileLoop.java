package compiler.Parser.ASTNodes.Statements;

import compiler.Parser.ASTNodes.Block;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;

public class WhileLoop extends Statement {
	private final Expression condition;
	private final Block block;

	public WhileLoop(Expression condition, Block block) {
		this.condition = condition;
		this.block = block;
	}

	public Expression getCondition() {
		return condition;
	}

	public Block getBlock() {
		return block;
	}

	@Override
	public String toString() {
		return "WhileLoop [condition=" + condition + ", block=" + block + "]";
	}
}
