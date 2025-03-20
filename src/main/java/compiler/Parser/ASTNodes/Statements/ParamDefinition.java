package compiler.Parser.ASTNodes.Statements;


//Params -> Param ParamsTail | .
//ParamsTail -> "," Param ParamsTail | .
//Param -> "identifier" Type .

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Types.Type;

public class ParamDefinition extends Statement {
	private final Symbol identifier;
	private final Type type;

	public ParamDefinition(Symbol identifier, Type type) {
		this.identifier = identifier;
		this.type = type;
	}

	public Symbol getIdentifier() {
		return identifier;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return "ParamDefinition [identifier=" + identifier.type + ", type=" + type + "]";
	}
}
