package compiler.Parser.ASTNodes.Statements;

import compiler.Lexer.Symbol;

import java.util.ArrayList;

public class RecordDefinition extends Statement {
	Symbol identifier;
	ArrayList<RecordField> fields;

	@Override
	public String toString() {
		return "Record, " + identifier.type + ", " + fields.toString();
	}
}
