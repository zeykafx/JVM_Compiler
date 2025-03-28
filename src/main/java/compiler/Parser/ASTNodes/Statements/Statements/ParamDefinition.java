package compiler.Parser.ASTNodes.Statements.Statements;


//Params -> Param ParamsTail | .
//ParamsTail -> "," Param ParamsTail | .
//Param -> "identifier" Type .

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Types.Type;
import compiler.SemanticAnalysis.Visitor;

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
		return "ParamDefinition [identifier=" + identifier.lexeme + ", type=" + type + "]";
	}
	
	@Override
	public String prettyPrint(int indent) {
        return "  ".repeat(indent) + "ParamDefinition: " + identifier.lexeme + " " + type.prettyPrint(0);
    }

	@Override
	public void accept(Visitor v) {
		v.visitParamDefinition(this);
	}
}
