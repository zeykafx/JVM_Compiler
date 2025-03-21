package compiler.Parser.ASTNodes.Statements.Statements;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Types.Type;

public class RecordFieldDefinition extends Statement {
	Symbol identifier;
	Type type;

	public RecordFieldDefinition(Symbol identifier, Type type) {
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
		return "RecordField: " + identifier.type + ", " + type.toString();
	}
	
	@Override
    public String prettyPrint(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append("  ".repeat(indent)).append("RecordField: ").append(identifier.lexeme).append(", ").append(type.toString());
        return sb.toString();
    }
}
