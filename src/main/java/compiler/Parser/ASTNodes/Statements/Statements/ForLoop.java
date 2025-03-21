package compiler.Parser.ASTNodes.Statements.Statements;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Block;
import compiler.Parser.ASTNodes.Types.NumType;

public class ForLoop extends Statement {

	private final Symbol variable;
	private final NumType start;
	private final NumType end;
	private final NumType step;
	private final Block block;

	public ForLoop(Symbol variable, NumType start, NumType end, NumType step, Block block) {
		this.variable = variable;
		this.start = start;
		this.end = end;
		this.step = step;
		this.block = block;
	}

	public Symbol getVariable() {
		return variable;
	}

	public NumType getStart() {
		return start;
	}

	public NumType getEnd() {
		return end;
	}

	public NumType getStep() {
		return step;
	}

	public Block getBlock() {
		return block;
	}

	@Override
	public String toString() {
		return "ForLoop [variable=" + variable + ", start=" + start + ", end=" + end + ", step=" + step + ", block="
				+ block + "]";
	}
	
	@Override
	public String prettyPrint(int indent) {
        return "  ".repeat(indent) + "ForLoop:\n" +  "  ".repeat(indent+1) + "LoopVar: " + variable.lexeme + "\n" + "  ".repeat(indent+1) + "Start: " + start.prettyPrint(0) + "\n" + "  ".repeat(indent+1) + "End: " + end.prettyPrint(0) + "\n" + "  ".repeat(indent+1) +  "Step: " + step.prettyPrint(0) + "\n" + block.prettyPrint(indent + 1);
    }
}
