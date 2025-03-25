package compiler.Parser.ASTNodes.Statements.Expressions.Expressions;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.Term;
import compiler.SemanticAnalysis.Visitor;

public class Identifier extends Term {
    private final Symbol symbol;

    public Identifier(Symbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return "Identifier [symbol=" + symbol.lexeme + "]";
    }

    public String getIdentifier() {
        return symbol.lexeme;
    }
    
    @Override
    public String prettyPrint(int indent) {
        return "  ".repeat(indent) + "Identifier, " + symbol.lexeme;
    }

    @Override
    public void accept(Visitor v) {
        v.visitIdentifier(this);
    }
}
