package compiler.Parser.ASTNodes.Statements.Expressions.Operators;

import compiler.Lexer.Symbol;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

/*
 * ! -
 */
public class UnaryOperator extends Operator {
    private final Symbol operator;

    public UnaryOperator(Symbol operator) {
        this.operator = operator;
    }

    public Symbol getOperator() {
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

    @Override
    public SemType accept(Visitor<SemType> v, SymbolTable table) {
        return v.visitUnaryOperator(this, table);
    }
}
