package compiler.Parser.ASTNodes.Statements.Statements;

import compiler.Lexer.Symbol;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

import java.util.ArrayList;

public class RecordDefinition extends Statement {

    private final Symbol identifier;
    private final ArrayList<RecordFieldDefinition> fields;

    public RecordDefinition(
        Symbol identifier,
        ArrayList<RecordFieldDefinition> fields,
        int line,
        int column
    ) {
		super(line, column);
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

    @Override
    public String prettyPrint(int indent) {
        StringBuilder sb = new StringBuilder();
        sb
            .append("  ".repeat(indent))
            .append("Record: ")
            .append(identifier.lexeme)
            .append("\n");

        if (!fields.isEmpty()) {
            sb.append("  ".repeat(indent + 1)).append("Fields:\n");
            for (RecordFieldDefinition field : fields) {
                sb.append(field.prettyPrint(indent + 2)).append("\n");
            }
        }

        return sb.toString();
    }

    @Override
    public <R, T> R accept(Visitor<R, T> v, T table) throws Exception {
        return v.visitRecordDefinition(this, table);
    }
}
