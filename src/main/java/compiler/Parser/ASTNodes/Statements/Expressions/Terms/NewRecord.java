package compiler.Parser.ASTNodes.Statements.Expressions.Terms;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;

import java.util.ArrayList;

public class NewRecord extends Term {

    private final Symbol identifier;
    private final ArrayList<ParamCall> terms;

    public NewRecord(Symbol identifier, ArrayList<ParamCall> terms) {
        this.identifier = identifier;
        this.terms = terms;
    }

    public Symbol getIdentifier() {
        return identifier;
    }

    public ArrayList<ParamCall> getTerms() {
        return terms;
    }

    @Override
    public String toString() {
        return "NewRecord [identifier=" + identifier + ", terms=" + terms + "]";
    }
}
