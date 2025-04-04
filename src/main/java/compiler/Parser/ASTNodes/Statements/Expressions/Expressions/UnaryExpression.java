package compiler.Parser.ASTNodes.Statements.Expressions.Expressions;

import compiler.Parser.ASTNodes.Statements.Expressions.Operators.Operator;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.Term;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class UnaryExpression extends Expression {

    private final Operator operator;
    private final Term term;

    public UnaryExpression(Operator operator, Term term) {
        this.operator = operator;
        this.term = term;
    }

    public Operator getOperator() {
        return operator;
    }

    public Term getTerm() {
        return term;
    }

    @Override
    public String toString() {
        return "UnaryExpression [operator=" + operator + ", term=" + term + "]";
    }
    
    @Override
    public String prettyPrint(int indent) {
        return "  ".repeat(indent) + "UnaryExpression: " + operator + "\n" + term.prettyPrint(indent+1);
    }

    @Override
    public SemType accept(Visitor<SemType> v, SymbolTable table) throws SemanticException {
        return v.visitUnaryExpression(this, table);
    }
}
