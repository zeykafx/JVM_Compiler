package compiler.Parser.ASTNodes.Statements.Statements;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Types.Type;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class RecordFieldDefinition extends Statement {
	Symbol identifier;
	Type type;
	int fieldIndex;

	public RecordFieldDefinition(Symbol identifier, Type type, int fieldIndex, int line, int column) {
		super(line, column);

		this.identifier = identifier;
		this.type = type;
		this.fieldIndex = fieldIndex;
	}

	public Symbol getIdentifier() {
		return identifier;
	}

	public Type getType() {
		return type;
	}

	public int getFieldIndex() {
		return fieldIndex;
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

	@Override
	public <R, T> R accept(Visitor<R, T> v, T table) throws Exception {
		return v.visitRecordFieldDefinition(this, table);
	}
}
