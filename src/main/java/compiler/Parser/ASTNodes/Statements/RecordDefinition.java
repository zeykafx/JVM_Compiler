package compiler.Parser.ASTNodes.Statements;

import compiler.Lexer.Symbol;

import java.util.ArrayList;

public class RecordDefinition extends Statement {
	private final Symbol identifier;
	private final ArrayList<RecordFieldDefinition> fields;

	public RecordDefinition(Symbol identifier, ArrayList<RecordFieldDefinition> fields) {
		this.identifier = identifier;
		this.fields = fields;
	}

	public Symbol getIdentifier() {
		return identifier;
	}

	public ArrayList<RecordFieldDefinition> getFields() {
		return fields;
	}

	@Override
	public String toString() {
		return "Record: " + identifier.lexeme + ", " + fields.toString();
	}
}
