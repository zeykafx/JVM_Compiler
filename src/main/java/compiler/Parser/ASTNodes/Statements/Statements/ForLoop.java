package compiler.Parser.ASTNodes.Statements.Statements;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Block;
import compiler.Parser.ASTNodes.Types.NumType;
import compiler.SemanticAnalysis.Visitor;

public class ForLoop extends Statement {

	private final Symbol variable;
	private final Symbol start;
	private final Symbol end;
	private final Symbol step;
	private final Block block;

	public ForLoop(Symbol variable, Symbol start, Symbol end, Symbol step, Block block) {
		this.variable = variable;
		this.start = start;
		this.end = end;
		this.step = step;
		this.block = block;
	}

	public Symbol getVariable() {
		return variable;
	}

	public Symbol getStart() {
		return start;
	}

	public Symbol getEnd() {
		return end;
	}

	public Symbol getStep() {
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
        return "  ".repeat(indent) + "ForLoop:\n" +  "  ".repeat(indent+1) + "LoopVar: " + variable.lexeme + "\n" + "  ".repeat(indent+1) + "Start: " + start.lexeme + "\n" + "  ".repeat(indent+1) + "End: " + end.lexeme + "\n" + "  ".repeat(indent+1) +  "Step: " + step.lexeme + "\n" + block.prettyPrint(indent + 1);
    }

	@Override
	public void accept(Visitor v) {
		v.visitForLoop(this);
	}
}
