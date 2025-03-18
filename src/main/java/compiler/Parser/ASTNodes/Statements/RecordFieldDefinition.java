package compiler.Parser.ASTNodes.Statements;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Types.Type;

public class RecordFieldDefinition extends Statement {
	Symbol identifier;
	Type type;

	@Override
	public String toString() {
		return "RecordField, " + identifier.type + ", " + type.toString();
	}
}
