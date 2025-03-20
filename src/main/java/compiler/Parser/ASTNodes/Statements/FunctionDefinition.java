package compiler.Parser.ASTNodes.Statements;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Block;
import compiler.Parser.ASTNodes.Types.Type;

import java.util.ArrayList;

public class FunctionDefinition extends Statement {

	private final Symbol name;
	private final Type returnType;
	private final ArrayList<ParamDefinition> paramDefinitions;
	private final Block block;

	public FunctionDefinition(Symbol name, Type returnType, ArrayList<ParamDefinition> paramDefinitions, Block block) {
		this.name = name;
		this.returnType = returnType;
		this.paramDefinitions = paramDefinitions;
		this.block = block;
	}

	public Symbol getName() {
		return name;
	}

	public Type getReturnType() {
		return returnType;
	}

	public ArrayList<ParamDefinition> getParamDefinitions() {
		return paramDefinitions;
	}

	public Block getBlock() {
		return block;
	}

	@Override
	public String toString() {
		return "Function, " + name.type + ", " + returnType.toString() + ", " +paramDefinitions+ ", " + block.toString();
	}
}
