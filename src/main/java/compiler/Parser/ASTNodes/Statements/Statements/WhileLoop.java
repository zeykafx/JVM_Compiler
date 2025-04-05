package compiler.Parser.ASTNodes.Statements.Statements;

import compiler.Parser.ASTNodes.Block;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class WhileLoop extends Statement {
	private final Expression condition;
	private final Block block;

	public WhileLoop(Expression condition, Block block, int line, int column) {
		super(line, column);

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
	
	@Override
	public String prettyPrint(int indent) {
        return "  ".repeat(indent) + "While: \n" + condition.prettyPrint(indent+1) + "\n" + block.prettyPrint(indent + 1);
    }

	@Override
	public SemType accept(Visitor<SemType> v, SymbolTable table) throws SemanticException {
		return v.visitWhileLoop(this, table);
	}
}
