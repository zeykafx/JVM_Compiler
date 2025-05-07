package compiler.Parser.ASTNodes.Statements.Statements;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Block;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class ForLoop extends Statement {

	private final Symbol variable;
//	private final Symbol start;
	public Expression startExpr;
	public Expression endExpr;
	public Expression stepExpr;
//	public SemType startType;
//	private final Symbol end;
//	public SemType endType;
//	private final Symbol step;
//	public SemType stepType;
	private final Block block;

	public ForLoop(Symbol variable, Expression startExpr, Expression endExpr, Expression stepExpr, Block block, int line, int column) {
		super(line, column);

		this.variable = variable;
		this.startExpr = startExpr;
		this.endExpr = endExpr;
		this.stepExpr = stepExpr;
		this.block = block;
	}

	public Symbol getVariable() {
		return variable;
	}

	public Expression getStart() {
		return startExpr;
	}

	public Expression getEnd() {
		return endExpr;
	}

	public Expression getStep() {
		return stepExpr;
	}

	public Block getBlock() {
		return block;
	}

	@Override
	public String toString() {
		return "ForLoop [variable=" + variable + ", start=" + startExpr + ", end=" + endExpr + ", step=" + stepExpr + ", block="
				+ block + "]";
	}
	
	@Override
	public String prettyPrint(int indent) {
        return "  ".repeat(indent) + "ForLoop:\n" +  "  ".repeat(indent+1) + "LoopVar: " + variable.lexeme + "\n" + "  ".repeat(indent+1) + "Start: " + startExpr.prettyPrint(indent+1) + "\n" + "  ".repeat(indent+1) + "End: " + endExpr.prettyPrint(indent + 1) + "\n" + "  ".repeat(indent+1) +  "Step: " + stepExpr.prettyPrint(indent+1) + "\n" + block.prettyPrint(indent + 1);
    }

	@Override
	public <R, T> R accept(Visitor<R, T> v, T table) throws Exception {
		return v.visitForLoop(this, table);
	}
}
