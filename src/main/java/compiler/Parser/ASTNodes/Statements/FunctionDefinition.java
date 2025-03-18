package compiler.Parser.ASTNodes.Statements;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Block;
import compiler.Parser.ASTNodes.Types.Type;

import java.util.Arrays;

public class FunctionDefinition extends Statement {

	private final Symbol name;
	private final Type returnType;
	private final Type[] paramTypes;
	private final Block block;

	public FunctionDefinition(Symbol name, Type returnType, Type[] paramTypes, Block block) {
		this.name = name;
		this.returnType = returnType;
		this.paramTypes = paramTypes;
		this.block = block;
	}

	@Override
	public String toString() {
		return "Function, " + name.type + ", " + returnType.toString() + ", " + Arrays.toString(paramTypes) + ", " + block.toString();
	}
}
