package compiler.Parser.ASTNodes.Statements.Expressions.Access;

import compiler.Lexer.Symbol;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class RecordAccess extends Access {

    private final Access headAccess;
    private final Symbol identifier;

    public RecordAccess(Access head, Symbol identifier, int line, int column) {
        super(line, column);

        this.headAccess = head;
        this.identifier = identifier;
    }

    public Symbol getIdentifier() {
        return identifier;
    }

    public Access getHeadAccess() {
        return headAccess;
    }

    @Override
    public String toString() {
        return (
            "RecordAccess [headExpression=" +
            headAccess +
            ", identifier=" +
            identifier +
            "]"
        );
    }

    @Override
    public String prettyPrint(int indent) {
        return (
            "  ".repeat(indent) +
            "RecordAccess: \n" +
            headAccess.prettyPrint(indent + 1) +
            "\n" +
            "  ".repeat(indent + 1) +
            "Identifier: " +
            identifier.lexeme
        );
    }

    @Override
    public <R, T> R accept(Visitor<R, T> v, T table) throws Exception {
        return v.visitRecordAccess(this, table);
    }
}
