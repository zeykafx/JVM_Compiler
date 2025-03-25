package compiler.Parser.ASTNodes.Statements.Expressions.Expressions;

import compiler.Parser.ASTNodes.Statements.Expressions.Terms.Term;
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
    public void accept(Visitor v) {
        v.visitAloneExpression(this);
    }
}
