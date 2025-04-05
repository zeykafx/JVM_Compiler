package compiler.Parser.ASTNodes.Statements.Expressions.Access;

import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class ArrayAccess extends Access {

    private final Access headAccess;
    private final Expression indexExpression;

    public ArrayAccess(Access head, Expression index, int line, int column) {
        super(line, column);
        this.headAccess = head;
        this.indexExpression = index;
    }

    public Access getHeadAccess() {
        return headAccess;
    }

    public Expression getIndexExpression() {
        return indexExpression;
    }

    @Override
    public String toString() {
        return (
            "ArrayAccess [headExpression=" +
            headAccess +
            ", indexExpression=" +
            indexExpression +
            "]"
        );
    }

    @Override
    public String prettyPrint(int indent) {
        return (
            "  ".repeat(indent) +
            "ArrayAccess: \n" +
            headAccess.prettyPrint(indent+1) + "\n"+
            // "[\n" +
            indexExpression.prettyPrint(indent+1) +"\n"
            // "\n]"
        );
    }

    @Override
    public SemType accept(Visitor<SemType> v, SymbolTable table) throws SemanticException {
        return v.visitArrayAccess(this, table);
    }
}
// a.x[myId]
