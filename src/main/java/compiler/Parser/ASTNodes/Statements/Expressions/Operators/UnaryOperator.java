package compiler.Parser.ASTNodes.Statements.Expressions.Operators;

import compiler.Lexer.Symbol;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

/*
 * ! -
 */
public class UnaryOperator extends Operator {
    private final Symbol operator;

    public UnaryOperator(Symbol operator, int line, int column) {
        super(line, column);

        this.operator = operator;
    }

    public Symbol getSymbol() {
        return operator;
    }

    @Override
    public String toString() {
        return "UnaryOperator [operator=" + operator.lexeme + "]";
    }
    
    @Override
    public String prettyPrint(int indent) {
        return "  ".repeat(indent) + "UnaryOperator, " + operator.lexeme;
    }

    public boolean isBooleanOperator() {
        return operator.lexeme.equals("!");
    }

    public boolean isNumberOperator() {
        return operator.lexeme.equals("-");
    }

    @Override
    public SemType accept(Visitor<SemType> v, SymbolTable table) throws SemanticException {
        return v.visitUnaryOperator(this, table);
    }
}
