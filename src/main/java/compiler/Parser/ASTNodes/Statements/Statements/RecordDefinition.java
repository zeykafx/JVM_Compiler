package compiler.Parser.ASTNodes.Statements.Statements;

import compiler.Lexer.Symbol;
import compiler.SemanticAnalysis.Visitor;

import java.util.ArrayList;

public class RecordDefinition extends Statement {

    private final Symbol identifier;
    private final ArrayList<RecordFieldDefinition> fields;

    public RecordDefinition(
        Symbol identifier,
        ArrayList<RecordFieldDefinition> fields
    ) {
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
    public void accept(Visitor v) {
        v.visitRecordDefinition(this);
    }
}
