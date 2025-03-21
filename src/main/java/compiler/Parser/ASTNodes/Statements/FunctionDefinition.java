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
	private final boolean voidReturnType;

	public FunctionDefinition(Symbol name, Type returnType, ArrayList<ParamDefinition> paramDefinitions, Block block) {
		this.name = name;
		this.returnType = returnType;
		this.paramDefinitions = paramDefinitions;
		this.block = block;
		this.voidReturnType = returnType == null;
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
//		return "Function, " + name.type + ", " + returnType.toString() + ", " +paramDefinitions+ ", " + block.toString();
		StringBuilder paramStr = new StringBuilder();
		for (ParamDefinition param : paramDefinitions) {
			paramStr.append(param.toString()).append(", ");
		}
		if (!paramStr.isEmpty()) {
			paramStr.setLength(paramStr.length() - 2); // Remove the last comma and space
		}
		String returnTypeStr = voidReturnType ? "void" : returnType.toString();
		return "Function, " + name.type + ", " + returnTypeStr + ", [" + paramStr + "], " + block.toString();
	}
	
	@Override
    public String prettyPrint(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append("  ".repeat(indent)).append("Function, ").append(name.lexeme).append("\n");
        
        sb.append("  ".repeat(indent + 1)).append("Return Type: ")
        .append(voidReturnType ? "void" : returnType.prettyPrint(0)).append("\n");
        
        if (!paramDefinitions.isEmpty()) {
            sb.append("  ".repeat(indent + 1)).append("Parameters:\n");
            for (ParamDefinition param : paramDefinitions) {
                sb.append(param.prettyPrint(indent + 2)).append("\n");
            }
        }
        
        sb.append("  ".repeat(indent + 1)).append("Body:\n");
        sb.append(block.prettyPrint(indent + 2));
        
        return sb.toString();
    }
}
