package compiler.Parser.ASTNodes.Statements.Expressions.Expressions;

import compiler.Parser.ASTNodes.Statements.Expressions.Terms.Term;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class AloneExpression extends Expression {
    // ! This class is redundant with Identifier, can probably be removed
    private final Term term;

    public AloneExpression(Term term) {
        this.term = term;
    }

    @Override
    public String toString() {
        return "AloneExpression [term=" + term + "]";
    }
    
    @Override
    public String prettyPrint(int indent) {
        return term.prettyPrint(indent);
    }

    @Override
    public SemType accept(Visitor<SemType> v, SymbolTable table) throws SemanticException {
        return v.visitAloneExpression(this, table);
    }
}
