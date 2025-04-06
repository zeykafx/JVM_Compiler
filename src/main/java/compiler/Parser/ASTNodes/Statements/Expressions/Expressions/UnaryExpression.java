package compiler.Parser.ASTNodes.Statements.Expressions.Expressions;

import compiler.Parser.ASTNodes.Statements.Expressions.Operators.Operator;
import compiler.Parser.ASTNodes.Statements.Expressions.Operators.UnaryOperator;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.Term;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class UnaryExpression extends Expression {

    private final UnaryOperator operator;
    private final Term term;

    public UnaryExpression(UnaryOperator operator, Term term, int line, int column) {
        super(line, column);

        this.operator = operator;
        this.term = term;
    }

    public UnaryOperator getOperator() {
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
