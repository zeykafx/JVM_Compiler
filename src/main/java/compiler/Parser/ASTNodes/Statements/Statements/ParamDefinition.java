package compiler.Parser.ASTNodes.Statements.Statements;


//Params -> Param ParamsTail | .
//ParamsTail -> "," Param ParamsTail | .
//Param -> "identifier" Type .

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Types.Type;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class ParamDefinition extends Statement {
	private final Symbol identifier;
	private final Type type;
	private final Integer paramIndex;


	public ParamDefinition(Symbol identifier, Type type, Integer paramIndex, int line, int column) {
		super(line, column);

		this.identifier = identifier;
		this.type = type;
		this.paramIndex = paramIndex;
	}

	public Symbol getIdentifier() {
		return identifier;
	}

	public Type getType() {
		return type;
	}

	public Integer getParamIndex() {
		return paramIndex;
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
	public <R, T> R accept(Visitor<R, T> v, T table) throws Exception {
		return v.visitParamDefinition(this, table);
	}
}
